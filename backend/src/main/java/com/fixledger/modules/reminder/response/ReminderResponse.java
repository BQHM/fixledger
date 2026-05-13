package com.fixledger.modules.reminder.response;

import java.time.LocalDateTime;

/**
 * <p>
 * 文件功能说明：提醒通知响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
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
