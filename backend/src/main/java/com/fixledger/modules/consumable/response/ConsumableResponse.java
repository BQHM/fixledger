package com.fixledger.modules.consumable.response;

import java.time.LocalDate;

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
