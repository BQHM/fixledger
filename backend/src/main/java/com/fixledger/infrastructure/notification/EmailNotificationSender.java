package com.fixledger.infrastructure.notification;

import com.fixledger.modules.reminder.enums.NotificationChannel;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "fixledger.notification.email",
    name = "enabled",
    havingValue = "true"
)
public class EmailNotificationSender implements NotificationChannelSender {

  private static final int MAX_SUBJECT_LENGTH = 120;

  private final JavaMailSender mailSender;
  private final String from;

  public EmailNotificationSender(
      JavaMailSender mailSender,
      NotificationProperties properties
  ) {
    this.mailSender = mailSender;
    this.from = properties.getEmail().getFrom();
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.EMAIL;
  }

  @Override
  public void send(NotificationMessage message) {
    validateAddress(from, "Email sender address is invalid");
    validateAddress(message.recipient(), "Email recipient is invalid");
    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setFrom(from);
    mail.setTo(message.recipient());
    mail.setSubject(subject(message.title()));
    mail.setText(body(message));
    try {
      mailSender.send(mail);
    } catch (MailException e) {
      throw new NotificationDeliveryException("Email provider rejected notification", e);
    }
  }

  private String subject(String title) {
    String safeTitle = title == null ? "家庭设备提醒" : title.replaceAll("[\\r\\n]+", " ");
    String subject = "[FixLedger] " + safeTitle;
    return subject.length() <= MAX_SUBJECT_LENGTH
        ? subject
        : subject.substring(0, MAX_SUBJECT_LENGTH);
  }

  private String body(NotificationMessage message) {
    String content = message.content() == null ? "请登录 FixLedger 查看提醒详情。" : message.content();
    return content + "\n\n此邮件由 FixLedger 家庭设备提醒服务发送。";
  }

  private void validateAddress(String address, String errorMessage) {
    try {
      InternetAddress internetAddress = new InternetAddress(address, true);
      internetAddress.validate();
    } catch (AddressException | NullPointerException e) {
      throw new NotificationDeliveryException(errorMessage);
    }
  }
}
