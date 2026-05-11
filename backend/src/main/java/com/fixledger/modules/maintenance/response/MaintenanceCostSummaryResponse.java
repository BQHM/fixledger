package com.fixledger.modules.maintenance.response;

import java.math.BigDecimal;

public record MaintenanceCostSummaryResponse(BigDecimal totalCost, Long recordCount) {
}
