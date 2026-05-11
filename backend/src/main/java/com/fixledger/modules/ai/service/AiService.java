package com.fixledger.modules.ai.service;

import com.fixledger.modules.ai.request.InvoiceParseRequest;
import com.fixledger.modules.ai.request.MaintenanceSummaryRequest;
import com.fixledger.modules.ai.request.TroubleshootingRequest;
import com.fixledger.modules.ai.response.InvoiceParseResponse;
import com.fixledger.modules.ai.response.MaintenanceSummaryResponse;
import com.fixledger.modules.ai.response.TroubleshootingResponse;

/**
 * AI 辅助服务，只提供录入、排查和总结建议，不直接覆盖核心业务数据。
 */
public interface AiService {

  /**
   * 从发票或订单文本中提取设备录入建议。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 发票文本请求
   * @return 设备名称、购买日期、价格等建议字段
   */
  InvoiceParseResponse parseInvoice(Long userId, Long familyId, InvoiceParseRequest request);

  /**
   * 根据设备信息和故障描述生成初步排查建议。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 故障描述请求
   * @return 排查摘要和建议列表
   */
  TroubleshootingResponse suggestTroubleshooting(
      Long userId,
      Long familyId,
      TroubleshootingRequest request
  );

  /**
   * 根据设备维修历史生成维护总结。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 设备总结请求
   * @return 维修总结和保养建议
   */
  MaintenanceSummaryResponse summarizeMaintenance(
      Long userId,
      Long familyId,
      MaintenanceSummaryRequest request
  );
}