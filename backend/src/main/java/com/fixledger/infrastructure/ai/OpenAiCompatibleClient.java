package com.fixledger.infrastructure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * <p>
 * 文件功能说明：AI 基础设施实现，封装外部依赖和技术细节。
 * </p>
 *
 * @Author FixLedger
 */
public class OpenAiCompatibleClient implements AiClient {

  private final AiProperties properties;
  private final ObjectMapper objectMapper;
  private final PromptTemplateService promptTemplateService;
  private final RestClient restClient;

  public OpenAiCompatibleClient(
      AiProperties properties,
      ObjectMapper objectMapper,
      PromptTemplateService promptTemplateService
  ) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.promptTemplateService = promptTemplateService;
    this.restClient = RestClient.create();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成解析数据基础设施操作。
   * </p>
   * @param text 待解析文本
   * @return 统一响应结果
   */
  @Override
  public InvoiceParseResult parseInvoiceText(String text) {
    String prompt = promptTemplateService.render("invoice-parse.st", Map.of("text", text));
    JsonNode json = readContentJson(requestCompletion(prompt));
    return new InvoiceParseResult(
        textOrNull(json, "deviceName"),
        dateOrNull(json, "purchaseDate"),
        decimalOrNull(json, "price"),
        textOrNull(json, "seller"),
        textOrNull(json, "suggestedCategory")
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成生成建议基础设施操作。
   * </p>
   * @param context 设备上下文
   * @param faultDescription 故障描述
   * @return 处理结果
   */
  @Override
  public TroubleshootingSuggestion suggestTroubleshooting(
      DeviceContext context,
      String faultDescription
  ) {
    String prompt = promptTemplateService.render("troubleshooting.st", Map.of(
        "deviceContext", formatDeviceContext(context),
        "faultDescription", faultDescription
    ));
    JsonNode json = readContentJson(requestCompletion(prompt));
    return new TroubleshootingSuggestion(textOrNull(json, "summary"), listOrEmpty(json));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成生成总结基础设施操作。
   * </p>
   * @param context 设备上下文
   * @param records 维修记录列表
   * @return 处理结果
   */
  @Override
  public MaintenanceSummary summarizeMaintenance(
      DeviceContext context,
      List<MaintenanceRecordDTO> records
  ) {
    String prompt = promptTemplateService.render("maintenance-summary.st", Map.of(
        "deviceContext", formatDeviceContext(context),
        "maintenanceRecords", formatMaintenanceRecords(records)
    ));
    JsonNode json = readContentJson(requestCompletion(prompt));
    return new MaintenanceSummary(
        textOrNull(json, "summary"),
        textOrNull(json, "careSuggestion")
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成返回 AI Provider 名称基础设施操作。
   * </p>
   * @return 处理结果
   */
  @Override
  public String providerName() {
    return "openai_compatible";
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成返回 AI 模型名称基础设施操作。
   * </p>
   * @return 处理结果
   */
  @Override
  public String modelName() {
    return properties.model();
  }

  private String requestCompletion(String prompt) {
    try {
      String response = restClient.post()
          .uri(chatCompletionsUrl())
          .headers(headers -> headers.setBearerAuth(properties.apiKey()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(buildRequest(prompt).toString())
          .retrieve()
          .body(String.class);
      return extractContent(response);
    } catch (BusinessException e) {
      throw e;
    } catch (RestClientException e) {
      throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE, "AI 服务调用失败", e);
    }
  }

  private ObjectNode buildRequest(String prompt) {
    // 系统提示强约束只返回 JSON，降低后续解析失败概率。
    ObjectNode request = objectMapper.createObjectNode();
    request.put("model", properties.model());
    ArrayNode messages = request.putArray("messages");
    messages.addObject()
        .put("role", "system")
        .put("content", "你是家庭设备管理助手，只返回严格 JSON，不输出 Markdown。");
    messages.addObject()
        .put("role", "user")
        .put("content", prompt);
    request.put("temperature", 0.2);
    return request;
  }

  private String extractContent(String response) {
    try {
      JsonNode root = objectMapper.readTree(response);
      String content = root.path("choices").path(0).path("message").path("content").asText();
      if (!StringUtils.hasText(content)) {
        throw new BusinessException(ErrorCode.AI_PARSE_FAILED, "AI 返回内容为空");
      }
      return content;
    } catch (JsonProcessingException e) {
      throw new BusinessException(ErrorCode.AI_PARSE_FAILED, "AI 返回解析失败", e);
    }
  }

  private JsonNode readContentJson(String content) {
    try {
      // 兼容模型偶尔返回的 Markdown 代码块，再交给 Jackson 解析。
      return objectMapper.readTree(stripJsonFence(content));
    } catch (JsonProcessingException e) {
      throw new BusinessException(ErrorCode.AI_PARSE_FAILED, "AI JSON 内容解析失败", e);
    }
  }

  private String stripJsonFence(String content) {
    String trimmed = content == null ? "" : content.trim();
    if (trimmed.startsWith("```json")) {
      trimmed = trimmed.substring("```json".length()).trim();
    } else if (trimmed.startsWith("```")) {
      trimmed = trimmed.substring("```".length()).trim();
    }
    if (trimmed.endsWith("```")) {
      trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
    }
    return trimmed;
  }

  private String chatCompletionsUrl() {
    // 支持传入服务根地址或完整 /chat/completions 地址，方便切换兼容服务。
    String baseUrl = properties.baseUrl();
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    if (baseUrl.endsWith("/chat/completions")) {
      return baseUrl;
    }
    return baseUrl + "/chat/completions";
  }

  private String formatDeviceContext(DeviceContext context) {
    return "设备ID=" + context.deviceId()
        + ", 名称=" + blankToDash(context.name())
        + ", 品牌=" + blankToDash(context.brand())
        + ", 型号=" + blankToDash(context.model())
        + ", 分类=" + blankToDash(context.categoryName())
        + ", 状态=" + blankToDash(context.status())
        + ", 购买日期=" + (context.purchaseDate() == null ? "-" : context.purchaseDate())
        + ", 位置=" + blankToDash(context.location());
  }

  private String formatMaintenanceRecords(List<MaintenanceRecordDTO> records) {
    if (records.isEmpty()) {
      return "暂无维修记录";
    }
    List<String> lines = new ArrayList<>();
    for (MaintenanceRecordDTO record : records) {
      lines.add("标题=" + record.title()
          + ", 故障=" + record.faultDescription()
          + ", 状态=" + record.status()
          + ", 费用=" + record.repairCost()
          + ", 结果=" + record.resultDescription());
    }
    return String.join("\n", lines);
  }

  private String blankToDash(String value) {
    return StringUtils.hasText(value) ? value : "-";
  }

  private String textOrNull(JsonNode json, String fieldName) {
    String value = json.path(fieldName).asText(null);
    return StringUtils.hasText(value) ? value : null;
  }

  private LocalDate dateOrNull(JsonNode json, String fieldName) {
    String value = textOrNull(json, fieldName);
    return value == null ? null : LocalDate.parse(value);
  }

  private BigDecimal decimalOrNull(JsonNode json, String fieldName) {
    JsonNode node = json.path(fieldName);
    if (node.isMissingNode() || node.isNull()) {
      return null;
    }
    if (node.isNumber()) {
      return node.decimalValue();
    }
    String value = node.asText();
    return StringUtils.hasText(value) ? new BigDecimal(value) : null;
  }

  private List<String> listOrEmpty(JsonNode json) {
    JsonNode node = json.path("suggestions");
    if (!node.isArray()) {
      return List.of();
    }
    List<String> suggestions = new ArrayList<>();
    for (JsonNode item : node) {
      if (StringUtils.hasText(item.asText())) {
        suggestions.add(item.asText());
      }
    }
    return suggestions;
  }
}
