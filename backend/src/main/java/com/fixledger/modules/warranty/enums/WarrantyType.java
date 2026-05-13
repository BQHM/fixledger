package com.fixledger.modules.warranty.enums;

import java.util.Arrays;

/**
 * <p>
 * 文件功能说明：保修业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum WarrantyType {

  OFFICIAL("OFFICIAL", "官方保修"),
  EXTENDED("EXTENDED", "延保"),
  STORE("STORE", "店铺保修"),
  OTHER("OTHER", "其他");

  private final String code;
  private final String description;

  WarrantyType(String code, String description) {
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
  public static WarrantyType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
