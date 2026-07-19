package com.fixledger.infrastructure.notification;

import com.fixledger.modules.reminder.entity.ReminderTaskEntity;

/**
 * 通知基础设施入口。当前实现只写站内通知记录，真实邮件/Webhook 后续在此扩展。
 */
public interface NotificationService {

  void createInAppNotifications(ReminderTaskEntity reminder);
}
