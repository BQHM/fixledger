package com.fixledger.modules.reminder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.fixledger.modules.reminder.enums.ReminderStatus;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.service.WarrantyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRedisConfig.class)
@Transactional
class ReminderControllerTest {

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

  @Autowired
  private WarrantyService warrantyService;

  @Test
  @DisplayName("未登录访问提醒列表返回未认证")
  void pageRemindersWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/families/1/reminders"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1002));
  }

  @Test
  @DisplayName("登录后可以扫描、查询、读取提醒")
  void scanPageAndReadReminderWithToken() throws Exception {
    TestFixture fixture = createFixture("reminderapi");
    LoginResponse login = authService.login(new LoginRequest("reminderapi", "123456"));
    String token = "Bearer " + login.accessToken();
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

    mockMvc.perform(post("/api/families/{familyId}/reminders/scan", fixture.familyId())
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.warrantyCreated").value(1));

    MvcResult pageResult = mockMvc.perform(get(
            "/api/families/{familyId}/reminders",
            fixture.familyId()
        )
            .header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andReturn();
    Long reminderId = readFirstReminderId(pageResult);

    mockMvc.perform(get("/api/families/{familyId}/reminders/unread-count", fixture.familyId())
            .header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.count").value(1));

    mockMvc.perform(patch(
            "/api/families/{familyId}/reminders/{reminderId}/read",
            fixture.familyId(),
            reminderId
        )
            .header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value(ReminderStatus.READ.getCode()));
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
    return new TestFixture(user.userId(), familyId, device.id());
  }

  private Long readFirstReminderId(MvcResult result) throws Exception {
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("data").path("records").get(0).path("id").asLong();
  }

  private record TestFixture(Long userId, Long familyId, Long deviceId) {
  }
}

