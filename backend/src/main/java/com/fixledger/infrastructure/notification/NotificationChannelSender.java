package com.fixledger.infrastructure.notification;

import com.fixledger.modules.reminder.enums.NotificationChannel;

public interface NotificationChannelSender {

  NotificationChannel channel();

  void send(NotificationMessage message);
}
