package com.fixledger.infrastructure.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

public class MockAiClient implements AiClient {

  private static final Pattern NAME_PATTERN = Pattern.compile("商品名称[:：]\\s*([^\\n\\r]+)");
  private static final Pattern DATE_PATTERN = Pattern.compile("购买日期[:：]\\s*(\\d{4}-\\d{2}-\\d{2})");
  private static final Pattern PRICE_PATTERN = Pattern.compile(
      "(?:金额|价格)[:：]\\s*([0-9]+(?:\\.[0-9]+)?)"
  );
  private static final Pattern SELLER_PATTERN = Pattern.compile("(?:销售方|商家)[:：]\\s*([^\\n\\r]+)");

  @Override
  public InvoiceParseResult parseInvoiceText(String text) {
    String deviceName = firstMatch(NAME_PATTERN, text);
    LocalDate purchaseDate = parseDate(firstMatch(DATE_PATTERN, text));
    BigDecimal price = parsePrice(firstMatch(PRICE_PATTERN, text));
    String seller = firstMatch(SELLER_PATTERN, text);
    String category = suggestCategory(deviceName + " " + text);
    return new InvoiceParseResult(deviceName, purchaseDate, price, seller, category);
  }

  @Override
  public TroubleshootingSuggestion suggestTroubleshooting(
      DeviceContext context,
      String faultDescription
  ) {
    String deviceName = StringUtils.hasText(context.name()) ? context.name() : "该设备";
    List<String> suggestions = buildTroubleshootingSuggestions(faultDescription);
    String summary = deviceName + "出现“" + faultDescription + "”，建议先从常见外部因素排查。";
    return new TroubleshootingSuggestion(summary, suggestions);
  }

  @Override
  public MaintenanceSummary summarizeMaintenance(
      DeviceContext context,
      List<MaintenanceRecordDTO> records
  ) {
    String deviceName = StringUtils.hasText(context.name()) ? context.name() : "该设备";
    if (records.isEmpty()) {
      return new MaintenanceSummary(
          deviceName + "暂无维修记录，当前没有明显的历史故障趋势。",
          "建议继续保留发票、保修凭证和后续维修记录，方便形成完整设备档案。"
      );
    }
    String summary = deviceName + "当前共有 " + records.size()
        + " 条维修记录，可重点关注重复出现的故障描述和维修费用变化。";
    String careSuggestion = "建议按保修、耗材和维修记录定期复盘，发现同类问题反复出现时优先联系售后。";
    return new MaintenanceSummary(summary, careSuggestion);
  }

  @Override
  public String providerName() {
    return "mock";
  }

  @Override
  public String modelName() {
    return "mock-rule-engine";
  }

  private List<String> buildTroubleshootingSuggestions(String faultDescription) {
    // Mock Provider 用关键词规则覆盖开发测试，不依赖真实外部模型。
    List<String> suggestions = new ArrayList<>();
    String text = faultDescription == null ? "" : faultDescription;
    if (containsAny(text, "出水", "滤芯", "净水")) {
      suggestions.add("检查滤芯是否达到更换周期，必要时先更换或复位滤芯状态。");
      suggestions.add("检查进水阀是否完全打开，并确认管路没有弯折或堵塞。");
    }
    if (containsAny(text, "网络", "断网", "路由", "Wi-Fi", "wifi")) {
      suggestions.add("重启路由器和光猫，确认网线、宽带账号和上级网络状态。");
      suggestions.add("检查设备固件版本和信号覆盖，必要时调整摆放位置。");
    }
    if (containsAny(text, "异响", "噪音", "震动")) {
      suggestions.add("检查设备是否放置平稳，并清理风道、滚刷或运动部件周围异物。");
    }
    if (containsAny(text, "不通电", "无法开机", "没电")) {
      suggestions.add("确认插座、电源线和适配器正常，再尝试断电等待后重新启动。");
    }
    if (suggestions.isEmpty()) {
      suggestions.add("先记录故障出现时间、频率和触发条件，便于后续维修沟通。");
      suggestions.add("检查电源、连接线、耗材寿命和官方说明书中的基础排查步骤。");
    }
    suggestions.add("如果仍未恢复，建议联系官方售后并保留沟通记录和维修凭证。");
    return suggestions;
  }

  private boolean containsAny(String text, String... keywords) {
    for (String keyword : keywords) {
      if (text.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  private String suggestCategory(String text) {
    if (!StringUtils.hasText(text)) {
      return "家用设备";
    }
    if (containsAny(text, "净水器", "冰箱", "电饭煲", "烤箱")) {
      return "厨房设备";
    }
    if (containsAny(text, "吸尘器", "扫地", "洗地机")) {
      return "清洁设备";
    }
    if (containsAny(text, "路由", "手机", "电脑", "耳机", "显示器")) {
      return "数码设备";
    }
    return "家用设备";
  }

  private String firstMatch(Pattern pattern, String text) {
    if (!StringUtils.hasText(text)) {
      return null;
    }
    Matcher matcher = pattern.matcher(text);
    return matcher.find() ? matcher.group(1).trim() : null;
  }

  private LocalDate parseDate(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    return LocalDate.parse(value);
  }

  private BigDecimal parsePrice(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    return new BigDecimal(value);
  }
}

