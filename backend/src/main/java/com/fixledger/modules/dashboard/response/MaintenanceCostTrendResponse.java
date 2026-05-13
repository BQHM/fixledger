package com.fixledger.modules.dashboard.response;

import java.math.BigDecimal;

/**
 * <p>
 * 文件功能说明：首页看板响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record MaintenanceCostTrendResponse(String month, BigDecimal cost) {
}
