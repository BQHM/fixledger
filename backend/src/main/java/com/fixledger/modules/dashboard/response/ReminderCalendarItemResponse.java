package com.fixledger.modules.dashboard.response;

import java.time.LocalDateTime;

public record ReminderCalendarItemResponse(
    Long id,
    String reminderType,
    String title,
    String status,
    String bizType,
    Long bizId,
    LocalDateTime remindAt
) {
}
