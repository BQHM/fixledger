package com.fixledger.modules.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixledger.common.cache.DashboardCacheInvalidator;
import com.fixledger.common.constant.RedisKeys;
import com.fixledger.infrastructure.redis.RedisService;
import com.fixledger.modules.dashboard.config.DashboardProperties;
import com.fixledger.modules.dashboard.response.DashboardSummaryResponse;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class DashboardSummaryCacheService implements DashboardCacheInvalidator {

  private static final String CACHE_REQUEST_METRIC =
      "fixledger.dashboard.summary.cache.requests";

  private final RedisService redisService;
  private final ObjectMapper objectMapper;
  private final DashboardProperties properties;
  private final MeterRegistry meterRegistry;

  public DashboardSummaryCacheService(
      RedisService redisService,
      ObjectMapper objectMapper,
      DashboardProperties properties,
      MeterRegistry meterRegistry
  ) {
    this.redisService = redisService;
    this.objectMapper = objectMapper;
    this.properties = properties;
    this.meterRegistry = meterRegistry;
  }

  public Optional<DashboardSummaryResponse> get(Long familyId) {
    if (!properties.isSummaryCacheEnabled()) {
      recordRequest("disabled");
      return Optional.empty();
    }
    String key = RedisKeys.dashboardSummary(familyId);
    Optional<String> cached = redisService.get(key);
    if (cached.isEmpty()) {
      recordRequest("miss");
      return Optional.empty();
    }
    try {
      DashboardSummaryResponse summary = objectMapper.readValue(
          cached.get(),
          DashboardSummaryResponse.class
      );
      recordRequest("hit");
      return Optional.of(summary);
    } catch (JsonProcessingException e) {
      recordRequest("invalid");
      redisService.delete(key);
      log.warn(
          "Dashboard summary cache ignored: familyId={}, error={}",
          familyId,
          e.getClass().getSimpleName()
      );
      log.debug("Dashboard summary cache parse failed: familyId={}", familyId, e);
      return Optional.empty();
    }
  }

  public void put(Long familyId, DashboardSummaryResponse summary) {
    if (!properties.isSummaryCacheEnabled()) {
      return;
    }
    try {
      redisService.set(
          RedisKeys.dashboardSummary(familyId),
          objectMapper.writeValueAsString(summary),
          properties.getSummaryCacheTtl()
      );
    } catch (JsonProcessingException e) {
      log.warn(
          "Dashboard summary cache write skipped: familyId={}, error={}",
          familyId,
          e.getClass().getSimpleName()
      );
      log.debug("Dashboard summary cache serialization failed: familyId={}", familyId, e);
    }
  }

  @Override
  public void evictAfterCommit(Long familyId) {
    if (!properties.isSummaryCacheEnabled()) {
      return;
    }
    if (TransactionSynchronizationManager.isActualTransactionActive()
        && TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              evict(familyId);
            }
          }
      );
      return;
    }
    evict(familyId);
  }

  private void evict(Long familyId) {
    redisService.delete(RedisKeys.dashboardSummary(familyId));
  }

  private void recordRequest(String result) {
    meterRegistry.counter(CACHE_REQUEST_METRIC, "result", result).increment();
  }
}
