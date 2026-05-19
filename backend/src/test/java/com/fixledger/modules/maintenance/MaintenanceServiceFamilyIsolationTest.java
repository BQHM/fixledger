package com.fixledger.modules.maintenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fixledger.modules.asset.entity.DeviceAssetEntity;
import com.fixledger.modules.asset.mapper.DeviceAssetMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.entity.MaintenanceRecordEntity;
import com.fixledger.modules.maintenance.enums.MaintenanceStatus;
import com.fixledger.modules.maintenance.mapper.MaintenanceRecordMapper;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import com.fixledger.modules.maintenance.service.MaintenanceServiceImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MaintenanceServiceFamilyIsolationTest {

  @Test
  @DisplayName("维修详情按家庭空间回查设备，避免异常外键泄露其他家庭设备名")
  void maintenanceDetailLooksUpDeviceWithinFamily() {
    MaintenanceRecordMapper maintenanceRecordMapper = mock(MaintenanceRecordMapper.class);
    DeviceAssetMapper deviceAssetMapper = mock(DeviceAssetMapper.class);
    FamilyService familyService = mock(FamilyService.class);
    MaintenanceServiceImpl service = new MaintenanceServiceImpl(
        maintenanceRecordMapper,
        deviceAssetMapper,
        familyService
    );
    MaintenanceRecordEntity maintenance = maintenance(1L, 999L);
    DeviceAssetEntity otherFamilyDevice = new DeviceAssetEntity();
    otherFamilyDevice.setId(999L);
    otherFamilyDevice.setFamilyId(300L);
    otherFamilyDevice.setName("其他家庭空调");

    when(maintenanceRecordMapper.selectOne(any())).thenReturn(maintenance);
    when(deviceAssetMapper.selectOne(any())).thenReturn(null);
    when(deviceAssetMapper.selectById(999L)).thenReturn(otherFamilyDevice);

    MaintenanceResponse response = service.getMaintenanceDetail(100L, 200L, 1L);

    assertThat(response.deviceName()).isNull();
    verify(deviceAssetMapper, never()).selectById(999L);
  }

  private MaintenanceRecordEntity maintenance(Long id, Long deviceId) {
    MaintenanceRecordEntity maintenance = new MaintenanceRecordEntity();
    maintenance.setId(id);
    maintenance.setFamilyId(200L);
    maintenance.setDeviceId(deviceId);
    maintenance.setTitle("出水变慢");
    maintenance.setFaultDescription("出水速度明显变慢");
    maintenance.setOccurredAt(LocalDateTime.of(2026, 5, 17, 10, 0));
    maintenance.setStatus(MaintenanceStatus.PENDING.getCode());
    return maintenance;
  }
}
