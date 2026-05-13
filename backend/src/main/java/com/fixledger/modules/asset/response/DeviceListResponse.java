package com.fixledger.modules.asset.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：设备档案响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
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
