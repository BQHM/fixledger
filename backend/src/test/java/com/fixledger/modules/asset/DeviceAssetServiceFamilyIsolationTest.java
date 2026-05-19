package com.fixledger.modules.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.enums.DeviceStatus;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.asset.response.DeviceDetailResponse;
import com.fixledger.modules.asset.service.DeviceAssetServiceImpl;
import com.fixledger.modules.family.service.FamilyService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeviceAssetServiceFamilyIsolationTest {

  @Test
  @DisplayName("设备详情按家庭空间回查分类，避免异常外键泄露其他家庭分类名")
  void deviceDetailLooksUpCategoryWithinFamily() {
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    DeviceCategoryMapper deviceCategoryMapper = mock(DeviceCategoryMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    DeviceAssetServiceImpl service = new DeviceAssetServiceImpl(
        deviceAssetMapper,
        deviceCategoryMapper,
        familyService
    );
    DeviceAssetEntity device = device(1L, 999L);
    DeviceCategoryEntity otherFamilyCategory = new DeviceCategoryEntity();
    otherFamilyCategory.setId(999L);
    otherFamilyCategory.setFamilyId(300L);
    otherFamilyCategory.setName("其他家庭分类");

    when(deviceAssetMapper.selectOne(any())).thenReturn(device);
    when(deviceCategoryMapper.selectOne(any())).thenReturn(null);
    when(deviceCategoryMapper.selectById(999L)).thenReturn(otherFamilyCategory);

    DeviceDetailResponse response = service.getDeviceDetail(100L, 200L, 1L);

    assertThat(response.categoryName()).isNull();
    verify(deviceCategoryMapper, never()).selectById(999L);
  }

  private DeviceAssetEntity device(Long id, Long categoryId) {
    DeviceAssetEntity device = new DeviceAssetEntity();
    device.setId(id);
    device.setFamilyId(200L);
    device.setCategoryId(categoryId);
    device.setName("小米净水器");
    device.setPurchaseDate(LocalDate.of(2026, 5, 17));
    device.setStatus(DeviceStatus.NORMAL.getCode());
    return device;
  }
}
