package com.fixledger.modules.reminder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.modules.family.entity.FamilyMemberEntity;
import com.fixledger.modules.family.mapper.FamilyMemberMapper;
import com.fixledger.modules.reminder.entity.NotificationRecordEntity;
import com.fixledger.modules.reminder.entity.ReminderTaskEntity;
import com.fixledger.modules.reminder.enums.NotificationChannel;
import com.fixledger.modules.reminder.enums.NotificationStatus;
import com.fixledger.modules.reminder.enums.ReminderStatus;
import com.fixledger.modules.reminder.enums.ReminderType;
import com.fixledger.modules.reminder.mapper.NotificationRecordMapper;
import com.fixledger.modules.reminder.mapper.ReminderTaskMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 文件功能说明：提醒创建服务，专门负责提醒任务和站内通知的同事务写入。
 * </p>
 *
 * @Author FixLedger
 */
@Service
public class ReminderCreationService {

  private final ReminderTaskMapper reminderTaskMapper;
  private final NotificationRecordMapper notificationRecordMapper;
  private final FamilyMemberMapper familyMemberMapper;

  public ReminderCreationService(
      ReminderTaskMapper reminderTaskMapper,
      NotificationRecordMapper notificationRecordMapper,
      FamilyMemberMapper familyMemberMapper
  ) {
    this.reminderTaskMapper = reminderTaskMapper;
    this.notificationRecordMapper = notificationRecordMapper;
    this.familyMemberMapper = familyMemberMapper;
  }

  /**
   * 创建提醒任务和对应站内通知，并使用数据库再次兜底去重。
   *
   * @param familyId 家庭空间 ID
   * @param reminderType 提醒类型
   * @param bizType 业务类型
   * @param bizId 业务 ID
   * @param title 提醒标题
   * @param content 提醒内容
   * @param remindAt 提醒时间
   * @return 是否新建提醒
   */
  @Transactional
  public boolean createReminderIfAbsent(
      Long familyId,
      ReminderType reminderType,
      String bizType,
      Long bizId,
      String title,
      String content,
      LocalDateTime remindAt
  ) {
    LocalDate remindDate = remindAt.toLocalDate();
    if (existsReminder(familyId, reminderType, bizType, bizId, remindDate)) {
      return false;
    }
    ReminderTaskEntity reminder = new ReminderTaskEntity();
    reminder.setFamilyId(familyId);
    reminder.setReminderType(reminderType.getCode());
    reminder.setBizType(bizType);
    reminder.setBizId(bizId);
    reminder.setTitle(title);
    reminder.setContent(content);
    reminder.setRemindAt(remindAt);
    reminder.setStatus(ReminderStatus.PENDING.getCode());
    reminderTaskMapper.insert(reminder);
    createInAppNotifications(reminder);
    return true;
  }

  private boolean existsReminder(
      Long familyId,
      ReminderType reminderType,
      String bizType,
      Long bizId,
      LocalDate remindDate
  ) {
    LocalDateTime start = remindDate.atStartOfDay();
    LocalDateTime end = remindDate.plusDays(1).atStartOfDay();
    Long count = reminderTaskMapper.selectCount(new LambdaQueryWrapper<ReminderTaskEntity>()
        .eq(ReminderTaskEntity::getFamilyId, familyId)
        .eq(ReminderTaskEntity::getReminderType, reminderType.getCode())
        .eq(ReminderTaskEntity::getBizType, bizType)
        .eq(ReminderTaskEntity::getBizId, bizId)
        .ge(ReminderTaskEntity::getRemindAt, start)
        .lt(ReminderTaskEntity::getRemindAt, end));
    return count > 0;
  }

  private void createInAppNotifications(ReminderTaskEntity reminder) {
    // 当前阶段只落站内通知记录，邮件和 Webhook 可在通知基础设施中扩展。
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
