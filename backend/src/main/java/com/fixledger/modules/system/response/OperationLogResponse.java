package com.fixledger.modules.system.response;

import java.time.LocalDateTime;

/**
 * 操作日志响应对象。
 */
public record OperationLogResponse(
    Long id,
    Long userId,
    Long familyId,
    String module,
    String action,
    String bizType,
    Long bizId,
    String requestMethod,
    String requestUri,
    String ipAddress,
    Boolean success,
    String errorMessage,
    LocalDateTime createdAt
) {
}
