package com.fixledger.infrastructure.notification;

import java.time.LocalDateTime;

public record NotificationMessage(
    Long notificationId,
    Long familyId,
    Long userId,
    Long reminderId,
    String recipient,
    String title,
    String content,
    LocalDateTime createdAt
) {
}
