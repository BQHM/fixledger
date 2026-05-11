package com.fixledger.infrastructure.ai;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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
