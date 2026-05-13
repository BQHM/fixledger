package com.fixledger.modules.maintenance.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件功能说明：维修响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
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
