package com.fixledger.infrastructure.ai;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * 文件功能说明：AI 基础设施配置对象，集中绑定配置文件中的相关参数。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@ConfigurationProperties(prefix = "fixledger.ai")
public record AiProperties(
    boolean enabled,
    @NotNull(message = "AI Provider 不能为空") Provider provider,
    String apiKey,
    String baseUrl,
    String model
) {

  public enum Provider {
    MOCK,
    OPENAI_COMPATIBLE
  }
}
