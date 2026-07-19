package com.fixledger.modules.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.enums.DeviceStatus;
import com.fixledger.modules.asset.query.DevicePageQuery;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.request.UpdateDeviceStatusRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import com.fixledger.modules.asset.response.DeviceDetailResponse;
import com.fixledger.modules.asset.response.DeviceListResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.service.FamilyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class DeviceAssetServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceCategoryService deviceCategoryService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Test
  @DisplayName("可以创建设备并查询设备详情")
  void createAndGetDeviceDetail() {
    TestFixture fixture = createFixture("devicecreate");

    CreateDeviceResponse created = createDevice(fixture, "小米净水器", "小米");
    DeviceDetailResponse detail = deviceAssetService.getDeviceDetail(
        fixture.userId(),
        fixture.familyId(),
        created.id()
    );

    assertThat(detail.name()).isEqualTo("小米净水器");
    assertThat(detail.categoryName()).isEqualTo("测试厨房设备");
    assertThat(detail.status()).isEqualTo(DeviceStatus.NORMAL.getCode());
  }

  @Test
  @DisplayName("设备分页支持关键词和状态筛选")
  void pageDevicesWithFilters() {
    TestFixture fixture = createFixture("devicepage");
    createDevice(fixture, "小米净水器", "小米");
    createDevice(fixture, "华为路由器", "华为");
    DevicePageQuery query = new DevicePageQuery();
    query.setKeyword("净水器");
    query.setStatus(DeviceStatus.NORMAL.getCode());

    PageResponse<DeviceListResponse> page = deviceAssetService.pageDevices(
        fixture.userId(),
        fixture.familyId(),
        query
    );

    assertThat(page.total()).isEqualTo(1);
    assertThat(page.records()).extracting(DeviceListResponse::name).containsExactly("小米净水器");
  }

  @Test
  @DisplayName("设备状态必须通过状态流转接口修改")
  void updateDeviceStatus() {
    TestFixture fixture = createFixture("devicestatus");
    CreateDeviceResponse created = createDevice(fixture, "戴森吸尘器", "戴森");

    DeviceDetailResponse detail = deviceAssetService.updateDeviceStatus(
        fixture.userId(),
        fixture.familyId(),
        created.id(),
        new UpdateDeviceStatusRequest(DeviceStatus.REPAIRING.getCode(), "已送修")
    );

    assertThat(detail.status()).isEqualTo(DeviceStatus.REPAIRING.getCode());
  }

  @Test
  @DisplayName("已报废设备不能恢复为其他状态")
  void scrappedDeviceCannotRecover() {
    TestFixture fixture = createFixture("devicescrap");
    CreateDeviceResponse created = createDevice(fixture, "旧路由器", "TP-LINK");
    deviceAssetService.updateDeviceStatus(
        fixture.userId(),
        fixture.familyId(),
        created.id(),
        new UpdateDeviceStatusRequest(DeviceStatus.SCRAPPED.getCode(), "淘汰")
    );

    assertThatThrownBy(() -> deviceAssetService.updateDeviceStatus(
        fixture.userId(),
        fixture.familyId(),
        created.id(),
        new UpdateDeviceStatusRequest(DeviceStatus.NORMAL.getCode(), "重新启用")
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DEVICE_STATUS_INVALID));
  }

  @Test
  @DisplayName("非家庭成员不能访问设备数据")
  void nonFamilyMemberCannotAccessDevice() {
    TestFixture owner = createFixture("deviceowner");
    RegisterResponse other = authService.register(new RegisterRequest(
        "deviceother",
        null,
        "123456",
        "deviceother"
    ));
    CreateDeviceResponse created = createDevice(owner, "冰箱", "海尔");

    assertThatThrownBy(() -> deviceAssetService.getDeviceDetail(
        other.userId(),
        owner.familyId(),
        created.id()
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
    return new TestFixture(user.userId(), familyId, category.id());
  }

  private CreateDeviceResponse createDevice(TestFixture fixture, String name, String brand) {
    return deviceAssetService.createDevice(
        fixture.userId(),
        fixture.familyId(),
        new CreateDeviceRequest(
            fixture.categoryId(),
            name,
            brand,
            "S1",
            "SN" + name.hashCode(),
            LocalDate.now(),
            "京东",
            BigDecimal.valueOf(1999),
            "厨房",
            "测试设备"
        )
    );
  }

  private record TestFixture(Long userId, Long familyId, Long categoryId) {
  }
}

