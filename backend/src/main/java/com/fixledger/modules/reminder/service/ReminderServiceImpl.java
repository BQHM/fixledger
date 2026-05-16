package com.fixledger.modules.reminder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fixledger.common.constant.RedisKeys;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.infrastructure.redis.RedisService;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.consumable.entity.ConsumableItemEntity;
import com.fixledger.modules.consumable.mapper.ConsumableItemMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.file.enums.FileBizType;
import com.fixledger.modules.reminder.entity.ReminderTaskEntity;
import com.fixledger.modules.reminder.enums.ReminderStatus;
import com.fixledger.modules.reminder.enums.ReminderType;
import com.fixledger.modules.reminder.mapper.ReminderTaskMapper;
import com.fixledger.modules.reminder.query.ReminderPageQuery;
import com.fixledger.modules.reminder.response.ReminderResponse;
import com.fixledger.modules.reminder.response.ReminderScanResponse;
import com.fixledger.modules.reminder.response.UnreadCountResponse;
import com.fixledger.modules.warranty.entity.WarrantyRecordEntity;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 文件功能说明：提醒通知服务实现，负责保修到期和耗材更换提醒的扫描、去重、查询和状态处理。
 * </p>
 *
 * @Author FixLedger
 */
@Service
public class ReminderServiceImpl implements ReminderService {

  private static final Duration DEDUPE_TTL = Duration.ofDays(2);

  private final ReminderTaskMapper reminderTaskMapper;
  private final WarrantyRecordMapper warrantyRecordMapper;
  private final ConsumableItemMapper consumableItemMapper;
  private final DeviceAssetMapper deviceAssetMapper;
  private final FamilyService familyService;
  private final RedisService redisService;
  private final ReminderCreationService reminderCreationService;

  public ReminderServiceImpl(
      ReminderTaskMapper reminderTaskMapper,
      WarrantyRecordMapper warrantyRecordMapper,
      ConsumableItemMapper consumableItemMapper,
      DeviceAssetMapper deviceAssetMapper,
      FamilyService familyService,
      RedisService redisService,
      ReminderCreationService reminderCreationService
  ) {
    this.reminderTaskMapper = reminderTaskMapper;
    this.warrantyRecordMapper = warrantyRecordMapper;
    this.consumableItemMapper = consumableItemMapper;
    this.deviceAssetMapper = deviceAssetMapper;
    this.familyService = familyService;
    this.redisService = redisService;
    this.reminderCreationService = reminderCreationService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：按状态和类型分页查询家庭提醒，查询条件始终带家庭空间隔离。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @Override
  public PageResponse<ReminderResponse> pageReminders(
      Long userId,
      Long familyId,
      ReminderPageQuery query
  ) {
    familyService.checkFamilyMember(userId, familyId);
    ReminderStatus status = resolveStatus(query.getStatus());
    ReminderType type = resolveType(query.getType());
    IPage<ReminderTaskEntity> page = reminderTaskMapper.selectPage(
        query.toPage(),
        buildPageWrapper(familyId, status, type)
    );
    return PageResponse.from(page.convert(this::toResponse));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：统计待处理提醒数量，用于首页、侧边栏等入口展示待办徽标。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 待处理提醒数量
   */
  @Override
  public UnreadCountResponse unreadCount(Long userId, Long familyId) {
    familyService.checkFamilyMember(userId, familyId);
    Long count = reminderTaskMapper.selectCount(new LambdaQueryWrapper<ReminderTaskEntity>()
        .eq(ReminderTaskEntity::getFamilyId, familyId)
        .eq(ReminderTaskEntity::getStatus, ReminderStatus.PENDING.getCode()));
    return new UnreadCountResponse(count);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：将提醒置为已读并记录读取时间，保留提醒生命周期记录。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param reminderId 提醒 ID
   * @return 已读状态的提醒数据
   */
  @Override
  @Transactional
  public ReminderResponse markRead(Long userId, Long familyId, Long reminderId) {
    familyService.checkFamilyMember(userId, familyId);
    ReminderTaskEntity reminder = getReminder(familyId, reminderId);
    reminder.setStatus(ReminderStatus.READ.getCode());
    reminder.setReadAt(LocalDateTime.now());
    reminderTaskMapper.updateById(reminder);
    return toResponse(reminder);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：忽略不需要处理的提醒，保留历史记录但不再计入待处理数量。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param reminderId 提醒 ID
   * @return 已忽略状态的提醒数据
   */
  @Override
  @Transactional
  public ReminderResponse ignore(Long userId, Long familyId, Long reminderId) {
    familyService.checkFamilyMember(userId, familyId);
    ReminderTaskEntity reminder = getReminder(familyId, reminderId);
    reminder.setStatus(ReminderStatus.IGNORED.getCode());
    reminder.setReadAt(LocalDateTime.now());
    reminderTaskMapper.updateById(reminder);
    return toResponse(reminder);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：手动扫描当前家庭的保修和耗材提醒，用于前端按钮或演示环境即时验证。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 本次生成和跳过数量
   */
  @Override
  public ReminderScanResponse scanFamily(Long userId, Long familyId) {
    familyService.checkFamilyMember(userId, familyId);
    return doScanFamily(familyId, LocalDate.now());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：定时任务按家庭和业务日期扫描保修、耗材提醒，不依赖前端触发。
   * </p>
   * @param familyId 家庭空间 ID
   * @param today 业务日期
   * @return 本次生成和跳过数量
   */
  @Override
  public ReminderScanResponse scanFamily(Long familyId, LocalDate today) {
    return doScanFamily(familyId, today);
  }

  private ReminderScanResponse doScanFamily(Long familyId, LocalDate today) {
    // 扫描拆成保修和耗材两条线，便于后续扩展维修回访等提醒类型。
    ReminderScanResponse warranty = scanWarrantyReminders(familyId, today);
    ReminderScanResponse consumable = scanConsumableReminders(familyId, today);
    return warranty.plus(consumable);
  }

  private ReminderScanResponse scanWarrantyReminders(Long familyId, LocalDate today) {
    List<WarrantyRecordEntity> warranties = warrantyRecordMapper.selectList(
        new LambdaQueryWrapper<WarrantyRecordEntity>()
            .eq(WarrantyRecordEntity::getFamilyId, familyId)
            .le(WarrantyRecordEntity::getEndDate, today.plusYears(1))
            .orderByAsc(WarrantyRecordEntity::getEndDate)
    );
    ScanCounter counter = new ScanCounter();
    for (WarrantyRecordEntity warranty : warranties) {
      LocalDate remindDate = warranty.getEndDate().minusDays(warranty.getRemindDaysBefore());
      // 未到提前提醒窗口的保修记录跳过，避免过早打扰用户。
      if (today.isBefore(remindDate)) {
        continue;
      }
      ReminderType type = warranty.getEndDate().isBefore(today)
          ? ReminderType.WARRANTY_EXPIRED
          : ReminderType.WARRANTY_EXPIRE_SOON;
      DeviceAssetEntity device = deviceAssetMapper.selectById(warranty.getDeviceId());
      String deviceName = device == null ? "设备" : device.getName();
      boolean created = createReminderIfAbsent(
          familyId,
          type,
          FileBizType.WARRANTY.getCode(),
          warranty.getId(),
          deviceName + type.getDescription(),
          deviceName + "的保修结束日期为 " + warranty.getEndDate(),
          today.atTime(8, 0)
      );
      counter.addWarranty(created);
    }
    return counter.toResponse();
  }

  private ReminderScanResponse scanConsumableReminders(Long familyId, LocalDate today) {
    List<ConsumableItemEntity> consumables = consumableItemMapper.selectList(
        new LambdaQueryWrapper<ConsumableItemEntity>()
            .eq(ConsumableItemEntity::getFamilyId, familyId)
            .eq(ConsumableItemEntity::getEnabled, true)
            .isNotNull(ConsumableItemEntity::getNextRemindDate)
            .le(ConsumableItemEntity::getNextRemindDate, today.plusYears(1))
            .orderByAsc(ConsumableItemEntity::getNextRemindDate)
    );
    ScanCounter counter = new ScanCounter();
    for (ConsumableItemEntity consumable : consumables) {
      LocalDate remindDate = consumable.getNextRemindDate()
          .minusDays(consumable.getRemindDaysBefore());
      // 耗材按下次提醒日期和提前天数进入提醒窗口。
      if (today.isBefore(remindDate)) {
        continue;
      }
      ReminderType type = consumable.getNextRemindDate().isBefore(today)
          ? ReminderType.CONSUMABLE_OVERDUE
          : ReminderType.CONSUMABLE_REPLACE_SOON;
      boolean created = createReminderIfAbsent(
          familyId,
          type,
          FileBizType.CONSUMABLE.getCode(),
          consumable.getId(),
          consumable.getName() + type.getDescription(),
          consumable.getName() + "的下次提醒日期为 " + consumable.getNextRemindDate(),
          today.atTime(8, 0)
      );
      counter.addConsumable(created);
    }
    return counter.toResponse();
  }

  private boolean createReminderIfAbsent(
      Long familyId,
      ReminderType reminderType,
      String bizType,
      Long bizId,
      String title,
      String content,
      LocalDateTime remindAt
  ) {
    LocalDate remindDate = remindAt.toLocalDate();
    String key = RedisKeys.reminderDedupe(reminderType.getCode(), bizId, remindDate);
    // Redis 先做短期去重，降低定时任务重复扫描带来的并发插入风险。
    if (!redisService.setIfAbsent(key, "1", DEDUPE_TTL)) {
      return false;
    }
    try {
      // Redis 已在事务外完成去重，数据库事务只包住提醒和站内通知写入。
      return reminderCreationService.createReminderIfAbsent(
          familyId,
          reminderType,
          bizType,
          bizId,
          title,
          content,
          remindAt
      );
    } catch (RuntimeException e) {
      // 写库失败时释放去重键，让下一次扫描可以重试，而不是被 Redis TTL 误拦截。
      redisService.delete(key);
      throw e;
    }
  }

  private LambdaQueryWrapper<ReminderTaskEntity> buildPageWrapper(
      Long familyId,
      ReminderStatus status,
      ReminderType type
  ) {
    LambdaQueryWrapper<ReminderTaskEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ReminderTaskEntity::getFamilyId, familyId);
    if (status != null) {
      wrapper.eq(ReminderTaskEntity::getStatus, status.getCode());
    }
    if (type != null) {
      wrapper.eq(ReminderTaskEntity::getReminderType, type.getCode());
    }
    return wrapper.orderByDesc(ReminderTaskEntity::getRemindAt);
  }

  private ReminderTaskEntity getReminder(Long familyId, Long reminderId) {
    ReminderTaskEntity reminder = reminderTaskMapper.selectOne(
        new LambdaQueryWrapper<ReminderTaskEntity>()
            .eq(ReminderTaskEntity::getFamilyId, familyId)
            .eq(ReminderTaskEntity::getId, reminderId)
    );
    if (reminder == null) {
      throw new BusinessException(ErrorCode.REMINDER_NOT_FOUND, "提醒不存在");
    }
    return reminder;
  }

  private ReminderStatus resolveStatus(String status) {
    if (!StringUtils.hasText(status)) {
      return null;
    }
    ReminderStatus reminderStatus = ReminderStatus.fromCode(status);
    if (reminderStatus == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "提醒状态无效");
    }
    return reminderStatus;
  }

  private ReminderType resolveType(String type) {
    if (!StringUtils.hasText(type)) {
      return null;
    }
    ReminderType reminderType = ReminderType.fromCode(type);
    if (reminderType == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "提醒类型无效");
    }
    return reminderType;
  }

  private ReminderResponse toResponse(ReminderTaskEntity reminder) {
    return new ReminderResponse(
        reminder.getId(),
        reminder.getReminderType(),
        reminder.getBizType(),
        reminder.getBizId(),
        reminder.getTitle(),
        reminder.getContent(),
        reminder.getRemindAt(),
        reminder.getStatus(),
        reminder.getReadAt()
    );
  }

  private static class ScanCounter {

    private int warrantyCreated;
    private int consumableCreated;
    private int notificationCreated;
    private int skippedDuplicate;

    void addWarranty(boolean created) {
      if (created) {
        warrantyCreated++;
        notificationCreated++;
        return;
      }
      skippedDuplicate++;
    }

    void addConsumable(boolean created) {
      if (created) {
        consumableCreated++;
        notificationCreated++;
        return;
      }
      skippedDuplicate++;
    }

    ReminderScanResponse toResponse() {
      return new ReminderScanResponse(
          warrantyCreated,
          consumableCreated,
          notificationCreated,
          skippedDuplicate,
          0
      );
    }
  }
}
