package com.fixledger.modules.asset.enums;

import java.util.Arrays;

public enum DeviceStatus {

  NORMAL("NORMAL", "正常使用"),
  PENDING_REPAIR("PENDING_REPAIR", "待维修"),
  REPAIRING("REPAIRING", "维修中"),
  REPAIRED("REPAIRED", "已维修"),
  IDLE("IDLE", "闲置"),
  SCRAPPED("SCRAPPED", "已报废");

  private final String code;
  private final String description;

  DeviceStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public static DeviceStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
