package com.fixledger.modules.reminder.service;

import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.reminder.query.ReminderPageQuery;
import com.fixledger.modules.reminder.response.ReminderResponse;
import com.fixledger.modules.reminder.response.ReminderScanResponse;
import com.fixledger.modules.reminder.response.UnreadCountResponse;
import java.time.LocalDate;

/**
 * 提醒服务，统一生成、查询和处理保修与耗材提醒。
 */
public interface ReminderService {

  /**
   * 分页查询家庭提醒列表。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 分页与筛选条件
   * @return 提醒分页结果
   */
  PageResponse<ReminderResponse> pageReminders(
      Long userId,
      Long familyId,
      ReminderPageQuery query
  );

  /**
   * 统计家庭中待处理提醒数量。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 未读数量
   */
  UnreadCountResponse unreadCount(Long userId, Long familyId);

  /**
   * 将提醒标记为已读。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param reminderId 提醒 ID
   * @return 更新后的提醒
   */
  ReminderResponse markRead(Long userId, Long familyId, Long reminderId);

  /**
   * 忽略提醒，保留记录但不再作为待处理项展示。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param reminderId 提醒 ID
   * @return 更新后的提醒
   */
  ReminderResponse ignore(Long userId, Long familyId, Long reminderId);

  /**
   * 手动扫描当前家庭提醒，需校验用户属于该家庭。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 本次生成和跳过数量
   */
  ReminderScanResponse scanFamily(Long userId, Long familyId);

  /**
   * 定时任务入口，按家庭和业务日期生成提醒。
   *
   * @param familyId 家庭空间 ID
   * @param today 业务日期
   * @return 本次生成和跳过数量
   */
  ReminderScanResponse scanFamily(Long familyId, LocalDate today);
}
