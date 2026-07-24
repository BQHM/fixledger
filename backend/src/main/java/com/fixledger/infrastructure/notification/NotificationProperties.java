package com.fixledger.infrastructure.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "fixledger.notification")
public class NotificationProperties {

  @Valid
  private final EmailSettings email = new EmailSettings();

  @Valid
  private final Webhook webhook = new Webhook();

  @Min(1)
  @Max(500)
  private int batchSize = 50;

  @Min(1)
  @Max(10)
  private int maxAttempts = 3;

  @NotNull
  private Duration retryDelay = Duration.ofMinutes(5);

  @NotNull
  private Duration maxRetryDelay = Duration.ofHours(6);

  @NotNull
  private Duration connectTimeout = Duration.ofSeconds(5);

  @NotNull
  private Duration requestTimeout = Duration.ofSeconds(10);

  @NotNull
  private Duration processingTimeout = Duration.ofMinutes(15);

  public EmailSettings getEmail() {
    return email;
  }

  public Webhook getWebhook() {
    return webhook;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public void setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  public Duration getRetryDelay() {
    return retryDelay;
  }

  public void setRetryDelay(Duration retryDelay) {
    this.retryDelay = retryDelay;
  }

  public Duration getMaxRetryDelay() {
    return maxRetryDelay;
  }

  public void setMaxRetryDelay(Duration maxRetryDelay) {
    this.maxRetryDelay = maxRetryDelay;
  }

  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public Duration getRequestTimeout() {
    return requestTimeout;
  }

  public void setRequestTimeout(Duration requestTimeout) {
    this.requestTimeout = requestTimeout;
  }

  public Duration getProcessingTimeout() {
    return processingTimeout;
  }

  public void setProcessingTimeout(Duration processingTimeout) {
    this.processingTimeout = processingTimeout;
  }

  @AssertTrue(message = "Notification durations must be positive")
  public boolean isDurationConfigurationValid() {
    return isPositive(retryDelay)
        && isPositive(maxRetryDelay)
        && isPositive(connectTimeout)
        && isPositive(requestTimeout)
        && isPositive(processingTimeout);
  }

  @AssertTrue(message = "Webhook endpoint must use HTTPS unless insecure HTTP is enabled")
  public boolean isWebhookEndpointValid() {
    if (!webhook.enabled) {
      return true;
    }
    if (!StringUtils.hasText(webhook.endpoint)) {
      return false;
    }
    try {
      URI endpoint = URI.create(webhook.endpoint);
      String scheme = endpoint.getScheme();
      boolean schemeAllowed = "https".equalsIgnoreCase(scheme)
          || (webhook.allowInsecureHttp && "http".equalsIgnoreCase(scheme));
      return schemeAllowed
          && endpoint.isAbsolute()
          && StringUtils.hasText(endpoint.getHost())
          && endpoint.getUserInfo() == null
          && endpoint.getFragment() == null;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @AssertTrue(message = "Notification email sender address is required when email is enabled")
  public boolean isEmailSenderValid() {
    return !email.enabled || StringUtils.hasText(email.from);
  }

  private boolean isPositive(Duration duration) {
    return duration != null && !duration.isZero() && !duration.isNegative();
  }

  public static class EmailSettings {

    private boolean enabled;

    @Email(message = "Notification email sender address is invalid")
    private String from;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getFrom() {
      return from;
    }

    public void setFrom(String from) {
      this.from = from;
    }
  }

  public static class Webhook {

    private boolean enabled;

    private String endpoint;

    private String secret;

    private boolean allowInsecureHttp;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public String getSecret() {
      return secret;
    }

    public void setSecret(String secret) {
      this.secret = secret;
    }

    public boolean isAllowInsecureHttp() {
      return allowInsecureHttp;
    }

    public void setAllowInsecureHttp(boolean allowInsecureHttp) {
      this.allowInsecureHttp = allowInsecureHttp;
    }
  }
}
