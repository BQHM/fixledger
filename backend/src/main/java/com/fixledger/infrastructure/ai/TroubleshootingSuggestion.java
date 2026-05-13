package com.fixledger.infrastructure.ai;

import java.util.List;

/**
 * <p>
 * 文件功能说明：AI 基础设施实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
public record TroubleshootingSuggestion(
    String summary,
    List<String> suggestions
) {
}
