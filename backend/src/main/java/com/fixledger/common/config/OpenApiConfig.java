package com.fixledger.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI fixLedgerOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("FixLedger API")
            .version("0.1.0")
            .description("Family device warranty and consumable management API"));
  }
}
