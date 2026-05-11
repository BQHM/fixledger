package com.fixledger.infrastructure.file;

public record StoredFile(
    String storageName,
    String storagePath,
    String extension,
    Long fileSize,
    String contentType
) {
}
