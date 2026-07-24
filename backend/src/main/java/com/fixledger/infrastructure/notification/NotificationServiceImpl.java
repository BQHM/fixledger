package com.fixledger.infrastructure.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRecordMapper notificationRecordMapper;
  private final FamilyMemberMapper familyMemberMapper;
  private final UserMapper userMapper;
  private final NotificationProperties properties;

  public NotificationServiceImpl(
      NotificationRecordMapper notificationRecordMapper,
      FamilyMemberMapper familyMemberMapper,
      UserMapper userMapper,
      NotificationProperties properties
  ) {
    this.notificationRecordMapper = notificationRecordMapper;
    this.familyMemberMapper = familyMemberMapper;
    this.userMapper = userMapper;
    this.properties = properties;
  }

  @Override
  public void createNotifications(ReminderTaskEntity reminder) {
    List<Long> userIds = resolveRecipientUserIds(reminder);
    userIds.forEach(userId -> createNotification(
        reminder,
        userId,
        NotificationChannel.IN_APP,
        null,
        NotificationStatus.SENT
    ));
    queueEmailNotifications(reminder, userIds);
    if (properties.getWebhook().isEnabled()) {
      createNotification(
          reminder,
          null,
          NotificationChannel.WEBHOOK,
          "configured-webhook",
          NotificationStatus.PENDING
      );
    }
  }

  private List<Long> resolveRecipientUserIds(ReminderTaskEntity reminder) {
    if (reminder.getUserId() != null) return List.of(reminder.getUserId());
    return familyMemberMapper.selectList(
        new LambdaQueryWrapper<FamilyMemberEntity>()
            .eq(FamilyMemberEntity::getFamilyId, reminder.getFamilyId())
    ).stream().map(FamilyMemberEntity::getUserId).distinct().toList();
  }

  private void queueEmailNotifications(ReminderTaskEntity reminder, List<Long> userIds) {
    if (!properties.getEmail().isEnabled() || userIds.isEmpty()) return;
    Map<Long, UserEntity> usersById = new HashMap<>();
    for (UserEntity user : userMapper.selectByIds(userIds)) {
      usersById.put(user.getId(), user);
    }
    for (Long userId : userIds) {
      UserEntity user = usersById.get(userId);
      if (user != null && StringUtils.hasText(user.getEmail())) {
        createNotification(
            reminder,
            userId,
            NotificationChannel.EMAIL,
            user.getEmail(),
            NotificationStatus.PENDING
        );
      }
    }
  }

  private void createNotification(
      ReminderTaskEntity reminder,
      Long userId,
      NotificationChannel channel,
      String recipient,
      NotificationStatus status
  ) {
    NotificationRecordEntity notification = new NotificationRecordEntity();
    notification.setFamilyId(reminder.getFamilyId());
    notification.setUserId(userId);
    notification.setReminderId(reminder.getId());
    notification.setChannel(channel.getCode());
    notification.setTitle(reminder.getTitle());
    notification.setContent(reminder.getContent());
    notification.setRecipient(recipient);
    notification.setStatus(status.getCode());
    notification.setAttemptCount(0);
    if (status == NotificationStatus.SENT) {
      notification.setSentAt(LocalDateTime.now());
    }
    notificationRecordMapper.insert(notification);
  }
}
