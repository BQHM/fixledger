package com.fixledger.infrastructure.notification;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fixledger.modules.reminder.entity.NotificationRecordEntity;
import com.fixledger.modules.reminder.enums.NotificationStatus;
import com.fixledger.modules.reminder.mapper.NotificationRecordMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationRecordStateService {

  private static final String TIMED_OUT_ERROR = "Notification delivery attempt timed out";
  private static final String MAX_ATTEMPTS_ERROR =
      "Notification delivery interrupted after maximum attempts";

  private final NotificationRecordMapper notificationRecordMapper;

  public NotificationRecordStateService(NotificationRecordMapper notificationRecordMapper) {
    this.notificationRecordMapper = notificationRecordMapper;
  }

  @Transactional
  public boolean claim(NotificationRecordEntity notification, LocalDateTime attemptedAt) {
    int currentAttempts = notification.getAttemptCount() == null
        ? 0
        : notification.getAttemptCount();
    LambdaUpdateWrapper<NotificationRecordEntity> update =
        new LambdaUpdateWrapper<NotificationRecordEntity>()
            .eq(NotificationRecordEntity::getId, notification.getId())
            .eq(NotificationRecordEntity::getStatus, notification.getStatus())
            .eq(NotificationRecordEntity::getAttemptCount, currentAttempts)
            .set(NotificationRecordEntity::getStatus, NotificationStatus.PROCESSING.getCode())
            .set(NotificationRecordEntity::getAttemptCount, currentAttempts + 1)
            .set(NotificationRecordEntity::getLastAttemptAt, attemptedAt)
            .set(NotificationRecordEntity::getNextRetryAt, null)
            .set(NotificationRecordEntity::getErrorMessage, null);
    return notificationRecordMapper.update(null, update) == 1;
  }

  @Transactional
  public void markSent(Long notificationId, LocalDateTime sentAt) {
    notificationRecordMapper.update(
        null,
        new LambdaUpdateWrapper<NotificationRecordEntity>()
            .eq(NotificationRecordEntity::getId, notificationId)
            .eq(NotificationRecordEntity::getStatus, NotificationStatus.PROCESSING.getCode())
            .set(NotificationRecordEntity::getStatus, NotificationStatus.SENT.getCode())
            .set(NotificationRecordEntity::getSentAt, sentAt)
            .set(NotificationRecordEntity::getErrorMessage, null)
            .set(NotificationRecordEntity::getNextRetryAt, null)
    );
  }

  @Transactional
  public void markFailed(
      Long notificationId,
      String errorMessage,
      LocalDateTime nextRetryAt
  ) {
    notificationRecordMapper.update(
        null,
        new LambdaUpdateWrapper<NotificationRecordEntity>()
            .eq(NotificationRecordEntity::getId, notificationId)
            .eq(NotificationRecordEntity::getStatus, NotificationStatus.PROCESSING.getCode())
            .set(NotificationRecordEntity::getStatus, NotificationStatus.FAILED.getCode())
            .set(NotificationRecordEntity::getErrorMessage, errorMessage)
            .set(NotificationRecordEntity::getNextRetryAt, nextRetryAt)
    );
  }

  @Transactional
  public void recoverTimedOutClaims(
      LocalDateTime timedOutBefore,
      LocalDateTime retryAt,
      int maxAttempts
  ) {
    notificationRecordMapper.update(
        null,
        timedOutClaimUpdate(timedOutBefore)
            .ge(NotificationRecordEntity::getAttemptCount, maxAttempts)
            .set(NotificationRecordEntity::getStatus, NotificationStatus.FAILED.getCode())
            .set(NotificationRecordEntity::getErrorMessage, MAX_ATTEMPTS_ERROR)
            .set(NotificationRecordEntity::getNextRetryAt, null)
    );
    notificationRecordMapper.update(
        null,
        timedOutClaimUpdate(timedOutBefore)
            .lt(NotificationRecordEntity::getAttemptCount, maxAttempts)
            .set(NotificationRecordEntity::getStatus, NotificationStatus.FAILED.getCode())
            .set(NotificationRecordEntity::getErrorMessage, TIMED_OUT_ERROR)
            .set(NotificationRecordEntity::getNextRetryAt, retryAt)
    );
  }

  private LambdaUpdateWrapper<NotificationRecordEntity> timedOutClaimUpdate(
      LocalDateTime timedOutBefore
  ) {
    return new LambdaUpdateWrapper<NotificationRecordEntity>()
        .eq(NotificationRecordEntity::getStatus, NotificationStatus.PROCESSING.getCode())
        .and(wrapper -> wrapper
            .isNull(NotificationRecordEntity::getLastAttemptAt)
            .or()
            .le(NotificationRecordEntity::getLastAttemptAt, timedOutBefore));
  }
}
