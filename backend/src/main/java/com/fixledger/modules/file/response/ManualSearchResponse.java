package com.fixledger.modules.file.response;

/**
 * 说明书全文搜索结果响应对象。
 */
public record ManualSearchResponse(
    Long fileId,
    String fileName,
    String contentType,
    Long fileSize,
    String snippet
) {
}
