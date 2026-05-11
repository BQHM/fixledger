package com.fixledger.modules.asset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
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
class DeviceAssetControllerTest {

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

  @Test
  @DisplayName("未登录访问设备列表返回未认证")
  void pageDevicesWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/families/1/devices"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1002));
  }

  @Test
  @DisplayName("登录后可以通过接口创建设备并分页查询")
  void createAndPageDeviceWithToken() throws Exception {
    RegisterResponse user = authService.register(new RegisterRequest(
        "deviceapi",
        null,
        "123456",
        "deviceapi"
    ));
    Long familyId = familyService.getDefaultFamilyId(user.userId());
    Long categoryId = deviceCategoryService.createCategory(
        user.userId(),
        familyId,
        new CreateDeviceCategoryRequest("厨房设备", null, 0)
    ).id();
    LoginResponse login = authService.login(new LoginRequest("deviceapi", "123456"));
    CreateDeviceRequest request = new CreateDeviceRequest(
        categoryId,
        "小米净水器",
        "小米",
        "S1",
        "SN123",
        LocalDate.now(),
        "京东",
        BigDecimal.valueOf(1999),
        "厨房",
        null
    );

    mockMvc.perform(post("/api/families/{familyId}/devices", familyId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.id").isNumber());

    mockMvc.perform(get("/api/families/{familyId}/devices", familyId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken())
            .param("keyword", "净水器"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.records[0].name").value("小米净水器"));
  }
}
