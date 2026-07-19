package com.fixledger.modules.reminder.enums;

/**
 * <p>
 * 文件功能说明：提醒通知业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum NotificationChannel {

  IN_APP("IN_APP", "站内通知"),
  EMAIL("EMAIL", "邮件"),
  WEBHOOK("WEBHOOK", "Webhook");

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
