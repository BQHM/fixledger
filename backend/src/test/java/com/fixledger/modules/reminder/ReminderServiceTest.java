package com.fixledger.modules.reminder;

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
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.reminder.enums.ReminderStatus;
import com.fixledger.modules.reminder.enums.ReminderType;
import com.fixledger.modules.reminder.query.ReminderPageQuery;
import com.fixledger.modules.reminder.response.ReminderResponse;
import com.fixledger.modules.reminder.response.ReminderScanResponse;
import com.fixledger.modules.reminder.response.UnreadCountResponse;
import com.fixledger.modules.reminder.service.ReminderService;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.service.WarrantyService;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class ReminderServiceTest {

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
  private ReminderService reminderService;

  @Test
  @DisplayName("扫描保修和耗材提醒并通过 Redis 去重")
  void scanWarrantyAndConsumableRemindersWithDedupe() {
    TestFixture fixture = createFixture("reminderscan");
    LocalDate today = LocalDate.now();
    warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            WarrantyType.OFFICIAL.getCode(),
            today.minusMonths(11),
            today.plusDays(5),
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
        new CreateConsumableRequest(
            "PP 棉滤芯",
            "小米",
            "PPC-001",
            180,
            today.minusDays(175),
            7,
            "即将更换"
        )
    );

    ReminderScanResponse first = reminderService.scanFamily(fixture.userId(), fixture.familyId());
    ReminderScanResponse second = reminderService.scanFamily(fixture.userId(), fixture.familyId());
    ReminderPageQuery query = new ReminderPageQuery();

    assertThat(first.warrantyCreated()).isEqualTo(1);
    assertThat(first.consumableCreated()).isEqualTo(1);
    assertThat(first.notificationCreated()).isEqualTo(2);
    assertThat(second.warrantyCreated()).isZero();
    assertThat(second.consumableCreated()).isZero();
    assertThat(second.skippedDuplicate()).isEqualTo(2);
    assertThat(reminderService.pageReminders(
        fixture.userId(),
        fixture.familyId(),
        query
    ).total()).isEqualTo(2);
  }

  @Test
  @DisplayName("可以查询未读提醒并标记已读和忽略")
  void readAndIgnoreReminders() {
    TestFixture fixture = createFixture("reminderread");
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
    consumableService.createConsumable(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateConsumableRequest(
            "活性炭滤芯",
            "小米",
            "AC-001",
            90,
            today.minusDays(91),
            7,
            "已逾期"
        )
    );
    reminderService.scanFamily(fixture.userId(), fixture.familyId());
    ReminderPageQuery query = new ReminderPageQuery();
    query.setStatus(ReminderStatus.PENDING.getCode());

    var page = reminderService.pageReminders(fixture.userId(), fixture.familyId(), query);
    ReminderResponse first = reminderService.markRead(
        fixture.userId(),
        fixture.familyId(),
        page.records().get(0).id()
    );
    ReminderResponse second = reminderService.ignore(
        fixture.userId(),
        fixture.familyId(),
        page.records().get(1).id()
    );
    UnreadCountResponse unread = reminderService.unreadCount(fixture.userId(), fixture.familyId());

    assertThat(first.status()).isEqualTo(ReminderStatus.READ.getCode());
    assertThat(second.status()).isEqualTo(ReminderStatus.IGNORED.getCode());
    assertThat(unread.count()).isZero();
  }

  @Test
  @DisplayName("提醒列表支持类型筛选")
  void pageRemindersByType() {
    TestFixture fixture = createFixture("remindertype");
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
    reminderService.scanFamily(fixture.userId(), fixture.familyId());
    ReminderPageQuery query = new ReminderPageQuery();
    query.setType(ReminderType.WARRANTY_EXPIRE_SOON.getCode());

    var page = reminderService.pageReminders(fixture.userId(), fixture.familyId(), query);

    assertThat(page.total()).isEqualTo(1);
    assertThat(page.records().getFirst().reminderType())
        .isEqualTo(ReminderType.WARRANTY_EXPIRE_SOON.getCode());
  }

  @Test
  @DisplayName("逾期保修和逾期耗材生成对应提醒类型")
  void scanExpiredWarrantyAndOverdueConsumableTypes() {
    TestFixture fixture = createFixture("reminderoverdue");
    LocalDate today = LocalDate.now();
    warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            WarrantyType.OFFICIAL.getCode(),
            today.minusYears(1),
            today.minusDays(1),
            7,
            null,
            null,
            "已经过保"
        )
    );
    consumableService.createConsumable(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateConsumableRequest(
            "反渗透滤芯",
            "小米",
            "RO-001",
            90,
            today.minusDays(91),
            7,
            "已经逾期"
        )
    );

    ReminderScanResponse scan = reminderService.scanFamily(fixture.userId(), fixture.familyId());
    ReminderPageQuery query = new ReminderPageQuery();
    var page = reminderService.pageReminders(fixture.userId(), fixture.familyId(), query);

    assertThat(scan.warrantyCreated()).isEqualTo(1);
    assertThat(scan.consumableCreated()).isEqualTo(1);
    assertThat(page.records()).extracting(ReminderResponse::reminderType)
        .containsExactlyInAnyOrder(
            ReminderType.WARRANTY_EXPIRED.getCode(),
            ReminderType.CONSUMABLE_OVERDUE.getCode()
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
        new CreateDeviceCategoryRequest("测试厨房设备", "Kitchen", 1)
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
