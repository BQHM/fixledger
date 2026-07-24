package com.fixledger.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailNotificationSenderTest {

  @Test
  @DisplayName("邮件发送器使用配置发件人并清理主题换行")
  void sendEmailWithConfiguredSenderAndSafeSubject() {
    JavaMailSender mailSender = mock(JavaMailSender.class);
    NotificationProperties properties = new NotificationProperties();
    properties.getEmail().setFrom("no-reply@example.com");
    EmailNotificationSender sender = new EmailNotificationSender(mailSender, properties);

    sender.send(message("member@example.com", "滤芯到期\r\nBcc: attacker@example.com"));

    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    SimpleMailMessage mail = captor.getValue();
    assertThat(mail.getFrom()).isEqualTo("no-reply@example.com");
    assertThat(mail.getTo()).containsExactly("member@example.com");
    assertThat(mail.getSubject()).isEqualTo("[FixLedger] 滤芯到期 Bcc: attacker@example.com");
    assertThat(mail.getText()).contains("预计 7 天后到期");
  }

  @Test
  @DisplayName("邮件发送器拒绝无效收件地址")
  void rejectInvalidRecipientAddress() {
    NotificationProperties properties = new NotificationProperties();
    properties.getEmail().setFrom("no-reply@example.com");
    EmailNotificationSender sender = new EmailNotificationSender(
        mock(JavaMailSender.class),
        properties
    );

    assertThatThrownBy(() -> sender.send(message("bad-address", "滤芯到期")))
        .isInstanceOf(NotificationDeliveryException.class)
        .hasMessageContaining("recipient");
  }

  private NotificationMessage message(String recipient, String title) {
    return new NotificationMessage(
        1L,
        10L,
        20L,
        30L,
        recipient,
        title,
        "预计 7 天后到期",
        LocalDateTime.of(2026, 7, 20, 8, 0)
    );
  }
}
