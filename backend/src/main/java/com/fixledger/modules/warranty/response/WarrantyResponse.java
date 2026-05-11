package com.fixledger.modules.warranty.response;

import java.time.LocalDate;

public record WarrantyResponse(
    Long id,
    Long deviceId,
    String deviceName,
    String warrantyType,
    LocalDate startDate,
    LocalDate endDate,
    Integer remindDaysBefore,
    String servicePhone,
    String serviceAddress,
    String serviceNote
) {
}
