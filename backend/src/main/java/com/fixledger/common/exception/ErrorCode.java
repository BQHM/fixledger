package com.fixledger.common.exception;

public enum ErrorCode {

  SYSTEM_ERROR(1000, "系统异常"),
  BAD_REQUEST(1001, "请求参数错误"),
  UNAUTHORIZED(1002, "未认证"),
  FORBIDDEN(1003, "无访问权限"),
  NOT_FOUND(1004, "资源不存在"),
  METHOD_NOT_ALLOWED(1005, "请求方法不支持"),

  USER_NOT_FOUND(2001, "用户不存在"),
  PASSWORD_ERROR(2002, "密码错误"),
  TOKEN_INVALID(2003, "登录状态无效"),

  FAMILY_SPACE_NOT_FOUND(3001, "家庭空间不存在"),
  FAMILY_MEMBER_NOT_FOUND(3002, "家庭成员不存在"),

  DEVICE_NOT_FOUND(4001, "设备不存在"),
  DEVICE_STATUS_INVALID(4002, "设备状态无效"),

  WARRANTY_NOT_FOUND(5001, "保修记录不存在"),
  WARRANTY_DATE_INVALID(5002, "保修日期无效"),

  CONSUMABLE_NOT_FOUND(6001, "耗材不存在"),
  REPLACE_DATE_INVALID(6002, "更换日期无效"),

  MAINTENANCE_NOT_FOUND(7001, "维修记录不存在"),
  MAINTENANCE_STATUS_INVALID(7002, "维修状态无效"),

  REMINDER_NOT_FOUND(8001, "提醒不存在"),
  NOTIFICATION_SEND_FAILED(8002, "通知发送失败"),

  FILE_UPLOAD_FAILED(9001, "文件上传失败"),
  FILE_TYPE_NOT_ALLOWED(9002, "文件类型不允许"),

  AI_SERVICE_UNAVAILABLE(10001, "AI 服务不可用"),
  AI_PARSE_FAILED(10002, "AI 解析失败"),

  CONFIG_NOT_FOUND(11001, "系统配置不存在");

  private final int code;
  private final String message;

  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
