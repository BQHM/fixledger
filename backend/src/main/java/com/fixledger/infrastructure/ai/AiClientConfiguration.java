package com.fixledger.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 文件功能说明：AI 基础设施实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
@Configuration
public class AiClientConfiguration {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理基础设施操作。
   * </p>
   * @param properties 文件存储配置
   * @param objectMapper JSON 处理器
   * @param promptTemplateService Prompt 模板服务
   * @return 处理结果
   */
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
