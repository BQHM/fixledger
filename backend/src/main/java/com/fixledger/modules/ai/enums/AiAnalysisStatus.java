package com.fixledger.modules.ai.enums;

import java.util.Arrays;

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

  public static AiAnalysisStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
