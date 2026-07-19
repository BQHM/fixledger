package com.fixledger.modules.warranty;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import com.fixledger.modules.auth.request.LoginRequest;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.LoginResponse;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WarrantyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceCategoryService deviceCategoryService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Test
  @DisplayName("未登录访问保修记录返回未认证")
  void listWarrantiesWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/families/1/devices/1/warranties"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1002));
  }

  @Test
  @DisplayName("登录后可以创建并查询设备保修记录")
  void createAndListWarrantyWithToken() throws Exception {
    TestFixture fixture = createFixture("warrantyapi");
    LoginResponse login = authService.login(new LoginRequest("warrantyapi", "123456"));
    CreateWarrantyRequest request = new CreateWarrantyRequest(
        WarrantyType.OFFICIAL.getCode(),
        fixture.purchaseDate(),
        fixture.purchaseDate().plusYears(2),
        30,
        "400-000-0000",
        "官方售后网点",
        "整机保修"
    );

    mockMvc.perform(post(
            "/api/families/{familyId}/devices/{deviceId}/warranties",
            fixture.familyId(),
            fixture.deviceId()
        )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.id").isNumber());

    mockMvc.perform(get(
            "/api/families/{familyId}/devices/{deviceId}/warranties",
            fixture.familyId(),
            fixture.deviceId()
        )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].warrantyType").value(WarrantyType.OFFICIAL.getCode()));
  }

  @Test
  @DisplayName("创建保修请求体无效时返回参数错误")
  void createWarrantyWithInvalidBodyReturnsBadRequest() throws Exception {
    TestFixture fixture = createFixture("warrantyinvalid");
    LoginResponse login = authService.login(new LoginRequest("warrantyinvalid", "123456"));
    CreateWarrantyRequest request = new CreateWarrantyRequest(
        WarrantyType.OFFICIAL.getCode(),
        null,
        fixture.purchaseDate().plusYears(1),
        30,
        null,
        null,
        "缺少开始日期"
    );

    mockMvc.perform(post(
            "/api/families/{familyId}/devices/{deviceId}/warranties",
            fixture.familyId(),
            fixture.deviceId()
        )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(1001));
  }

  @Test
  @DisplayName("非家庭成员访问保修记录返回无权限")
  void listWarrantiesByNonFamilyMemberReturnsForbidden() throws Exception {
    TestFixture owner = createFixture("warrantyownerapi");
    authService.register(new RegisterRequest(
        "warrantyotherapi",
        null,
        "123456",
        "warrantyotherapi"
    ));
    LoginResponse otherLogin = authService.login(new LoginRequest("warrantyotherapi", "123456"));

    mockMvc.perform(get(
            "/api/families/{familyId}/devices/{deviceId}/warranties",
            owner.familyId(),
            owner.deviceId()
        )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherLogin.accessToken()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(1003));
  }

  private TestFixture createFixture(String username) {
    LocalDate purchaseDate = LocalDate.now().minusMonths(1);
    RegisterResponse user = authService.register(new RegisterRequest(
        username,
        null,
        "123456",
        username
    ));
    Long familyId = familyService.getDefaultFamilyId(user.userId());
    Long categoryId = deviceCategoryService.createCategory(
        user.userId(),
        familyId,
        new CreateDeviceCategoryRequest("测试厨房设备", null, 0)
    ).id();
    CreateDeviceResponse device = deviceAssetService.createDevice(
        user.userId(),
        familyId,
        new CreateDeviceRequest(
            categoryId,
            "小米净水器",
            "小米",
            "S1",
            "SN" + username,
            purchaseDate,
            "京东",
            BigDecimal.valueOf(1999),
            "厨房",
            null
        )
    );
    return new TestFixture(familyId, device.id(), purchaseDate);
  }

  private record TestFixture(Long familyId, Long deviceId, LocalDate purchaseDate) {
  }
}
