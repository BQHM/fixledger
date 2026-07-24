package com.fixledger.modules.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fixledger.modules.exporter.config.ExportProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExportPropertiesTest {

  private static final ValidatorFactory VALIDATOR_FACTORY =
      Validation.buildDefaultValidatorFactory();
  private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

  @AfterAll
  static void closeValidatorFactory() {
    VALIDATOR_FACTORY.close();
  }

  @Test
  @DisplayName("同步导出默认上限合法")
  void acceptDefaultLimit() {
    Set<ConstraintViolation<ExportProperties>> violations =
        VALIDATOR.validate(new ExportProperties());

    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("同步导出上限不能小于一行")
  void rejectLimitBelowMinimum() {
    ExportProperties properties = new ExportProperties();
    properties.setMaxSyncRows(0);

    Set<ConstraintViolation<ExportProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations).isNotEmpty();
  }

  @Test
  @DisplayName("同步导出上限不能超过十万行")
  void rejectLimitAboveMaximum() {
    ExportProperties properties = new ExportProperties();
    properties.setMaxSyncRows(100001);

    Set<ConstraintViolation<ExportProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations).isNotEmpty();
  }
}
