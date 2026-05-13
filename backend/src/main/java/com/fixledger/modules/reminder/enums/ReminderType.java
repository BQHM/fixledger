package com.fixledger.modules.reminder.enums;

import java.util.Arrays;

/**
 * <p>
 * 文件功能说明：提醒通知业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum ReminderType {

  WARRANTY_EXPIRE_SOON("WARRANTY_EXPIRE_SOON", "保修即将到期"),
  WARRANTY_EXPIRED("WARRANTY_EXPIRED", "保修已到期"),
  CONSUMABLE_REPLACE_SOON("CONSUMABLE_REPLACE_SOON", "耗材即将更换"),
  CONSUMABLE_OVERDUE("CONSUMABLE_OVERDUE", "耗材已逾期"),
  MAINTENANCE_FOLLOW_UP("MAINTENANCE_FOLLOW_UP", "维修待跟进");

  private final String code;
  private final String description;

  ReminderType(String code, String description) {
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
  public static ReminderType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
