package com.fixledger.infrastructure.ai;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 文件功能说明：AI 基础设施实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
@Component
public class PromptTemplateService {

  private final ResourceLoader resourceLoader;

  public PromptTemplateService(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成执行业务处理基础设施操作。
   * </p>
   * @param templateName templateName 参数
   * @param MapString MapString 参数
   * @param variables variables 参数
   * @return 处理结果
   */
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
