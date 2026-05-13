package com.fixledger.infrastructure.ai;

import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：AI 基础设施实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
public record DeviceContext(
    Long deviceId,
    String name,
    String brand,
    String model,
    String categoryName,
    String status,
    LocalDate purchaseDate,
    String location
) {
}
