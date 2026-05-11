package com.fixledger.modules.ai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.modules.ai.request.InvoiceParseRequest;
import com.fixledger.modules.ai.request.MaintenanceSummaryRequest;
import com.fixledger.modules.ai.request.TroubleshootingRequest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AiControllerTest {

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
  @DisplayName("未登录访问 AI 接口返回未认证")
  void aiWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/api/families/1/ai/invoice-parse")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new InvoiceParseRequest("商品名称：路由器"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1002));
  }

  @Test
  @DisplayName("登录后可以调用票据提取、故障建议和维修总结接口")
  void aiEndpointsWorkWithToken() throws Exception {
    TestFixture fixture = createFixture("aiapi");
    LoginResponse login = authService.login(new LoginRequest("aiapi", "123456"));
    String token = "Bearer " + login.accessToken();

    mockMvc.perform(post("/api/families/{familyId}/ai/invoice-parse", fixture.familyId())
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new InvoiceParseRequest("""
                商品名称：戴森吸尘器 V12
                购买日期：2026-01-15
                金额：3999
                销售方：京东自营
                """))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.analysisId").isNumber())
        .andExpect(jsonPath("$.data.deviceName").value("戴森吸尘器 V12"));

    mockMvc.perform(post("/api/families/{familyId}/ai/troubleshooting", fixture.familyId())
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new TroubleshootingRequest(
                fixture.deviceId(),
                null,
                "净水器出水变慢，机器有异响"
            ))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.analysisId").isNumber())
        .andExpect(jsonPath("$.data.suggestions[0]").exists());

    mockMvc.perform(post("/api/families/{familyId}/ai/maintenance-summary", fixture.familyId())
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MaintenanceSummaryRequest(fixture.deviceId())
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.analysisId").isNumber())
        .andExpect(jsonPath("$.data.summary").exists());
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

  private record TestFixture(Long familyId, Long deviceId) {
  }
}
