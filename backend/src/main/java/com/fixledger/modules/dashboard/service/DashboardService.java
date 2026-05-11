package com.fixledger.modules.dashboard.service;

import com.fixledger.modules.dashboard.response.DashboardSummaryResponse;
import com.fixledger.modules.dashboard.response.DeviceCategoryDistributionResponse;
import com.fixledger.modules.dashboard.response.MaintenanceCostTrendResponse;
import com.fixledger.modules.dashboard.response.ReminderCalendarDayResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * 首页看板服务，汇总设备、保修、耗材、维修和提醒数据。
 */
public interface DashboardService {

  /**
   * 查询首页核心指标摘要。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 看板摘要
   */
  DashboardSummaryResponse summary(Long userId, Long familyId);

  /**
   * 按设备分类统计设备数量。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 分类分布列表
   */
  List<DeviceCategoryDistributionResponse> deviceCategoryDistribution(
      Long userId,
      Long familyId
  );

  /**
   * 统计最近若干月维修费用趋势。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param months 统计月份数，范围 1 到 24
   * @return 月度费用趋势
   */
  List<MaintenanceCostTrendResponse> maintenanceCostTrend(
      Long userId,
      Long familyId,
      Integer months
  );

  /**
   * 查询提醒日历数据。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param startDate 开始日期，默认今天
   * @param endDate 结束日期，默认开始后 30 天
   * @return 按日期聚合的提醒列表
   */
  List<ReminderCalendarDayResponse> reminderCalendar(
      Long userId,
      Long familyId,
      LocalDate startDate,
      LocalDate endDate
  );
}