package com.fixledger.infrastructure.ai;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class PromptTemplateService {

  private final ResourceLoader resourceLoader;

  public PromptTemplateService(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public String render(String templateName, Map<String, String> variables) {
    String rendered = loadTemplate(templateName);
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      String placeholder = "{{" + entry.getKey() + "}}";
      rendered = rendered.replace(placeholder, entry.getValue() == null ? "" : entry.getValue());
    }
    return rendered;
  }

  private String loadTemplate(String templateName) {
    Resource resource = resourceLoader.getResource("classpath:prompts/" + templateName);
    try {
      return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.AI_PARSE_FAILED, "AI Prompt 模板读取失败", e);
    }
  }
}
