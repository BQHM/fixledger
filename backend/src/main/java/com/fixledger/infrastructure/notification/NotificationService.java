package com.fixledger.infrastructure.notification;

import com.fixledger.modules.reminder.entity.ReminderTaskEntity;

/**
 * 通知记录创建入口。提醒事务只写数据库记录，不调用外部邮件或 Webhook。
 */
public interface NotificationService {

  void createNotifications(ReminderTaskEntity reminder);
}
