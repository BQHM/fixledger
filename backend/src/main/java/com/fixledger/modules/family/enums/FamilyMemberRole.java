package com.fixledger.modules.family.enums;

public enum FamilyMemberRole {

  OWNER("OWNER", "所有者"),
  MEMBER("MEMBER", "成员");

  private final String code;
  private final String description;

  FamilyMemberRole(String code, String description) {
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
