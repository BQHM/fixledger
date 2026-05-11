package com.fixledger.modules.asset.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DeviceDetailResponse(
    Long id,
    Long categoryId,
    String categoryName,
    String name,
    String brand,
    String model,
    String serialNumber,
    String status,
    LocalDate purchaseDate,
    String purchaseChannel,
    BigDecimal purchasePrice,
    String location,
    String remark,
    List<Object> warranties,
    List<Object> consumables,
    List<Object> maintenanceRecords,
    List<Object> files
) {
}
