package com.fixledger.modules.reminder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.infrastructure.notification.NotificationService;
import com.fixledger.modules.reminder.entity.ReminderTaskEntity;
import com.fixledger.modules.reminder.enums.ReminderStatus;
import com.fixledger.modules.reminder.enums.ReminderType;
import com.fixledger.modules.reminder.mapper.ReminderTaskMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private final NotificationService notificationService;

  public ReminderCreationService(
      ReminderTaskMapper reminderTaskMapper,
      NotificationService notificationService
  ) {
    this.reminderTaskMapper = reminderTaskMapper;
    this.notificationService = notificationService;
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
    notificationService.createInAppNotifications(reminder);
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

}
