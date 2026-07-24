package com.fixledger.infrastructure.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.modules.reminder.enums.NotificationChannel;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(
    prefix = "fixledger.notification.webhook",
    name = "enabled",
    havingValue = "true"
)
public class WebhookNotificationSender implements NotificationChannelSender {

  private static final String EVENT_NAME = "reminder.notification";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final URI endpoint;
  private final String secret;
  private final NotificationProperties properties;

  public WebhookNotificationSender(
      HttpClient httpClient,
      ObjectMapper objectMapper,
      NotificationProperties properties
  ) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.endpoint = URI.create(properties.getWebhook().getEndpoint());
    this.secret = properties.getWebhook().getSecret();
    this.properties = properties;
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.WEBHOOK;
  }

  @Override
  public void send(NotificationMessage message) {
    byte[] payload = payload(message);
    HttpRequest.Builder request = HttpRequest.newBuilder(endpoint)
        .timeout(properties.getRequestTimeout())
        .header("Content-Type", "application/json")
        .header("User-Agent", "FixLedger/0.1")
        .header("X-FixLedger-Event", EVENT_NAME)
        .POST(HttpRequest.BodyPublishers.ofByteArray(payload));
    if (StringUtils.hasText(secret)) {
      request.header("X-FixLedger-Signature", signature(payload));
    }
    try {
      HttpResponse<Void> response = httpClient.send(
          request.build(),
          HttpResponse.BodyHandlers.discarding()
      );
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new NotificationDeliveryException(
            "Webhook returned HTTP " + response.statusCode()
        );
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new NotificationDeliveryException("Webhook delivery was interrupted", e);
    } catch (IOException e) {
      throw new NotificationDeliveryException("Webhook connection failed", e);
    }
  }

  private byte[] payload(NotificationMessage message) {
    try {
      return objectMapper.writeValueAsBytes(new WebhookPayload(
          EVENT_NAME,
          message.notificationId(),
          message.familyId(),
          message.reminderId(),
          message.title(),
          message.content(),
          message.createdAt()
      ));
    } catch (JsonProcessingException e) {
      throw new NotificationDeliveryException("Webhook payload serialization failed", e);
    }
  }

  private String signature(byte[] payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload));
    } catch (GeneralSecurityException e) {
      throw new NotificationDeliveryException("Webhook signature generation failed", e);
    }
  }

  private record WebhookPayload(
      String event,
      Long notificationId,
      Long familyId,
      Long reminderId,
      String title,
      String content,
      LocalDateTime createdAt
  ) {
  }
}
