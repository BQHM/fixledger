package com.fixledger.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.fixledger.infrastructure.redis.TestRedisConfig;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.service.ConsumableService;
import com.fixledger.modules.dashboard.response.DashboardSummaryResponse;
import com.fixledger.modules.dashboard.response.MaintenanceCostTrendResponse;
import com.fixledger.modules.dashboard.service.DashboardService;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.enums.MaintenanceStatus;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceStatusRequest;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import com.fixledger.modules.maintenance.service.MaintenanceService;
import com.fixledger.modules.reminder.service.ReminderService;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.service.WarrantyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestRedisConfig.class)
@Transactional
class DashboardServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceCategoryService deviceCategoryService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Autowired
  private WarrantyService warrantyService;

  @Autowired
  private ConsumableService consumableService;

  @Autowired
  private MaintenanceService maintenanceService;

  @Autowired
  private ReminderService reminderService;

  @Autowired
  private DashboardService dashboardService;

  @Test
  @DisplayName("首页总览统计设备、保修、耗材和维修费用")
  void summaryCountsCoreBusinessData() {
    TestFixture fixture = createFixture("dashsummary");
    LocalDate today = LocalDate.now();
    warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            WarrantyType.OFFICIAL.getCode(),
            today.minusYears(1),
            today.plusDays(10),
            30,
            null,
            null,
            "即将过保"
        )
    );
    consumableService.createConsumable(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateConsumableRequest("PP 棉滤芯", "小米", "PPC", 180, today.minusDays(175), 7, null)
    );
    MaintenanceResponse maintenance = maintenanceService.createMaintenance(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateMaintenanceRequest("出水变慢", "出水速度变慢", LocalDateTime.now(), null, null)
    );
    completeMaintenance(fixture, maintenance.id(), BigDecimal.valueOf(89));

    DashboardSummaryResponse summary = dashboardService.summary(
        fixture.userId(),
        fixture.familyId()
    );

    assertThat(summary.deviceTotal()).isEqualTo(1);
    assertThat(summary.warrantyExpiringCount()).isEqualTo(1);
    assertThat(summary.consumableDueSoonCount()).isEqualTo(1);
    assertThat(summary.monthlyMaintenanceCost()).isEqualByComparingTo(BigDecimal.valueOf(89));
  }

  @Test
  @DisplayName("看板提供分类分布、维修趋势和提醒日历")
  void dashboardProvidesDistributionTrendAndCalendar() {
    TestFixture fixture = createFixture("dashcalendar");
    LocalDate today = LocalDate.now();
    warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            WarrantyType.OFFICIAL.getCode(),
            today.minusYears(1),
            today.plusDays(1),
            7,
            null,
            null,
            "即将过保"
        )
    );
    MaintenanceResponse maintenance = maintenanceService.createMaintenance(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateMaintenanceRequest("出水变慢", "出水速度变慢", LocalDateTime.now(), null, null)
    );
    completeMaintenance(fixture, maintenance.id(), BigDecimal.valueOf(120));
    reminderService.scanFamily(fixture.userId(), fixture.familyId());

    var distribution = dashboardService.deviceCategoryDistribution(
        fixture.userId(),
        fixture.familyId()
    );
    List<MaintenanceCostTrendResponse> trend = dashboardService.maintenanceCostTrend(
        fixture.userId(),
        fixture.familyId(),
        1
    );
    var calendar = dashboardService.reminderCalendar(
        fixture.userId(),
        fixture.familyId(),
        today,
        today
    );

    assertThat(distribution).extracting("categoryName").contains("厨房设备");
    assertThat(trend).hasSize(1);
    assertThat(trend.getFirst().cost()).isEqualByComparingTo(BigDecimal.valueOf(120));
    assertThat(calendar).hasSize(1);
    assertThat(calendar.getFirst().count()).isEqualTo(1);
  }

  private void completeMaintenance(TestFixture fixture, Long maintenanceId, BigDecimal cost) {
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
            "已修复",
            cost,
            LocalDateTime.now(),
            true
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
            LocalDate.now().minusYears(1),
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

