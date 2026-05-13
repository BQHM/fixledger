package com.fixledger.infrastructure.file;

/**
 * <p>
 * 文件功能说明：文件存储实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
public record StoredFile(
    String storageName,
    String storagePath,
    String extension,
    Long fileSize,
    String contentType
) {
}
