package com.fixledger.modules.exporter.service;

/**
 * CSV 导出结果。
 */
public record CsvExportFile(
    String filename,
    byte[] content
) {
}
