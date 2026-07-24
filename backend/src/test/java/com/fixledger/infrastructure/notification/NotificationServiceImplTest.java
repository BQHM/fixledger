package com.fixledger.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fixledger.modules.family.entity.FamilyMemberEntity;
import com.fixledger.modules.family.mapper.FamilyMemberMapper;
import com.fixledger.modules.reminder.entity.NotificationRecordEntity;
import com.fixledger.modules.reminder.entity.ReminderTaskEntity;
import com.fixledger.modules.reminder.enums.NotificationChannel;
import com.fixledger.modules.reminder.enums.NotificationStatus;
import com.fixledger.modules.reminder.mapper.NotificationRecordMapper;
import com.fixledger.modules.user.entity.UserEntity;
import com.fixledger.modules.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationServiceImplTest {

  @Test
  @DisplayName("外部渠道关闭时只创建站内通知")
  void createOnlyInAppNotificationsWhenExternalChannelsDisabled() {
    NotificationRecordMapper recordMapper = mock(NotificationRecordMapper.class);
    FamilyMemberMapper memberMapper = mock(FamilyMemberMapper.class);
    UserMapper userMapper = mock(UserMapper.class);
    NotificationProperties properties = new NotificationProperties();
    NotificationService service = new NotificationServiceImpl(
        recordMapper,
        memberMapper,
        userMapper,
        properties
    );
    when(memberMapper.selectList(any())).thenReturn(List.of(member(11L), member(12L)));

    service.createNotifications(reminder());

    ArgumentCaptor<NotificationRecordEntity> captor =
        ArgumentCaptor.forClass(NotificationRecordEntity.class);
    verify(recordMapper, times(2)).insert(captor.capture());
    assertThat(captor.getAllValues())
        .allSatisfy(notification -> {
          assertThat(notification.getChannel()).isEqualTo(NotificationChannel.IN_APP.getCode());
          assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT.getCode());
          assertThat(notification.getSentAt()).isNotNull();
        });
    verify(userMapper, times(0)).selectByIds(any());
  }

  @Test
  @DisplayName("外部渠道启用时创建邮件和单条 Webhook 待投递记录")
  void queueEmailAndWebhookNotificationsWhenEnabled() {
    NotificationRecordMapper recordMapper = mock(NotificationRecordMapper.class);
    FamilyMemberMapper memberMapper = mock(FamilyMemberMapper.class);
    UserMapper userMapper = mock(UserMapper.class);
    NotificationProperties properties = new NotificationProperties();
    properties.getEmail().setEnabled(true);
    properties.getWebhook().setEnabled(true);
    NotificationService service = new NotificationServiceImpl(
        recordMapper,
        memberMapper,
        userMapper,
        properties
    );
    when(memberMapper.selectList(any())).thenReturn(List.of(member(11L), member(12L)));
    when(userMapper.selectByIds(any())).thenReturn(List.of(
        user(11L, "member@example.com"),
        user(12L, null)
    ));

    service.createNotifications(reminder());

    ArgumentCaptor<NotificationRecordEntity> captor =
        ArgumentCaptor.forClass(NotificationRecordEntity.class);
    verify(recordMapper, times(4)).insert(captor.capture());
    List<NotificationRecordEntity> records = captor.getAllValues();
    assertThat(records).filteredOn(
        item -> NotificationChannel.IN_APP.getCode().equals(item.getChannel())
    ).hasSize(2);
    assertThat(records).filteredOn(
        item -> NotificationChannel.EMAIL.getCode().equals(item.getChannel())
    ).singleElement().satisfies(notification -> {
      assertThat(notification.getUserId()).isEqualTo(11L);
      assertThat(notification.getRecipient()).isEqualTo("member@example.com");
      assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING.getCode());
    });
    assertThat(records).filteredOn(
        item -> NotificationChannel.WEBHOOK.getCode().equals(item.getChannel())
    ).singleElement().satisfies(notification -> {
      assertThat(notification.getUserId()).isNull();
      assertThat(notification.getRecipient()).isEqualTo("configured-webhook");
      assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING.getCode());
    });
  }

  private ReminderTaskEntity reminder() {
    ReminderTaskEntity reminder = new ReminderTaskEntity();
    reminder.setId(101L);
    reminder.setFamilyId(1L);
    reminder.setTitle("净水器滤芯即将更换");
    reminder.setContent("预计 7 天后到期");
    reminder.setRemindAt(LocalDateTime.of(2026, 7, 27, 8, 0));
    return reminder;
  }

  private FamilyMemberEntity member(Long userId) {
    FamilyMemberEntity member = new FamilyMemberEntity();
    member.setFamilyId(1L);
    member.setUserId(userId);
    return member;
  }

  private UserEntity user(Long id, String email) {
    UserEntity user = new UserEntity();
    user.setId(id);
    user.setEmail(email);
    return user;
  }
}
