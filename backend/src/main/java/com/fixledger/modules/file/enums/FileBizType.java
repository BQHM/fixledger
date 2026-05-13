package com.fixledger.modules.file.enums;

import java.util.Arrays;

/**
 * <p>
 * 文件功能说明：附件资源业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum FileBizType {

  DEVICE("DEVICE", "设备"),
  WARRANTY("WARRANTY", "保修"),
  MAINTENANCE("MAINTENANCE", "维修"),
  CONSUMABLE("CONSUMABLE", "耗材"),
  MANUAL("MANUAL", "说明书");

  private final String code;
  private final String description;

  FileBizType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：按编码转换枚举。
   * </p>
   * @param code 编码值
   * @return 处理结果
   */
  public static FileBizType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
