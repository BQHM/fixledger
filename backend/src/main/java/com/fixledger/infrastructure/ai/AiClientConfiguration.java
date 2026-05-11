package com.fixledger.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class AiClientConfiguration {

  @Bean
  public AiClient aiClient(
      AiProperties properties,
      ObjectMapper objectMapper,
      PromptTemplateService promptTemplateService
  ) {
    if (shouldUseOpenAiCompatible(properties)) {
      return new OpenAiCompatibleClient(properties, objectMapper, promptTemplateService);
    }
    return new MockAiClient();
  }

  private boolean shouldUseOpenAiCompatible(AiProperties properties) {
    return properties.enabled()
        && properties.provider() == AiProperties.Provider.OPENAI_COMPATIBLE
        && StringUtils.hasText(properties.apiKey())
        && StringUtils.hasText(properties.baseUrl())
        && StringUtils.hasText(properties.model());
  }
}
