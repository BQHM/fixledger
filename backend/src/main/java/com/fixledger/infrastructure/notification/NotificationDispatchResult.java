package com.fixledger.infrastructure.notification;

public record NotificationDispatchResult(
    int selected,
    int sent,
    int failed,
    int skipped
) {
}
