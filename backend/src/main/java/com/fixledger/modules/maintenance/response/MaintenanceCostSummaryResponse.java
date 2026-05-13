package com.fixledger.modules.maintenance.response;

import java.math.BigDecimal;

/**
 * <p>
 * 文件功能说明：维修响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record MaintenanceCostSummaryResponse(BigDecimal totalCost, Long recordCount) {
}
