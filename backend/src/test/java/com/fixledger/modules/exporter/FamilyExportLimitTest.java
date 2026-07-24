package com.fixledger.modules.exporter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.asset.mapper.DeviceCategoryMapper;
import com.fixledger.modules.exporter.config.ExportProperties;
import com.fixledger.modules.exporter.service.FamilyExportServiceImpl;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FamilyExportLimitTest {

  @Test
  @DisplayName("设备数量超过同步上限时拒绝导出而不是静默截断")
  void rejectDeviceExportAboveSynchronousLimit() {
    FamilyService familyService = mock(FamilyService.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    DeviceCategoryMapper deviceCategoryMapper = mock(DeviceCategoryMapper.class);
    MaintenanceRecordMapper maintenanceRecordMapper = mock(MaintenanceRecordMapper.class);
    ExportProperties properties = new ExportProperties();
    properties.setMaxSyncRows(1);
    FamilyExportServiceImpl service = new FamilyExportServiceImpl(
        familyService,
        deviceAssetMapper,
        deviceCategoryMapper,
        maintenanceRecordMapper,
        properties,
        new SimpleMeterRegistry()
    );
    List<DeviceAssetEntity> devices = LongStream.rangeClosed(1, 2)
        .mapToObj(this::device)
        .toList();
    Page<DeviceAssetEntity> page = Page.of(1, 2, false);
    page.setRecords(devices);
    when(deviceAssetMapper.selectPage(any(), any())).thenReturn(page);

    assertThatThrownBy(() -> service.exportDevices(100L, 200L))
        .isInstanceOfSatisfying(BusinessException.class, exception ->
            org.assertj.core.api.Assertions.assertThat(exception.getMessage())
                .contains("1"));
  }

  private DeviceAssetEntity device(long id) {
    DeviceAssetEntity device = new DeviceAssetEntity();
    device.setId(id);
    device.setFamilyId(200L);
    device.setName("设备" + id);
    return device;
  }
}
