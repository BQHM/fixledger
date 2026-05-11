package com.fixledger.modules.dashboard.response;

import java.time.LocalDate;
import java.util.List;

public record ReminderCalendarDayResponse(
    LocalDate date,
    long count,
    List<ReminderCalendarItemResponse> reminders
) {
}
