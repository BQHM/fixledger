package com.fixledger.modules.ai.response;

public record MaintenanceSummaryResponse(
    Long analysisId,
    String summary,
    String careSuggestion
) {
}
