package com.fixledger.infrastructure.ai;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceRecordDTO(
    String title,
    String faultDescription,
    LocalDateTime occurredAt,
    String status,
    BigDecimal repairCost,
    String resultDescription,
    LocalDateTime completedAt
) {
}
