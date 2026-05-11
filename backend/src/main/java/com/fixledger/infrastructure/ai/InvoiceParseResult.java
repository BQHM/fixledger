package com.fixledger.infrastructure.ai;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceParseResult(
    String deviceName,
    LocalDate purchaseDate,
    BigDecimal price,
    String seller,
    String suggestedCategory
) {
}
