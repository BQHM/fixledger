package com.fixledger.modules.consumable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.consumable.enums.ConsumableStatus;
import com.fixledger.modules.consumable.query.ConsumableDueSoonQuery;
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.request.CreateReplaceRecordRequest;
import com.fixledger.modules.consumable.request.UpdateConsumableRequest;
import com.fixledger.modules.consumable.response.ConsumableReplaceRecordResponse;
import com.fixledger.modules.consumable.response.ConsumableResponse;
import com.fixledger.modules.consumable.service.ConsumableService;
import com.fixledger.modules.family.service.FamilyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ConsumableServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceCategoryService deviceCategoryService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Autowired
  private ConsumableService consumableService;

  @Test
  @DisplayName("可以创建并查询设备耗材")
  void createAndListConsumables() {
    TestFixture fixture = createFixture("consumablecreate");

    ConsumableResponse created = createConsumable(
        fixture,
        LocalDate.now().minusDays(10),
        180
    );
    List<ConsumableResponse> list = consumableService.listDeviceConsumables(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId()
    );

    assertThat(created.id()).isNotNull();
    assertThat(created.nextRemindDate()).isEqualTo(LocalDate.now().plusDays(170));
    assertThat(list).extracting(ConsumableResponse::name).containsExactly("PP 棉滤芯");
  }

  @Test
  @DisplayName("未设置上次更换日期时不生成下次提醒日期")
  void createConsumableWithoutLastReplacedDateHasNoNextRemindDate() {
    TestFixture fixture = createFixture("consumablenodate");

    ConsumableResponse created = createConsumable(fixture, null, 180);

    assertThat(created.nextRemindDate()).isNull();
    assertThat(created.status()).isEqualTo(ConsumableStatus.NORMAL.getCode());
  }

  @Test
  @DisplayName("记录耗材更换后刷新上次更换和下次提醒日期")
  void replaceRecordRefreshesNextRemindDate() {
    TestFixture fixture = createFixture("consumablereplace");
    ConsumableResponse consumable = createConsumable(
        fixture,
        LocalDate.now().minusDays(60),
        90
    );
    LocalDate replacedDate = LocalDate.now().minusDays(1);

    ConsumableReplaceRecordResponse record = consumableService.createReplaceRecord(
        fixture.userId(),
        fixture.familyId(),
        consumable.id(),
        new CreateReplaceRecordRequest(replacedDate, BigDecimal.valueOf(89), "自行更换")
    );
    List<ConsumableReplaceRecordResponse> records = consumableService.listReplaceRecords(
        fixture.userId(),
        fixture.familyId(),
        consumable.id()
    );
    ConsumableResponse refreshed = consumableService.listDeviceConsumables(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId()
    ).getFirst();

    assertThat(record.id()).isNotNull();
    assertThat(records).hasSize(1);
    assertThat(refreshed.lastReplacedDate()).isEqualTo(replacedDate);
    assertThat(refreshed.nextRemindDate()).isEqualTo(replacedDate.plusDays(90));
  }

  @Test
  @DisplayName("停用耗材不能记录更换")
  void disabledConsumableCannotCreateReplaceRecord() {
    TestFixture fixture = createFixture("consumabledisabled");
    ConsumableResponse consumable = createConsumable(
        fixture,
        LocalDate.now().minusDays(60),
        90
    );
    consumableService.updateConsumable(
        fixture.userId(),
        fixture.familyId(),
        consumable.id(),
        new UpdateConsumableRequest(
            "PP 棉滤芯",
            "小米",
            "PPC-001",
            90,
            LocalDate.now().minusDays(60),
            7,
            false,
            "暂停使用"
        )
    );

    assertThatThrownBy(() -> consumableService.createReplaceRecord(
        fixture.userId(),
        fixture.familyId(),
        consumable.id(),
        new CreateReplaceRecordRequest(LocalDate.now(), BigDecimal.valueOf(89), "停用后更换")
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.REPLACE_DATE_INVALID));
  }

  @Test
  @DisplayName("即将更换分页返回到期和逾期耗材")
  void pageDueSoonConsumables() {
    TestFixture fixture = createFixture("consumabledue");
    createConsumable(fixture, LocalDate.now().minusDays(175), 180);
    createConsumable(fixture, LocalDate.now().minusDays(10), 180);
    ConsumableDueSoonQuery query = new ConsumableDueSoonQuery();
    query.setDays(7);

    PageResponse<ConsumableResponse> page = consumableService.pageDueSoon(
        fixture.userId(),
        fixture.familyId(),
        query
    );

    assertThat(page.total()).isEqualTo(1);
    assertThat(page.records().getFirst().status()).isEqualTo(ConsumableStatus.DUE_SOON.getCode());
  }

  @Test
  @DisplayName("更换周期必须大于 0")
  void rejectInvalidCycleDays() {
    TestFixture fixture = createFixture("consumablecycle");

    assertThatThrownBy(() -> consumableService.createConsumable(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateConsumableRequest("滤芯", null, null, 0, LocalDate.now(), 7, null)
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.REPLACE_DATE_INVALID));
  }

  @Test
  @DisplayName("非家庭成员不能访问耗材")
  void nonFamilyMemberCannotAccessConsumable() {
    TestFixture owner = createFixture("consumableowner");
    RegisterResponse other = authService.register(new RegisterRequest(
        "consumableother",
        null,
        "123456",
        "consumableother"
    ));

    assertThatThrownBy(() -> consumableService.listDeviceConsumables(
        other.userId(),
        owner.familyId(),
        owner.deviceId()
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
  }

  @Test
  @DisplayName("可以更新和删除耗材")
  void updateAndDeleteConsumable() {
    TestFixture fixture = createFixture("consumableupdate");
    ConsumableResponse created = createConsumable(fixture, LocalDate.now().minusDays(10), 180);

    ConsumableResponse updated = consumableService.updateConsumable(
        fixture.userId(),
        fixture.familyId(),
        created.id(),
        new UpdateConsumableRequest(
            "活性炭滤芯",
            "小米",
            "AC-001",
            365,
            LocalDate.now().minusDays(30),
            15,
            true,
            "一年一换"
        )
    );
    boolean deleted = consumableService.deleteConsumable(
        fixture.userId(),
        fixture.familyId(),
        created.id()
    );

    assertThat(updated.name()).isEqualTo("活性炭滤芯");
    assertThat(updated.nextRemindDate()).isEqualTo(LocalDate.now().minusDays(30).plusDays(365));
    assertThat(deleted).isTrue();
  }

  private ConsumableResponse createConsumable(
      TestFixture fixture,
      LocalDate lastReplacedDate,
      int cycleDays
  ) {
    return consumableService.createConsumable(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateConsumableRequest(
            "PP 棉滤芯",
            "小米",
            "PPC-001",
            cycleDays,
            lastReplacedDate,
            7,
            "半年一换"
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
