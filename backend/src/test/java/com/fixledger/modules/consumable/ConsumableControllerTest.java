package com.fixledger.modules.consumable;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.fixledger.modules.consumable.enums.ConsumableStatus;
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.request.CreateReplaceRecordRequest;
import com.fixledger.modules.family.service.FamilyService;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConsumableControllerTest {

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
  @DisplayName("未登录访问耗材列表返回未认证")
  void listConsumablesWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/families/1/devices/1/consumables"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1002));
  }

  @Test
  @DisplayName("登录后可以创建耗材、查询即将更换并记录更换")
  void createDueSoonAndReplaceConsumableWithToken() throws Exception {
    TestFixture fixture = createFixture("consumableapi");
    LoginResponse login = authService.login(new LoginRequest("consumableapi", "123456"));
    String token = "Bearer " + login.accessToken();
    LocalDate lastReplacedDate = LocalDate.now().minusDays(175);
    CreateConsumableRequest request = new CreateConsumableRequest(
        "PP 棉滤芯",
        "小米",
        "PPC-001",
        180,
        lastReplacedDate,
        7,
        "半年一换"
    );

    MvcResult createResult = mockMvc.perform(post(
            "/api/families/{familyId}/devices/{deviceId}/consumables",
            fixture.familyId(),
            fixture.deviceId()
        )
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.name").value("PP 棉滤芯"))
        .andExpect(jsonPath("$.data.status").value(ConsumableStatus.DUE_SOON.getCode()))
        .andReturn();
    Long consumableId = readId(createResult);

    mockMvc.perform(get(
            "/api/families/{familyId}/devices/{deviceId}/consumables",
            fixture.familyId(),
            fixture.deviceId()
        )
            .header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value("PP 棉滤芯"));

    mockMvc.perform(get("/api/families/{familyId}/consumables/due-soon", fixture.familyId())
            .header(HttpHeaders.AUTHORIZATION, token)
            .param("days", "7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.records[0].status").value(
            ConsumableStatus.DUE_SOON.getCode()
        ));

    LocalDate replacedDate = LocalDate.now().minusDays(1);
    CreateReplaceRecordRequest replaceRequest = new CreateReplaceRecordRequest(
        replacedDate,
        BigDecimal.valueOf(89),
        "自行更换"
    );
    mockMvc.perform(post(
            "/api/families/{familyId}/consumables/{consumableId}/replace-records",
            fixture.familyId(),
            consumableId
        )
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(replaceRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.replacedDate").value(replacedDate.toString()));
  }

  @Test
  @DisplayName("创建耗材请求体无效时返回参数错误")
  void createConsumableWithInvalidBodyReturnsBadRequest() throws Exception {
    TestFixture fixture = createFixture("consumableinvalid");
    LoginResponse login = authService.login(new LoginRequest("consumableinvalid", "123456"));
    CreateConsumableRequest request = new CreateConsumableRequest(
        "",
        "小米",
        "PPC-001",
        0,
        LocalDate.now(),
        7,
        "无效耗材"
    );

    mockMvc.perform(post(
            "/api/families/{familyId}/devices/{deviceId}/consumables",
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
  @DisplayName("非家庭成员访问耗材列表返回无权限")
  void listConsumablesByNonFamilyMemberReturnsForbidden() throws Exception {
    TestFixture owner = createFixture("consumableownerapi");
    authService.register(new RegisterRequest(
        "consumableotherapi",
        null,
        "123456",
        "consumableotherapi"
    ));
    LoginResponse otherLogin = authService.login(new LoginRequest("consumableotherapi", "123456"));

    mockMvc.perform(get(
            "/api/families/{familyId}/devices/{deviceId}/consumables",
            owner.familyId(),
            owner.deviceId()
        )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherLogin.accessToken()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(1003));
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
        new CreateDeviceCategoryRequest("厨房设备", null, 0)
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
            LocalDate.now().minusMonths(3),
            "京东",
            BigDecimal.valueOf(1999),
            "厨房",
            null
        )
    );
    return new TestFixture(familyId, device.id());
  }

  private Long readId(MvcResult result) throws Exception {
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("data").path("id").asLong();
  }

  private record TestFixture(Long familyId, Long deviceId) {
  }
}
