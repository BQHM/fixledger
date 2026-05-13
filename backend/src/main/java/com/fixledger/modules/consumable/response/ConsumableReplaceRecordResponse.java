package com.fixledger.modules.consumable.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：耗材响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record ConsumableReplaceRecordResponse(
    Long id,
    Long consumableId,
    Long deviceId,
    LocalDate replacedDate,
    BigDecimal cost,
    String note
) {
}
