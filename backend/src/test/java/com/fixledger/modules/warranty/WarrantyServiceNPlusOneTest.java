package com.fixledger.modules.warranty;

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
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.warranty.entity.WarrantyRecordEntity;
import com.fixledger.modules.warranty.mapper.WarrantyRecordMapper;
import com.fixledger.modules.warranty.query.WarrantyExpiringQuery;
import com.fixledger.modules.warranty.response.WarrantyResponse;
import com.fixledger.modules.warranty.service.WarrantyServiceImpl;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WarrantyServiceNPlusOneTest {

  @Test
  @DisplayName("即将过保分页批量加载设备名称，避免按保修记录逐条查询设备")
  void pageExpiringBatchLoadsDeviceNames() {
    WarrantyRecordMapper warrantyRecordMapper = mock(WarrantyRecordMapper.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    WarrantyServiceImpl service = new WarrantyServiceImpl(
        warrantyRecordMapper,
        deviceAssetMapper,
        familyService
    );
    WarrantyExpiringQuery query = new WarrantyExpiringQuery();
    query.setPageSize(2);
    WarrantyRecordEntity first = warranty(1L, 10L);
    WarrantyRecordEntity second = warranty(2L, 11L);
    IPage<WarrantyRecordEntity> page = Page.of(1, 2, 2);
    page.setRecords(List.of(first, second));

    when(warrantyRecordMapper.selectPage(any(), any())).thenReturn(page);
    when(deviceAssetMapper.selectList(any(LambdaQueryWrapper.class)))
        .thenReturn(List.of(device(10L, "净水器"), device(11L, "路由器")));

    PageResponse<WarrantyResponse> response = service.pageExpiring(100L, 200L, query);

    assertThat(response.records()).extracting(WarrantyResponse::deviceName)
        .containsExactly("净水器", "路由器");
    verify(deviceAssetMapper).selectList(any(LambdaQueryWrapper.class));
    verify(deviceAssetMapper, never()).selectById(10L);
    verify(deviceAssetMapper, never()).selectById(11L);
  }

  private WarrantyRecordEntity warranty(Long id, Long deviceId) {
    WarrantyRecordEntity warranty = new WarrantyRecordEntity();
    warranty.setId(id);
    warranty.setFamilyId(200L);
    warranty.setDeviceId(deviceId);
    warranty.setWarrantyType("OFFICIAL");
    warranty.setStartDate(LocalDate.now().minusYears(1));
    warranty.setEndDate(LocalDate.now().plusDays(10));
    warranty.setRemindDaysBefore(30);
    return warranty;
  }

  private DeviceAssetEntity device(Long id, String name) {
    DeviceAssetEntity device = new DeviceAssetEntity();
    device.setId(id);
    device.setName(name);
    return device;
  }
}
