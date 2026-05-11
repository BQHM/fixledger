package com.fixledger.modules.file.enums;

import java.util.Arrays;

public enum FileBizType {

  DEVICE("DEVICE", "设备"),
  WARRANTY("WARRANTY", "保修"),
  MAINTENANCE("MAINTENANCE", "维修"),
  CONSUMABLE("CONSUMABLE", "耗材"),
  MANUAL("MANUAL", "说明书");

  private final String code;
  private final String description;

  FileBizType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public static FileBizType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
