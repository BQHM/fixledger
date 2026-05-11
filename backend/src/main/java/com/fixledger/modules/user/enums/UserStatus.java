package com.fixledger.modules.user.enums;

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
