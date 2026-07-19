package com.fixledger.modules.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fixledger.infrastructure.redis.TestRedisConfig;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRedisConfig.class)
@Transactional
class DashboardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceCategoryService deviceCategoryService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Test
  @DisplayName("未登录访问首页看板返回未认证")
  void dashboardWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/families/1/dashboard/summary"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1002));
  }

  @Test
  @DisplayName("登录后可以查询首页总览和分类分布")
  void dashboardSummaryAndDistributionWithToken() throws Exception {
    TestFixture fixture = createFixture("dashboardapi");
    LoginResponse login = authService.login(new LoginRequest("dashboardapi", "123456"));
    String token = "Bearer " + login.accessToken();

    mockMvc.perform(get("/api/families/{familyId}/dashboard/summary", fixture.familyId())
            .header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.deviceTotal").value(1));

    mockMvc.perform(get(
            "/api/families/{familyId}/dashboard/device-category-distribution",
            fixture.familyId()
        )
            .header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].categoryName").value("测试厨房设备"))
        .andExpect(jsonPath("$.data[0].count").value(1));
  }


  @Test
  @DisplayName("维修费用趋势月份超过上限时返回参数错误")
  void maintenanceCostTrendWithInvalidMonthsReturnsBadRequest() throws Exception {
    TestFixture fixture = createFixture("dashboardmonths");
    LoginResponse login = authService.login(new LoginRequest("dashboardmonths", "123456"));

    mockMvc.perform(get(
            "/api/families/{familyId}/dashboard/maintenance-cost-trend",
            fixture.familyId()
        )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken())
            .param("months", "25"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(1001));
  }

  @Test
  @DisplayName("提醒日历结束日期早于开始日期时返回参数错误")
  void reminderCalendarWithInvalidDateRangeReturnsBadRequest() throws Exception {
    TestFixture fixture = createFixture("dashboarddaterange");
    LoginResponse login = authService.login(new LoginRequest("dashboarddaterange", "123456"));

    mockMvc.perform(get(
            "/api/families/{familyId}/dashboard/reminder-calendar",
            fixture.familyId()
        )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken())
            .param("startDate", LocalDate.now().toString())
            .param("endDate", LocalDate.now().minusDays(1).toString()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(1001));
  }

  private TestFixture createFixture(String username) {
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
            LocalDate.now().minusYears(1),
            "京东",
            BigDecimal.valueOf(1999),
            "厨房",
            null
        )
    );
    return new TestFixture(familyId, device.id());
  }

  private record TestFixture(Long familyId, Long deviceId) {
  }
}
