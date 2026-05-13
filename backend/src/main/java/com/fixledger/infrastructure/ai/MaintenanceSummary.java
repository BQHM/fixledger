package com.fixledger.infrastructure.ai;

/**
 * <p>
 * 文件功能说明：AI 基础设施实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
public record MaintenanceSummary(
    String summary,
    String careSuggestion
) {
}
