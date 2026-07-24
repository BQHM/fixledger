package com.fixledger.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fixledger.modules.reminder.entity.NotificationRecordEntity;
import com.fixledger.modules.reminder.enums.NotificationChannel;
import com.fixledger.modules.reminder.enums.NotificationStatus;
import com.fixledger.modules.reminder.mapper.NotificationRecordMapper;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class NotificationDeliveryServiceTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 8, 0);
  private static final Clock CLOCK = Clock.fixed(
      Instant.parse("2026-07-20T00:00:00Z"),
      ZoneId.of("Asia/Shanghai")
  );

  @Test
  @DisplayName("外部发送不包裹事务且状态更新使用独立事务")
  void keepExternalDeliveryOutsideTransactions() throws NoSuchMethodException {
    Method dispatch = NotificationDeliveryService.class.getMethod("dispatchPending");
    Method claim = NotificationRecordStateService.class.getMethod(
        "claim",
        NotificationRecordEntity.class,
        LocalDateTime.class
    );
    Method markSent = NotificationRecordStateService.class.getMethod(
        "markSent",
        Long.class,
        LocalDateTime.class
    );
    Method recover = NotificationRecordStateService.class.getMethod(
        "recoverTimedOutClaims",
        LocalDateTime.class,
        LocalDateTime.class,
        int.class
    );

    assertThat(dispatch.isAnnotationPresent(Transactional.class)).isFalse();
    assertThat(claim.isAnnotationPresent(Transactional.class)).isTrue();
    assertThat(markSent.isAnnotationPresent(Transactional.class)).isTrue();
    assertThat(recover.isAnnotationPresent(Transactional.class)).isTrue();
  }

  @Test
  @DisplayName("单条投递失败不阻断后续通知并记录重试时间")
  void continueDispatchingAfterOneDeliveryFails() {
    NotificationRecordMapper mapper = mock(NotificationRecordMapper.class);
    NotificationRecordStateService stateService = mock(NotificationRecordStateService.class);
    NotificationChannelSender emailSender = sender(NotificationChannel.EMAIL);
    NotificationChannelSender webhookSender = sender(NotificationChannel.WEBHOOK);
    NotificationProperties properties = new NotificationProperties();
    NotificationRecordEntity failed = notification(1L, NotificationChannel.EMAIL, 0);
    NotificationRecordEntity sent = notification(2L, NotificationChannel.WEBHOOK, 0);
    when(mapper.selectList(any())).thenReturn(List.of(failed, sent));
    when(stateService.claim(failed, NOW)).thenReturn(true);
    when(stateService.claim(sent, NOW)).thenReturn(true);
    doThrow(new NotificationDeliveryException("SMTP unavailable"))
        .when(emailSender).send(any());
    NotificationDeliveryService service = new NotificationDeliveryService(
        mapper,
        stateService,
        List.of(emailSender, webhookSender),
        properties,
        CLOCK
    );

    NotificationDispatchResult result = service.dispatchPending();

    assertThat(result).isEqualTo(new NotificationDispatchResult(2, 1, 1, 0));
    verify(stateService).markFailed(
        eq(1L),
        contains("SMTP unavailable"),
        eq(NOW.plusMinutes(5))
    );
    verify(stateService).markSent(2L, NOW);
    verify(webhookSender).send(any());
  }

  @Test
  @DisplayName("达到最大尝试次数后记录失败但不再安排重试")
  void stopRetryingAfterMaximumAttempts() {
    NotificationRecordMapper mapper = mock(NotificationRecordMapper.class);
    NotificationRecordStateService stateService = mock(NotificationRecordStateService.class);
    NotificationChannelSender emailSender = sender(NotificationChannel.EMAIL);
    NotificationProperties properties = new NotificationProperties();
    NotificationRecordEntity record = notification(5L, NotificationChannel.EMAIL, 2);
    when(mapper.selectList(any())).thenReturn(List.of(record));
    when(stateService.claim(record, NOW)).thenReturn(true);
    doThrow(new NotificationDeliveryException("SMTP unavailable"))
        .when(emailSender).send(any());
    NotificationDeliveryService service = new NotificationDeliveryService(
        mapper,
        stateService,
        List.of(emailSender),
        properties,
        CLOCK
    );

    NotificationDispatchResult result = service.dispatchPending();

    assertThat(result).isEqualTo(new NotificationDispatchResult(1, 0, 1, 0));
    verify(stateService).markFailed(
        eq(5L),
        contains("SMTP unavailable"),
        isNull()
    );
  }

  @Test
  @DisplayName("没有启用对应发送器时保留待投递记录")
  void skipRecordWhenChannelSenderIsUnavailable() {
    NotificationRecordMapper mapper = mock(NotificationRecordMapper.class);
    NotificationRecordStateService stateService = mock(NotificationRecordStateService.class);
    NotificationDeliveryService service = new NotificationDeliveryService(
        mapper,
        stateService,
        List.of(),
        new NotificationProperties(),
        CLOCK
    );

    NotificationDispatchResult result = service.dispatchPending();

    assertThat(result).isEqualTo(new NotificationDispatchResult(0, 0, 0, 0));
    verifyNoInteractions(mapper, stateService);
  }

  @Test
  @DisplayName("每次投递前恢复超时未完成的领取记录")
  void recoverTimedOutClaimsBeforeSelectingDueNotifications() {
    NotificationRecordMapper mapper = mock(NotificationRecordMapper.class);
    NotificationRecordStateService stateService = mock(NotificationRecordStateService.class);
    NotificationChannelSender sender = sender(NotificationChannel.WEBHOOK);
    when(mapper.selectList(any())).thenReturn(List.of());
    NotificationDeliveryService service = new NotificationDeliveryService(
        mapper,
        stateService,
        List.of(sender),
        new NotificationProperties(),
        CLOCK
    );

    NotificationDispatchResult result = service.dispatchPending();

    assertThat(result).isEqualTo(new NotificationDispatchResult(0, 0, 0, 0));
    verify(stateService).recoverTimedOutClaims(
        NOW.minusMinutes(15),
        NOW,
        3
    );
  }

  @Test
  @DisplayName("领取竞争失败时跳过发送避免重复投递")
  void skipSendingWhenClaimLosesRace() {
    NotificationRecordMapper mapper = mock(NotificationRecordMapper.class);
    NotificationRecordStateService stateService = mock(NotificationRecordStateService.class);
    NotificationChannelSender sender = sender(NotificationChannel.EMAIL);
    NotificationRecordEntity record = notification(4L, NotificationChannel.EMAIL, 0);
    when(mapper.selectList(any())).thenReturn(List.of(record));
    when(stateService.claim(record, NOW)).thenReturn(false);
    NotificationDeliveryService service = new NotificationDeliveryService(
        mapper,
        stateService,
        List.of(sender),
        new NotificationProperties(),
        CLOCK
    );

    NotificationDispatchResult result = service.dispatchPending();

    assertThat(result).isEqualTo(new NotificationDispatchResult(1, 0, 0, 1));
    verify(sender, never()).send(any());
  }

  private NotificationChannelSender sender(NotificationChannel channel) {
    NotificationChannelSender sender = mock(NotificationChannelSender.class);
    when(sender.channel()).thenReturn(channel);
    return sender;
  }

  private NotificationRecordEntity notification(
      Long id,
      NotificationChannel channel,
      int attempts
  ) {
    NotificationRecordEntity notification = new NotificationRecordEntity();
    notification.setId(id);
    notification.setFamilyId(10L);
    notification.setReminderId(20L);
    notification.setChannel(channel.getCode());
    notification.setTitle("测试提醒");
    notification.setContent("测试内容");
    notification.setRecipient("recipient");
    notification.setStatus(NotificationStatus.PENDING.getCode());
    notification.setAttemptCount(attempts);
    notification.setCreatedAt(NOW.minusMinutes(1));
    return notification;
  }
}
