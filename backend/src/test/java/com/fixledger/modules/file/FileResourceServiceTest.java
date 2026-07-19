package com.fixledger.modules.file;

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
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.response.ConsumableResponse;
import com.fixledger.modules.consumable.service.ConsumableService;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.file.enums.FileBizType;
import com.fixledger.modules.file.response.FileResourceResponse;
import com.fixledger.modules.file.service.FileResourceService;
import com.fixledger.modules.file.service.FileResourceService.FileDownloadResource;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import com.fixledger.modules.maintenance.service.MaintenanceService;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.response.WarrantyResponse;
import com.fixledger.modules.warranty.service.WarrantyService;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FileResourceServiceTest {

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
  @DisplayName("可以上传设备附件并按业务对象查询")
  void uploadAndListDeviceFile() {
    TestFixture fixture = createFixture("filedevice");
    MockMultipartFile file = mockJpeg("invoice.jpg");

    FileResourceResponse uploaded = fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.DEVICE.getCode(),
        fixture.deviceId(),
        file
    );
    List<FileResourceResponse> files = fileResourceService.listFiles(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.DEVICE.getCode(),
        fixture.deviceId()
    );

    assertThat(uploaded.id()).isNotNull();
    assertThat(uploaded.originalName()).isEqualTo("invoice.jpg");
    assertThat(uploaded.bizType()).isEqualTo(FileBizType.DEVICE.getCode());
    assertThat(uploaded.fileSize()).isPositive();
    assertThat(files).extracting(FileResourceResponse::originalName)
        .containsExactly("invoice.jpg");
  }

  @Test
  @DisplayName("可以上传保修凭证附件")
  void uploadWarrantyFile() {
    TestFixture fixture = createFixture("filewarranty");
    LocalDate purchaseDate = fixture.purchaseDate();
    WarrantyResponse warranty = warrantyService.createWarranty(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateWarrantyRequest(
            WarrantyType.OFFICIAL.getCode(),
            purchaseDate,
            purchaseDate.plusYears(2),
            30,
            "400-000-0000",
            "官方售后网点",
            "保修卡"
        )
    );

    FileResourceResponse uploaded = fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.WARRANTY.getCode(),
        warranty.id(),
        mockPng("warranty-card.png")
    );

    assertThat(uploaded.bizType()).isEqualTo(FileBizType.WARRANTY.getCode());
    assertThat(uploaded.bizId()).isEqualTo(warranty.id());
  }

  @Test
  @DisplayName("可以下载已上传附件")
  void downloadFileResource() throws Exception {
    TestFixture fixture = createFixture("filedownload");
    byte[] content = "%PDF-1.7\nmanual-content".getBytes(StandardCharsets.UTF_8);
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "manual.pdf",
        "application/pdf",
        content
    );
    FileResourceResponse uploaded = fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.MANUAL.getCode(),
        fixture.deviceId(),
        file
    );

    FileDownloadResource download = fileResourceService.downloadFile(
        fixture.userId(),
        fixture.familyId(),
        uploaded.id()
    );

    assertThat(download.originalName()).isEqualTo("manual.pdf");
    assertThat(download.contentType()).isEqualTo("application/pdf");
    assertThat(download.fileSize()).isEqualTo((long) content.length);
    try (InputStream input = download.resource().getInputStream()) {
      assertThat(input.readAllBytes()).isEqualTo(content);
    }
  }

  @Test
  @DisplayName("拒绝不允许的文件扩展名和 MIME 类型")
  void rejectInvalidExtensionAndMime() {
    TestFixture fixture = createFixture("fileinvalid");

    assertThatThrownBy(() -> fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.DEVICE.getCode(),
        fixture.deviceId(),
        mockFile("malware.exe", "application/octet-stream", "bad")
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED));

    assertThatThrownBy(() -> fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.DEVICE.getCode(),
        fixture.deviceId(),
        mockFile("invoice.jpg", "text/plain", "bad")
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED));
  }

  @Test
  @DisplayName("拒绝包含路径穿越字符的文件名")
  void rejectPathTraversalFileName() {
    TestFixture fixture = createFixture("filepath");

    assertThatThrownBy(() -> fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.DEVICE.getCode(),
        fixture.deviceId(),
        mockJpeg("../invoice.jpg")
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED));
  }

  @Test
  @DisplayName("非家庭成员不能下载附件")
  void nonFamilyMemberCannotDownloadFile() {
    TestFixture owner = createFixture("fileowner");
    RegisterResponse other = authService.register(new RegisterRequest(
        "fileother",
        null,
        "123456",
        "fileother"
    ));
    FileResourceResponse uploaded = fileResourceService.uploadFile(
        owner.userId(),
        owner.familyId(),
        FileBizType.DEVICE.getCode(),
        owner.deviceId(),
        mockJpeg("invoice.jpg")
    );

    assertThatThrownBy(() -> fileResourceService.downloadFile(
        other.userId(),
        owner.familyId(),
        uploaded.id()
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
  }

  @Test
  @DisplayName("可以上传耗材和维修附件")
  void uploadConsumableAndMaintenanceFiles() {
    TestFixture fixture = createFixture("filep4attach");
    ConsumableResponse consumable = consumableService.createConsumable(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateConsumableRequest(
            "PP 棉滤芯",
            "小米",
            "PPC-001",
            180,
            LocalDate.now().minusDays(30),
            7,
            "耗材附件测试"
        )
    );
    MaintenanceResponse maintenance = maintenanceService.createMaintenance(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateMaintenanceRequest(
            "净水器出水变慢",
            "出水速度明显变慢",
            LocalDateTime.now().minusHours(1),
            "官方售后",
            "400-000-0000"
        )
    );

    FileResourceResponse consumableFile = fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.CONSUMABLE.getCode(),
        consumable.id(),
        mockJpeg("consumable.jpg")
    );
    FileResourceResponse maintenanceFile = fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.MAINTENANCE.getCode(),
        maintenance.id(),
        mockJpeg("repair.jpg")
    );

    assertThat(consumableFile.bizType()).isEqualTo(FileBizType.CONSUMABLE.getCode());
    assertThat(consumableFile.bizId()).isEqualTo(consumable.id());
    assertThat(maintenanceFile.bizType()).isEqualTo(FileBizType.MAINTENANCE.getCode());
    assertThat(maintenanceFile.bizId()).isEqualTo(maintenance.id());
  }

  @Test
  @DisplayName("拒绝内容与扩展名或 MIME 类型不一致的伪装附件")
  void rejectSpoofedFileContent() {
    TestFixture fixture = createFixture("filespoof");

    assertThatThrownBy(() -> fileResourceService.uploadFile(
        fixture.userId(),
        fixture.familyId(),
        FileBizType.DEVICE.getCode(),
        fixture.deviceId(),
        mockFile("invoice.jpg", "image/jpeg", "not-real-jpeg")
    )).isInstanceOfSatisfying(BusinessException.class, e ->
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED));
  }
  private TestFixture createFixture(String username) {
    LocalDate purchaseDate = LocalDate.now().minusMonths(2);
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
        new CreateDeviceCategoryRequest("测试数码设备", "Digital", 1)
    );
    CreateDeviceResponse device = deviceAssetService.createDevice(
        user.userId(),
        familyId,
        new CreateDeviceRequest(
            category.id(),
            "华为路由器",
            "华为",
            "AX3",
            "SN" + username,
            purchaseDate,
            "京东",
            BigDecimal.valueOf(399),
            "书房",
            "测试设备"
        )
    );
    return new TestFixture(user.userId(), familyId, device.id(), purchaseDate);
  }

  private MockMultipartFile mockJpeg(String originalName) {
    return new MockMultipartFile(
        "file",
        originalName,
        "image/jpeg",
        new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
    );
  }

  private MockMultipartFile mockPng(String originalName) {
    return new MockMultipartFile(
        "file",
        originalName,
        "image/png",
        new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        }
    );
  }

  private MockMultipartFile mockFile(String originalName, String contentType, String content) {
    return new MockMultipartFile(
        "file",
        originalName,
        contentType,
        content.getBytes(StandardCharsets.UTF_8)
    );
  }

  private record TestFixture(
      Long userId,
      Long familyId,
      Long deviceId,
      LocalDate purchaseDate
  ) {
  }
}
