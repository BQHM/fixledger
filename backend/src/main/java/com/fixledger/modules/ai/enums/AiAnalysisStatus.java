package com.fixledger.modules.ai.enums;

import java.util.Arrays;

/**
 * <p>
 * 文件功能说明：AI 辅助业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum AiAnalysisStatus {

  SUCCESS("SUCCESS", "成功"),
  FALLBACK("FALLBACK", "兜底"),
  FAILED("FAILED", "失败");

  private final String code;
  private final String description;

  AiAnalysisStatus(String code, String description) {
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
  public static AiAnalysisStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
