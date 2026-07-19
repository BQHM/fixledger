package com.fixledger.modules.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.exporter.service.CsvExportFile;
import com.fixledger.modules.exporter.service.FamilyExportService;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.enums.MaintenanceStatus;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceStatusRequest;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import com.fixledger.modules.maintenance.service.MaintenanceService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
class FamilyExportServiceTest {

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

  @Autowired
  private FamilyExportService familyExportService;

  @Test
  @DisplayName("设备资产清单导出包含分类并防止 CSV 公式注入")
  void exportDevicesEscapesFormulaCells() {
    TestFixture fixture = createFixture("exportdevices");
    createDevice(fixture, "=SUM(1,1)", "+危险品牌");

    CsvExportFile file = familyExportService.exportDevices(
        fixture.userId(),
        fixture.familyId()
    );
    String csv = new String(file.content(), StandardCharsets.UTF_8);

    assertThat(file.filename()).startsWith("fixledger-devices-");
    assertThat(csv).startsWith("\uFEFF设备ID,设备名称");
    assertThat(csv).contains("测试厨房设备");
    assertThat(csv).contains("\"'=SUM(1,1)\"");
    assertThat(csv).contains("\"'+危险品牌\"");
  }

  @Test
  @DisplayName("维修费用报表只导出未取消且有费用的记录")
  void exportMaintenanceCostsExcludesCanceledRecords() {
    TestFixture fixture = createFixture("exportmaintenance");
    MaintenanceResponse completed = createMaintenance(fixture, "净水器出水变慢");
    MaintenanceResponse canceled = createMaintenance(fixture, "净水器误报");
    transitionToCompleted(fixture, completed.id());
    maintenanceService.updateMaintenanceStatus(
        fixture.userId(),
        fixture.familyId(),
        canceled.id(),
        new UpdateMaintenanceStatusRequest(
            MaintenanceStatus.CANCELED.getCode(),
            "误报取消",
            BigDecimal.valueOf(999),
            null,
            false
        )
    );

    CsvExportFile file = familyExportService.exportMaintenanceCosts(
        fixture.userId(),
        fixture.familyId(),
        LocalDate.now().minusDays(1),
        LocalDate.now().plusDays(1)
    );
    String csv = new String(file.content(), StandardCharsets.UTF_8);

    assertThat(file.filename()).startsWith("fixledger-maintenance-costs-");
    assertThat(csv).contains("维修ID,设备名称,维修标题");
    assertThat(csv).contains("净水器出水变慢");
    assertThat(csv).contains("89");
    assertThat(csv).doesNotContain("净水器误报");
    assertThat(csv).doesNotContain("999");
  }

  @Test
  @DisplayName("非家庭成员不能导出家庭数据")
  void nonFamilyMemberCannotExportFamilyData() {
    TestFixture owner = createFixture("exportowner");
    RegisterResponse other = authService.register(new RegisterRequest(
        "exportother",
        null,
        "123456",
        "exportother"
    ));

    assertThatThrownBy(() -> familyExportService.exportDevices(
        other.userId(),
        owner.familyId()
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
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
        new CreateDeviceCategoryRequest("测试厨房设备", "Kitchen", 1)
    );
    CreateDeviceResponse device = createDevice(
        user.userId(),
        familyId,
        category.id(),
        "小米净水器",
        "小米"
    );
    return new TestFixture(user.userId(), familyId, category.id(), device.id());
  }

  private CreateDeviceResponse createDevice(TestFixture fixture, String name, String brand) {
    return createDevice(fixture.userId(), fixture.familyId(), fixture.categoryId(), name, brand);
  }

  private CreateDeviceResponse createDevice(
      Long userId,
      Long familyId,
      Long categoryId,
      String name,
      String brand
  ) {
    return deviceAssetService.createDevice(
        userId,
        familyId,
        new CreateDeviceRequest(
            categoryId,
            name,
            brand,
            "S1",
            "SN" + name.hashCode(),
            LocalDate.now().minusMonths(3),
            "京东",
            BigDecimal.valueOf(1999),
            "厨房",
            "测试设备"
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
            "出水速度明显变慢",
            LocalDateTime.now().minusHours(2),
            "官方售后",
            "400-000-0000"
        )
    );
  }

  private void transitionToCompleted(TestFixture fixture, Long maintenanceId) {
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
    maintenanceService.updateMaintenanceStatus(
        fixture.userId(),
        fixture.familyId(),
        maintenanceId,
        new UpdateMaintenanceStatusRequest(
            MaintenanceStatus.COMPLETED.getCode(),
            "更换滤芯后恢复正常",
            BigDecimal.valueOf(89),
            LocalDateTime.now(),
            false
        )
    );
  }

  private record TestFixture(Long userId, Long familyId, Long categoryId, Long deviceId) {
  }
}
