package com.fixledger.modules.file.response;

public record FileResourceResponse(
    Long id,
    String originalName,
    String contentType,
    Long fileSize,
    String bizType,
    Long bizId
) {
}
