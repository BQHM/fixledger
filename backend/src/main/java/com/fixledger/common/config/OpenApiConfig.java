package com.fixledger.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 文件功能说明：基础配置类，集中声明 Spring 容器中的基础 Bean。
 * </p>
 *
 * @Author FixLedger
 */
@Configuration
public class OpenApiConfig {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成声明 OpenAPI 信息配置。
   * </p>
   * @return 处理结果
   */
  @Bean
  public OpenAPI fixLedgerOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("FixLedger API")
            .version("0.1.0")
            .description("Family device warranty and consumable management API"));
  }
}
