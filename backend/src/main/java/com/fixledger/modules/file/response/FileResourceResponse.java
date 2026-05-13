package com.fixledger.modules.file.response;

/**
 * <p>
 * 文件功能说明：附件资源响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record FileResourceResponse(
    Long id,
    String originalName,
    String contentType,
    Long fileSize,
    String bizType,
    Long bizId
) {
}
