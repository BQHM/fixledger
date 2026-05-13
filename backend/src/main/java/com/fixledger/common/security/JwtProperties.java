package com.fixledger.common.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * 文件功能说明：认证安全组件，为各业务模块提供可复用能力。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@ConfigurationProperties(prefix = "fixledger.jwt")
public record JwtProperties(
    @NotBlank(message = "JWT 密钥不能为空") String secret,
    @Positive(message = "访问令牌有效期必须大于 0") long accessTokenTtlSeconds
) {
}
