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
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.response.ConsumableResponse;
import com.fixledger.modules.consumable.service.ConsumableService;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.file.enums.FileBizType;
import com.fixledger.modules.file.service.FileResourceService;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import com.fixledger.modules.maintenance.service.MaintenanceService;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.response.WarrantyResponse;
import com.fixledger.modules.warranty.service.WarrantyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  @Autowired
  private WarrantyService warrantyService;

  @Autowired
  private ConsumableService consumableService;

  @Autowired
  private MaintenanceService maintenanceService;

  @Autowired
  private FileResourceService fileResourceService;

  @Test
  @DisplayName("未登录上传附件返回未认证")
  void uploadWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(multipart("/api/families/1/files")
            .file(mockJpeg("invoice.jpg"))
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
            .file(mockJpeg("invoice.jpg"))
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

  @Test
  @DisplayName("可以按设备聚合查询凭证盒")
  void getCredentialBoxAggregatesDeviceRelatedFiles() throws Exception {
    TestFixture fixture = createFixture("fileboxapi");
    LoginResponse login = authService.login(new LoginRequest("fileboxapi", "123456"));
    WarrantyResponse warranty = warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            WarrantyType.OFFICIAL.getCode(),
            LocalDate.now().minusMonths(1),
            LocalDate.now().plusYears(2),
            30,
            "400-000-0000",
            "官方售后",
            "保修卡"
        )
    );
    ConsumableResponse consumable = consumableService.createConsumable(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateConsumableRequest(
            "PP 棉滤芯",
            "小米",
            "PPC-001",
            180,
            LocalDate.now().minusDays(10),
            15,
            "聚合接口测试"
        )
    );
    MaintenanceResponse maintenance = maintenanceService.createMaintenance(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateMaintenanceRequest(
            "出水变慢",
            "净水器出水速度明显变慢",
            LocalDateTime.now().minusDays(1),
            "官方售后",
            "400-000-0000"
        )
    );
    fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.DEVICE.getCode(),
        fixture.deviceId(),
        mockJpeg("invoice.jpg")
    );
    fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.MANUAL.getCode(),
        fixture.deviceId(),
        mockPdf("manual.pdf")
    );
    fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.WARRANTY.getCode(),
        warranty.id(),
        mockJpeg("warranty.jpg")
    );
    fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.CONSUMABLE.getCode(),
        consumable.id(),
        mockJpeg("filter.jpg")
    );
    fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.MAINTENANCE.getCode(),
        maintenance.id(),
        mockJpeg("repair.jpg")
    );

    mockMvc.perform(get(
            "/api/families/{familyId}/devices/{deviceId}/credential-box",
            fixture.familyId(),
            fixture.deviceId()
        ).header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.deviceId").value(fixture.deviceId()))
        .andExpect(jsonPath("$.data.completionPercent").value(100))
        .andExpect(jsonPath("$.data.archivedTypeCount").value(5))
        .andExpect(jsonPath("$.data.totalFileCount").value(5))
        .andExpect(jsonPath("$.data.groups[0].bizType").value(FileBizType.DEVICE.getCode()))
        .andExpect(jsonPath("$.data.groups[0].targets[0].bizId").value(fixture.deviceId()))
        .andExpect(jsonPath("$.data.groups[0].files[0].targetLabel").value("华为路由器"))
        .andExpect(jsonPath("$.data.groups[1].bizType").value(FileBizType.MANUAL.getCode()))
        .andExpect(jsonPath("$.data.groups[2].bizType").value(FileBizType.WARRANTY.getCode()))
        .andExpect(jsonPath("$.data.groups[2].targets[0].bizId").value(warranty.id()))
        .andExpect(jsonPath("$.data.groups[3].bizType").value(FileBizType.MAINTENANCE.getCode()))
        .andExpect(jsonPath("$.data.groups[3].targets[0].bizId").value(maintenance.id()))
        .andExpect(jsonPath("$.data.groups[4].bizType").value(FileBizType.CONSUMABLE.getCode()))
        .andExpect(jsonPath("$.data.groups[4].targets[0].bizId").value(consumable.id()));
  }

  @Test
  @DisplayName("可以搜索已索引的说明书内容")
  void searchManualsByKeyword() throws Exception {
    TestFixture fixture = createFixture("manualsearchapi");
    LoginResponse login = authService.login(new LoginRequest("manualsearchapi", "123456"));
    fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.MANUAL.getCode(),
        fixture.deviceId(),
        mockPdf("router-manual.pdf", "Reset password: hold the reset button for 8 seconds.")
    );

    mockMvc.perform(get(
            "/api/families/{familyId}/devices/{deviceId}/manuals/search",
            fixture.familyId(),
            fixture.deviceId()
        ).param("keyword", "reset")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data[0].fileName").value("router-manual.pdf"))
        .andExpect(jsonPath("$.data[0].snippet").value(org.hamcrest.Matchers.containsString("reset")))
        .andExpect(jsonPath("$.data[0].fileId").exists());
  }

  @Test
  @DisplayName("说明书搜索拒绝空关键词")
  void searchManualsRejectsBlankKeyword() throws Exception {
    TestFixture fixture = createFixture("manualblankapi");
    LoginResponse login = authService.login(new LoginRequest("manualblankapi", "123456"));

    mockMvc.perform(get(
            "/api/families/{familyId}/devices/{deviceId}/manuals/search",
            fixture.familyId(),
            fixture.deviceId()
        ).param("keyword", " ")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(1001));
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
    return new TestFixture(user.userId(), familyId, device.id());
  }

  private MockMultipartFile mockJpeg(String originalName) {
    return new MockMultipartFile(
        "file",
        originalName,
        "image/jpeg",
        new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
    );
  }

  private MockMultipartFile mockPdf(String originalName) {
    return mockPdf(originalName, "manual");
  }

  private MockMultipartFile mockPdf(String originalName, String content) {
    return new MockMultipartFile(
        "file",
        originalName,
        "application/pdf",
        ("%PDF-1.7\n" + content).getBytes()
    );
  }

  private record TestFixture(Long userId, Long familyId, Long deviceId) {
  }
}

