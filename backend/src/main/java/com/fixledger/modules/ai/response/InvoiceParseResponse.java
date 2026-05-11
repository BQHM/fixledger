package com.fixledger.modules.ai.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceParseResponse(
    Long analysisId,
    String deviceName,
    LocalDate purchaseDate,
    BigDecimal price,
    String seller,
    String suggestedCategory
) {
}
