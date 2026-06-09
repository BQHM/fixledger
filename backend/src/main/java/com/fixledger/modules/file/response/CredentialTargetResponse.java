package com.fixledger.modules.file.response;

/**
 * 凭证盒可挂载目标响应对象。
 */
public record CredentialTargetResponse(
    Long bizId,
    String label
) {
}
