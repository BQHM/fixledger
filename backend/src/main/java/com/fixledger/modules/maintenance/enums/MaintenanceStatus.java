package com.fixledger.modules.maintenance.enums;

import java.util.Arrays;
import java.util.Set;

/**
 * <p>
 * 文件功能说明：维修业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
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

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：执行业务处理。
   * </p>
   * @param target target 参数
   * @return 是否处理成功
   */
  public boolean canTransitionTo(MaintenanceStatus target) {
    return switch (this) {
      case PENDING -> Set.of(REPORTED, CANCELED).contains(target);
      case REPORTED -> Set.of(REPAIRING, CANCELED).contains(target);
      case REPAIRING -> Set.of(COMPLETED, CANCELED).contains(target);
      case COMPLETED, CANCELED -> false;
    };
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：按编码转换枚举。
   * </p>
   * @param code 编码值
   * @return 处理结果
   */
  public static MaintenanceStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
