package com.fixledger.modules.reminder.enums;

/**
 * <p>
 * 文件功能说明：提醒通知业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum NotificationStatus {

  PENDING("PENDING", "待发送"),
  PROCESSING("PROCESSING", "发送中"),
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
