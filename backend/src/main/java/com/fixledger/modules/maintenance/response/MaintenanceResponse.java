package com.fixledger.modules.maintenance.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceResponse(
    Long id,
    Long deviceId,
    String deviceName,
    String title,
    String faultDescription,
    LocalDateTime occurredAt,
    String status,
    String repairChannel,
    String repairContact,
    BigDecimal repairCost,
    String resultDescription,
    LocalDateTime completedAt
) {
}
