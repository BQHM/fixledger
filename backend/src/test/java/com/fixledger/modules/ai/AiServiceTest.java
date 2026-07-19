package com.fixledger.modules.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.modules.ai.entity.AiAnalysisEntity;
import com.fixledger.modules.ai.enums.AiAnalysisStatus;
import com.fixledger.modules.ai.enums.AiAnalysisType;
import com.fixledger.modules.ai.mapper.AiAnalysisMapper;
import com.fixledger.modules.ai.request.InvoiceParseRequest;
import com.fixledger.modules.ai.request.MaintenanceSummaryRequest;
import com.fixledger.modules.ai.request.TroubleshootingRequest;
import com.fixledger.modules.ai.response.InvoiceParseResponse;
import com.fixledger.modules.ai.response.MaintenanceSummaryResponse;
import com.fixledger.modules.ai.response.TroubleshootingResponse;
import com.fixledger.modules.ai.service.AiService;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.service.MaintenanceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AiServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceCategoryService deviceCategoryService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Autowired
  private MaintenanceService maintenanceService;

  @Autowired
  private AiService aiService;

  @Autowired
  private AiAnalysisMapper aiAnalysisMapper;

  @Test
  @DisplayName("Mock AI 可以提取票据信息并保存分析记录")
  void parseInvoiceCreatesAnalysisRecord() {
    TestFixture fixture = createFixture("aiinvoice");
    InvoiceParseRequest request = new InvoiceParseRequest("""
        商品名称：戴森吸尘器 V12
        购买日期：2026-01-15
        金额：3999
        销售方：京东自营
        """);

    InvoiceParseResponse response = aiService.parseInvoice(
        fixture.userId(),
        fixture.familyId(),
        request
    );
    AiAnalysisEntity analysis = aiAnalysisMapper.selectById(response.analysisId());

    assertThat(response.deviceName()).isEqualTo("戴森吸尘器 V12");
    assertThat(response.purchaseDate()).isEqualTo(LocalDate.of(2026, 1, 15));
    assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(3999));
    assertThat(response.suggestedCategory()).isEqualTo("清洁设备");
    assertThat(analysis.getAnalysisType()).isEqualTo(AiAnalysisType.INVOICE_PARSE.getCode());
    assertThat(analysis.getStatus()).isEqualTo(AiAnalysisStatus.SUCCESS.getCode());
    assertThat(analysis.getResultJson()).contains("戴森吸尘器 V12");
  }

  @Test
  @DisplayName("Mock AI 根据设备和故障描述生成排查建议")
  void troubleshootingReturnsSuggestions() {
    TestFixture fixture = createFixture("aitrouble");

    TroubleshootingResponse response = aiService.suggestTroubleshooting(
        fixture.userId(),
        fixture.familyId(),
        new TroubleshootingRequest(
            fixture.deviceId(),
            null,
            "净水器出水变慢，机器有异响"
        )
    );
    AiAnalysisEntity analysis = aiAnalysisMapper.selectById(response.analysisId());

    assertThat(response.summary()).contains("小米净水器");
    assertThat(response.suggestions()).anyMatch(item -> item.contains("滤芯"));
    assertThat(analysis.getBizType()).isEqualTo("DEVICE");
    assertThat(analysis.getBizId()).isEqualTo(fixture.deviceId());
    assertThat(analysis.getAnalysisType()).isEqualTo(AiAnalysisType.TROUBLESHOOTING.getCode());
  }

  @Test
  @DisplayName("维修总结在没有记录时返回友好提示")
  void maintenanceSummaryReturnsFriendlyMessageWithoutRecords() {
    TestFixture fixture = createFixture("aisummaryempty");

    MaintenanceSummaryResponse response = aiService.summarizeMaintenance(
        fixture.userId(),
        fixture.familyId(),
        new MaintenanceSummaryRequest(fixture.deviceId())
    );

    assertThat(response.analysisId()).isNotNull();
    assertThat(response.summary()).contains("暂无维修记录");
    assertThat(response.careSuggestion()).contains("维修记录");
  }

  @Test
  @DisplayName("维修总结会读取设备维修历史")
  void maintenanceSummaryUsesMaintenanceHistory() {
    TestFixture fixture = createFixture("aisummaryhistory");
    maintenanceService.createMaintenance(
        fixture.userId(),
        fixture.familyId(),
        fixture.deviceId(),
        new CreateMaintenanceRequest(
            "净水器出水变慢",
            "出水速度明显变慢",
            LocalDateTime.now().minusDays(1),
            "官方售后",
            "400-000-0000"
        )
    );

    MaintenanceSummaryResponse response = aiService.summarizeMaintenance(
        fixture.userId(),
        fixture.familyId(),
        new MaintenanceSummaryRequest(fixture.deviceId())
    );

    assertThat(response.summary()).contains("1 条维修记录");
  }

  @Test
  @DisplayName("故障建议传入维修记录时保存维修关联")
  void troubleshootingCanAssociateMaintenanceRecord() {
    TestFixture fixture = createFixture("aitroublemaint");
    Long maintenanceId = maintenanceService.createMaintenance(
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
    ).id();

    TroubleshootingResponse response = aiService.suggestTroubleshooting(
        fixture.userId(),
        fixture.familyId(),
        new TroubleshootingRequest(fixture.deviceId(), maintenanceId, "净水器出水变慢")
    );
    AiAnalysisEntity analysis = aiAnalysisMapper.selectById(response.analysisId());

    assertThat(analysis.getBizType()).isEqualTo("MAINTENANCE");
    assertThat(analysis.getBizId()).isEqualTo(maintenanceId);
  }
  @Test
  @DisplayName("非家庭成员不能调用 AI 分析家庭空间数据")
  void nonFamilyMemberCannotUseAiOnFamilyData() {
    TestFixture fixture = createFixture("ainonmemberowner");
    RegisterResponse other = authService.register(new RegisterRequest(
        "ainonmember",
        null,
        "123456",
        "非成员"
    ));

    assertThatThrownBy(() -> aiService.suggestTroubleshooting(
        other.userId(),
        fixture.familyId(),
        new TroubleshootingRequest(fixture.deviceId(), null, "无法开机")
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN));
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
        new CreateDeviceCategoryRequest("测试厨房设备", "Kitchen", 1)
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
            "测试设备"
        )
    );
    return new TestFixture(user.userId(), familyId, device.id());
  }

  private record TestFixture(Long userId, Long familyId, Long deviceId) {
  }
}

