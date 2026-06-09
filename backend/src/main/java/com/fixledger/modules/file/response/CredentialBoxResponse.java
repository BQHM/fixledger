package com.fixledger.modules.file.response;

import java.util.List;

/**
 * 设备维度凭证盒聚合响应对象。
 */
public record CredentialBoxResponse(
    Long deviceId,
    String deviceName,
    String location,
    Integer completionPercent,
    Integer archivedTypeCount,
    Integer totalTypeCount,
    Integer totalFileCount,
    Long totalFileSize,
    List<CredentialBoxGroupResponse> groups
) {
}
