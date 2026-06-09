package com.fixledger.modules.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.infrastructure.file.FileStorageService;
import com.fixledger.infrastructure.file.StoredFile;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.consumable.entity.ConsumableItemEntity;
import com.fixledger.modules.consumable.mapper.ConsumableItemMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.file.entity.FileResourceEntity;
import com.fixledger.modules.file.entity.ManualTextIndexEntity;
import com.fixledger.modules.file.enums.FileBizType;
import com.fixledger.modules.file.mapper.FileResourceMapper;
import com.fixledger.modules.file.mapper.ManualTextIndexMapper;
import com.fixledger.modules.file.response.CredentialBoxGroupResponse;
import com.fixledger.modules.file.response.CredentialBoxResponse;
import com.fixledger.modules.file.response.CredentialFileResponse;
import com.fixledger.modules.file.response.CredentialTargetResponse;
import com.fixledger.modules.file.response.FileResourceResponse;
import com.fixledger.modules.file.response.ManualSearchResponse;
import com.fixledger.modules.maintenance.entity.MaintenanceRecordEntity;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.entity.WarrantyRecordEntity;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件功能说明：附件资源服务实现，负责附件元数据、家庭空间鉴权、业务对象归属校验和存储服务调用。
 * </p>
 *
 * @Author FixLedger
 */
@Slf4j
@Service
public class FileResourceServiceImpl implements FileResourceService {

  private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
  private static final int MAGIC_BYTES_LENGTH = 8;
  private static final int MANUAL_INDEX_TEXT_MAX_LENGTH = 20_000;
  private static final int MANUAL_SEARCH_KEYWORD_MAX_LENGTH = 64;
  private static final int MANUAL_SEARCH_SNIPPET_RADIUS = 36;
  private static final int MANUAL_SEARCH_LIMIT = 20;
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
      "image/jpeg",
      "image/png",
      "application/pdf"
  );
  private static final List<FileBizType> CREDENTIAL_GROUP_ORDER = List.of(
      FileBizType.DEVICE,
      FileBizType.MANUAL,
      FileBizType.WARRANTY,
      FileBizType.MAINTENANCE,
      FileBizType.CONSUMABLE
  );

  private final FileResourceMapper fileResourceMapper;
  private final ManualTextIndexMapper manualTextIndexMapper;
  private final FileStorageService fileStorageService;
  private final FamilyService familyService;
  private final DeviceAssetMapper deviceAssetMapper;
  private final WarrantyRecordMapper warrantyRecordMapper;
  private final MaintenanceRecordMapper maintenanceRecordMapper;
  private final ConsumableItemMapper consumableItemMapper;

  public FileResourceServiceImpl(
      FileResourceMapper fileResourceMapper,
      ManualTextIndexMapper manualTextIndexMapper,
      FileStorageService fileStorageService,
      FamilyService familyService,
      DeviceAssetMapper deviceAssetMapper,
      WarrantyRecordMapper warrantyRecordMapper,
      MaintenanceRecordMapper maintenanceRecordMapper,
      ConsumableItemMapper consumableItemMapper
  ) {
    this.fileResourceMapper = fileResourceMapper;
    this.manualTextIndexMapper = manualTextIndexMapper;
    this.fileStorageService = fileStorageService;
    this.familyService = familyService;
    this.deviceAssetMapper = deviceAssetMapper;
    this.warrantyRecordMapper = warrantyRecordMapper;
    this.maintenanceRecordMapper = maintenanceRecordMapper;
    this.consumableItemMapper = consumableItemMapper;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：校验家庭成员和业务对象归属后上传凭证文件，并写入附件元数据。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param bizType 业务类型
   * @param bizId 业务 ID
   * @param file 上传文件
   * @return 附件元数据
   */
  @Override
  public FileResourceResponse uploadFile(
      Long userId,
      Long familyId,
      String bizType,
      Long bizId,
      MultipartFile file
  ) {
    familyService.checkFamilyMember(userId, familyId);
    FileBizType fileBizType = validateBizType(bizType);
    // 先确认业务对象属于当前家庭，再写文件内容和元数据。
    validateBizExists(familyId, fileBizType, bizId);
    String originalName = validateFile(file);

    StoredFile storedFile = fileStorageService.store(familyId, fileBizType.getCode(), file);
    FileResourceEntity entity = new FileResourceEntity();
    entity.setFamilyId(familyId);
    entity.setBizType(fileBizType.getCode());
    entity.setBizId(bizId);
    entity.setOriginalName(originalName);
    entity.setStorageName(storedFile.storageName());
    entity.setStoragePath(storedFile.storagePath());
    entity.setContentType(storedFile.contentType());
    entity.setFileSize(storedFile.fileSize());
    entity.setExtension(storedFile.extension());
    try {
      fileResourceMapper.insert(entity);
    } catch (RuntimeException e) {
      cleanupStoredFile(storedFile.storagePath(), e);
      throw e;
    }
    indexManualTextIfNeeded(fileBizType, entity, file);
    return toResponse(entity);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：查询指定设备、保修、维修或耗材下的附件列表。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param bizType 业务类型
   * @param bizId 业务 ID
   * @return 列表结果
   */
  @Override
  public List<FileResourceResponse> listFiles(
      Long userId,
      Long familyId,
      String bizType,
      Long bizId
  ) {
    familyService.checkFamilyMember(userId, familyId);
    FileBizType fileBizType = validateBizType(bizType);
    validateBizExists(familyId, fileBizType, bizId);
    return fileResourceMapper.selectList(new LambdaQueryWrapper<FileResourceEntity>()
            .eq(FileResourceEntity::getFamilyId, familyId)
            .eq(FileResourceEntity::getBizType, fileBizType.getCode())
            .eq(FileResourceEntity::getBizId, bizId)
            .orderByDesc(FileResourceEntity::getCreatedAt))
        .stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：按设备聚合凭证盒，统一返回各业务对象可挂载目标、附件分组和完整度统计。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 凭证盒聚合数据
   */
  @Override
  public CredentialBoxResponse getCredentialBox(Long userId, Long familyId, Long deviceId) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceAssetEntity device = getDevice(familyId, deviceId);
    List<WarrantyRecordEntity> warranties = listDeviceWarranties(familyId, deviceId);
    List<MaintenanceRecordEntity> maintenances = listDeviceMaintenances(familyId, deviceId);
    List<ConsumableItemEntity> consumables = listDeviceConsumables(familyId, deviceId);

    Map<FileBizType, List<CredentialTargetResponse>> targetsByType = buildTargets(
        device,
        warranties,
        maintenances,
        consumables
    );
    Map<FileBizType, List<Long>> targetIdsByType = buildTargetIds(targetsByType);
    Map<String, String> targetLabels = buildTargetLabels(targetsByType);
    Map<FileBizType, List<CredentialFileResponse>> filesByType = buildFilesByType(
        familyId,
        deviceId,
        targetIdsByType,
        targetLabels
    );

    List<CredentialBoxGroupResponse> groups = CREDENTIAL_GROUP_ORDER.stream()
        .map(type -> buildGroup(type, targetsByType.get(type), filesByType.get(type)))
        .toList();
    int archivedTypeCount = (int) groups.stream()
        .filter(group -> !group.files().isEmpty())
        .count();
    int totalFileCount = groups.stream()
        .mapToInt(group -> group.files().size())
        .sum();
    long totalFileSize = groups.stream()
        .flatMap(group -> group.files().stream())
        .mapToLong(file -> file.fileSize() == null ? 0L : file.fileSize())
        .sum();
    int totalTypeCount = CREDENTIAL_GROUP_ORDER.size();
    int completionPercent = archivedTypeCount * 100 / totalTypeCount;
    return new CredentialBoxResponse(
        device.getId(),
        device.getName(),
        device.getLocation(),
        completionPercent,
        archivedTypeCount,
        totalTypeCount,
        totalFileCount,
        totalFileSize,
        groups
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：按设备搜索说明书文本索引，只返回短片段用于定位原始说明书。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param keyword 搜索关键词
   * @return 搜索结果
   */
  @Override
  public List<ManualSearchResponse> searchManuals(
      Long userId,
      Long familyId,
      Long deviceId,
      String keyword
  ) {
    familyService.checkFamilyMember(userId, familyId);
    getDevice(familyId, deviceId);
    String normalizedKeyword = normalizeManualKeyword(keyword);
    List<ManualTextIndexEntity> indexes = manualTextIndexMapper.selectList(
        new LambdaQueryWrapper<ManualTextIndexEntity>()
            .eq(ManualTextIndexEntity::getFamilyId, familyId)
            .eq(ManualTextIndexEntity::getDeviceId, deviceId)
            .eq(ManualTextIndexEntity::getIndexStatus, "INDEXED")
            .orderByDesc(ManualTextIndexEntity::getUpdatedAt)
    );
    List<Long> fileIds = indexes.stream()
        .map(ManualTextIndexEntity::getFileId)
        .toList();
    Map<Long, FileResourceEntity> filesById = listFilesByIds(familyId, fileIds);
    return indexes.stream()
        .filter(index -> containsIgnoreCase(index.getContentText(), normalizedKeyword)
            || containsIgnoreCase(index.getFileName(), normalizedKeyword))
        .limit(MANUAL_SEARCH_LIMIT)
        .map(index -> toManualSearchResponse(
            index,
            filesById.get(index.getFileId()),
            normalizedKeyword
        ))
        .toList();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：下载附件内容，读取存储前确认附件元数据属于当前家庭。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param fileId 文件 ID
   * @return 下载资源和响应头所需元数据
   */
  @Override
  public FileDownloadResource downloadFile(Long userId, Long familyId, Long fileId) {
    familyService.checkFamilyMember(userId, familyId);
    FileResourceEntity entity = getFile(familyId, fileId);
    Resource resource = fileStorageService.loadAsResource(entity.getStoragePath());
    return new FileDownloadResource(
        resource,
        entity.getOriginalName(),
        entity.getContentType(),
        entity.getFileSize()
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：逻辑删除附件元数据，物理文件等待后续清理任务处理。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param fileId 文件 ID
   * @return 是否删除成功
   */
  @Override
  @Transactional
  public boolean deleteFile(Long userId, Long familyId, Long fileId) {
    familyService.checkFamilyMember(userId, familyId);
    FileResourceEntity entity = getFile(familyId, fileId);
    deleteManualIndexIfNeeded(entity);
    // 附件采用逻辑删除，物理文件交给后续清理任务处理。
    return fileResourceMapper.deleteById(entity.getId()) > 0;
  }

  private void cleanupStoredFile(String storagePath, RuntimeException cause) {
    try {
      fileStorageService.delete(storagePath);
    } catch (RuntimeException cleanupException) {
      cause.addSuppressed(cleanupException);
    }
  }

  private void indexManualTextIfNeeded(
      FileBizType fileBizType,
      FileResourceEntity fileEntity,
      MultipartFile file
  ) {
    if (fileBizType != FileBizType.MANUAL) {
      return;
    }
    try {
      ManualTextIndexEntity index = new ManualTextIndexEntity();
      index.setFamilyId(fileEntity.getFamilyId());
      index.setDeviceId(fileEntity.getBizId());
      index.setFileId(fileEntity.getId());
      index.setFileName(fileEntity.getOriginalName());
      fillManualTextIndex(index, fileEntity, file);
      manualTextIndexMapper.insert(index);
    } catch (RuntimeException e) {
      log.warn("Manual text index skipped: fileId={}", fileEntity.getId(), e);
    }
  }

  private void fillManualTextIndex(
      ManualTextIndexEntity index,
      FileResourceEntity fileEntity,
      MultipartFile file
  ) {
    if (!"application/pdf".equals(fileEntity.getContentType())) {
      index.setContentText(fileEntity.getOriginalName());
      index.setIndexStatus("INDEXED");
      return;
    }
    try {
      index.setContentText(extractManualText(file));
      index.setIndexStatus("INDEXED");
    } catch (RuntimeException e) {
      log.warn("Manual text extraction failed: fileId={}", fileEntity.getId(), e);
      index.setContentText(fileEntity.getOriginalName());
      index.setIndexStatus("FAILED");
      index.setErrorMessage(limitText(e.getMessage(), 512));
    }
  }

  private void deleteManualIndexIfNeeded(FileResourceEntity fileEntity) {
    if (!FileBizType.MANUAL.getCode().equals(fileEntity.getBizType())) {
      return;
    }
    manualTextIndexMapper.delete(new LambdaQueryWrapper<ManualTextIndexEntity>()
        .eq(ManualTextIndexEntity::getFamilyId, fileEntity.getFamilyId())
        .eq(ManualTextIndexEntity::getFileId, fileEntity.getId()));
  }

  private String extractManualText(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      byte[] bytes = inputStream.readNBytes(MANUAL_INDEX_TEXT_MAX_LENGTH * 2);
      String rawText = new String(bytes, StandardCharsets.UTF_8);
      return normalizeExtractedText(rawText);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "说明书文本索引读取失败", e);
    }
  }

  private String normalizeExtractedText(String rawText) {
    if (!StringUtils.hasText(rawText)) {
      return "";
    }
    String text = rawText.replaceFirst("^%PDF-[^\\n\\r]*(\\r\\n|\\n|\\r)?", "");
    text = text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Zs}\\r\\n]", " ");
    text = text.replaceAll("\\s+", " ").trim();
    return limitText(text, MANUAL_INDEX_TEXT_MAX_LENGTH);
  }

  private String normalizeManualKeyword(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "搜索关键词不能为空");
    }
    String normalized = keyword.trim();
    if (normalized.length() > MANUAL_SEARCH_KEYWORD_MAX_LENGTH) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "搜索关键词不能超过 64 个字符");
    }
    return normalized;
  }

  private Map<Long, FileResourceEntity> listFilesByIds(Long familyId, List<Long> fileIds) {
    if (fileIds.isEmpty()) {
      return Map.of();
    }
    List<FileResourceEntity> files = fileResourceMapper.selectList(
        new LambdaQueryWrapper<FileResourceEntity>()
            .eq(FileResourceEntity::getFamilyId, familyId)
            .in(FileResourceEntity::getId, fileIds)
    );
    Map<Long, FileResourceEntity> filesById = new HashMap<>();
    files.forEach(file -> filesById.put(file.getId(), file));
    return filesById;
  }

  private boolean containsIgnoreCase(String value, String keyword) {
    if (!StringUtils.hasText(value)) {
      return false;
    }
    return value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
  }

  private ManualSearchResponse toManualSearchResponse(
      ManualTextIndexEntity index,
      FileResourceEntity file,
      String keyword
  ) {
    return new ManualSearchResponse(
        index.getFileId(),
        index.getFileName(),
        file == null ? "application/pdf" : file.getContentType(),
        file == null ? 0L : file.getFileSize(),
        buildSnippet(index.getContentText(), keyword)
    );
  }

  private String buildSnippet(String contentText, String keyword) {
    if (!StringUtils.hasText(contentText)) {
      return "";
    }
    String lowerText = contentText.toLowerCase(Locale.ROOT);
    String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
    int index = lowerText.indexOf(lowerKeyword);
    if (index < 0) {
      return limitText(contentText, MANUAL_SEARCH_SNIPPET_RADIUS * 2);
    }
    int start = Math.max(0, index - MANUAL_SEARCH_SNIPPET_RADIUS);
    int end = Math.min(
        contentText.length(),
        index + keyword.length() + MANUAL_SEARCH_SNIPPET_RADIUS
    );
    String prefix = start > 0 ? "..." : "";
    String suffix = end < contentText.length() ? "..." : "";
    return prefix + contentText.substring(start, end) + suffix;
  }

  private String limitText(String text, int maxLength) {
    if (text == null || text.length() <= maxLength) {
      return text;
    }
    return text.substring(0, maxLength);
  }

  private DeviceAssetEntity getDevice(Long familyId, Long deviceId) {
    DeviceAssetEntity device = deviceAssetMapper.selectOne(
        new LambdaQueryWrapper<DeviceAssetEntity>()
            .eq(DeviceAssetEntity::getId, deviceId)
            .eq(DeviceAssetEntity::getFamilyId, familyId)
    );
    if (device == null) {
      throw new BusinessException(ErrorCode.DEVICE_NOT_FOUND, "设备不存在");
    }
    return device;
  }

  private List<WarrantyRecordEntity> listDeviceWarranties(Long familyId, Long deviceId) {
    return warrantyRecordMapper.selectList(new LambdaQueryWrapper<WarrantyRecordEntity>()
        .eq(WarrantyRecordEntity::getFamilyId, familyId)
        .eq(WarrantyRecordEntity::getDeviceId, deviceId)
        .orderByDesc(WarrantyRecordEntity::getEndDate));
  }

  private List<MaintenanceRecordEntity> listDeviceMaintenances(Long familyId, Long deviceId) {
    return maintenanceRecordMapper.selectList(new LambdaQueryWrapper<MaintenanceRecordEntity>()
        .eq(MaintenanceRecordEntity::getFamilyId, familyId)
        .eq(MaintenanceRecordEntity::getDeviceId, deviceId)
        .orderByDesc(MaintenanceRecordEntity::getOccurredAt));
  }

  private List<ConsumableItemEntity> listDeviceConsumables(Long familyId, Long deviceId) {
    return consumableItemMapper.selectList(new LambdaQueryWrapper<ConsumableItemEntity>()
        .eq(ConsumableItemEntity::getFamilyId, familyId)
        .eq(ConsumableItemEntity::getDeviceId, deviceId)
        .orderByAsc(ConsumableItemEntity::getNextRemindDate));
  }

  private Map<FileBizType, List<CredentialTargetResponse>> buildTargets(
      DeviceAssetEntity device,
      List<WarrantyRecordEntity> warranties,
      List<MaintenanceRecordEntity> maintenances,
      List<ConsumableItemEntity> consumables
  ) {
    Map<FileBizType, List<CredentialTargetResponse>> targets = emptyCredentialMap();
    CredentialTargetResponse deviceTarget = new CredentialTargetResponse(
        device.getId(),
        device.getName()
    );
    targets.put(FileBizType.DEVICE, List.of(deviceTarget));
    targets.put(FileBizType.MANUAL, List.of(deviceTarget));
    targets.put(FileBizType.WARRANTY, warranties.stream()
        .map(warranty -> new CredentialTargetResponse(warranty.getId(), warrantyLabel(warranty)))
        .toList());
    targets.put(FileBizType.MAINTENANCE, maintenances.stream()
        .map(maintenance -> new CredentialTargetResponse(
            maintenance.getId(),
            maintenance.getTitle()
        ))
        .toList());
    targets.put(FileBizType.CONSUMABLE, consumables.stream()
        .map(consumable -> new CredentialTargetResponse(consumable.getId(), consumable.getName()))
        .toList());
    return targets;
  }

  private Map<FileBizType, List<Long>> buildTargetIds(
      Map<FileBizType, List<CredentialTargetResponse>> targetsByType
  ) {
    Map<FileBizType, List<Long>> targetIds = new EnumMap<>(FileBizType.class);
    CREDENTIAL_GROUP_ORDER.forEach(type -> targetIds.put(
        type,
        targetsByType.getOrDefault(type, List.of()).stream()
            .map(CredentialTargetResponse::bizId)
            .toList()
    ));
    return targetIds;
  }

  private Map<String, String> buildTargetLabels(
      Map<FileBizType, List<CredentialTargetResponse>> targetsByType
  ) {
    Map<String, String> labels = new HashMap<>();
    targetsByType.forEach((type, targets) -> targets.forEach(target ->
        labels.put(labelKey(type.getCode(), target.bizId()), target.label())
    ));
    return labels;
  }

  private Map<FileBizType, List<CredentialFileResponse>> buildFilesByType(
      Long familyId,
      Long deviceId,
      Map<FileBizType, List<Long>> targetIdsByType,
      Map<String, String> targetLabels
  ) {
    List<FileResourceEntity> files = fileResourceMapper.selectList(
        new LambdaQueryWrapper<FileResourceEntity>()
            .eq(FileResourceEntity::getFamilyId, familyId)
            .and(wrapper -> wrapper
                .and(deviceWrapper -> deviceWrapper
                    .in(FileResourceEntity::getBizType, List.of(
                        FileBizType.DEVICE.getCode(),
                        FileBizType.MANUAL.getCode()
                    ))
                    .eq(FileResourceEntity::getBizId, deviceId)
                )
                .or(warrantyWrapper -> appendBizTypeTargets(
                    warrantyWrapper,
                    FileBizType.WARRANTY,
                    targetIdsByType.get(FileBizType.WARRANTY)
                ))
                .or(maintenanceWrapper -> appendBizTypeTargets(
                    maintenanceWrapper,
                    FileBizType.MAINTENANCE,
                    targetIdsByType.get(FileBizType.MAINTENANCE)
                ))
                .or(consumableWrapper -> appendBizTypeTargets(
                    consumableWrapper,
                    FileBizType.CONSUMABLE,
                    targetIdsByType.get(FileBizType.CONSUMABLE)
                ))
            )
            .orderByDesc(FileResourceEntity::getCreatedAt)
    );
    Map<FileBizType, List<CredentialFileResponse>> filesByType = emptyCredentialMap();
    files.forEach(file -> {
      FileBizType type = FileBizType.fromCode(file.getBizType());
      if (type == null) {
        return;
      }
      String targetLabel = targetLabels.get(labelKey(file.getBizType(), file.getBizId()));
      filesByType.computeIfAbsent(type, ignored -> new ArrayList<>())
          .add(toCredentialFile(file, targetLabel));
    });
    return filesByType;
  }

  private LambdaQueryWrapper<FileResourceEntity> appendBizTypeTargets(
      LambdaQueryWrapper<FileResourceEntity> wrapper,
      FileBizType bizType,
      List<Long> targetIds
  ) {
    if (targetIds == null || targetIds.isEmpty()) {
      return wrapper.eq(FileResourceEntity::getBizType, bizType.getCode())
          .eq(FileResourceEntity::getBizId, -1L);
    }
    return wrapper.eq(FileResourceEntity::getBizType, bizType.getCode())
        .in(FileResourceEntity::getBizId, targetIds);
  }

  private CredentialBoxGroupResponse buildGroup(
      FileBizType type,
      List<CredentialTargetResponse> targets,
      List<CredentialFileResponse> files
  ) {
    return new CredentialBoxGroupResponse(
        type.getCode(),
        groupTitle(type),
        groupShortTitle(type),
        groupDescription(type),
        targets == null ? List.of() : targets,
        files == null ? List.of() : files
    );
  }

  private String warrantyLabel(WarrantyRecordEntity warranty) {
    WarrantyType warrantyType = WarrantyType.fromCode(warranty.getWarrantyType());
    String typeLabel = warrantyType == null
        ? warranty.getWarrantyType()
        : warrantyType.getDescription();
    if (warranty.getEndDate() == null) {
      return typeLabel;
    }
    return typeLabel + " · " + warranty.getEndDate();
  }

  private String labelKey(String bizType, Long bizId) {
    return bizType + ":" + bizId;
  }

  private <T> Map<FileBizType, List<T>> emptyCredentialMap() {
    Map<FileBizType, List<T>> map = new EnumMap<>(FileBizType.class);
    CREDENTIAL_GROUP_ORDER.forEach(type -> map.put(type, new ArrayList<>()));
    return map;
  }

  private String groupTitle(FileBizType type) {
    return switch (type) {
      case DEVICE -> "购买凭证";
      case MANUAL -> "说明书";
      case WARRANTY -> "保修凭证";
      case MAINTENANCE -> "维修凭证";
      case CONSUMABLE -> "耗材凭证";
    };
  }

  private String groupShortTitle(FileBizType type) {
    return switch (type) {
      case DEVICE -> "发票";
      case MANUAL -> "说明书";
      case WARRANTY -> "保修";
      case MAINTENANCE -> "维修";
      case CONSUMABLE -> "耗材";
    };
  }

  private String groupDescription(FileBizType type) {
    return switch (type) {
      case DEVICE -> "购买发票、订单截图和支付凭证";
      case MANUAL -> "电子说明书、安装指南和参数文档";
      case WARRANTY -> "保修卡、延保单和售后政策";
      case MAINTENANCE -> "维修单、售后截图和费用凭证";
      case CONSUMABLE -> "滤芯、电池等耗材购买记录";
    };
  }

  private String validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "上传文件不能为空");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件大小不能超过 20MB");
    }
    String originalName = cleanOriginalName(file.getOriginalFilename());
    String extension = resolveExtension(originalName);
    // 扩展名和 MIME 类型双重校验，降低伪装文件上传风险。
    if (!StringUtils.hasText(extension) || !ALLOWED_EXTENSIONS.contains(extension)) {
      throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "文件扩展名不允许");
    }
    if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
      throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "文件 MIME 类型不允许");
    }
    validateFileSignature(file, extension);
    return originalName;
  }

  private void validateFileSignature(MultipartFile file, String extension) {
    String contentType = file.getContentType();
    byte[] header = readHeader(file);
    boolean matched = switch (extension) {
      case "jpg", "jpeg" -> "image/jpeg".equals(contentType)
          && startsWith(header, 0xFF, 0xD8, 0xFF);
      case "png" -> "image/png".equals(contentType)
          && startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
      case "pdf" -> "application/pdf".equals(contentType)
          && startsWith(header, 0x25, 0x50, 0x44, 0x46, 0x2D);
      default -> false;
    };
    if (!matched) {
      throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "文件内容与类型不匹配");
    }
  }

  private byte[] readHeader(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      return inputStream.readNBytes(MAGIC_BYTES_LENGTH);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件读取失败", e);
    }
  }

  private boolean startsWith(byte[] header, int... expected) {
    if (header.length < expected.length) {
      return false;
    }
    for (int i = 0; i < expected.length; i++) {
      if ((header[i] & 0xFF) != expected[i]) {
        return false;
      }
    }
    return true;
  }

  private FileBizType validateBizType(String bizType) {
    FileBizType fileBizType = FileBizType.fromCode(bizType);
    if (fileBizType == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "附件业务类型无效");
    }
    return fileBizType;
  }

  private void validateBizExists(Long familyId, FileBizType bizType, Long bizId) {
    if (bizId == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "附件业务 ID 不能为空");
    }
    // 每类附件都必须回查 family_id，防止把文件挂到其他家庭的数据上。
    if (bizType == FileBizType.DEVICE || bizType == FileBizType.MANUAL) {
      ensureDeviceExists(familyId, bizId);
      return;
    }
    if (bizType == FileBizType.WARRANTY) {
      ensureWarrantyExists(familyId, bizId);
      return;
    }
    if (bizType == FileBizType.MAINTENANCE) {
      ensureMaintenanceExists(familyId, bizId);
      return;
    }
    if (bizType == FileBizType.CONSUMABLE) {
      ensureConsumableExists(familyId, bizId);
      return;
    }
    throw new BusinessException(ErrorCode.BAD_REQUEST, "该附件业务类型暂未支持");
  }

  private void ensureDeviceExists(Long familyId, Long deviceId) {
    Long count = deviceAssetMapper.selectCount(new LambdaQueryWrapper<DeviceAssetEntity>()
        .eq(DeviceAssetEntity::getId, deviceId)
        .eq(DeviceAssetEntity::getFamilyId, familyId));
    if (count == 0) {
      throw new BusinessException(ErrorCode.DEVICE_NOT_FOUND, "设备不存在");
    }
  }

  private void ensureWarrantyExists(Long familyId, Long warrantyId) {
    Long count = warrantyRecordMapper.selectCount(new LambdaQueryWrapper<WarrantyRecordEntity>()
        .eq(WarrantyRecordEntity::getId, warrantyId)
        .eq(WarrantyRecordEntity::getFamilyId, familyId));
    if (count == 0) {
      throw new BusinessException(ErrorCode.WARRANTY_NOT_FOUND, "保修记录不存在");
    }
  }

  private void ensureMaintenanceExists(Long familyId, Long maintenanceId) {
    Long count = maintenanceRecordMapper.selectCount(
        new LambdaQueryWrapper<MaintenanceRecordEntity>()
            .eq(MaintenanceRecordEntity::getId, maintenanceId)
            .eq(MaintenanceRecordEntity::getFamilyId, familyId)
    );
    if (count == 0) {
      throw new BusinessException(ErrorCode.MAINTENANCE_NOT_FOUND, "维修记录不存在");
    }
  }

  private void ensureConsumableExists(Long familyId, Long consumableId) {
    Long count = consumableItemMapper.selectCount(new LambdaQueryWrapper<ConsumableItemEntity>()
        .eq(ConsumableItemEntity::getId, consumableId)
        .eq(ConsumableItemEntity::getFamilyId, familyId));
    if (count == 0) {
      throw new BusinessException(ErrorCode.CONSUMABLE_NOT_FOUND, "耗材不存在");
    }
  }

  private FileResourceEntity getFile(Long familyId, Long fileId) {
    FileResourceEntity entity = fileResourceMapper.selectOne(
        new LambdaQueryWrapper<FileResourceEntity>()
            .eq(FileResourceEntity::getId, fileId)
            .eq(FileResourceEntity::getFamilyId, familyId)
    );
    if (entity == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "附件不存在");
    }
    return entity;
  }

  private String cleanOriginalName(String originalName) {
    if (!StringUtils.hasText(originalName)) {
      return "";
    }
    String cleaned = StringUtils.cleanPath(originalName);
    if (cleaned.contains("..") || cleaned.contains("/") || cleaned.contains("\\")) {
      throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "文件名不允许包含路径字符");
    }
    return cleaned;
  }

  private String resolveExtension(String originalName) {
    if (!StringUtils.hasText(originalName)) {
      return null;
    }
    int index = originalName.lastIndexOf('.');
    if (index < 0 || index == originalName.length() - 1) {
      return null;
    }
    return originalName.substring(index + 1).toLowerCase();
  }

  private FileResourceResponse toResponse(FileResourceEntity entity) {
    return new FileResourceResponse(
        entity.getId(),
        entity.getOriginalName(),
        entity.getContentType(),
        entity.getFileSize(),
        entity.getBizType(),
        entity.getBizId()
    );
  }

  private CredentialFileResponse toCredentialFile(FileResourceEntity entity, String targetLabel) {
    return new CredentialFileResponse(
        entity.getId(),
        entity.getOriginalName(),
        entity.getContentType(),
        entity.getFileSize(),
        entity.getBizType(),
        entity.getBizId(),
        targetLabel
    );
  }
}
