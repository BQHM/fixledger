package com.fixledger.modules.warranty.response;

import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：保修响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
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
