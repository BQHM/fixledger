package com.fixledger.modules.dashboard.response;

import java.math.BigDecimal;

public record MaintenanceCostTrendResponse(String month, BigDecimal cost) {
}
