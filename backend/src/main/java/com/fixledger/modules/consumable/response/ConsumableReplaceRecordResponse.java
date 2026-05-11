package com.fixledger.modules.consumable.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConsumableReplaceRecordResponse(
    Long id,
    Long consumableId,
    Long deviceId,
    LocalDate replacedDate,
    BigDecimal cost,
    String note
) {
}
