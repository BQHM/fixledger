package com.fixledger.modules.ai.response;

import java.util.List;

/**
 * <p>
 * 文件功能说明：AI 辅助响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record TroubleshootingResponse(
    Long analysisId,
    String summary,
    List<String> suggestions
) {
}
