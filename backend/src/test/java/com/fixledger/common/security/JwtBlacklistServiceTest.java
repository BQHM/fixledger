package com.fixledger.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.infrastructure.redis.RedisService;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;

class JwtBlacklistServiceTest {

  @Test
  @DisplayName("黑名单读取 Redis 失败时按令牌已失效处理")
  void redisReadFailureTreatsTokenAsBlacklisted() {
    JwtBlacklistService service = new JwtBlacklistService(new FailingRedisService());

    boolean blacklisted = service.isBlacklisted("token-id");

    assertThat(blacklisted).isTrue();
  }

  @Test
  @DisplayName("黑名单写入 Redis 失败时转换为业务异常")
  void redisWriteFailureThrowsBusinessException() {
    JwtBlacklistService service = new JwtBlacklistService(new FailingRedisService());

    assertThatThrownBy(() -> service.blacklist("token-id", Duration.ofMinutes(5)))
        .isInstanceOfSatisfying(BusinessException.class, e ->
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SYSTEM_ERROR));
  }

  private static class FailingRedisService implements RedisService {

    @Override
    public boolean setIfAbsent(String key, String value, Duration ttl) {
      throw new RedisConnectionFailureException("redis unavailable");
    }

    @Override
    public void set(String key, String value, Duration ttl) {
      throw new RedisConnectionFailureException("redis unavailable");
    }

    @Override
    public Optional<String> get(String key) {
      throw new RedisConnectionFailureException("redis unavailable");
    }

    @Override
    public void delete(String key) {
      throw new RedisConnectionFailureException("redis unavailable");
    }
  }
}
