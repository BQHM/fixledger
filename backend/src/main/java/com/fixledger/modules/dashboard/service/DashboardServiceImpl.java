package com.fixledger.modules.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.common.constant.RedisKeys;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.infrastructure.redis.RedisService;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.enums.DeviceStatus;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.consumable.entity.ConsumableItemEntity;
import com.fixledger.modules.consumable.enums.ConsumableStatus;
import com.fixledger.modules.consumable.mapper.ConsumableItemMapper;
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
import com.fixledger.modules.warranty.entity.WarrantyRecordEntity;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

  private static final int DEFAULT_MONTHS = 6;
  private static final int MAX_MONTHS = 24;
  private static final Duration SUMMARY_CACHE_TTL = Duration.ofMinutes(5);

  private final DeviceAssetMapper deviceAssetMapper;
  private final DeviceCategoryMapper deviceCategoryMapper;
  private final WarrantyRecordMapper warrantyRecordMapper;
  private final ConsumableItemMapper consumableItemMapper;
  private final MaintenanceRecordMapper maintenanceRecordMapper;
  private final ReminderTaskMapper reminderTaskMapper;
  private final FamilyService familyService;
  private final RedisService redisService;

  public DashboardServiceImpl(
      DeviceAssetMapper deviceAssetMapper,
      DeviceCategoryMapper deviceCategoryMapper,
      WarrantyRecordMapper warrantyRecordMapper,
      ConsumableItemMapper consumableItemMapper,
      MaintenanceRecordMapper maintenanceRecordMapper,
      ReminderTaskMapper reminderTaskMapper,
      FamilyService familyService,
      RedisService redisService
  ) {
    this.deviceAssetMapper = deviceAssetMapper;
    this.deviceCategoryMapper = deviceCategoryMapper;
    this.warrantyRecordMapper = warrantyRecordMapper;
    this.consumableItemMapper = consumableItemMapper;
    this.maintenanceRecordMapper = maintenanceRecordMapper;
    this.reminderTaskMapper = reminderTaskMapper;
    this.familyService = familyService;
    this.redisService = redisService;
  }

  @Override
  public DashboardSummaryResponse summary(Long userId, Long familyId) {
    familyService.checkFamilyMember(userId, familyId);
    // 当前先写入刷新标记，后续可替换为完整摘要缓存。
    redisService.set(RedisKeys.dashboardSummary(familyId), "refreshed", SUMMARY_CACHE_TTL);
    LocalDate today = LocalDate.now();
    LocalDateTime monthStart = YearMonth.from(today).atDay(1).atStartOfDay();
    LocalDateTime monthEnd = YearMonth.from(today).plusMonths(1).atDay(1).atStartOfDay();
    return new DashboardSummaryResponse(
        countDevices(familyId),
        countWarrantyExpiring(familyId, today),
        countWarrantyExpired(familyId, today),
        countConsumables(familyId, ConsumableStatus.DUE_SOON),
        countConsumables(familyId, ConsumableStatus.OVERDUE),
        countRepairing(familyId),
        sumMaintenanceCost(familyId, monthStart, monthEnd)
    );
  }

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
    List<DeviceCategoryDistributionResponse> result = new ArrayList<>();
    for (DeviceCategoryEntity category : categories) {
      Long count = deviceAssetMapper.selectCount(new LambdaQueryWrapper<DeviceAssetEntity>()
          .eq(DeviceAssetEntity::getFamilyId, familyId)
          .eq(DeviceAssetEntity::getCategoryId, category.getId()));
      result.add(new DeviceCategoryDistributionResponse(category.getName(), count));
    }
    Long uncategorized = deviceAssetMapper.selectCount(new LambdaQueryWrapper<DeviceAssetEntity>()
        .eq(DeviceAssetEntity::getFamilyId, familyId)
        .isNull(DeviceAssetEntity::getCategoryId));
    if (uncategorized > 0) {
      result.add(new DeviceCategoryDistributionResponse("未分类", uncategorized));
    }
    return result;
  }

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

  private long countDevices(Long familyId) {
    return deviceAssetMapper.selectCount(new LambdaQueryWrapper<DeviceAssetEntity>()
        .eq(DeviceAssetEntity::getFamilyId, familyId));
  }

  private long countWarrantyExpiring(Long familyId, LocalDate today) {
    return warrantyRecordMapper.selectCount(new LambdaQueryWrapper<WarrantyRecordEntity>()
        .eq(WarrantyRecordEntity::getFamilyId, familyId)
        .ge(WarrantyRecordEntity::getEndDate, today)
        .le(WarrantyRecordEntity::getEndDate, today.plusDays(30)));
  }

  private long countWarrantyExpired(Long familyId, LocalDate today) {
    return warrantyRecordMapper.selectCount(new LambdaQueryWrapper<WarrantyRecordEntity>()
        .eq(WarrantyRecordEntity::getFamilyId, familyId)
        .lt(WarrantyRecordEntity::getEndDate, today));
  }

  private long countConsumables(Long familyId, ConsumableStatus status) {
    return consumableItemMapper.selectCount(new LambdaQueryWrapper<ConsumableItemEntity>()
        .eq(ConsumableItemEntity::getFamilyId, familyId)
        .eq(ConsumableItemEntity::getStatus, status.getCode())
        .eq(ConsumableItemEntity::getEnabled, true));
  }

  private long countRepairing(Long familyId) {
    return maintenanceRecordMapper.selectCount(new LambdaQueryWrapper<MaintenanceRecordEntity>()
        .eq(MaintenanceRecordEntity::getFamilyId, familyId)
        .eq(MaintenanceRecordEntity::getStatus, MaintenanceStatus.REPAIRING.getCode()));
  }

  private BigDecimal sumMaintenanceCost(
      Long familyId,
      LocalDateTime start,
      LocalDateTime end
  ) {
    BigDecimal total = BigDecimal.ZERO;
    for (MaintenanceRecordEntity record : listCostRecords(familyId, start, end)) {
      total = total.add(record.getRepairCost());
    }
    return total;
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
