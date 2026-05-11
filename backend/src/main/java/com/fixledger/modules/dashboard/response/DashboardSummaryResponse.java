package com.fixledger.modules.dashboard.response;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
    long deviceTotal,
    long warrantyExpiringCount,
    long warrantyExpiredCount,
    long consumableDueSoonCount,
    long consumableOverdueCount,
    long repairingCount,
    BigDecimal monthlyMaintenanceCost
) {
}
