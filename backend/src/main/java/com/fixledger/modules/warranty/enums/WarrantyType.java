package com.fixledger.modules.warranty.enums;

import java.util.Arrays;

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

  public static WarrantyType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
