package com.fixledger.modules.consumable.response;

import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：耗材响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record ConsumableResponse(
    Long id,
    Long deviceId,
    String deviceName,
    String name,
    String brand,
    String model,
    Integer cycleDays,
    LocalDate lastReplacedDate,
    LocalDate nextRemindDate,
    Integer remindDaysBefore,
    String status,
    Boolean enabled,
    String remark
) {
}
