package com.fixledger.modules.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fixledger.common.cache.DashboardCacheInvalidator;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.enums.DeviceStatus;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.asset.query.DevicePageQuery;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.request.UpdateDeviceRequest;
import com.fixledger.modules.asset.request.UpdateDeviceStatusRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceDetailResponse;
import com.fixledger.modules.asset.response.DeviceListResponse;
import com.fixledger.modules.family.service.FamilyService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 文件功能说明：设备档案服务实现，负责业务编排、事务边界、状态校验和持久化调用。
 * </p>
 *
 * @Author FixLedger
 */
@Service
public class DeviceAssetServiceImpl implements DeviceAssetService {

  private final DeviceAssetMapper deviceAssetMapper;
  private final DeviceCategoryMapper deviceCategoryMapper;
  private final FamilyService familyService;
  private final DashboardCacheInvalidator dashboardCacheInvalidator;

  public DeviceAssetServiceImpl(
      DeviceAssetMapper deviceAssetMapper,
      DeviceCategoryMapper deviceCategoryMapper,
      FamilyService familyService,
      DashboardCacheInvalidator dashboardCacheInvalidator
  ) {
    this.deviceAssetMapper = deviceAssetMapper;
    this.deviceCategoryMapper = deviceCategoryMapper;
    this.familyService = familyService;
    this.dashboardCacheInvalidator = dashboardCacheInvalidator;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现设备档案分页查询业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @Override
  public PageResponse<DeviceListResponse> pageDevices(
      Long userId,
      Long familyId,
      DevicePageQuery query
  ) {
    familyService.checkFamilyMember(userId, familyId);
    // 查询入口先校验状态枚举，避免无效条件落到数据库层。
    validateStatusIfPresent(query.getStatus());

    IPage<DeviceAssetEntity> page = deviceAssetMapper.selectPage(
        query.toPage(),
        buildPageWrapper(familyId, query)
    );
    Map<Long, String> categoryNames = listCategoryNames(familyId, page.getRecords());
    IPage<DeviceListResponse> responsePage = page.convert(device ->
        toListResponse(device, categoryNames));
    return PageResponse.from(responsePage);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现设备档案创建业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @Override
  @Transactional
  public CreateDeviceResponse createDevice(
      Long userId,
      Long familyId,
      CreateDeviceRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    // 分类必须属于当前家庭空间，防止跨家庭挂载设备。
    validateCategory(familyId, request.categoryId());

    DeviceAssetEntity device = new DeviceAssetEntity();
    applyCreateRequest(device, familyId, request);
    deviceAssetMapper.insert(device);
    dashboardCacheInvalidator.evictAfterCommit(familyId);
    return new CreateDeviceResponse(device.getId());
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现设备档案查询业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 查询结果
   */
  @Override
  public DeviceDetailResponse getDeviceDetail(Long userId, Long familyId, Long deviceId) {
    familyService.checkFamilyMember(userId, familyId);
    return toDetailResponse(getDevice(familyId, deviceId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现设备档案更新业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @Override
  @Transactional
  public DeviceDetailResponse updateDevice(
      Long userId,
      Long familyId,
      Long deviceId,
      UpdateDeviceRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    validateCategory(familyId, request.categoryId());
    DeviceAssetEntity device = getDevice(familyId, deviceId);

    applyUpdateRequest(device, request);
    deviceAssetMapper.updateById(device);
    dashboardCacheInvalidator.evictAfterCommit(familyId);
    return toDetailResponse(device);
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现设备档案删除业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 是否处理成功
   */
  @Override
  @Transactional
  public boolean deleteDevice(Long userId, Long familyId, Long deviceId) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceAssetEntity device = getDevice(familyId, deviceId);
    // BaseEntity 配置了 @TableLogic，这里会转为逻辑删除而不是物理删除。
    boolean deleted = deviceAssetMapper.deleteById(device.getId()) > 0;
    if (deleted) {
      dashboardCacheInvalidator.evictAfterCommit(familyId);
    }
    return deleted;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：实现设备档案更新业务逻辑。
   * </p>
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @Override
  @Transactional
  public DeviceDetailResponse updateDeviceStatus(
      Long userId,
      Long familyId,
      Long deviceId,
      UpdateDeviceStatusRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceStatus targetStatus = DeviceStatus.fromCode(request.status());
    if (targetStatus == null) {
      throw new BusinessException(ErrorCode.DEVICE_STATUS_INVALID, "设备状态无效");
    }

    DeviceAssetEntity device = getDevice(familyId, deviceId);
    // 状态只能通过受控方法流转，当前禁止报废设备恢复为其他状态。
    validateStatusTransition(device.getStatus(), targetStatus);
    device.setStatus(targetStatus.getCode());
    deviceAssetMapper.updateById(device);
    dashboardCacheInvalidator.evictAfterCommit(familyId);
    return toDetailResponse(device);
  }

  private LambdaQueryWrapper<DeviceAssetEntity> buildPageWrapper(
      Long familyId,
      DevicePageQuery query
  ) {
    LambdaQueryWrapper<DeviceAssetEntity> wrapper = new LambdaQueryWrapper<DeviceAssetEntity>()
        .eq(DeviceAssetEntity::getFamilyId, familyId);
    if (StringUtils.hasText(query.getKeyword())) {
      wrapper.and(item -> item
          .like(DeviceAssetEntity::getName, query.getKeyword())
          .or()
          .like(DeviceAssetEntity::getBrand, query.getKeyword())
          .or()
          .like(DeviceAssetEntity::getModel, query.getKeyword()));
    }
    if (query.getCategoryId() != null) {
      wrapper.eq(DeviceAssetEntity::getCategoryId, query.getCategoryId());
    }
    if (StringUtils.hasText(query.getStatus())) {
      wrapper.eq(DeviceAssetEntity::getStatus, query.getStatus());
    }
    if (StringUtils.hasText(query.getBrand())) {
      wrapper.eq(DeviceAssetEntity::getBrand, query.getBrand());
    }
    return wrapper.orderByDesc(DeviceAssetEntity::getUpdatedAt);
  }

  private void applyCreateRequest(
      DeviceAssetEntity device,
      Long familyId,
      CreateDeviceRequest request
  ) {
    device.setFamilyId(familyId);
    device.setCategoryId(request.categoryId());
    device.setName(request.name());
    device.setBrand(request.brand());
    device.setModel(request.model());
    device.setSerialNumber(request.serialNumber());
    device.setPurchaseDate(request.purchaseDate());
    device.setPurchaseChannel(request.purchaseChannel());
    device.setPurchasePrice(request.purchasePrice());
    device.setLocation(request.location());
    device.setStatus(DeviceStatus.NORMAL.getCode());
    device.setRemark(request.remark());
  }

  private void applyUpdateRequest(DeviceAssetEntity device, UpdateDeviceRequest request) {
    device.setCategoryId(request.categoryId());
    device.setName(request.name());
    device.setBrand(request.brand());
    device.setModel(request.model());
    device.setSerialNumber(request.serialNumber());
    device.setPurchaseDate(request.purchaseDate());
    device.setPurchaseChannel(request.purchaseChannel());
    device.setPurchasePrice(request.purchasePrice());
    device.setLocation(request.location());
    device.setRemark(request.remark());
  }

  private void validateCategory(Long familyId, Long categoryId) {
    if (categoryId == null) {
      return;
    }
    Long count = deviceCategoryMapper.selectCount(
        new LambdaQueryWrapper<DeviceCategoryEntity>()
            .eq(DeviceCategoryEntity::getId, categoryId)
            .eq(DeviceCategoryEntity::getFamilyId, familyId)
    );
    if (count == 0) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "设备分类不存在");
    }
  }

  private void validateStatusIfPresent(String status) {
    if (StringUtils.hasText(status) && DeviceStatus.fromCode(status) == null) {
      throw new BusinessException(ErrorCode.DEVICE_STATUS_INVALID, "设备状态无效");
    }
  }

  private void validateStatusTransition(String currentStatus, DeviceStatus targetStatus) {
    if (DeviceStatus.SCRAPPED.getCode().equals(currentStatus)
        && targetStatus != DeviceStatus.SCRAPPED) {
      throw new BusinessException(ErrorCode.DEVICE_STATUS_INVALID, "已报废设备不能恢复状态");
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

  private Map<Long, String> listCategoryNames(Long familyId, List<DeviceAssetEntity> devices) {
    Set<Long> categoryIds = devices.stream()
        .map(DeviceAssetEntity::getCategoryId)
        .filter(id -> id != null)
        .collect(Collectors.toSet());
    if (categoryIds.isEmpty()) {
      return Map.of();
    }
    return deviceCategoryMapper.selectList(new LambdaQueryWrapper<DeviceCategoryEntity>()
            .eq(DeviceCategoryEntity::getFamilyId, familyId)
            .in(DeviceCategoryEntity::getId, categoryIds))
        .stream()
        .collect(Collectors.toMap(
            DeviceCategoryEntity::getId,
            DeviceCategoryEntity::getName,
            (left, right) -> left
        ));
  }

  private DeviceListResponse toListResponse(
      DeviceAssetEntity device,
      Map<Long, String> categoryNames
  ) {
    return new DeviceListResponse(
        device.getId(),
        device.getName(),
        device.getBrand(),
        device.getModel(),
        categoryNames.get(device.getCategoryId()),
        device.getPurchaseDate(),
        device.getPurchasePrice(),
        device.getLocation(),
        device.getStatus(),
        null,
        null
    );
  }

  private DeviceDetailResponse toDetailResponse(DeviceAssetEntity device) {
    return new DeviceDetailResponse(
        device.getId(),
        device.getCategoryId(),
        getCategoryName(device.getFamilyId(), device.getCategoryId()),
        device.getName(),
        device.getBrand(),
        device.getModel(),
        device.getSerialNumber(),
        device.getStatus(),
        device.getPurchaseDate(),
        device.getPurchaseChannel(),
        device.getPurchasePrice(),
        device.getLocation(),
        device.getRemark(),
        List.of(),
        List.of(),
        List.of(),
        List.of()
    );
  }

  private String getCategoryName(Long familyId, Long categoryId) {
    if (categoryId == null) {
      return null;
    }
    DeviceCategoryEntity category = deviceCategoryMapper.selectOne(
        new LambdaQueryWrapper<DeviceCategoryEntity>()
            .eq(DeviceCategoryEntity::getId, categoryId)
            .eq(DeviceCategoryEntity::getFamilyId, familyId)
    );
    return category == null ? null : category.getName();
  }
}
