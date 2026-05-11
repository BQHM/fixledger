package com.fixledger.modules.ai.enums;

import java.util.Arrays;

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

  public static AiAnalysisType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
