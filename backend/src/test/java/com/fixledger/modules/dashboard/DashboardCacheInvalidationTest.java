package com.fixledger.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.fixledger.common.constant.RedisKeys;
import com.fixledger.infrastructure.redis.RedisService;
import com.fixledger.infrastructure.redis.TestRedisConfig;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.auth.request.RegisterRequest;
import com.fixledger.modules.auth.response.RegisterResponse;
import com.fixledger.modules.auth.service.AuthService;
import com.fixledger.modules.dashboard.service.DashboardService;
import com.fixledger.modules.family.service.FamilyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestRedisConfig.class)
class DashboardCacheInvalidationTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private FamilyService familyService;

  @Autowired
  private DeviceAssetService deviceAssetService;

  @Autowired
  private DashboardService dashboardService;

  @Autowired
  private RedisService redisService;

  @Test
  @DisplayName("设备写事务提交后失效对应家庭的首页摘要缓存")
  void deviceWriteEvictsDashboardSummaryAfterCommit() {
    RegisterResponse user = authService.register(new RegisterRequest(
        "dashboardcacheevict",
        null,
        "123456",
        "缓存失效测试"
    ));
    Long familyId = familyService.getDefaultFamilyId(user.userId());
    String cacheKey = RedisKeys.dashboardSummary(familyId);
    dashboardService.summary(user.userId(), familyId);
    assertThat(redisService.get(cacheKey)).isPresent();

    deviceAssetService.createDevice(
        user.userId(),
        familyId,
        new CreateDeviceRequest(
            null,
            "新增设备",
            "测试品牌",
            "P29",
            "P29-CACHE-EVICT",
            LocalDate.now(),
            "线下",
            BigDecimal.valueOf(100),
            "客厅",
            null
        )
    );

    assertThat(redisService.get(cacheKey)).isEmpty();
  }
}
