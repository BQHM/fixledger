package com.fixledger.modules.reminder.response;

public record ReminderScanResponse(
    int warrantyCreated,
    int consumableCreated,
    int notificationCreated,
    int skippedDuplicate,
    int failedCount
) {

  public ReminderScanResponse plus(ReminderScanResponse other) {
    return new ReminderScanResponse(
        warrantyCreated + other.warrantyCreated,
        consumableCreated + other.consumableCreated,
        notificationCreated + other.notificationCreated,
        skippedDuplicate + other.skippedDuplicate,
        failedCount + other.failedCount
    );
  }
}
