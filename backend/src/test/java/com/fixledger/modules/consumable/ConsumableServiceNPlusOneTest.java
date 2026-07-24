package com.fixledger.modules.consumable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fixledger.common.cache.DashboardCacheInvalidator;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.consumable.entity.ConsumableItemEntity;
import com.fixledger.modules.consumable.enums.ConsumableStatus;
import com.fixledger.modules.consumable.mapper.ConsumableItemMapper;
import com.fixledger.modules.consumable.mapper.ConsumableReplaceRecordMapper;
import com.fixledger.modules.consumable.query.ConsumableDueSoonQuery;
import com.fixledger.modules.consumable.response.ConsumableResponse;
import com.fixledger.modules.consumable.service.ConsumableServiceImpl;
import com.fixledger.modules.family.service.FamilyService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsumableServiceNPlusOneTest {

  @Test
  @DisplayName("即将更换分页批量加载设备名称，避免按耗材逐条查询设备")
  void pageDueSoonBatchLoadsDeviceNames() {
    ConsumableItemMapper consumableItemMapper = mock(ConsumableItemMapper.class);
    ConsumableReplaceRecordMapper replaceRecordMapper = mock(ConsumableReplaceRecordMapper.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    ConsumableServiceImpl service = new ConsumableServiceImpl(
        consumableItemMapper,
        replaceRecordMapper,
        deviceAssetMapper,
        familyService,
        mock(DashboardCacheInvalidator.class)
    );
    ConsumableDueSoonQuery query = new ConsumableDueSoonQuery();
    query.setPageSize(2);
    ConsumableItemEntity first = consumable(1L, 10L, "PP 棉滤芯");
    ConsumableItemEntity second = consumable(2L, 11L, "活性炭滤芯");
    IPage<ConsumableItemEntity> page = Page.of(1, 2, 2);
    page.setRecords(List.of(first, second));

    when(consumableItemMapper.selectPage(any(), any())).thenReturn(page);
    when(deviceAssetMapper.selectList(any(LambdaQueryWrapper.class)))
        .thenReturn(List.of(device(10L, "净水器"), device(11L, "路由器")));

    PageResponse<ConsumableResponse> response = service.pageDueSoon(100L, 200L, query);

    assertThat(response.records()).extracting(ConsumableResponse::deviceName)
        .containsExactly("净水器", "路由器");
    verify(deviceAssetMapper).selectList(any(LambdaQueryWrapper.class));
    verify(deviceAssetMapper, never()).selectById(10L);
    verify(deviceAssetMapper, never()).selectById(11L);
  }

  private ConsumableItemEntity consumable(Long id, Long deviceId, String name) {
    ConsumableItemEntity consumable = new ConsumableItemEntity();
    consumable.setId(id);
    consumable.setFamilyId(200L);
    consumable.setDeviceId(deviceId);
    consumable.setName(name);
    consumable.setCycleDays(180);
    consumable.setLastReplacedDate(LocalDate.now().minusDays(175));
    consumable.setNextRemindDate(LocalDate.now().plusDays(5));
    consumable.setRemindDaysBefore(7);
    consumable.setEnabled(true);
    consumable.setStatus(ConsumableStatus.NORMAL.getCode());
    return consumable;
  }

  private DeviceAssetEntity device(Long id, String name) {
    DeviceAssetEntity device = new DeviceAssetEntity();
    device.setId(id);
    device.setName(name);
    return device;
  }
}
