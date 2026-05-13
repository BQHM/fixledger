package com.fixledger.modules.ai.enums;

import java.util.Arrays;

/**
 * <p>
 * 文件功能说明：AI 辅助业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum AiAnalysisType {

  INVOICE_PARSE("INVOICE_PARSE", "票据信息提取"),
  TROUBLESHOOTING("TROUBLESHOOTING", "故障排查建议"),
  MAINTENANCE_SUMMARY("MAINTENANCE_SUMMARY", "维修总结"),
  CARE_SUGGESTION("CARE_SUGGESTION", "保养建议");

  private final String code;
  private final String description;

  AiAnalysisType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：按编码转换枚举。
   * </p>
   * @param code 编码值
   * @return 处理结果
   */
  public static AiAnalysisType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
