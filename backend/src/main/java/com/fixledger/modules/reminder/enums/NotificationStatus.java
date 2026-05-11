package com.fixledger.modules.reminder.enums;

public enum NotificationStatus {

  SENT("SENT", "已发送"),
  FAILED("FAILED", "发送失败");

  private final String code;
  private final String description;

  NotificationStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}
