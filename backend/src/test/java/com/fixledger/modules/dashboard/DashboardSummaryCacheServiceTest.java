package com.fixledger.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.common.constant.RedisKeys;
import com.fixledger.infrastructure.redis.RedisService;
import com.fixledger.modules.dashboard.config.DashboardProperties;
import com.fixledger.modules.dashboard.response.DashboardSummaryResponse;
import com.fixledger.modules.dashboard.service.DashboardSummaryCacheService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class DashboardSummaryCacheServiceTest {

  @Test
  @DisplayName("非法缓存值被删除并按未命中回源")
  void invalidCachedValueIsEvicted() {
    RedisService redisService = mock(RedisService.class);
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    DashboardSummaryCacheService cacheService = cacheService(redisService, meterRegistry);
    String key = RedisKeys.dashboardSummary(20L);
    when(redisService.get(key)).thenReturn(Optional.of("not-json"));

    Optional<DashboardSummaryResponse> cached = cacheService.get(20L);

    assertThat(cached).isEmpty();
    verify(redisService).delete(key);
    assertThat(meterRegistry.counter(
        "fixledger.dashboard.summary.cache.requests",
        "result",
        "invalid"
    ).count()).isEqualTo(1);
  }

  @Test
  @DisplayName("首页摘要按配置 TTL 写入 Redis")
  void summaryIsCachedWithConfiguredTtl() {
    RedisService redisService = mock(RedisService.class);
    DashboardProperties properties = new DashboardProperties();
    properties.setSummaryCacheTtl(Duration.ofSeconds(45));
    DashboardSummaryCacheService cacheService = new DashboardSummaryCacheService(
        redisService,
        new ObjectMapper(),
        properties,
        new SimpleMeterRegistry()
    );

    cacheService.put(20L, summary());

    verify(redisService).set(
        eq(RedisKeys.dashboardSummary(20L)),
        any(String.class),
        eq(Duration.ofSeconds(45))
    );
  }

  @Test
  @DisplayName("事务内请求的缓存失效延迟到提交后执行")
  void evictionRunsAfterTransactionCommit() {
    RedisService redisService = mock(RedisService.class);
    DashboardSummaryCacheService cacheService = cacheService(
        redisService,
        new SimpleMeterRegistry()
    );
    TransactionSynchronizationManager.setActualTransactionActive(true);
    TransactionSynchronizationManager.initSynchronization();
    try {
      cacheService.evictAfterCommit(20L);
      verify(redisService, never()).delete(any());

      TransactionSynchronizationManager.getSynchronizations()
          .forEach(synchronization -> synchronization.afterCommit());

      verify(redisService).delete(RedisKeys.dashboardSummary(20L));
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
      TransactionSynchronizationManager.setActualTransactionActive(false);
    }
  }

  private DashboardSummaryCacheService cacheService(
      RedisService redisService,
      SimpleMeterRegistry meterRegistry
  ) {
    return new DashboardSummaryCacheService(
        redisService,
        new ObjectMapper(),
        new DashboardProperties(),
        meterRegistry
    );
  }

  private DashboardSummaryResponse summary() {
    return new DashboardSummaryResponse(1, 2, 3, 4, 5, 6, BigDecimal.TEN);
  }
}
