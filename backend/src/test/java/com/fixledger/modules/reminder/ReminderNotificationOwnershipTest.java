package com.fixledger.modules.reminder;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fixledger.infrastructure.redis.TestRedisConfig;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.family.mapper.FamilyMemberMapper;
import com.fixledger.modules.family.service.FamilyService;
import com.fixledger.modules.reminder.entity.NotificationRecordEntity;
import com.fixledger.modules.reminder.mapper.NotificationRecordMapper;
import com.fixledger.modules.reminder.response.ReminderScanResponse;
import com.fixledger.modules.reminder.service.ReminderService;
import com.fixledger.modules.warranty.enums.WarrantyType;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.service.WarrantyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestRedisConfig.class)
@Transactional
class ReminderNotificationOwnershipTest {

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
  private ReminderService reminderService;

  @Autowired
  private NotificationRecordMapper notificationRecordMapper;

  @Autowired
  private FamilyMemberMapper familyMemberMapper;

  @Test
  @DisplayName("扫描生成提醒时为家庭成员创建有用户归属的站内通知")
  void scanCreatesNotificationsForFamilyMembers() {
    TestFixture fixture = createFixture("notifyowner");
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

    ReminderScanResponse scan = reminderService.scanFamily(fixture.userId(), fixture.familyId());
    var notifications = notificationRecordMapper.selectList(
        new LambdaQueryWrapper<NotificationRecordEntity>()
            .eq(NotificationRecordEntity::getFamilyId, fixture.familyId())
    );

    assertThat(scan.notificationCreated()).isEqualTo(1);
    assertThat(notifications).hasSize(1);
    assertThat(notifications.getFirst().getUserId()).isEqualTo(fixture.userId());
  }

  private TestFixture createFixture(String username) {
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
        new CreateDeviceCategoryRequest("厨房设备", "Kitchen", 1)
    );
    CreateDeviceResponse device = deviceAssetService.createDevice(
        user.userId(),
        familyId,
        new CreateDeviceRequest(
            category.id(),
            "小米净水器",
            "小米",
            "S1",
            "SN" + username,
            LocalDate.now().minusYears(1),
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
