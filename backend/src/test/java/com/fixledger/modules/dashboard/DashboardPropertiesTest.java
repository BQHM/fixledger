package com.fixledger.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.fixledger.modules.dashboard.config.DashboardProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DashboardPropertiesTest {

  private static final ValidatorFactory VALIDATOR_FACTORY =
      Validation.buildDefaultValidatorFactory();
  private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

  @AfterAll
  static void closeValidatorFactory() {
    VALIDATOR_FACTORY.close();
  }

  @Test
  @DisplayName("首页缓存默认配置合法")
  void acceptDefaultConfiguration() {
    Set<ConstraintViolation<DashboardProperties>> violations =
        VALIDATOR.validate(new DashboardProperties());

    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("首页缓存 TTL 必须为正数")
  void rejectNonPositiveCacheTtl() {
    DashboardProperties properties = new DashboardProperties();
    properties.setSummaryCacheTtl(Duration.ZERO);

    Set<ConstraintViolation<DashboardProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Dashboard summary cache TTL must be positive");
  }
}
