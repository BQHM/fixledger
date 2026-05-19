package com.fixledger.modules.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.asset.query.DevicePageQuery;
import com.fixledger.modules.asset.response.DeviceListResponse;
import com.fixledger.modules.asset.service.DeviceAssetServiceImpl;
import com.fixledger.modules.family.service.FamilyService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeviceAssetServiceNPlusOneTest {

  @Test
  @DisplayName("设备分页批量加载分类名称，避免按设备逐条查询分类")
  void pageDevicesBatchLoadsCategoryNames() {
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    DeviceCategoryMapper deviceCategoryMapper = mock(DeviceCategoryMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    DeviceAssetServiceImpl service = new DeviceAssetServiceImpl(
        deviceAssetMapper,
        deviceCategoryMapper,
        familyService
    );
    DevicePageQuery query = new DevicePageQuery();
    query.setPageSize(2);
    DeviceAssetEntity first = device(1L, 10L, "净水器");
    DeviceAssetEntity second = device(2L, 11L, "路由器");
    IPage<DeviceAssetEntity> page = Page.of(1, 2, 2);
    page.setRecords(List.of(first, second));

    when(deviceAssetMapper.selectPage(any(), any())).thenReturn(page);
    when(deviceCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
        .thenReturn(List.of(category(10L, "厨房设备"), category(11L, "网络设备")));

    PageResponse<DeviceListResponse> response = service.pageDevices(100L, 200L, query);

    assertThat(response.records()).extracting(DeviceListResponse::categoryName)
        .containsExactly("厨房设备", "网络设备");
    verify(deviceCategoryMapper).selectList(any(LambdaQueryWrapper.class));
    verify(deviceCategoryMapper, never()).selectById(10L);
    verify(deviceCategoryMapper, never()).selectById(11L);
  }

  private DeviceAssetEntity device(Long id, Long categoryId, String name) {
    DeviceAssetEntity device = new DeviceAssetEntity();
    device.setId(id);
    device.setFamilyId(200L);
    device.setCategoryId(categoryId);
    device.setName(name);
    device.setPurchaseDate(LocalDate.of(2026, 1, 1));
    device.setStatus("NORMAL");
    return device;
  }

  private DeviceCategoryEntity category(Long id, String name) {
    DeviceCategoryEntity category = new DeviceCategoryEntity();
    category.setId(id);
    category.setFamilyId(200L);
    category.setName(name);
    return category;
  }
}
