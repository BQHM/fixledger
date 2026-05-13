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
import com.fixledger.modules.file.enums.FileBizType;
import com.fixledger.modules.file.mapper.FileResourceMapper;
import com.fixledger.modules.file.response.FileResourceResponse;
import com.fixledger.modules.maintenance.entity.MaintenanceRecordEntity;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import com.fixledger.modules.warranty.entity.WarrantyRecordEntity;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件功能说明：附件资源服务实现，负责业务编排、事务边界、状态校验和持久化调用。
 * </p>
 *
 * @Author FixLedger
 */
@Service
public class FileResourceServiceImpl implements FileResourceService {

  private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
      "image/jpeg",
      "image/png",
      "application/pdf"
  );

  private final FileResourceMapper fileResourceMapper;
  private final FileStorageService fileStorageService;
  private final FamilyService familyService;
  private final DeviceAssetMapper deviceAssetMapper;
  private final WarrantyRecordMapper warrantyRecordMapper;
  private final MaintenanceRecordMapper maintenanceRecordMapper;
  private final ConsumableItemMapper consumableItemMapper;

  public FileResourceServiceImpl(
      FileResourceMapper fileResourceMapper,
      FileStorageService fileStorageService,
      FamilyService familyService,
      DeviceAssetMapper deviceAssetMapper,
      WarrantyRecordMapper warrantyRecordMapper,
      MaintenanceRecordMapper maintenanceRecordMapper,
      ConsumableItemMapper consumableItemMapper
  ) {
    this.fileResourceMapper = fileResourceMapper;
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
   * 功能说明：实现附件上传文件业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param bizType 业务类型
   * @param bizId 业务 ID
   * @param file 上传文件
   * @return 业务响应数据
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
    fileResourceMapper.insert(entity);
    return toResponse(entity);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现附件查询列表业务逻辑。
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
   * 功能说明：实现附件下载文件业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param fileId 文件 ID
   * @return 处理结果
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
   * 功能说明：实现附件删除业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param fileId 文件 ID
   * @return 是否处理成功
   */
  @Override
  @Transactional
  public boolean deleteFile(Long userId, Long familyId, Long fileId) {
    familyService.checkFamilyMember(userId, familyId);
    FileResourceEntity entity = getFile(familyId, fileId);
    // 附件采用逻辑删除，物理文件交给后续清理任务处理。
    return fileResourceMapper.deleteById(entity.getId()) > 0;
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
    return originalName;
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
    return StringUtils.hasText(originalName) ? StringUtils.cleanPath(originalName) : "";
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
}
