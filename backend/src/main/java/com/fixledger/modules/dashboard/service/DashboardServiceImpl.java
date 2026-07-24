package com.fixledger.modules.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.dashboard.dto.DashboardSummaryStatisticsDTO;
import com.fixledger.modules.dashboard.mapper.DashboardStatisticsMapper;
import com.fixledger.modules.dashboard.response.DashboardSummaryResponse;
import com.fixledger.modules.dashboard.response.DeviceCategoryDistributionResponse;
import com.fixledger.modules.dashboard.response.MaintenanceCostTrendResponse;
import com.fixledger.modules.dashboard.response.ReminderCalendarDayResponse;
import com.fixledger.modules.dashboard.response.ReminderCalendarItemResponse;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.entity.MaintenanceRecordEntity;
import com.fixledger.modules.maintenance.enums.MaintenanceStatus;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import com.fixledger.modules.reminder.entity.ReminderTaskEntity;
import com.fixledger.modules.reminder.mapper.ReminderTaskMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件功能说明：首页看板服务实现，负责业务编排、事务边界、状态校验和持久化调用。
 * </p>
 *
 * @Author FixLedger
 */
@Service
public class DashboardServiceImpl implements DashboardService {

  private static final int DEFAULT_MONTHS = 6;
  private static final int MAX_MONTHS = 24;
  private static final String SUMMARY_LOAD_METRIC = "fixledger.dashboard.summary.load";

  private final DeviceAssetMapper deviceAssetMapper;
  private final DeviceCategoryMapper deviceCategoryMapper;
  private final MaintenanceRecordMapper maintenanceRecordMapper;
  private final ReminderTaskMapper reminderTaskMapper;
  private final DashboardStatisticsMapper dashboardStatisticsMapper;
  private final FamilyService familyService;
  private final DashboardSummaryCacheService summaryCacheService;
  private final Timer summaryLoadTimer;

  public DashboardServiceImpl(
      DeviceAssetMapper deviceAssetMapper,
      DeviceCategoryMapper deviceCategoryMapper,
      MaintenanceRecordMapper maintenanceRecordMapper,
      ReminderTaskMapper reminderTaskMapper,
      DashboardStatisticsMapper dashboardStatisticsMapper,
      FamilyService familyService,
      DashboardSummaryCacheService summaryCacheService,
      MeterRegistry meterRegistry
  ) {
    this.deviceAssetMapper = deviceAssetMapper;
    this.deviceCategoryMapper = deviceCategoryMapper;
    this.maintenanceRecordMapper = maintenanceRecordMapper;
    this.reminderTaskMapper = reminderTaskMapper;
    this.dashboardStatisticsMapper = dashboardStatisticsMapper;
    this.familyService = familyService;
    this.summaryCacheService = summaryCacheService;
    this.summaryLoadTimer = Timer.builder(SUMMARY_LOAD_METRIC)
        .publishPercentileHistogram()
        .register(meterRegistry);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现首页看板执行业务处理业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 业务响应数据
   */
  @Override
  public DashboardSummaryResponse summary(Long userId, Long familyId) {
    familyService.checkFamilyMember(userId, familyId);
    Optional<DashboardSummaryResponse> cached = summaryCacheService.get(familyId);
    if (cached.isPresent()) {
      return cached.get();
    }
    DashboardSummaryResponse summary = summaryLoadTimer.record(() -> loadSummary(familyId));
    summaryCacheService.put(familyId, summary);
    return summary;
  }

  private DashboardSummaryResponse loadSummary(Long familyId) {
    LocalDate today = LocalDate.now();
    LocalDateTime monthStart = YearMonth.from(today).atDay(1).atStartOfDay();
    LocalDateTime monthEnd = YearMonth.from(today).plusMonths(1).atDay(1).atStartOfDay();
    DashboardSummaryStatisticsDTO statistics = dashboardStatisticsMapper.selectSummary(
        familyId,
        today,
        today.plusDays(30),
        monthStart,
        monthEnd
    );
    return new DashboardSummaryResponse(
        statistics.getDeviceTotal(),
        statistics.getWarrantyExpiringCount(),
        statistics.getWarrantyExpiredCount(),
        statistics.getConsumableDueSoonCount(),
        statistics.getConsumableOverdueCount(),
        statistics.getRepairingCount(),
        statistics.getMonthlyMaintenanceCost()
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现首页看板执行业务处理业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 业务响应数据
   */
  @Override
  public List<DeviceCategoryDistributionResponse> deviceCategoryDistribution(
      Long userId,
      Long familyId
  ) {
    familyService.checkFamilyMember(userId, familyId);
    List<DeviceCategoryEntity> categories = deviceCategoryMapper.selectList(
        new LambdaQueryWrapper<DeviceCategoryEntity>()
            .eq(DeviceCategoryEntity::getFamilyId, familyId)
            .orderByAsc(DeviceCategoryEntity::getSortOrder)
    );
    Map<Long, Long> categoryCounts = countDevicesByCategory(familyId);
    List<DeviceCategoryDistributionResponse> result = new ArrayList<>();
    for (DeviceCategoryEntity category : categories) {
      Long count = categoryCounts.getOrDefault(category.getId(), 0L);
      result.add(new DeviceCategoryDistributionResponse(category.getName(), count));
    }
    Long uncategorized = categoryCounts.getOrDefault(null, 0L);
    if (uncategorized > 0) {
      result.add(new DeviceCategoryDistributionResponse("未分类", uncategorized));
    }
    return result;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现首页看板执行业务处理业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param months months 参数
   * @return 业务响应数据
   */
  @Override
  public List<MaintenanceCostTrendResponse> maintenanceCostTrend(
      Long userId,
      Long familyId,
      Integer months
  ) {
    familyService.checkFamilyMember(userId, familyId);
    int safeMonths = resolveMonths(months);
    // 先初始化连续月份，确保没有费用的月份也返回 0，方便前端画折线图。
    YearMonth current = YearMonth.now();
    YearMonth startMonth = current.minusMonths(safeMonths - 1L);
    Map<YearMonth, BigDecimal> costs = initMonthCosts(startMonth, safeMonths);
    LocalDateTime start = startMonth.atDay(1).atStartOfDay();
    LocalDateTime end = current.plusMonths(1).atDay(1).atStartOfDay();
    for (MaintenanceRecordEntity record : listCostRecords(familyId, start, end)) {
      YearMonth month = YearMonth.from(record.getCompletedAt());
      costs.computeIfPresent(month, (key, value) -> value.add(record.getRepairCost()));
    }
    return costs.entrySet().stream()
        .map(entry -> new MaintenanceCostTrendResponse(entry.getKey().toString(), entry.getValue()))
        .toList();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现首页看板执行业务处理业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param startDate startDate 参数
   * @param endDate endDate 参数
   * @return 业务响应数据
   */
  @Override
  public List<ReminderCalendarDayResponse> reminderCalendar(
      Long userId,
      Long familyId,
      LocalDate startDate,
      LocalDate endDate
  ) {
    familyService.checkFamilyMember(userId, familyId);
    LocalDate start = startDate == null ? LocalDate.now() : startDate;
    LocalDate end = endDate == null ? start.plusDays(30) : endDate;
    if (end.isBefore(start)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "结束日期不能早于开始日期");
    }
    Map<LocalDate, List<ReminderCalendarItemResponse>> grouped = new LinkedHashMap<>();
    // 使用 LinkedHashMap 保持日期顺序，前端无需再次排序。
    for (ReminderTaskEntity reminder : listCalendarReminders(familyId, start, end)) {
      LocalDate date = reminder.getRemindAt().toLocalDate();
      grouped.computeIfAbsent(date, key -> new ArrayList<>()).add(toCalendarItem(reminder));
    }
    return grouped.entrySet().stream()
        .map(entry -> new ReminderCalendarDayResponse(
            entry.getKey(),
            entry.getValue().size(),
            entry.getValue()
        ))
        .toList();
  }

  private Map<Long, Long> countDevicesByCategory(Long familyId) {
    List<Map<String, Object>> rows = deviceAssetMapper.selectMaps(
        new QueryWrapper<DeviceAssetEntity>()
            .select("category_id AS categoryId", "COUNT(*) AS deviceCount")
            .eq("family_id", familyId)
            .groupBy("category_id")
    );
    Map<Long, Long> categoryCounts = new HashMap<>();
    for (Map<String, Object> row : rows) {
      Long categoryId = toLong(getColumnValue(row, "categoryId", "category_id"));
      Long count = toLong(getColumnValue(row, "deviceCount", "count"));
      categoryCounts.put(categoryId, count == null ? 0L : count);
    }
    return categoryCounts;
  }

  private Object getColumnValue(Map<String, Object> row, String... names) {
    for (String name : names) {
      if (row.containsKey(name)) {
        return row.get(name);
      }
      for (Map.Entry<String, Object> entry : row.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(name)) {
          return entry.getValue();
        }
      }
    }
    return null;
  }

  private Long toLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.valueOf(value.toString());
  }

  private List<MaintenanceRecordEntity> listCostRecords(
      Long familyId,
      LocalDateTime start,
      LocalDateTime end
  ) {
    // 维修费用看板排除取消记录，并以完成时间作为统计归属月份。
    return maintenanceRecordMapper.selectList(new LambdaQueryWrapper<MaintenanceRecordEntity>()
        .eq(MaintenanceRecordEntity::getFamilyId, familyId)
        .ne(MaintenanceRecordEntity::getStatus, MaintenanceStatus.CANCELED.getCode())
        .isNotNull(MaintenanceRecordEntity::getRepairCost)
        .isNotNull(MaintenanceRecordEntity::getCompletedAt)
        .ge(MaintenanceRecordEntity::getCompletedAt, start)
        .lt(MaintenanceRecordEntity::getCompletedAt, end));
  }

  private Map<YearMonth, BigDecimal> initMonthCosts(YearMonth startMonth, int months) {
    Map<YearMonth, BigDecimal> costs = new LinkedHashMap<>();
    for (int i = 0; i < months; i++) {
      costs.put(startMonth.plusMonths(i), BigDecimal.ZERO);
    }
    return costs;
  }

  private int resolveMonths(Integer months) {
    if (months == null) {
      return DEFAULT_MONTHS;
    }
    if (months < 1 || months > MAX_MONTHS) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "统计月份必须在 1 到 24 之间");
    }
    return months;
  }

  private List<ReminderTaskEntity> listCalendarReminders(
      Long familyId,
      LocalDate start,
      LocalDate end
  ) {
    return reminderTaskMapper.selectList(new LambdaQueryWrapper<ReminderTaskEntity>()
        .eq(ReminderTaskEntity::getFamilyId, familyId)
        .ge(ReminderTaskEntity::getRemindAt, start.atStartOfDay())
        .lt(ReminderTaskEntity::getRemindAt, end.plusDays(1).atStartOfDay())
        .orderByAsc(ReminderTaskEntity::getRemindAt));
  }

  private ReminderCalendarItemResponse toCalendarItem(ReminderTaskEntity reminder) {
    return new ReminderCalendarItemResponse(
        reminder.getId(),
        reminder.getReminderType(),
        reminder.getTitle(),
        reminder.getStatus(),
        reminder.getBizType(),
        reminder.getBizId(),
        reminder.getRemindAt()
    );
  }
}
