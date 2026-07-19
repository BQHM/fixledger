package com.fixledger.modules.asset;

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
import com.fixledger.modules.family.request.CreateFamilyRequest;
import com.fixledger.modules.family.service.FamilyService;
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
class DeviceCategoryServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceCategoryService deviceCategoryService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Test
  @DisplayName("家庭成员可以新增并查询设备分类")
  void createAndListCategories() {
    RegisterResponse user = register("catuser");
    Long familyId = familyService.getDefaultFamilyId(user.userId());

    DeviceCategoryResponse created = deviceCategoryService.createCategory(
        user.userId(),
        familyId,
        new CreateDeviceCategoryRequest("测试厨房设备", "Kitchen", 1)
    );
    List<DeviceCategoryResponse> categories = deviceCategoryService.listCategories(
        user.userId(),
        familyId
    );

    assertThat(created.id()).isNotNull();
    assertThat(categories).extracting(DeviceCategoryResponse::name).contains("测试厨房设备");
  }

  @Test
  @DisplayName("注册后的默认家庭空间会初始化常用设备分类")
  void registerInitializesDefaultCategories() {
    RegisterResponse user = register("catdefaults");
    Long familyId = familyService.getDefaultFamilyId(user.userId());

    List<DeviceCategoryResponse> categories = deviceCategoryService.listCategories(
        user.userId(),
        familyId
    );

    assertThat(categories).extracting(DeviceCategoryResponse::name)
        .containsExactly("数码设备", "大家电", "小家电", "网络设备", "厨房设备", "清洁设备", "家居设备", "其他");
    assertThat(categories).allSatisfy(category -> assertThat(category.systemDefault()).isTrue());
  }

  @Test
  @DisplayName("手动创建的家庭空间也会初始化常用设备分类")
  void createFamilyInitializesDefaultCategories() {
    RegisterResponse user = register("catfamily");

    Long familyId = familyService.createFamily(
        user.userId(),
        new CreateFamilyRequest("杭州的家", null)
    ).id();

    List<DeviceCategoryResponse> categories = deviceCategoryService.listCategories(
        user.userId(),
        familyId
    );

    assertThat(categories).extracting(DeviceCategoryResponse::name)
        .containsExactly("数码设备", "大家电", "小家电", "网络设备", "厨房设备", "清洁设备", "家居设备", "其他");
  }

  @Test
  @DisplayName("系统默认设备分类不允许删除")
  void systemDefaultCategoryCannotBeDeleted() {
    RegisterResponse user = register("catdefaultdelete");
    Long familyId = familyService.getDefaultFamilyId(user.userId());
    DeviceCategoryResponse defaultCategory = deviceCategoryService.listCategories(
        user.userId(),
        familyId
    ).getFirst();

    assertThatThrownBy(() -> deviceCategoryService.deleteCategory(
        user.userId(),
        familyId,
        defaultCategory.id()
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
  }

  @Test
  @DisplayName("同一家庭空间内设备分类名称不能重复")
  void duplicateCategoryNameRejected() {
    RegisterResponse user = register("catdup");
    Long familyId = familyService.getDefaultFamilyId(user.userId());
    CreateDeviceCategoryRequest request = new CreateDeviceCategoryRequest("测试数码设备", null, 0);
    deviceCategoryService.createCategory(user.userId(), familyId, request);

    assertThatThrownBy(() -> deviceCategoryService.createCategory(user.userId(), familyId, request))
        .isInstanceOfSatisfying(BusinessException.class, e ->
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
  }

  @Test
  @DisplayName("分类下存在设备时不能删除分类")
  void categoryWithDevicesCannotBeDeleted() {
    RegisterResponse user = register("catdelete");
    Long familyId = familyService.getDefaultFamilyId(user.userId());
    DeviceCategoryResponse category = deviceCategoryService.createCategory(
        user.userId(),
        familyId,
        new CreateDeviceCategoryRequest("测试清洁设备", null, 0)
    );
    CreateDeviceResponse device = deviceAssetService.createDevice(
        user.userId(),
        familyId,
        new CreateDeviceRequest(
            category.id(),
            "扫地机器人",
            "米家",
            "S1",
            null,
            LocalDate.now(),
            null,
            null,
            "客厅",
            null
        )
    );

    assertThat(device.id()).isNotNull();
    assertThatThrownBy(() -> deviceCategoryService.deleteCategory(
        user.userId(),
        familyId,
        category.id()
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
  }

  private RegisterResponse register(String username) {
    return authService.register(new RegisterRequest(username, null, "123456", username));
  }
}

