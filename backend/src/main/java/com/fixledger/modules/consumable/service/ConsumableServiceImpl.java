package com.fixledger.modules.consumable.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.consumable.entity.ConsumableItemEntity;
import com.fixledger.modules.consumable.entity.ConsumableReplaceRecordEntity;
import com.fixledger.modules.consumable.enums.ConsumableStatus;
import com.fixledger.modules.consumable.mapper.ConsumableItemMapper;
import com.fixledger.modules.consumable.mapper.ConsumableReplaceRecordMapper;
import com.fixledger.modules.consumable.query.ConsumableDueSoonQuery;
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.request.CreateReplaceRecordRequest;
import com.fixledger.modules.consumable.request.UpdateConsumableRequest;
import com.fixledger.modules.consumable.response.ConsumableReplaceRecordResponse;
import com.fixledger.modules.consumable.response.ConsumableResponse;
import com.fixledger.modules.family.service.FamilyService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 文件功能说明：耗材服务实现，负责业务编排、事务边界、状态校验和持久化调用。
 * </p>
 *
 * @Author FixLedger
 */
@Service
public class ConsumableServiceImpl implements ConsumableService {

  private static final int DEFAULT_REMIND_DAYS_BEFORE = 7;

  private final ConsumableItemMapper consumableItemMapper;
  private final ConsumableReplaceRecordMapper replaceRecordMapper;
  private final DeviceAssetMapper deviceAssetMapper;
  private final FamilyService familyService;

  public ConsumableServiceImpl(
      ConsumableItemMapper consumableItemMapper,
      ConsumableReplaceRecordMapper replaceRecordMapper,
      DeviceAssetMapper deviceAssetMapper,
      FamilyService familyService
  ) {
    this.consumableItemMapper = consumableItemMapper;
    this.replaceRecordMapper = replaceRecordMapper;
    this.deviceAssetMapper = deviceAssetMapper;
    this.familyService = familyService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现耗材查询列表业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 列表结果
   */
  @Override
  public List<ConsumableResponse> listDeviceConsumables(
      Long userId,
      Long familyId,
      Long deviceId
  ) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceAssetEntity device = getDevice(familyId, deviceId);
    List<ConsumableItemEntity> consumables = consumableItemMapper.selectList(
        new LambdaQueryWrapper<ConsumableItemEntity>()
            .eq(ConsumableItemEntity::getFamilyId, familyId)
            .eq(ConsumableItemEntity::getDeviceId, deviceId)
            .orderByAsc(ConsumableItemEntity::getNextRemindDate)
            .orderByDesc(ConsumableItemEntity::getUpdatedAt)
    );
    return consumables.stream()
        .map(consumable -> toResponse(consumable, device.getName()))
        .toList();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现耗材创建业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @Override
  @Transactional
  public ConsumableResponse createConsumable(
      Long userId,
      Long familyId,
      Long deviceId,
      CreateConsumableRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceAssetEntity device = getDevice(familyId, deviceId);

    ConsumableItemEntity entity = new ConsumableItemEntity();
    entity.setFamilyId(familyId);
    entity.setDeviceId(deviceId);
    entity.setName(request.name());
    entity.setBrand(request.brand());
    entity.setModel(request.model());
    entity.setCycleDays(resolveCycleDays(request.cycleDays()));
    entity.setLastReplacedDate(request.lastReplacedDate());
    // 下次提醒日期由最近更换日期和周期计算，前端只负责展示。
    entity.setNextRemindDate(calculateNextRemindDate(
        request.lastReplacedDate(),
        entity.getCycleDays()
    ));
    entity.setRemindDaysBefore(resolveRemindDays(request.remindDaysBefore()));
    entity.setEnabled(true);
    refreshStatus(entity);
    entity.setRemark(request.remark());
    consumableItemMapper.insert(entity);
    return toResponse(entity, device.getName());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现耗材更新业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @Override
  @Transactional
  public ConsumableResponse updateConsumable(
      Long userId,
      Long familyId,
      Long consumableId,
      UpdateConsumableRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    ConsumableItemEntity entity = getConsumable(familyId, consumableId);
    DeviceAssetEntity device = getDevice(familyId, entity.getDeviceId());

    entity.setName(request.name());
    entity.setBrand(request.brand());
    entity.setModel(request.model());
    entity.setCycleDays(resolveCycleDays(request.cycleDays()));
    entity.setLastReplacedDate(request.lastReplacedDate());
    // 下次提醒日期由最近更换日期和周期计算，前端只负责展示。
    entity.setNextRemindDate(calculateNextRemindDate(
        request.lastReplacedDate(),
        entity.getCycleDays()
    ));
    entity.setRemindDaysBefore(resolveRemindDays(request.remindDaysBefore()));
    entity.setEnabled(request.enabled() == null ? true : request.enabled());
    refreshStatus(entity);
    entity.setRemark(request.remark());
    consumableItemMapper.updateById(entity);
    return toResponse(entity, device.getName());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现耗材删除业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @return 是否处理成功
   */
  @Override
  @Transactional
  public boolean deleteConsumable(Long userId, Long familyId, Long consumableId) {
    familyService.checkFamilyMember(userId, familyId);
    ConsumableItemEntity entity = getConsumable(familyId, consumableId);
    return consumableItemMapper.deleteById(entity.getId()) > 0;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现耗材创建业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @Override
  @Transactional
  public ConsumableReplaceRecordResponse createReplaceRecord(
      Long userId,
      Long familyId,
      Long consumableId,
      CreateReplaceRecordRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    ConsumableItemEntity consumable = getConsumable(familyId, consumableId);
    if (Boolean.FALSE.equals(consumable.getEnabled())) {
      throw new BusinessException(ErrorCode.REPLACE_DATE_INVALID, "停用耗材不能记录更换");
    }

    ConsumableReplaceRecordEntity record = new ConsumableReplaceRecordEntity();
    record.setFamilyId(familyId);
    record.setConsumableId(consumableId);
    record.setDeviceId(consumable.getDeviceId());
    record.setReplacedDate(request.replacedDate());
    record.setCost(request.cost());
    record.setNote(request.note());
    replaceRecordMapper.insert(record);

    // 每次更换后立即刷新耗材主表，保证首页和提醒扫描读取最新状态。
    consumable.setLastReplacedDate(request.replacedDate());
    consumable.setNextRemindDate(calculateNextRemindDate(
        request.replacedDate(),
        consumable.getCycleDays()
    ));
    refreshStatus(consumable);
    consumableItemMapper.updateById(consumable);
    return toReplaceResponse(record);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现耗材查询列表业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @return 列表结果
   */
  @Override
  public List<ConsumableReplaceRecordResponse> listReplaceRecords(
      Long userId,
      Long familyId,
      Long consumableId
  ) {
    familyService.checkFamilyMember(userId, familyId);
    ConsumableItemEntity consumable = getConsumable(familyId, consumableId);
    return replaceRecordMapper.selectList(
            new LambdaQueryWrapper<ConsumableReplaceRecordEntity>()
                .eq(ConsumableReplaceRecordEntity::getFamilyId, familyId)
                .eq(ConsumableReplaceRecordEntity::getConsumableId, consumable.getId())
                .orderByDesc(ConsumableReplaceRecordEntity::getReplacedDate)
        )
        .stream()
        .map(this::toReplaceResponse)
        .toList();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现耗材分页查询业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @Override
  public PageResponse<ConsumableResponse> pageDueSoon(
      Long userId,
      Long familyId,
      ConsumableDueSoonQuery query
  ) {
    familyService.checkFamilyMember(userId, familyId);
    LocalDate today = LocalDate.now();
    // 已逾期耗材也满足 <= endDate，会在看板中优先暴露。
    LocalDate endDate = today.plusDays(query.getDays());
    IPage<ConsumableItemEntity> page = consumableItemMapper.selectPage(
        query.toPage(),
        new LambdaQueryWrapper<ConsumableItemEntity>()
            .eq(ConsumableItemEntity::getFamilyId, familyId)
            .eq(ConsumableItemEntity::getEnabled, true)
            .isNotNull(ConsumableItemEntity::getNextRemindDate)
            .le(ConsumableItemEntity::getNextRemindDate, endDate)
            .orderByAsc(ConsumableItemEntity::getNextRemindDate)
    );
    Map<Long, String> deviceNames = listDeviceNames(familyId, page.getRecords());
    return PageResponse.from(page.convert(consumable -> toResponse(consumable, deviceNames)));
  }

  private DeviceAssetEntity getDevice(Long familyId, Long deviceId) {
    DeviceAssetEntity device = deviceAssetMapper.selectOne(
        new LambdaQueryWrapper<DeviceAssetEntity>()
            .eq(DeviceAssetEntity::getId, deviceId)
            .eq(DeviceAssetEntity::getFamilyId, familyId)
    );
    if (device == null) {
      throw new BusinessException(ErrorCode.DEVICE_NOT_FOUND, "设备不存在");
    }
    return device;
  }

  private ConsumableItemEntity getConsumable(Long familyId, Long consumableId) {
    ConsumableItemEntity entity = consumableItemMapper.selectOne(
        new LambdaQueryWrapper<ConsumableItemEntity>()
            .eq(ConsumableItemEntity::getId, consumableId)
            .eq(ConsumableItemEntity::getFamilyId, familyId)
    );
    if (entity == null) {
      throw new BusinessException(ErrorCode.CONSUMABLE_NOT_FOUND, "耗材不存在");
    }
    return entity;
  }

  private Integer resolveCycleDays(Integer cycleDays) {
    if (cycleDays == null || cycleDays <= 0) {
      throw new BusinessException(ErrorCode.REPLACE_DATE_INVALID, "更换周期必须大于 0");
    }
    return cycleDays;
  }

  private Integer resolveRemindDays(Integer remindDaysBefore) {
    if (remindDaysBefore == null) {
      return DEFAULT_REMIND_DAYS_BEFORE;
    }
    if (remindDaysBefore < 0) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "提前提醒天数不能小于 0");
    }
    return remindDaysBefore;
  }

  private LocalDate calculateNextRemindDate(LocalDate lastReplacedDate, Integer cycleDays) {
    return lastReplacedDate == null ? null : lastReplacedDate.plusDays(cycleDays);
  }

  private void refreshStatus(ConsumableItemEntity entity) {
    // 状态只由 enabled、nextRemindDate 和提前提醒天数推导，避免多处手写状态。
    if (Boolean.FALSE.equals(entity.getEnabled())) {
      entity.setStatus(ConsumableStatus.DISABLED.getCode());
      return;
    }
    if (entity.getNextRemindDate() == null) {
      entity.setStatus(ConsumableStatus.NORMAL.getCode());
      return;
    }
    LocalDate today = LocalDate.now();
    if (entity.getNextRemindDate().isBefore(today)) {
      entity.setStatus(ConsumableStatus.OVERDUE.getCode());
      return;
    }
    LocalDate remindStart = entity.getNextRemindDate().minusDays(entity.getRemindDaysBefore());
    if (!today.isBefore(remindStart)) {
      entity.setStatus(ConsumableStatus.DUE_SOON.getCode());
      return;
    }
    entity.setStatus(ConsumableStatus.NORMAL.getCode());
  }

  private Map<Long, String> listDeviceNames(Long familyId, List<ConsumableItemEntity> consumables) {
    Set<Long> deviceIds = consumables.stream()
        .map(ConsumableItemEntity::getDeviceId)
        .filter(id -> id != null)
        .collect(Collectors.toSet());
    if (deviceIds.isEmpty()) {
      return Map.of();
    }
    return deviceAssetMapper.selectList(new LambdaQueryWrapper<DeviceAssetEntity>()
            .eq(DeviceAssetEntity::getFamilyId, familyId)
            .in(DeviceAssetEntity::getId, deviceIds))
        .stream()
        .collect(Collectors.toMap(
            DeviceAssetEntity::getId,
            DeviceAssetEntity::getName,
            (left, right) -> left
        ));
  }

  private ConsumableResponse toResponse(
      ConsumableItemEntity entity,
      Map<Long, String> deviceNames
  ) {
    return toResponse(entity, deviceNames.get(entity.getDeviceId()));
  }

  private ConsumableResponse toResponse(ConsumableItemEntity entity, String deviceName) {
    refreshStatus(entity);
    return new ConsumableResponse(
        entity.getId(),
        entity.getDeviceId(),
        deviceName,
        entity.getName(),
        entity.getBrand(),
        entity.getModel(),
        entity.getCycleDays(),
        entity.getLastReplacedDate(),
        entity.getNextRemindDate(),
        entity.getRemindDaysBefore(),
        entity.getStatus(),
        entity.getEnabled(),
        entity.getRemark()
    );
  }

  private ConsumableReplaceRecordResponse toReplaceResponse(
      ConsumableReplaceRecordEntity record
  ) {
    return new ConsumableReplaceRecordResponse(
        record.getId(),
        record.getConsumableId(),
        record.getDeviceId(),
        record.getReplacedDate(),
        record.getCost(),
        record.getNote()
    );
  }
}
