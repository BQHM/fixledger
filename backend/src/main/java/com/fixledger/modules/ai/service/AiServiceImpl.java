package com.fixledger.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.infrastructure.ai.AiClient;
import com.fixledger.infrastructure.ai.DeviceContext;
import com.fixledger.infrastructure.ai.InvoiceParseResult;
import com.fixledger.infrastructure.ai.MaintenanceRecordDTO;
import com.fixledger.infrastructure.ai.MaintenanceSummary;
import com.fixledger.infrastructure.ai.TroubleshootingSuggestion;
import com.fixledger.modules.ai.entity.AiAnalysisEntity;
import com.fixledger.modules.ai.enums.AiAnalysisStatus;
import com.fixledger.modules.ai.enums.AiAnalysisType;
import com.fixledger.modules.ai.mapper.AiAnalysisMapper;
import com.fixledger.modules.ai.request.InvoiceParseRequest;
import com.fixledger.modules.ai.request.MaintenanceSummaryRequest;
import com.fixledger.modules.ai.request.TroubleshootingRequest;
import com.fixledger.modules.ai.response.InvoiceParseResponse;
import com.fixledger.modules.ai.response.MaintenanceSummaryResponse;
import com.fixledger.modules.ai.response.TroubleshootingResponse;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.entity.MaintenanceRecordEntity;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 文件功能说明：AI 辅助服务实现，负责业务编排、事务边界、状态校验和持久化调用。
 * </p>
 *
 * @Author FixLedger
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

  private static final int INPUT_SUMMARY_LIMIT = 1024;
  private static final int RESULT_JSON_LIMIT = 8000;
  private static final int ERROR_MESSAGE_LIMIT = 1024;
  private static final int SUMMARY_LIMIT = 500;
  private static final int SUGGESTION_LIMIT = 300;

  private final AiClient aiClient;
  private final AiAnalysisMapper aiAnalysisMapper;
  private final DeviceAssetMapper deviceAssetMapper;
  private final DeviceCategoryMapper deviceCategoryMapper;
  private final MaintenanceRecordMapper maintenanceRecordMapper;
  private final FamilyService familyService;
  private final ObjectMapper objectMapper;

  public AiServiceImpl(
      AiClient aiClient,
      AiAnalysisMapper aiAnalysisMapper,
      DeviceAssetMapper deviceAssetMapper,
      DeviceCategoryMapper deviceCategoryMapper,
      MaintenanceRecordMapper maintenanceRecordMapper,
      FamilyService familyService,
      ObjectMapper objectMapper
  ) {
    this.aiClient = aiClient;
    this.aiAnalysisMapper = aiAnalysisMapper;
    this.deviceAssetMapper = deviceAssetMapper;
    this.deviceCategoryMapper = deviceCategoryMapper;
    this.maintenanceRecordMapper = maintenanceRecordMapper;
    this.familyService = familyService;
    this.objectMapper = objectMapper;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现AI 辅助解析数据业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 请求参数
   * @return 业务响应数据
   */
  @Override
  public InvoiceParseResponse parseInvoice(
      Long userId,
      Long familyId,
      InvoiceParseRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    // AI 输入只保存摘要，避免把完整票据文本长期落库。
    String inputSummary = limit(request.text(), INPUT_SUMMARY_LIMIT);
    try {
      InvoiceParseResult result = sanitize(aiClient.parseInvoiceText(request.text()));
      InvoiceParseResponse response = new InvoiceParseResponse(
          null,
          result.deviceName(),
          result.purchaseDate(),
          result.price(),
          result.seller(),
          result.suggestedCategory()
      );
      Long analysisId = saveAnalysis(
          userId,
          familyId,
          AiAnalysisType.INVOICE_PARSE,
          null,
          null,
          inputSummary,
          response,
          AiAnalysisStatus.SUCCESS,
          null
      );
      return new InvoiceParseResponse(
          analysisId,
          result.deviceName(),
          result.purchaseDate(),
          result.price(),
          result.seller(),
          result.suggestedCategory()
      );
    } catch (BusinessException e) {
      // AI 是辅助能力，失败时返回可人工确认的兜底结果并记录分析状态。
      log.warn("Invoice AI parse fallback: familyId={}, userId={}", familyId, userId, e);
      InvoiceParseResponse fallback = fallbackInvoiceResponse();
      Long analysisId = saveAnalysis(
          userId,
          familyId,
          AiAnalysisType.INVOICE_PARSE,
          null,
          null,
          inputSummary,
          fallback,
          AiAnalysisStatus.FALLBACK,
          e.getMessage()
      );
      return new InvoiceParseResponse(analysisId, null, null, null, null, "待人工确认");
    }
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现AI 辅助生成建议业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 请求参数
   * @return 业务响应数据
   */
  @Override
  public TroubleshootingResponse suggestTroubleshooting(
      Long userId,
      Long familyId,
      TroubleshootingRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceAssetEntity device = getDevice(familyId, request.deviceId());
    // 如果请求绑定维修记录，必须确认维修记录属于同一家庭和同一设备。
    validateMaintenanceIfPresent(familyId, device.getId(), request.maintenanceId());
    DeviceContext context = toDeviceContext(device);
    String inputSummary = limit(request.faultDescription(), INPUT_SUMMARY_LIMIT);
    try {
      TroubleshootingSuggestion suggestion = sanitize(
          aiClient.suggestTroubleshooting(context, request.faultDescription())
      );
      TroubleshootingResponse response = new TroubleshootingResponse(
          null,
          suggestion.summary(),
          suggestion.suggestions()
      );
      Long analysisId = saveAnalysis(
          userId,
          familyId,
          AiAnalysisType.TROUBLESHOOTING,
          troubleshootingBizType(request),
          troubleshootingBizId(device, request),
          inputSummary,
          response,
          AiAnalysisStatus.SUCCESS,
          null
      );
      return new TroubleshootingResponse(
          analysisId,
          suggestion.summary(),
          suggestion.suggestions()
      );
    } catch (BusinessException e) {
      log.warn(
          "Troubleshooting AI fallback: familyId={}, deviceId={}",
          familyId,
          device.getId(),
          e
      );
      TroubleshootingResponse fallback = fallbackTroubleshootingResponse(device.getName());
      Long analysisId = saveAnalysis(
          userId,
          familyId,
          AiAnalysisType.TROUBLESHOOTING,
          troubleshootingBizType(request),
          troubleshootingBizId(device, request),
          inputSummary,
          fallback,
          AiAnalysisStatus.FALLBACK,
          e.getMessage()
      );
      return new TroubleshootingResponse(
          analysisId,
          fallback.summary(),
          fallback.suggestions()
      );
    }
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现AI 辅助生成总结业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 请求参数
   * @return 业务响应数据
   */
  @Override
  public MaintenanceSummaryResponse summarizeMaintenance(
      Long userId,
      Long familyId,
      MaintenanceSummaryRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceAssetEntity device = getDevice(familyId, request.deviceId());
    DeviceContext context = toDeviceContext(device);
    // 维护总结只读取已有维修历史，不直接改写设备或维修数据。
    List<MaintenanceRecordDTO> records = listMaintenanceRecords(familyId, device.getId());
    String inputSummary = "deviceId=" + device.getId() + ", recordCount=" + records.size();
    try {
      MaintenanceSummary summary = sanitize(aiClient.summarizeMaintenance(context, records));
      MaintenanceSummaryResponse response = new MaintenanceSummaryResponse(
          null,
          summary.summary(),
          summary.careSuggestion()
      );
      Long analysisId = saveAnalysis(
          userId,
          familyId,
          AiAnalysisType.MAINTENANCE_SUMMARY,
          "DEVICE",
          device.getId(),
          inputSummary,
          response,
          AiAnalysisStatus.SUCCESS,
          null
      );
      return new MaintenanceSummaryResponse(
          analysisId,
          summary.summary(),
          summary.careSuggestion()
      );
    } catch (BusinessException e) {
      log.warn(
          "Maintenance summary AI fallback: familyId={}, deviceId={}",
          familyId,
          device.getId(),
          e
      );
      MaintenanceSummaryResponse fallback = fallbackMaintenanceSummaryResponse(device.getName());
      Long analysisId = saveAnalysis(
          userId,
          familyId,
          AiAnalysisType.MAINTENANCE_SUMMARY,
          "DEVICE",
          device.getId(),
          inputSummary,
          fallback,
          AiAnalysisStatus.FALLBACK,
          e.getMessage()
      );
      return new MaintenanceSummaryResponse(
          analysisId,
          fallback.summary(),
          fallback.careSuggestion()
      );
    }
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

  private void validateMaintenanceIfPresent(Long familyId, Long deviceId, Long maintenanceId) {
    if (maintenanceId == null) {
      return;
    }
    Long count = maintenanceRecordMapper.selectCount(
        new LambdaQueryWrapper<MaintenanceRecordEntity>()
            .eq(MaintenanceRecordEntity::getId, maintenanceId)
            .eq(MaintenanceRecordEntity::getFamilyId, familyId)
            .eq(MaintenanceRecordEntity::getDeviceId, deviceId)
    );
    if (count == 0) {
      throw new BusinessException(ErrorCode.MAINTENANCE_NOT_FOUND, "维修记录不存在");
    }
  }

  private List<MaintenanceRecordDTO> listMaintenanceRecords(Long familyId, Long deviceId) {
    return maintenanceRecordMapper.selectList(
            new LambdaQueryWrapper<MaintenanceRecordEntity>()
                .eq(MaintenanceRecordEntity::getFamilyId, familyId)
                .eq(MaintenanceRecordEntity::getDeviceId, deviceId)
                .orderByDesc(MaintenanceRecordEntity::getOccurredAt)
        ).stream()
        .map(record -> new MaintenanceRecordDTO(
            record.getTitle(),
            record.getFaultDescription(),
            record.getOccurredAt(),
            record.getStatus(),
            record.getRepairCost(),
            record.getResultDescription(),
            record.getCompletedAt()
        ))
        .toList();
  }

  private Long saveAnalysis(
      Long userId,
      Long familyId,
      AiAnalysisType analysisType,
      String bizType,
      Long bizId,
      String inputSummary,
      Object result,
      AiAnalysisStatus status,
      String errorMessage
  ) {
    // 每次 AI 调用都落分析记录，方便面试时说明可审计和可回溯设计。
    AiAnalysisEntity entity = new AiAnalysisEntity();
    entity.setUserId(userId);
    entity.setFamilyId(familyId);
    entity.setAnalysisType(analysisType.getCode());
    entity.setBizType(bizType);
    entity.setBizId(bizId);
    entity.setProvider(aiClient.providerName());
    entity.setModel(aiClient.modelName());
    entity.setInputSummary(limit(inputSummary, INPUT_SUMMARY_LIMIT));
    entity.setResultJson(limit(writeJson(result), RESULT_JSON_LIMIT));
    entity.setStatus(status.getCode());
    entity.setErrorMessage(limit(errorMessage, ERROR_MESSAGE_LIMIT));
    aiAnalysisMapper.insert(entity);
    return entity.getId();
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new BusinessException(ErrorCode.AI_PARSE_FAILED, "AI 结果序列化失败", e);
    }
  }

  private DeviceContext toDeviceContext(DeviceAssetEntity device) {
    return new DeviceContext(
        device.getId(),
        device.getName(),
        device.getBrand(),
        device.getModel(),
        getCategoryName(device.getCategoryId()),
        device.getStatus(),
        device.getPurchaseDate(),
        device.getLocation()
    );
  }

  private String troubleshootingBizType(TroubleshootingRequest request) {
    return request.maintenanceId() == null ? "DEVICE" : "MAINTENANCE";
  }

  private Long troubleshootingBizId(DeviceAssetEntity device, TroubleshootingRequest request) {
    return request.maintenanceId() == null ? device.getId() : request.maintenanceId();
  }

  private String getCategoryName(Long categoryId) {
    if (categoryId == null) {
      return null;
    }
    DeviceCategoryEntity category = deviceCategoryMapper.selectById(categoryId);
    return category == null ? null : category.getName();
  }

  private InvoiceParseResult sanitize(InvoiceParseResult result) {
    // AI 输出统一做长度和金额兜底，避免异常内容直接进入响应或审计表。
    return new InvoiceParseResult(
        limit(result.deviceName(), 128),
        result.purchaseDate(),
        nonNegative(result.price()),
        limit(result.seller(), 128),
        limit(result.suggestedCategory(), 64)
    );
  }

  private TroubleshootingSuggestion sanitize(TroubleshootingSuggestion suggestion) {
    List<String> suggestions = suggestion.suggestions() == null
        ? List.of()
        : suggestion.suggestions();
    List<String> limited = suggestions.stream()
        .filter(StringUtils::hasText)
        .map(item -> limit(item, SUGGESTION_LIMIT))
        .limit(6)
        .toList();
    if (limited.isEmpty()) {
      limited = fallbackTroubleshootingResponse("该设备").suggestions();
    }
    return new TroubleshootingSuggestion(limit(suggestion.summary(), SUMMARY_LIMIT), limited);
  }

  private MaintenanceSummary sanitize(MaintenanceSummary summary) {
    return new MaintenanceSummary(
        limit(summary.summary(), SUMMARY_LIMIT),
        limit(summary.careSuggestion(), SUMMARY_LIMIT)
    );
  }

  private BigDecimal nonNegative(BigDecimal price) {
    if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
      return null;
    }
    return price;
  }

  private InvoiceParseResponse fallbackInvoiceResponse() {
    return new InvoiceParseResponse(null, null, null, null, null, "待人工确认");
  }

  private TroubleshootingResponse fallbackTroubleshootingResponse(String deviceName) {
    String name = StringUtils.hasText(deviceName) ? deviceName : "该设备";
    return new TroubleshootingResponse(
        null,
        name + "暂时无法生成 AI 排查建议，请先按通用步骤排查。",
        List.of(
            "确认电源、网络、连接线和耗材状态是否正常。",
            "记录故障出现时间、频率和现场现象，便于联系售后。",
            "如果设备仍在保修期内，优先联系官方售后并保存维修凭证。"
        )
    );
  }

  private MaintenanceSummaryResponse fallbackMaintenanceSummaryResponse(String deviceName) {
    String name = StringUtils.hasText(deviceName) ? deviceName : "该设备";
    return new MaintenanceSummaryResponse(
        null,
        name + "暂时无法生成 AI 维修总结，请查看维修记录列表。",
        "建议持续补充维修结果、费用和凭证，后续可重新生成维护总结。"
    );
  }

  private String limit(String value, int maxLength) {
    if (!StringUtils.hasText(value)) {
      return value;
    }
    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }
}
