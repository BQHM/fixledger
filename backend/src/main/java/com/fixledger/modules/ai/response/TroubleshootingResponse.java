package com.fixledger.modules.ai.response;

import java.util.List;

public record TroubleshootingResponse(
    Long analysisId,
    String summary,
    List<String> suggestions
) {
}
