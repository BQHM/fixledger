package com.fixledger.modules.asset.enums;

import java.util.Arrays;

/**
 * <p>
 * 文件功能说明：设备档案业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum DeviceStatus {

  NORMAL("NORMAL", "正常使用"),
  PENDING_REPAIR("PENDING_REPAIR", "待维修"),
  REPAIRING("REPAIRING", "维修中"),
  REPAIRED("REPAIRED", "已维修"),
  IDLE("IDLE", "闲置"),
  SCRAPPED("SCRAPPED", "已报废");

  private final String code;
  private final String description;

  DeviceStatus(String code, String description) {
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
  public static DeviceStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
