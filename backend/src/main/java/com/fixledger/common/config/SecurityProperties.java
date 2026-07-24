package com.fixledger.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全入口配置，控制只应在开发环境公开的辅助端点。
 *
 * @param apiDocsPublic 是否允许匿名访问 OpenAPI 与 Swagger UI
 */
@ConfigurationProperties(prefix = "fixledger.security")
public record SecurityProperties(boolean apiDocsPublic) {
}
