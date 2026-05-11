package com.fixledger.modules.reminder.enums;

public enum NotificationChannel {

  IN_APP("IN_APP", "站内通知");

  private final String code;
  private final String description;

  NotificationChannel(String code, String description) {
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
