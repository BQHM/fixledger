package com.fixledger.modules.ai.response;

/**
 * <p>
 * 文件功能说明：AI 辅助响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record MaintenanceSummaryResponse(
    Long analysisId,
    String summary,
    String careSuggestion
) {
}
