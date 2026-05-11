package com.fixledger.modules.reminder.enums;

import java.util.Arrays;

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

  public static ReminderStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
