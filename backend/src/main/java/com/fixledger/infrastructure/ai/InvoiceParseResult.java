package com.fixledger.infrastructure.ai;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：AI 基础设施实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
public record InvoiceParseResult(
    String deviceName,
    LocalDate purchaseDate,
    BigDecimal price,
    String seller,
    String suggestedCategory
) {
}
