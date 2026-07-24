package com.fixledger.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

@DisplayName("生产 Profile 安全配置")
class ProductionProfileConfigurationTest {

  @Test
  @DisplayName("生产环境关闭开发入口并启用迁移和优雅停机")
  void shouldLoadSafeProductionDefaults() throws IOException {
    YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
    List<PropertySource<?>> sources = loader.load(
        "application-prod",
        new ClassPathResource("application-prod.yml")
    );

    assertThat(valueOf(sources, "spring.sql.init.mode")).isEqualTo("never");
    assertThat(valueOf(sources, "spring.flyway.enabled")).isEqualTo(true);
    assertThat(valueOf(sources, "spring.flyway.clean-disabled")).isEqualTo(true);
    assertThat(valueOf(sources, "springdoc.api-docs.enabled")).isEqualTo(false);
    assertThat(valueOf(sources, "springdoc.swagger-ui.enabled")).isEqualTo(false);
    assertThat(valueOf(sources, "fixledger.security.api-docs-public")).isEqualTo(false);
    assertThat(valueOf(sources, "server.shutdown")).isEqualTo("graceful");
  }

  @Test
  @DisplayName("Flyway 条件补列使用 MySQL 兼容语法")
  void shouldUseMysqlCompatibleConditionalMigration() throws IOException {
    String baseline = new ClassPathResource(
        "db/migration/V1__baseline_schema.sql"
    ).getContentAsString(StandardCharsets.UTF_8);
    String incremental = new ClassPathResource(
        "db/migration/V2__complete_notification_and_dashboard_indexes.sql"
    ).getContentAsString(StandardCharsets.UTF_8);

    assertThat(baseline).doesNotContain("ADD COLUMN IF NOT EXISTS");
    assertThat(incremental)
        .contains("information_schema.columns")
        .contains("column_name = 'recipient'")
        .contains("column_name = 'attempt_count'")
        .contains("column_name = 'next_retry_at'")
        .contains("column_name = 'last_attempt_at'");
  }

  private Object valueOf(List<PropertySource<?>> sources, String name) {
    return sources.stream()
        .map(source -> source.getProperty(name))
        .filter(value -> value != null)
        .findFirst()
        .orElse(null);
  }
}
