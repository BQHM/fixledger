package com.fixledger.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationPropertiesTest {

  private static final ValidatorFactory VALIDATOR_FACTORY =
      Validation.buildDefaultValidatorFactory();
  private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

  @AfterAll
  static void closeValidatorFactory() {
    VALIDATOR_FACTORY.close();
  }

  @Test
  @DisplayName("邮件启用时必须配置发件地址")
  void requireSenderAddressWhenEmailEnabled() {
    NotificationProperties properties = new NotificationProperties();
    properties.getEmail().setEnabled(true);

    Set<ConstraintViolation<NotificationProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Notification email sender address is required when email is enabled");
  }

  @Test
  @DisplayName("Webhook 启用时必须配置 HTTPS 地址")
  void requireHttpsEndpointWhenWebhookEnabled() {
    NotificationProperties properties = new NotificationProperties();
    properties.getWebhook().setEnabled(true);
    properties.getWebhook().setEndpoint("http://example.com/fixledger");

    Set<ConstraintViolation<NotificationProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Webhook endpoint must use HTTPS unless insecure HTTP is enabled");
  }

  @Test
  @DisplayName("Webhook 地址必须包含有效主机名")
  void requireHostInWebhookEndpoint() {
    NotificationProperties properties = new NotificationProperties();
    properties.getWebhook().setEnabled(true);
    properties.getWebhook().setEndpoint("https:relative-path");

    Set<ConstraintViolation<NotificationProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Webhook endpoint must use HTTPS unless insecure HTTP is enabled");
  }

  @Test
  @DisplayName("显式允许不安全 HTTP 时可使用本地 Webhook")
  void allowLocalHttpEndpointWhenExplicitlyEnabled() {
    NotificationProperties properties = new NotificationProperties();
    properties.getWebhook().setEnabled(true);
    properties.getWebhook().setEndpoint("http://localhost:9002/fixledger");
    properties.getWebhook().setAllowInsecureHttp(true);

    Set<ConstraintViolation<NotificationProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("通知投递时间配置必须为正数")
  void rejectNonPositiveDurations() {
    NotificationProperties properties = new NotificationProperties();
    properties.setProcessingTimeout(java.time.Duration.ZERO);

    Set<ConstraintViolation<NotificationProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Notification durations must be positive");
  }

  @Test
  @DisplayName("邮件发件地址必须符合邮箱格式")
  void rejectInvalidEmailSenderAddress() {
    NotificationProperties properties = new NotificationProperties();
    properties.getEmail().setEnabled(true);
    properties.getEmail().setFrom("invalid-address");

    Set<ConstraintViolation<NotificationProperties>> violations = VALIDATOR.validate(properties);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Notification email sender address is invalid");
  }
}
