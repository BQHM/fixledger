package com.fixledger.modules.family.enums;

/**
 * <p>
 * 文件功能说明：家庭空间业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
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
