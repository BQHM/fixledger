package com.fixledger.modules.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.UpdateDeviceCategoryRequest;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import com.fixledger.modules.family.service.FamilyService;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceCategoryServiceImpl implements DeviceCategoryService {

  private final DeviceCategoryMapper deviceCategoryMapper;
  private final DeviceAssetMapper deviceAssetMapper;
  private final FamilyService familyService;

  public DeviceCategoryServiceImpl(
      DeviceCategoryMapper deviceCategoryMapper,
      @Lazy DeviceAssetMapper deviceAssetMapper,
      FamilyService familyService
  ) {
    this.deviceCategoryMapper = deviceCategoryMapper;
    this.deviceAssetMapper = deviceAssetMapper;
    this.familyService = familyService;
  }

  @Override
  public List<DeviceCategoryResponse> listCategories(Long userId, Long familyId) {
    familyService.checkFamilyMember(userId, familyId);
    return deviceCategoryMapper.selectList(new LambdaQueryWrapper<DeviceCategoryEntity>()
            .eq(DeviceCategoryEntity::getFamilyId, familyId)
            .orderByAsc(DeviceCategoryEntity::getSortOrder)
            .orderByAsc(DeviceCategoryEntity::getId))
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public DeviceCategoryResponse createCategory(
      Long userId,
      Long familyId,
      CreateDeviceCategoryRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    ensureNameAvailable(familyId, request.name(), null);

    DeviceCategoryEntity category = new DeviceCategoryEntity();
    category.setFamilyId(familyId);
    category.setName(request.name());
    category.setIcon(request.icon());
    category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    category.setSystemDefault(false);
    deviceCategoryMapper.insert(category);
    return toResponse(category);
  }

  @Override
  @Transactional
  public DeviceCategoryResponse updateCategory(
      Long userId,
      Long familyId,
      Long categoryId,
      UpdateDeviceCategoryRequest request
  ) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceCategoryEntity category = getCategory(familyId, categoryId);
    ensureNameAvailable(familyId, request.name(), categoryId);

    category.setName(request.name());
    category.setIcon(request.icon());
    category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    deviceCategoryMapper.updateById(category);
    return toResponse(category);
  }

  @Override
  @Transactional
  public boolean deleteCategory(Long userId, Long familyId, Long categoryId) {
    familyService.checkFamilyMember(userId, familyId);
    DeviceCategoryEntity category = getCategory(familyId, categoryId);
    if (Boolean.TRUE.equals(category.getSystemDefault())) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "系统默认分类不允许删除");
    }

    // 分类删除前确认没有设备引用，避免设备列表出现悬空分类。
    Long deviceCount = deviceAssetMapper.selectCount(new LambdaQueryWrapper<DeviceAssetEntity>()
        .eq(DeviceAssetEntity::getFamilyId, familyId)
        .eq(DeviceAssetEntity::getCategoryId, categoryId));
    if (deviceCount > 0) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "分类下存在设备，不能删除");
    }
    return deviceCategoryMapper.deleteById(categoryId) > 0;
  }

  private DeviceCategoryEntity getCategory(Long familyId, Long categoryId) {
    DeviceCategoryEntity category = deviceCategoryMapper.selectOne(
        new LambdaQueryWrapper<DeviceCategoryEntity>()
            .eq(DeviceCategoryEntity::getId, categoryId)
            .eq(DeviceCategoryEntity::getFamilyId, familyId)
    );
    if (category == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "设备分类不存在");
    }
    return category;
  }

  private void ensureNameAvailable(Long familyId, String name, Long excludeId) {
    LambdaQueryWrapper<DeviceCategoryEntity> wrapper =
        new LambdaQueryWrapper<DeviceCategoryEntity>()
            .eq(DeviceCategoryEntity::getFamilyId, familyId)
            .eq(DeviceCategoryEntity::getName, name);
    if (excludeId != null) {
      wrapper.ne(DeviceCategoryEntity::getId, excludeId);
    }
    Long count = deviceCategoryMapper.selectCount(wrapper);
    if (count > 0) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "设备分类名称已存在");
    }
  }

  private DeviceCategoryResponse toResponse(DeviceCategoryEntity category) {
    return new DeviceCategoryResponse(
        category.getId(),
        category.getName(),
        category.getIcon(),
        category.getSortOrder(),
        category.getSystemDefault()
    );
  }
}


