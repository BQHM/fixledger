package com.fixledger.modules.reminder.response;

/**
 * <p>
 * 文件功能说明：提醒通知响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record ReminderScanResponse(
    int warrantyCreated,
    int consumableCreated,
    int notificationCreated,
    int skippedDuplicate,
    int failedCount
) {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：合并扫描结果。
   * </p>
   * @param other 待合并结果
   * @return 业务响应数据
   */
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
