package com.fixledger.modules.maintenance.enums;

import java.util.Arrays;
import java.util.Set;

public enum MaintenanceStatus {

  PENDING("PENDING", "待处理"),
  REPORTED("REPORTED", "已报修"),
  REPAIRING("REPAIRING", "维修中"),
  COMPLETED("COMPLETED", "已完成"),
  CANCELED("CANCELED", "已取消");

  private final String code;
  private final String description;

  MaintenanceStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public boolean canTransitionTo(MaintenanceStatus target) {
    return switch (this) {
      case PENDING -> Set.of(REPORTED, CANCELED).contains(target);
      case REPORTED -> Set.of(REPAIRING, CANCELED).contains(target);
      case REPAIRING -> Set.of(COMPLETED, CANCELED).contains(target);
      case COMPLETED, CANCELED -> false;
    };
  }

  public static MaintenanceStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
