package com.fixledger.modules.maintenance;

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
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.entity.MaintenanceRecordEntity;
import com.fixledger.modules.maintenance.enums.MaintenanceStatus;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import com.fixledger.modules.maintenance.query.MaintenancePageQuery;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import com.fixledger.modules.maintenance.service.MaintenanceServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MaintenanceServiceNPlusOneTest {

  @Test
  @DisplayName("维修分页批量加载设备名称，避免按维修记录逐条查询设备")
  void pageMaintenanceBatchLoadsDeviceNames() {
    MaintenanceRecordMapper maintenanceRecordMapper = mock(MaintenanceRecordMapper.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    MaintenanceServiceImpl service = new MaintenanceServiceImpl(
        maintenanceRecordMapper,
        deviceAssetMapper,
        familyService,
        mock(DashboardCacheInvalidator.class)
    );
    MaintenancePageQuery query = new MaintenancePageQuery();
    query.setPageSize(2);
    MaintenanceRecordEntity first = maintenance(1L, 10L, "出水变慢");
    MaintenanceRecordEntity second = maintenance(2L, 11L, "网络断连");
    IPage<MaintenanceRecordEntity> page = Page.of(1, 2, 2);
    page.setRecords(List.of(first, second));

    when(maintenanceRecordMapper.selectPage(any(), any())).thenReturn(page);
    when(deviceAssetMapper.selectList(any(LambdaQueryWrapper.class)))
        .thenReturn(List.of(device(10L, "净水器"), device(11L, "路由器")));

    PageResponse<MaintenanceResponse> response = service.pageMaintenance(100L, 200L, query);

    assertThat(response.records()).extracting(MaintenanceResponse::deviceName)
        .containsExactly("净水器", "路由器");
    verify(deviceAssetMapper).selectList(any(LambdaQueryWrapper.class));
    verify(deviceAssetMapper, never()).selectById(10L);
    verify(deviceAssetMapper, never()).selectById(11L);
  }

  private MaintenanceRecordEntity maintenance(Long id, Long deviceId, String title) {
    MaintenanceRecordEntity maintenance = new MaintenanceRecordEntity();
    maintenance.setId(id);
    maintenance.setFamilyId(200L);
    maintenance.setDeviceId(deviceId);
    maintenance.setTitle(title);
    maintenance.setFaultDescription(title);
    maintenance.setOccurredAt(LocalDateTime.of(2026, 5, 15, 10, 0));
    maintenance.setStatus(MaintenanceStatus.PENDING.getCode());
    return maintenance;
  }

  private DeviceAssetEntity device(Long id, String name) {
    DeviceAssetEntity device = new DeviceAssetEntity();
    device.setId(id);
    device.setFamilyId(200L);
    device.setName(name);
    return device;
  }
}
