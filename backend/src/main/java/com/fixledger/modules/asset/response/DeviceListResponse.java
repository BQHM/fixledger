package com.fixledger.modules.asset.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeviceListResponse(
    Long id,
    String name,
    String brand,
    String model,
    String categoryName,
    LocalDate purchaseDate,
    BigDecimal purchasePrice,
    String location,
    String status,
    String warrantyStatus,
    LocalDate nextReminderDate
) {
}
