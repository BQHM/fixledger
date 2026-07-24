package com.fixledger.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WebhookNotificationSenderTest {

  @Test
  @DisplayName("Webhook 使用固定事件契约并附加 HMAC 签名")
  void sendSignedWebhookPayload() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    @SuppressWarnings("unchecked")
    HttpResponse<Void> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(204);
    when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
    NotificationProperties properties = properties();
    WebhookNotificationSender sender = new WebhookNotificationSender(
        httpClient,
        new ObjectMapper().findAndRegisterModules(),
        properties
    );

    sender.send(message());

    ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(httpClient).send(captor.capture(), any(HttpResponse.BodyHandler.class));
    HttpRequest request = captor.getValue();
    assertThat(request.uri().toString()).isEqualTo("https://hooks.example.com/fixledger");
    assertThat(request.headers().firstValue("Content-Type")).contains("application/json");
    assertThat(request.headers().firstValue("X-FixLedger-Event"))
        .contains("reminder.notification");
    assertThat(request.headers().firstValue("X-FixLedger-Signature"))
        .hasValueSatisfying(value -> assertThat(value).matches("sha256=[0-9a-f]{64}"));
    assertThat(request.bodyPublisher()).hasValueSatisfying(
        publisher -> assertThat(publisher.contentLength()).isPositive()
    );
  }

  @Test
  @DisplayName("Webhook 非 2xx 响应转换为投递异常且不暴露端点")
  void rejectNonSuccessfulWebhookResponse() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    @SuppressWarnings("unchecked")
    HttpResponse<Void> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(503);
    when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
    WebhookNotificationSender sender = new WebhookNotificationSender(
        httpClient,
        new ObjectMapper().findAndRegisterModules(),
        properties()
    );

    assertThatThrownBy(() -> sender.send(message()))
        .isInstanceOf(NotificationDeliveryException.class)
        .hasMessageContaining("503")
        .hasMessageNotContaining("hooks.example.com");
  }

  private NotificationProperties properties() {
    NotificationProperties properties = new NotificationProperties();
    properties.getWebhook().setEndpoint("https://hooks.example.com/fixledger");
    properties.getWebhook().setSecret("test-signing-secret");
    return properties;
  }

  private NotificationMessage message() {
    return new NotificationMessage(
        1L,
        10L,
        null,
        30L,
        "configured-webhook",
        "滤芯即将更换",
        "预计 7 天后到期",
        LocalDateTime.of(2026, 7, 20, 8, 0)
    );
  }
}
