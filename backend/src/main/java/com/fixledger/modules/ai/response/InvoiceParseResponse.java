package com.fixledger.modules.ai.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：AI 辅助响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record InvoiceParseResponse(
    Long analysisId,
    String deviceName,
    LocalDate purchaseDate,
    BigDecimal price,
    String seller,
    String suggestedCategory
) {
}
