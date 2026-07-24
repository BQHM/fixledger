package com.fixledger.infrastructure.scheduler;

import com.fixledger.infrastructure.notification.NotificationDeliveryService;
import com.fixledger.infrastructure.notification.NotificationDispatchResult;
import com.fixledger.infrastructure.notification.NotificationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 独立调度邮件和 Webhook 通知，避免外部调用进入提醒生成事务。
 */
@Slf4j
@Component
public class NotificationDeliveryScheduler {

  private final NotificationDeliveryService notificationDeliveryService;
  private final NotificationProperties properties;

  public NotificationDeliveryScheduler(
      NotificationDeliveryService notificationDeliveryService,
      NotificationProperties properties
  ) {
    this.notificationDeliveryService = notificationDeliveryService;
    this.properties = properties;
  }

  @Scheduled(cron = "${fixledger.notification.dispatch-cron:0 */1 * * * *}")
  public void dispatchNotifications() {
    if (!externalChannelEnabled()) {
      return;
    }
    log.info("Notification delivery started");
    NotificationDispatchResult result = notificationDeliveryService.dispatchPending();
    log.info(
        "Notification delivery finished: selected={}, sent={}, failed={}, skipped={}",
        result.selected(),
        result.sent(),
        result.failed(),
        result.skipped()
    );
  }

  private boolean externalChannelEnabled() {
    return properties.getEmail().isEnabled() || properties.getWebhook().isEnabled();
  }
}
