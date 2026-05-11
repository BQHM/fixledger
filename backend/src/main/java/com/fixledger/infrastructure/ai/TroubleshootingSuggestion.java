package com.fixledger.infrastructure.ai;

import java.util.List;

public record TroubleshootingSuggestion(
    String summary,
    List<String> suggestions
) {
}
