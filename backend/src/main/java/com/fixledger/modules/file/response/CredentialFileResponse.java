package com.fixledger.modules.file.response;

/**
 * 凭证盒附件响应对象，补充业务目标展示名称。
 */
public record CredentialFileResponse(
    Long id,
    String originalName,
    String contentType,
    Long fileSize,
    String bizType,
    Long bizId,
    String targetLabel
) {
}
