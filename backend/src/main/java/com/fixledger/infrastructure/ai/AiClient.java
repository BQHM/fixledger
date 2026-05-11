package com.fixledger.infrastructure.ai;

import java.util.List;

/**
 * AI 供应商适配接口，业务层只依赖该抽象以便切换 Mock 或真实 Provider。
 */
public interface AiClient {

  /**
   * 解析发票或订单文本，输出可供用户确认的设备字段建议。
   *
   * @param text 发票或订单文本
   * @return 解析结果
   */
  InvoiceParseResult parseInvoiceText(String text);

  /**
   * 结合设备上下文生成故障排查建议。
   *
   * @param context 设备上下文
   * @param faultDescription 故障描述
   * @return 排查建议
   */
  TroubleshootingSuggestion suggestTroubleshooting(
      DeviceContext context,
      String faultDescription
  );

  /**
   * 根据维修历史生成设备维护总结。
   *
   * @param context 设备上下文
   * @param records 维修历史记录
   * @return 维护总结
   */
  MaintenanceSummary summarizeMaintenance(
      DeviceContext context,
      List<MaintenanceRecordDTO> records
  );

  /**
   * 返回当前 AI Provider 名称，用于审计记录。
   *
   * @return Provider 名称
   */
  String providerName();

  /**
   * 返回当前模型名称，用于审计记录。
   *
   * @return 模型名称
   */
  String modelName();
}