package com.fixledger.modules.warranty;

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
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.query.WarrantyExpiringQuery;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.request.UpdateWarrantyRequest;
import com.fixledger.modules.warranty.response.WarrantyResponse;
import com.fixledger.modules.warranty.service.WarrantyService;
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
class WarrantyServiceTest {

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

  @Test
  @DisplayName("可以创建设备保修记录并按设备查询")
  void createAndListDeviceWarranties() {
    LocalDate purchaseDate = LocalDate.now().minusMonths(1);
    TestFixture fixture = createFixture("warrantycreate", purchaseDate);

    WarrantyResponse created = createWarranty(
        fixture,
        purchaseDate,
        purchaseDate.plusYears(2)
    );
    List<WarrantyResponse> warranties = warrantyService.listDeviceWarranties(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId()
    );

    assertThat(created.id()).isNotNull();
    assertThat(created.warrantyType()).isEqualTo(WarrantyType.OFFICIAL.getCode());
    assertThat(warranties).hasSize(1);
    assertThat(warranties.getFirst().deviceName()).isEqualTo("小米净水器");
  }

  @Test
  @DisplayName("未指定保修类型和提醒天数时使用默认值")
  void createWarrantyUsesDefaultsWhenOptionalFieldsMissing() {
    LocalDate purchaseDate = LocalDate.now().minusMonths(1);
    TestFixture fixture = createFixture("warrantydefault", purchaseDate);

    WarrantyResponse created = warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            null,
            purchaseDate,
            purchaseDate.plusYears(1),
            null,
            null,
            null,
            "默认保修配置"
        )
    );

    assertThat(created.warrantyType()).isEqualTo(WarrantyType.OFFICIAL.getCode());
    assertThat(created.remindDaysBefore()).isEqualTo(30);
  }

  @Test
  @DisplayName("提前提醒天数不能为负数")
  void rejectNegativeRemindDaysBefore() {
    LocalDate purchaseDate = LocalDate.now().minusMonths(1);
    TestFixture fixture = createFixture("warrantynegative", purchaseDate);

    assertThatThrownBy(() -> warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            WarrantyType.OFFICIAL.getCode(),
            purchaseDate,
            purchaseDate.plusYears(1),
            -1,
            null,
            null,
            "负数提醒天数"
        )
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
  }

  @Test
  @DisplayName("可以修改并删除保修记录")
  void updateAndDeleteWarranty() {
    LocalDate purchaseDate = LocalDate.now().minusMonths(1);
    TestFixture fixture = createFixture("warrantyupdate", purchaseDate);
    WarrantyResponse created = createWarranty(fixture, purchaseDate, purchaseDate.plusYears(1));

    WarrantyResponse updated = warrantyService.updateWarranty(
        fixture.userId(),
        fixture.familyId(),
        created.id(),
        new UpdateWarrantyRequest(
            WarrantyType.EXTENDED.getCode(),
            purchaseDate,
            purchaseDate.plusYears(3),
            15,
            "400-000-0000",
            "延保服务点",
            "追加延保一年"
        )
    );
    boolean deleted = warrantyService.deleteWarranty(
        fixture.userId(),
        fixture.familyId(),
        created.id()
    );

    assertThat(updated.warrantyType()).isEqualTo(WarrantyType.EXTENDED.getCode());
    assertThat(updated.remindDaysBefore()).isEqualTo(15);
    assertThat(deleted).isTrue();
    assertThat(warrantyService.listDeviceWarranties(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId()
    )).isEmpty();
  }

  @Test
  @DisplayName("保修结束日期不能早于开始日期")
  void rejectEndDateBeforeStartDate() {
    LocalDate purchaseDate = LocalDate.now().minusMonths(1);
    TestFixture fixture = createFixture("warrantystart", purchaseDate);

    assertThatThrownBy(() -> createWarranty(
        fixture,
        purchaseDate.plusDays(10),
        purchaseDate.plusDays(1)
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.WARRANTY_DATE_INVALID));
  }

  @Test
  @DisplayName("保修结束日期不能早于设备购买日期")
  void rejectEndDateBeforePurchaseDate() {
    LocalDate purchaseDate = LocalDate.now().minusMonths(1);
    TestFixture fixture = createFixture("warrantypurchase", purchaseDate);

    assertThatThrownBy(() -> createWarranty(
        fixture,
        purchaseDate.minusDays(10),
        purchaseDate.minusDays(1)
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.WARRANTY_DATE_INVALID));
  }

  @Test
  @DisplayName("即将过保分页只返回指定天数内的保修记录")
  void pageExpiringWarranties() {
    LocalDate today = LocalDate.now();
    TestFixture soon = createFixture("warrantysoon", today.minusMonths(1));
    TestFixture later = createFixture("warrantylater", today.minusMonths(1));
    createWarranty(soon, today.minusMonths(1), today.plusDays(10));
    createWarranty(later, today.minusMonths(1), today.plusDays(60));
    WarrantyExpiringQuery query = new WarrantyExpiringQuery();
    query.setDays(30);

    PageResponse<WarrantyResponse> page = warrantyService.pageExpiring(
        soon.userId(),
        soon.familyId(),
        query
    );

    assertThat(page.total()).isEqualTo(1);
    assertThat(page.records()).extracting(WarrantyResponse::deviceId)
        .containsExactly(soon.deviceId());
  }

  @Test
  @DisplayName("非家庭成员不能访问保修记录")
  void nonFamilyMemberCannotAccessWarranty() {
    LocalDate purchaseDate = LocalDate.now().minusMonths(1);
    TestFixture owner = createFixture("warrantyowner", purchaseDate);
    RegisterResponse other = authService.register(new RegisterRequest(
        "warrantyother",
        null,
        "123456",
        "warrantyother"
    ));

    assertThatThrownBy(() -> warrantyService.listDeviceWarranties(
        other.userId(),
        owner.familyId(),
        owner.deviceId()
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
  }

  private TestFixture createFixture(String username, LocalDate purchaseDate) {
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
            purchaseDate,
            "京东",
            BigDecimal.valueOf(1999),
            "厨房",
            "测试设备"
        )
    );
    return new TestFixture(user.userId(), familyId, device.id());
  }

  private WarrantyResponse createWarranty(
      TestFixture fixture,
      LocalDate startDate,
      LocalDate endDate
  ) {
    return warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            WarrantyType.OFFICIAL.getCode(),
            startDate,
            endDate,
            30,
            "400-000-0000",
            "官方售后网点",
            "整机保修"
        )
    );
  }

  private record TestFixture(Long userId, Long familyId, Long deviceId) {
  }
}
