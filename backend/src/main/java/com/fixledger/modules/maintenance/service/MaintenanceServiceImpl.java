package com.fixledger.modules.maintenance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.enums.DeviceStatus;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.entity.MaintenanceRecordEntity;
import com.fixledger.modules.maintenance.enums.MaintenanceStatus;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import com.fixledger.modules.maintenance.query.MaintenancePageQuery;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceStatusRequest;
import com.fixledger.modules.maintenance.response.MaintenanceCostSummaryResponse;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

  private final MaintenanceRecordMapper maintenanceRecordMapper;
  private final DeviceAssetMapper deviceAssetMapper;
  private final FamilyService familyService;

  public MaintenanceServiceImpl(
      MaintenanceRecordMapper maintenanceRecordMapper,
      DeviceAssetMapper deviceAssetMapper,
      FamilyService familyService
  ) {
    this.maintenanceRecordMapper = maintenanceRecordMapper;
    this.deviceAssetMapper = deviceAssetMapper;
    this.familyService = familyService;
  }

  @Override
  @Transactional
  public MaintenanceResponse createMaintenance(
      Long userId,
      Long familyId,
      Long deviceId,
      CreateMaintenanceRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    getDevice(familyId, deviceId);

    MaintenanceRecordEntity entity = new MaintenanceRecordEntity();
    entity.setFamilyId(familyId);
    entity.setDeviceId(deviceId);
    entity.setTitle(request.title());
    entity.setFaultDescription(request.faultDescription());
    entity.setOccurredAt(resolveOccurredAt(request.occurredAt()));
    entity.setStatus(MaintenanceStatus.PENDING.getCode());
    entity.setRepairChannel(request.repairChannel());
    entity.setRepairContact(request.repairContact());
    maintenanceRecordMapper.insert(entity);
    return toResponse(entity);
  }

  @Override
  public PageResponse<MaintenanceResponse> pageMaintenance(
      Long userId,
      Long familyId,
      MaintenancePageQuery query
  ) {
    familyService.checkFamilyMember(userId, familyId);
    MaintenanceStatus status = resolveStatusIfPresent(query.getStatus());
    IPage<MaintenanceRecordEntity> page = maintenanceRecordMapper.selectPage(
        query.toPage(),
        buildPageWrapper(familyId, query.getDeviceId(), status)
    );
    return PageResponse.from(page.convert(this::toResponse));
  }

  @Override
  public MaintenanceResponse getMaintenanceDetail(Long userId, Long familyId, Long maintenanceId) {
    familyService.checkFamilyMember(userId, familyId);
    return toResponse(getMaintenance(familyId, maintenanceId));
  }

  @Override
  @Transactional
  public MaintenanceResponse updateMaintenance(
      Long userId,
      Long familyId,
      Long maintenanceId,
      UpdateMaintenanceRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    MaintenanceRecordEntity entity = getMaintenance(familyId, maintenanceId);
    if (isTerminal(entity.getStatus())) {
      throw new BusinessException(ErrorCode.MAINTENANCE_STATUS_INVALID, "终态维修记录不能修改");
    }

    entity.setTitle(request.title());
    entity.setFaultDescription(request.faultDescription());
    entity.setOccurredAt(resolveOccurredAt(request.occurredAt()));
    entity.setRepairChannel(request.repairChannel());
    entity.setRepairContact(request.repairContact());
    entity.setRepairCost(request.repairCost());
    entity.setResultDescription(request.resultDescription());
    entity.setCompletedAt(request.completedAt());
    maintenanceRecordMapper.updateById(entity);
    return toResponse(entity);
  }

  @Override
  @Transactional
  public MaintenanceResponse updateMaintenanceStatus(
      Long userId,
      Long familyId,
      Long maintenanceId,
      UpdateMaintenanceStatusRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    MaintenanceRecordEntity entity = getMaintenance(familyId, maintenanceId);
    // 维修状态必须走枚举状态机，禁止任意跨阶段更新。
    MaintenanceStatus current = MaintenanceStatus.fromCode(entity.getStatus());
    MaintenanceStatus target = MaintenanceStatus.fromCode(request.status());
    if (current == null || target == null || !current.canTransitionTo(target)) {
      throw new BusinessException(ErrorCode.MAINTENANCE_STATUS_INVALID, "维修状态流转无效");
    }

    entity.setStatus(target.getCode());
    applyCompletionFields(entity, target, request);
    maintenanceRecordMapper.updateById(entity);
    // 完成维修时由用户决定是否把设备同步为已维修；维修中则自动同步。
    if (shouldSyncDeviceRepaired(target, request)) {
      syncDeviceStatus(familyId, entity.getDeviceId(), DeviceStatus.REPAIRED);
    }
    if (target == MaintenanceStatus.REPAIRING) {
      syncDeviceStatus(familyId, entity.getDeviceId(), DeviceStatus.REPAIRING);
    }
    return toResponse(entity);
  }

  @Override
  @Transactional
  public boolean deleteMaintenance(Long userId, Long familyId, Long maintenanceId) {
    familyService.checkFamilyMember(userId, familyId);
    MaintenanceRecordEntity entity = getMaintenance(familyId, maintenanceId);
    return maintenanceRecordMapper.deleteById(entity.getId()) > 0;
  }

  @Override
  public MaintenanceCostSummaryResponse costSummary(
      Long userId,
      Long familyId,
      LocalDate startDate,
      LocalDate endDate
  ) {
    familyService.checkFamilyMember(userId, familyId);
    LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
    LocalDateTime end = endDate == null ? null : endDate.plusDays(1).atStartOfDay();
    LambdaQueryWrapper<MaintenanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
    // 费用统计排除已取消维修，只统计有实际费用的记录。
    wrapper.eq(MaintenanceRecordEntity::getFamilyId, familyId)
        .ne(MaintenanceRecordEntity::getStatus, MaintenanceStatus.CANCELED.getCode())
        .isNotNull(MaintenanceRecordEntity::getRepairCost);
    if (start != null) {
      wrapper.ge(MaintenanceRecordEntity::getCompletedAt, start);
    }
    if (end != null) {
      wrapper.lt(MaintenanceRecordEntity::getCompletedAt, end);
    }
    BigDecimal total = BigDecimal.ZERO;
    long count = 0;
    for (MaintenanceRecordEntity record : maintenanceRecordMapper.selectList(wrapper)) {
      total = total.add(record.getRepairCost());
      count++;
    }
    return new MaintenanceCostSummaryResponse(total, count);
  }

  private LambdaQueryWrapper<MaintenanceRecordEntity> buildPageWrapper(
      Long familyId,
      Long deviceId,
      MaintenanceStatus status
  ) {
    LambdaQueryWrapper<MaintenanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
    // 列表查询先按家庭空间隔离，再叠加设备和状态筛选条件。
    wrapper.eq(MaintenanceRecordEntity::getFamilyId, familyId);
    if (deviceId != null) {
      wrapper.eq(MaintenanceRecordEntity::getDeviceId, deviceId);
    }
    if (status != null) {
      wrapper.eq(MaintenanceRecordEntity::getStatus, status.getCode());
    }
    return wrapper.orderByDesc(MaintenanceRecordEntity::getOccurredAt);
  }

  private boolean shouldSyncDeviceRepaired(
      MaintenanceStatus target,
      UpdateMaintenanceStatusRequest request
  ) {
    return target == MaintenanceStatus.COMPLETED
        && Boolean.TRUE.equals(request.syncDeviceRepaired());
  }

  private void applyCompletionFields(
      MaintenanceRecordEntity entity,
      MaintenanceStatus target,
      UpdateMaintenanceStatusRequest request
  ) {
    // 完成时间允许前端指定；未指定时用服务端时间兜底。
    if (request.repairCost() != null) {
      entity.setRepairCost(request.repairCost());
    }
    if (StringUtils.hasText(request.resultDescription())) {
      entity.setResultDescription(request.resultDescription());
    }
    if (target == MaintenanceStatus.COMPLETED) {
      entity.setCompletedAt(request.completedAt() == null
          ? LocalDateTime.now()
          : request.completedAt());
    }
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

  private MaintenanceRecordEntity getMaintenance(Long familyId, Long maintenanceId) {
    MaintenanceRecordEntity entity = maintenanceRecordMapper.selectOne(
        new LambdaQueryWrapper<MaintenanceRecordEntity>()
            .eq(MaintenanceRecordEntity::getId, maintenanceId)
            .eq(MaintenanceRecordEntity::getFamilyId, familyId)
    );
    if (entity == null) {
      throw new BusinessException(ErrorCode.MAINTENANCE_NOT_FOUND, "维修记录不存在");
    }
    return entity;
  }

  private void syncDeviceStatus(Long familyId, Long deviceId, DeviceStatus status) {
    DeviceAssetEntity device = getDevice(familyId, deviceId);
    // 报废是设备终态，维修流程不能把它恢复为其他状态。
    if (DeviceStatus.SCRAPPED.getCode().equals(device.getStatus())) {
      return;
    }
    device.setStatus(status.getCode());
    deviceAssetMapper.updateById(device);
  }

  private MaintenanceStatus resolveStatusIfPresent(String status) {
    if (!StringUtils.hasText(status)) {
      return null;
    }
    MaintenanceStatus maintenanceStatus = MaintenanceStatus.fromCode(status);
    if (maintenanceStatus == null) {
      throw new BusinessException(ErrorCode.MAINTENANCE_STATUS_INVALID, "维修状态无效");
    }
    return maintenanceStatus;
  }

  private boolean isTerminal(String status) {
    return MaintenanceStatus.COMPLETED.getCode().equals(status)
        || MaintenanceStatus.CANCELED.getCode().equals(status);
  }

  private LocalDateTime resolveOccurredAt(LocalDateTime occurredAt) {
    return occurredAt == null ? LocalDateTime.now() : occurredAt;
  }

  private MaintenanceResponse toResponse(MaintenanceRecordEntity entity) {
    DeviceAssetEntity device = deviceAssetMapper.selectById(entity.getDeviceId());
    return new MaintenanceResponse(
        entity.getId(),
        entity.getDeviceId(),
        device == null ? null : device.getName(),
        entity.getTitle(),
        entity.getFaultDescription(),
        entity.getOccurredAt(),
        entity.getStatus(),
        entity.getRepairChannel(),
        entity.getRepairContact(),
        entity.getRepairCost(),
        entity.getResultDescription(),
        entity.getCompletedAt()
    );
  }
}


