package com.fixledger.infrastructure.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.modules.family.entity.FamilyMemberEntity;
import com.fixledger.modules.family.mapper.FamilyMemberMapper;
import com.fixledger.modules.reminder.entity.NotificationRecordEntity;
import com.fixledger.modules.reminder.entity.ReminderTaskEntity;
import com.fixledger.modules.reminder.enums.NotificationChannel;
import com.fixledger.modules.reminder.enums.NotificationStatus;
import com.fixledger.modules.reminder.mapper.NotificationRecordMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRecordMapper notificationRecordMapper;
  private final FamilyMemberMapper familyMemberMapper;

  public NotificationServiceImpl(
      NotificationRecordMapper notificationRecordMapper,
      FamilyMemberMapper familyMemberMapper
  ) {
    this.notificationRecordMapper = notificationRecordMapper;
    this.familyMemberMapper = familyMemberMapper;
  }

  @Override
  public void createInAppNotifications(ReminderTaskEntity reminder) {
    if (reminder.getUserId() != null) {
      createInAppNotification(reminder, reminder.getUserId());
      return;
    }
    List<FamilyMemberEntity> members = familyMemberMapper.selectList(
        new LambdaQueryWrapper<FamilyMemberEntity>()
            .eq(FamilyMemberEntity::getFamilyId, reminder.getFamilyId())
    );
    for (FamilyMemberEntity member : members) {
      createInAppNotification(reminder, member.getUserId());
    }
  }

  private void createInAppNotification(ReminderTaskEntity reminder, Long userId) {
    NotificationRecordEntity notification = new NotificationRecordEntity();
    notification.setFamilyId(reminder.getFamilyId());
    notification.setUserId(userId);
    notification.setReminderId(reminder.getId());
    notification.setChannel(NotificationChannel.IN_APP.getCode());
    notification.setTitle(reminder.getTitle());
    notification.setContent(reminder.getContent());
    notification.setStatus(NotificationStatus.SENT.getCode());
    notification.setSentAt(LocalDateTime.now());
    notificationRecordMapper.insert(notification);
  }
}
