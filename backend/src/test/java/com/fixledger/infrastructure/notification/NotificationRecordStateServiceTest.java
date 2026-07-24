package com.fixledger.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.fixledger.infrastructure.redis.TestRedisConfig;
import com.fixledger.modules.reminder.entity.NotificationRecordEntity;
import com.fixledger.modules.reminder.enums.NotificationChannel;
import com.fixledger.modules.reminder.enums.NotificationStatus;
import com.fixledger.modules.reminder.mapper.NotificationRecordMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestRedisConfig.class)
@Transactional
class NotificationRecordStateServiceTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 8, 0);

  @Autowired
  private NotificationRecordStateService stateService;

  @Autowired
  private NotificationRecordMapper notificationRecordMapper;

  @Test
  @DisplayName("恢复超时领取并终止已达到尝试上限的记录")
  void recoverTimedOutProcessingRecords() {
    NotificationRecordEntity retryable = insertProcessingRecord(1, NOW.minusMinutes(16));
    NotificationRecordEntity exhausted = insertProcessingRecord(3, NOW.minusMinutes(16));
    NotificationRecordEntity active = insertProcessingRecord(1, NOW.minusMinutes(10));

    stateService.recoverTimedOutClaims(NOW.minusMinutes(15), NOW, 3);

    NotificationRecordEntity recovered = notificationRecordMapper.selectById(retryable.getId());
    assertThat(recovered.getStatus()).isEqualTo(NotificationStatus.FAILED.getCode());
    assertThat(recovered.getNextRetryAt()).isEqualTo(NOW);
    assertThat(recovered.getErrorMessage()).contains("timed out");

    NotificationRecordEntity stopped = notificationRecordMapper.selectById(exhausted.getId());
    assertThat(stopped.getStatus()).isEqualTo(NotificationStatus.FAILED.getCode());
    assertThat(stopped.getNextRetryAt()).isNull();
    assertThat(stopped.getErrorMessage()).contains("maximum attempts");

    NotificationRecordEntity inProgress = notificationRecordMapper.selectById(active.getId());
    assertThat(inProgress.getStatus()).isEqualTo(NotificationStatus.PROCESSING.getCode());
  }

  private NotificationRecordEntity insertProcessingRecord(
      int attemptCount,
      LocalDateTime lastAttemptAt
  ) {
    NotificationRecordEntity notification = new NotificationRecordEntity();
    notification.setFamilyId(1L);
    notification.setChannel(NotificationChannel.EMAIL.getCode());
    notification.setTitle("测试通知");
    notification.setStatus(NotificationStatus.PROCESSING.getCode());
    notification.setAttemptCount(attemptCount);
    notification.setLastAttemptAt(lastAttemptAt);
    notificationRecordMapper.insert(notification);
    return notification;
  }
}
