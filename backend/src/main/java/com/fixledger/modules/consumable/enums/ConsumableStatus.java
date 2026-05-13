package com.fixledger.modules.consumable.enums;

import java.util.Arrays;

/**
 * <p>
 * 文件功能说明：耗材业务枚举，统一维护状态码和展示描述。
 * </p>
 *
 * @Author FixLedger
 */
public enum ConsumableStatus {

  NORMAL("NORMAL", "正常"),
  DUE_SOON("DUE_SOON", "即将到期"),
  OVERDUE("OVERDUE", "已逾期"),
  DISABLED("DISABLED", "停用");

  private final String code;
  private final String description;

  ConsumableStatus(String code, String description) {
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
  public static ConsumableStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }
}
