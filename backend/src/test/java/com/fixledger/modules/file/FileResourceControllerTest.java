package com.fixledger.modules.file;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.fixledger.modules.file.enums.FileBizType;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileResourceControllerTest {

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
  @DisplayName("未登录上传附件返回未认证")
  void uploadWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(multipart("/api/families/1/files")
            .file(mockFile("invoice.jpg", "image/jpeg", "invoice"))
            .param("bizType", FileBizType.DEVICE.getCode())
            .param("bizId", "1"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(1002));
  }

  @Test
  @DisplayName("登录后可以上传并查询设备附件")
  void uploadAndListFileWithToken() throws Exception {
    TestFixture fixture = createFixture("fileapi");
    LoginResponse login = authService.login(new LoginRequest("fileapi", "123456"));

    mockMvc.perform(multipart("/api/families/{familyId}/files", fixture.familyId())
            .file(mockFile("invoice.jpg", "image/jpeg", "invoice"))
            .param("bizType", FileBizType.DEVICE.getCode())
            .param("bizId", fixture.deviceId().toString())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.originalName").value("invoice.jpg"));

    mockMvc.perform(get("/api/families/{familyId}/files", fixture.familyId())
            .param("bizType", FileBizType.DEVICE.getCode())
            .param("bizId", fixture.deviceId().toString())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].bizType").value(FileBizType.DEVICE.getCode()));
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
        new CreateDeviceCategoryRequest("数码设备", null, 0)
    ).id();
    CreateDeviceResponse device = deviceAssetService.createDevice(
        user.userId(),
        familyId,
        new CreateDeviceRequest(
            categoryId,
            "华为路由器",
            "华为",
            "AX3",
            "SN" + username,
            purchaseDate,
            "京东",
            BigDecimal.valueOf(399),
            "书房",
            null
        )
    );
    return new TestFixture(familyId, device.id());
  }

  private MockMultipartFile mockFile(String originalName, String contentType, String content) {
    return new MockMultipartFile(
        "file",
        originalName,
        contentType,
        content.getBytes(StandardCharsets.UTF_8)
    );
  }

  private record TestFixture(Long familyId, Long deviceId) {
  }
}

