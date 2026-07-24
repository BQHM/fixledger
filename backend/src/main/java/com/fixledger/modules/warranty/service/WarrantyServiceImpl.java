package com.fixledger.modules.warranty.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fixledger.common.cache.DashboardCacheInvalidator;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.warranty.entity.WarrantyRecordEntity;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import com.fixledger.modules.warranty.query.WarrantyExpiringQuery;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.request.UpdateWarrantyRequest;
import com.fixledger.modules.warranty.response.WarrantyResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 文件功能说明：保修服务实现，负责业务编排、事务边界、状态校验和持久化调用。
 * </p>
 *
 * @Author FixLedger
 */
@Service
public class WarrantyServiceImpl implements WarrantyService {

  private static final int DEFAULT_REMIND_DAYS_BEFORE = 30;

  private final WarrantyRecordMapper warrantyRecordMapper;
  private final DeviceAssetMapper deviceAssetMapper;
  private final FamilyService familyService;
  private final DashboardCacheInvalidator dashboardCacheInvalidator;

  public WarrantyServiceImpl(
      WarrantyRecordMapper warrantyRecordMapper,
      DeviceAssetMapper deviceAssetMapper,
      FamilyService familyService,
      DashboardCacheInvalidator dashboardCacheInvalidator
  ) {
    this.warrantyRecordMapper = warrantyRecordMapper;
    this.deviceAssetMapper = deviceAssetMapper;
    this.familyService = familyService;
    this.dashboardCacheInvalidator = dashboardCacheInvalidator;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现保修查询列表业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 列表结果
   */
  @Override
  public List<WarrantyResponse> listDeviceWarranties(
      Long userId,
      Long familyId,
      Long deviceId
  ) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceAssetEntity device = getDevice(familyId, deviceId);
    List<WarrantyRecordEntity> warranties = warrantyRecordMapper.selectList(
        new LambdaQueryWrapper<WarrantyRecordEntity>()
            .eq(WarrantyRecordEntity::getFamilyId, familyId)
            .eq(WarrantyRecordEntity::getDeviceId, deviceId)
            .orderByDesc(WarrantyRecordEntity::getEndDate)
    );
    return warranties.stream()
        .map(warranty -> toResponse(warranty, device.getName()))
        .toList();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现保修创建业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @Override
  @Transactional
  public WarrantyResponse createWarranty(
      Long userId,
      Long familyId,
      Long deviceId,
      CreateWarrantyRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceAssetEntity device = getDevice(familyId, deviceId);
    validateWarrantyDates(device, request.startDate(), request.endDate());

    WarrantyRecordEntity warranty = new WarrantyRecordEntity();
    warranty.setFamilyId(familyId);
    warranty.setDeviceId(deviceId);
    warranty.setWarrantyType(resolveWarrantyType(request.warrantyType()).getCode());
    warranty.setStartDate(request.startDate());
    warranty.setEndDate(request.endDate());
    warranty.setRemindDaysBefore(resolveRemindDays(request.remindDaysBefore()));
    warranty.setServicePhone(request.servicePhone());
    warranty.setServiceAddress(request.serviceAddress());
    warranty.setServiceNote(request.serviceNote());
    warrantyRecordMapper.insert(warranty);
    dashboardCacheInvalidator.evictAfterCommit(familyId);
    return toResponse(warranty, device.getName());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现保修更新业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param warrantyId 保修记录 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @Override
  @Transactional
  public WarrantyResponse updateWarranty(
      Long userId,
      Long familyId,
      Long warrantyId,
      UpdateWarrantyRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    WarrantyRecordEntity warranty = getWarranty(familyId, warrantyId);
    DeviceAssetEntity device = getDevice(familyId, warranty.getDeviceId());
    validateWarrantyDates(device, request.startDate(), request.endDate());

    warranty.setWarrantyType(resolveWarrantyType(request.warrantyType()).getCode());
    warranty.setStartDate(request.startDate());
    warranty.setEndDate(request.endDate());
    warranty.setRemindDaysBefore(resolveRemindDays(request.remindDaysBefore()));
    warranty.setServicePhone(request.servicePhone());
    warranty.setServiceAddress(request.serviceAddress());
    warranty.setServiceNote(request.serviceNote());
    warrantyRecordMapper.updateById(warranty);
    dashboardCacheInvalidator.evictAfterCommit(familyId);
    return toResponse(warranty, device.getName());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现保修删除业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param warrantyId 保修记录 ID
   * @return 是否处理成功
   */
  @Override
  @Transactional
  public boolean deleteWarranty(Long userId, Long familyId, Long warrantyId) {
    familyService.checkFamilyMember(userId, familyId);
    WarrantyRecordEntity warranty = getWarranty(familyId, warrantyId);
    boolean deleted = warrantyRecordMapper.deleteById(warranty.getId()) > 0;
    if (deleted) {
      dashboardCacheInvalidator.evictAfterCommit(familyId);
    }
    return deleted;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现保修分页查询业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @Override
  public PageResponse<WarrantyResponse> pageExpiring(
      Long userId,
      Long familyId,
      WarrantyExpiringQuery query
  ) {
    familyService.checkFamilyMember(userId, familyId);
    // 即将过保窗口从今天开始计算，过期记录由提醒扫描单独处理。
    LocalDate today = LocalDate.now();
    LocalDate endDate = today.plusDays(query.getDays());
    IPage<WarrantyRecordEntity> page = warrantyRecordMapper.selectPage(
        query.toPage(),
        new LambdaQueryWrapper<WarrantyRecordEntity>()
            .eq(WarrantyRecordEntity::getFamilyId, familyId)
            .ge(WarrantyRecordEntity::getEndDate, today)
            .le(WarrantyRecordEntity::getEndDate, endDate)
            .orderByAsc(WarrantyRecordEntity::getEndDate)
    );
    Map<Long, String> deviceNames = listDeviceNames(familyId, page.getRecords());
    return PageResponse.from(page.convert(warranty -> toResponse(warranty, deviceNames)));
  }

  private void validateWarrantyDates(
      DeviceAssetEntity device,
      LocalDate startDate,
      LocalDate endDate
  ) {
    // 保修结束日期不能早于保修开始日期，也不能早于设备购买日期。
    if (endDate.isBefore(startDate)) {
      throw new BusinessException(ErrorCode.WARRANTY_DATE_INVALID, "保修结束日期不能早于开始日期");
    }
    if (device.getPurchaseDate() != null && endDate.isBefore(device.getPurchaseDate())) {
      throw new BusinessException(ErrorCode.WARRANTY_DATE_INVALID, "保修结束日期不能早于购买日期");
    }
  }

  private WarrantyType resolveWarrantyType(String warrantyType) {
    if (!StringUtils.hasText(warrantyType)) {
      return WarrantyType.OFFICIAL;
    }
    WarrantyType type = WarrantyType.fromCode(warrantyType);
    if (type == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "保修类型无效");
    }
    return type;
  }

  private int resolveRemindDays(Integer remindDaysBefore) {
    if (remindDaysBefore == null) {
      return DEFAULT_REMIND_DAYS_BEFORE;
    }
    if (remindDaysBefore < 0) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "提前提醒天数不能小于 0");
    }
    return remindDaysBefore;
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

  private WarrantyRecordEntity getWarranty(Long familyId, Long warrantyId) {
    WarrantyRecordEntity warranty = warrantyRecordMapper.selectOne(
        new LambdaQueryWrapper<WarrantyRecordEntity>()
            .eq(WarrantyRecordEntity::getId, warrantyId)
            .eq(WarrantyRecordEntity::getFamilyId, familyId)
    );
    if (warranty == null) {
      throw new BusinessException(ErrorCode.WARRANTY_NOT_FOUND, "保修记录不存在");
    }
    return warranty;
  }

  private Map<Long, String> listDeviceNames(Long familyId, List<WarrantyRecordEntity> warranties) {
    Set<Long> deviceIds = warranties.stream()
        .map(WarrantyRecordEntity::getDeviceId)
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

  private WarrantyResponse toResponse(
      WarrantyRecordEntity warranty,
      Map<Long, String> deviceNames
  ) {
    return toResponse(warranty, deviceNames.get(warranty.getDeviceId()));
  }

  private WarrantyResponse toResponse(WarrantyRecordEntity warranty, String deviceName) {
    return new WarrantyResponse(
        warranty.getId(),
        warranty.getDeviceId(),
        deviceName,
        warranty.getWarrantyType(),
        warranty.getStartDate(),
        warranty.getEndDate(),
        warranty.getRemindDaysBefore(),
        warranty.getServicePhone(),
        warranty.getServiceAddress(),
        warranty.getServiceNote()
    );
  }
}
