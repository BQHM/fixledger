package com.fixledger.infrastructure.ai;

import java.time.LocalDate;

public record DeviceContext(
    Long deviceId,
    String name,
    String brand,
    String model,
    String categoryName,
    String status,
    LocalDate purchaseDate,
    String location
) {
}
