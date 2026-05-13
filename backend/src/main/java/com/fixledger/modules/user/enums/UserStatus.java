package com.fixledger.modules.user.enums;

/**
 * <p>
 * 文件功能说明：用户业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum UserStatus {

  ENABLED("ENABLED", "启用"),
  DISABLED("DISABLED", "停用");

  private final String code;
  private final String description;

  UserStatus(String code, String description) {
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
