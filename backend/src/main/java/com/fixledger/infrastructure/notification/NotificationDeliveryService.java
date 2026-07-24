package com.fixledger.infrastructure.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.modules.reminder.entity.NotificationRecordEntity;
import com.fixledger.modules.reminder.enums.NotificationStatus;
import com.fixledger.modules.reminder.mapper.NotificationRecordMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class NotificationDeliveryService {

  private static final int MAX_ERROR_LENGTH = 1000;

  private final NotificationRecordMapper notificationRecordMapper;
  private final NotificationRecordStateService stateService;
  private final Map<String, NotificationChannelSender> sendersByChannel;
  private final NotificationProperties properties;
  private final Clock clock;

  public NotificationDeliveryService(
      NotificationRecordMapper notificationRecordMapper,
      NotificationRecordStateService stateService,
      List<NotificationChannelSender> senders,
      NotificationProperties properties,
      Clock clock
  ) {
    this.notificationRecordMapper = notificationRecordMapper;
    this.stateService = stateService;
    this.sendersByChannel = new HashMap<>();
    for (NotificationChannelSender sender : senders) {
      this.sendersByChannel.put(sender.channel().getCode(), sender);
    }
    this.properties = properties;
    this.clock = clock;
  }

  public NotificationDispatchResult dispatchPending() {
    if (sendersByChannel.isEmpty()) {
      return new NotificationDispatchResult(0, 0, 0, 0);
    }
    LocalDateTime now = LocalDateTime.now(clock);
    stateService.recoverTimedOutClaims(
        now.minus(properties.getProcessingTimeout()),
        now,
        properties.getMaxAttempts()
    );
    List<NotificationRecordEntity> notifications = findDueNotifications(now);
    int sent = 0;
    int failed = 0;
    int skipped = 0;
    for (NotificationRecordEntity notification : notifications) {
      NotificationChannelSender sender = sendersByChannel.get(notification.getChannel());
      if (sender == null || !stateService.claim(notification, now)) {
        skipped++;
        continue;
      }
      try {
        sender.send(toMessage(notification));
        stateService.markSent(notification.getId(), now);
        sent++;
      } catch (Exception e) {
        int currentAttempt = attempts(notification) + 1;
        LocalDateTime nextRetryAt = currentAttempt < properties.getMaxAttempts()
            ? now.plus(retryDelay(currentAttempt))
            : null;
        stateService.markFailed(notification.getId(), errorMessage(e), nextRetryAt);
        failed++;
        log.warn(
            "Notification delivery failed: notificationId={}, channel={}",
            notification.getId(),
            notification.getChannel(),
            e
        );
      }
    }
    return new NotificationDispatchResult(notifications.size(), sent, failed, skipped);
  }

  private List<NotificationRecordEntity> findDueNotifications(LocalDateTime now) {
    return notificationRecordMapper.selectList(
        new LambdaQueryWrapper<NotificationRecordEntity>()
            .in(
                NotificationRecordEntity::getStatus,
                NotificationStatus.PENDING.getCode(),
                NotificationStatus.FAILED.getCode()
            )
            .in(NotificationRecordEntity::getChannel, sendersByChannel.keySet())
            .lt(NotificationRecordEntity::getAttemptCount, properties.getMaxAttempts())
            .and(wrapper -> wrapper
                .isNull(NotificationRecordEntity::getNextRetryAt)
                .or()
                .le(NotificationRecordEntity::getNextRetryAt, now))
            .orderByAsc(NotificationRecordEntity::getCreatedAt)
            .last("LIMIT " + properties.getBatchSize())
    );
  }

  private NotificationMessage toMessage(NotificationRecordEntity notification) {
    return new NotificationMessage(
        notification.getId(),
        notification.getFamilyId(),
        notification.getUserId(),
        notification.getReminderId(),
        notification.getRecipient(),
        notification.getTitle(),
        notification.getContent(),
        notification.getCreatedAt()
    );
  }

  private Duration retryDelay(int currentAttempt) {
    long multiplier = 1L << Math.min(currentAttempt - 1, 20);
    Duration delay = properties.getRetryDelay().multipliedBy(multiplier);
    return delay.compareTo(properties.getMaxRetryDelay()) > 0
        ? properties.getMaxRetryDelay()
        : delay;
  }

  private int attempts(NotificationRecordEntity notification) {
    return notification.getAttemptCount() == null ? 0 : notification.getAttemptCount();
  }

  private String errorMessage(Exception e) {
    String message = StringUtils.hasText(e.getMessage()) ? e.getMessage() : "delivery failed";
    String normalized = (e.getClass().getSimpleName() + ": " + message)
        .replaceAll("[\\r\\n\\t]", " ");
    return normalized.length() <= MAX_ERROR_LENGTH
        ? normalized
        : normalized.substring(0, MAX_ERROR_LENGTH);
  }
}
