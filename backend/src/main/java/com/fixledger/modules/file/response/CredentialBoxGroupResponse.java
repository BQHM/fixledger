package com.fixledger.modules.file.response;

import java.util.List;

/**
 * 凭证盒分组响应对象，按设备凭证、说明书、保修、维修和耗材归档。
 */
public record CredentialBoxGroupResponse(
    String bizType,
    String title,
    String shortTitle,
    String description,
    List<CredentialTargetResponse> targets,
    List<CredentialFileResponse> files
) {
}
