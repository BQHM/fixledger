package com.fixledger.modules.reminder.response;

import java.time.LocalDateTime;

public record ReminderResponse(
    Long id,
    String reminderType,
    String bizType,
    Long bizId,
    String title,
    String content,
    LocalDateTime remindAt,
    String status,
    LocalDateTime readAt
) {
}
