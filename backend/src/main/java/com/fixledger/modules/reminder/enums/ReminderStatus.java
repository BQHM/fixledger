package com.fixledger.modules.reminder.enums;

import java.util.Arrays;

/**
 * <p>
 * 文件功能说明：提醒通知业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum ReminderStatus {

  PENDING("PENDING", "待提醒"),
  SENT("SENT", "已发送"),
  READ("READ", "已读"),
  IGNORED("IGNORED", "已忽略"),
  FAILED("FAILED", "发送失败");

  private final String code;
  private final String description;

  ReminderStatus(String code, String description) {
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
  public static ReminderStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
