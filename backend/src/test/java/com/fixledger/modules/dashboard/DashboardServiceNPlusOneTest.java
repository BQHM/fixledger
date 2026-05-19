package com.fixledger.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fixledger.infrastructure.redis.RedisService;
import com.fixledger.modules.asset.entity.DeviceCategoryEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.consumable.mapper.ConsumableItemMapper;
import com.fixledger.modules.dashboard.response.DeviceCategoryDistributionResponse;
import com.fixledger.modules.dashboard.service.DashboardServiceImpl;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import com.fixledger.modules.reminder.mapper.ReminderTaskMapper;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DashboardServiceNPlusOneTest {

  @Test
  @DisplayName("设备分类分布使用分组统计，避免按分类逐条 count")
  void deviceCategoryDistributionUsesGroupedCount() {
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    DeviceCategoryMapper deviceCategoryMapper = mock(DeviceCategoryMapper.class);
    WarrantyRecordMapper warrantyRecordMapper = mock(WarrantyRecordMapper.class);
    ConsumableItemMapper consumableItemMapper = mock(ConsumableItemMapper.class);
    MaintenanceRecordMapper maintenanceRecordMapper = mock(MaintenanceRecordMapper.class);
    ReminderTaskMapper reminderTaskMapper = mock(ReminderTaskMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    RedisService redisService = mock(RedisService.class);
    DashboardServiceImpl service = new DashboardServiceImpl(
        deviceAssetMapper,
        deviceCategoryMapper,
        warrantyRecordMapper,
        consumableItemMapper,
        maintenanceRecordMapper,
        reminderTaskMapper,
        familyService,
        redisService
    );

    when(deviceCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
        .thenReturn(List.of(category(10L, "厨房设备"), category(11L, "网络设备")));
    when(deviceAssetMapper.selectMaps(any(QueryWrapper.class)))
        .thenReturn(List.of(
            Map.of("categoryId", 10L, "deviceCount", 2L),
            Map.of("categoryId", 11L, "deviceCount", 1L)
        ));

    List<DeviceCategoryDistributionResponse> response = service.deviceCategoryDistribution(
        100L,
        200L
    );

    assertThat(response).extracting(DeviceCategoryDistributionResponse::count)
        .containsExactly(2L, 1L);
    verify(deviceAssetMapper).selectMaps(any(QueryWrapper.class));
    verify(deviceAssetMapper, times(0)).selectCount(any(LambdaQueryWrapper.class));
  }

  private DeviceCategoryEntity category(Long id, String name) {
    DeviceCategoryEntity category = new DeviceCategoryEntity();
    category.setId(id);
    category.setFamilyId(200L);
    category.setName(name);
    category.setSortOrder(1);
    return category;
  }
}
