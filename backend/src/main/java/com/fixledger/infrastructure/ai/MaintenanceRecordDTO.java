package com.fixledger.infrastructure.ai;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件功能说明：AI 基础设施实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
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
