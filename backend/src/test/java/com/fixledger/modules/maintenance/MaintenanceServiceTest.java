package com.fixledger.modules.maintenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.request.UpdateDeviceStatusRequest;
import com.fixledger.modules.asset.enums.DeviceStatus;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import com.fixledger.modules.asset.response.DeviceDetailResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.enums.MaintenanceStatus;
import com.fixledger.modules.maintenance.query.MaintenancePageQuery;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceStatusRequest;
import com.fixledger.modules.maintenance.response.MaintenanceCostSummaryResponse;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import com.fixledger.modules.maintenance.service.MaintenanceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MaintenanceServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceCategoryService deviceCategoryService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Autowired
  private MaintenanceService maintenanceService;

  @Test
  @DisplayName("可以创建维修记录并查询详情")
  void createAndGetMaintenanceDetail() {
    TestFixture fixture = createFixture("maintenancecreate");

    MaintenanceResponse created = createMaintenance(fixture, "净水器出水变慢");
    MaintenanceResponse detail = maintenanceService.getMaintenanceDetail(
        fixture.userId(),
        fixture.familyId(),
        created.id()
    );

    assertThat(created.id()).isNotNull();
    assertThat(detail.status()).isEqualTo(MaintenanceStatus.PENDING.getCode());
    assertThat(detail.deviceName()).isEqualTo("小米净水器");
  }

  @Test
  @DisplayName("维修记录分页支持设备和状态筛选")
  void pageMaintenanceWithFilters() {
    TestFixture fixture = createFixture("maintenancepage");
    MaintenanceResponse created = createMaintenance(fixture, "净水器出水变慢");
    createMaintenance(fixture, "净水器漏水");
    maintenanceService.updateMaintenanceStatus(
        fixture.userId(),
        fixture.familyId(),
        created.id(),
        new UpdateMaintenanceStatusRequest(
            MaintenanceStatus.REPORTED.getCode(),
            null,
            null,
            null,
            false
        )
    );
    MaintenancePageQuery query = new MaintenancePageQuery();
    query.setDeviceId(fixture.deviceId());
    query.setStatus(MaintenanceStatus.REPORTED.getCode());

    PageResponse<MaintenanceResponse> page = maintenanceService.pageMaintenance(
        fixture.userId(),
        fixture.familyId(),
        query
    );

    assertThat(page.total()).isEqualTo(1);
    assertThat(page.records().getFirst().title()).isEqualTo("净水器出水变慢");
  }

  @Test
  @DisplayName("维修状态只能按规则流转")
  void rejectInvalidStatusTransition() {
    TestFixture fixture = createFixture("maintenancestatus");
    MaintenanceResponse created = createMaintenance(fixture, "净水器出水变慢");

    assertThatThrownBy(() -> maintenanceService.updateMaintenanceStatus(
        fixture.userId(),
        fixture.familyId(),
        created.id(),
        new UpdateMaintenanceStatusRequest(
            MaintenanceStatus.COMPLETED.getCode(),
            "直接完成",
            BigDecimal.valueOf(89),
            LocalDateTime.now(),
            true
        )
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MAINTENANCE_STATUS_INVALID));
  }

  @Test
  @DisplayName("完成维修时可同步更新设备状态")
  void completeMaintenanceCanSyncDeviceStatus() {
    TestFixture fixture = createFixture("maintenancecomplete");
    MaintenanceResponse created = createMaintenance(fixture, "净水器出水变慢");
    MaintenanceResponse completed = transitionToCompleted(fixture, created.id(), true);
    DeviceDetailResponse device = deviceAssetService.getDeviceDetail(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId()
    );

    assertThat(completed.status()).isEqualTo(MaintenanceStatus.COMPLETED.getCode());
    assertThat(device.status()).isEqualTo("REPAIRED");
  }

  @Test
  @DisplayName("终态维修记录不能修改")
  void terminalMaintenanceCannotBeUpdated() {
    TestFixture fixture = createFixture("maintenanceterminal");
    MaintenanceResponse created = createMaintenance(fixture, "净水器出水变慢");
    transitionToCompleted(fixture, created.id(), false);

    assertThatThrownBy(() -> maintenanceService.updateMaintenance(
        fixture.userId(),
        fixture.familyId(),
        created.id(),
        new UpdateMaintenanceRequest(
            "净水器恢复复查",
            "完成后再次修改维修记录",
            LocalDateTime.now().minusHours(1),
            "官方售后",
            "400-000-0000",
            BigDecimal.valueOf(99),
            "补充处理结果",
            LocalDateTime.now()
        )
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MAINTENANCE_STATUS_INVALID));
  }

  @Test
  @DisplayName("报废设备不能被维修流程恢复")
  void scrappedDeviceCannotBeRestoredByMaintenanceFlow() {
    TestFixture fixture = createFixture("maintenancescrapped");
    deviceAssetService.updateDeviceStatus(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new UpdateDeviceStatusRequest(DeviceStatus.SCRAPPED.getCode(), "设备老化报废")
    );
    MaintenanceResponse created = createMaintenance(fixture, "报废后误建维修单");

    transitionToCompleted(fixture, created.id(), true);
    DeviceDetailResponse device = deviceAssetService.getDeviceDetail(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId()
    );

    assertThat(device.status()).isEqualTo(DeviceStatus.SCRAPPED.getCode());
  }

  @Test
  @DisplayName("维修费用统计排除已取消记录")
  void costSummaryExcludesCanceledRecords() {
    TestFixture fixture = createFixture("maintenancecost");
    MaintenanceResponse completed = createMaintenance(fixture, "净水器出水变慢");
    MaintenanceResponse canceled = createMaintenance(fixture, "净水器漏水");
    transitionToCompleted(fixture, completed.id(), false);
    maintenanceService.updateMaintenanceStatus(
        fixture.userId(),
        fixture.familyId(),
        canceled.id(),
        new UpdateMaintenanceStatusRequest(
            MaintenanceStatus.CANCELED.getCode(),
            "误报",
            BigDecimal.valueOf(999),
            null,
            false
        )
    );

    MaintenanceCostSummaryResponse summary = maintenanceService.costSummary(
        fixture.userId(),
        fixture.familyId(),
        LocalDate.now().minusDays(1),
        LocalDate.now().plusDays(1)
    );

    assertThat(summary.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(89));
    assertThat(summary.recordCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("非家庭成员不能访问维修记录")
  void nonFamilyMemberCannotAccessMaintenance() {
    TestFixture owner = createFixture("maintenanceowner");
    RegisterResponse other = authService.register(new RegisterRequest(
        "maintenanceother",
        null,
        "123456",
        "maintenanceother"
    ));
    MaintenanceResponse created = createMaintenance(owner, "净水器出水变慢");

    assertThatThrownBy(() -> maintenanceService.getMaintenanceDetail(
        other.userId(),
        owner.familyId(),
        created.id()
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
  }

  private MaintenanceResponse transitionToCompleted(
      TestFixture fixture,
      Long maintenanceId,
      boolean syncDevice
  ) {
    maintenanceService.updateMaintenanceStatus(
        fixture.userId(),
        fixture.familyId(),
        maintenanceId,
        new UpdateMaintenanceStatusRequest(
            MaintenanceStatus.REPORTED.getCode(),
            null,
            null,
            null,
            false
        )
    );
    maintenanceService.updateMaintenanceStatus(
        fixture.userId(),
        fixture.familyId(),
        maintenanceId,
        new UpdateMaintenanceStatusRequest(
            MaintenanceStatus.REPAIRING.getCode(),
            null,
            null,
            null,
            false
        )
    );
    return maintenanceService.updateMaintenanceStatus(
        fixture.userId(),
        fixture.familyId(),
        maintenanceId,
        new UpdateMaintenanceStatusRequest(
            MaintenanceStatus.COMPLETED.getCode(),
            "更换滤芯后恢复正常",
            BigDecimal.valueOf(89),
            LocalDateTime.now(),
            syncDevice
        )
    );
  }

  private MaintenanceResponse createMaintenance(TestFixture fixture, String title) {
    return maintenanceService.createMaintenance(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateMaintenanceRequest(
            title,
            "出水速度明显变慢，机器偶尔有异响",
            LocalDateTime.now().minusHours(2),
            "官方售后",
            "400-000-0000"
        )
    );
  }

  private TestFixture createFixture(String username) {
    RegisterResponse user = authService.register(new RegisterRequest(
        username,
        null,
        "123456",
        username
    ));
    Long familyId = familyService.getDefaultFamilyId(user.userId());
    DeviceCategoryResponse category = deviceCategoryService.createCategory(
        user.userId(),
        familyId,
        new CreateDeviceCategoryRequest("厨房设备", "Kitchen", 1)
    );
    CreateDeviceResponse device = deviceAssetService.createDevice(
        user.userId(),
        familyId,
        new CreateDeviceRequest(
            category.id(),
            "小米净水器",
            "小米",
            "S1",
            "SN" + username,
            LocalDate.now().minusMonths(3),
            "京东",
            BigDecimal.valueOf(1999),
            "厨房",
            "测试设备"
        )
    );
    return new TestFixture(user.userId(), familyId, device.id());
  }

  private record TestFixture(Long userId, Long familyId, Long deviceId) {
  }
}
