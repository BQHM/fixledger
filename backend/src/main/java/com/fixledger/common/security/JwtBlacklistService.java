package com.fixledger.common.security;

import com.fixledger.common.constant.RedisKeys;
import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import com.fixledger.infrastructure.redis.RedisService;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 文件功能说明：JWT 黑名单服务，使用 Redis 让退出登录后的访问令牌立即失效。
 * </p>
 *
 * @Author FixLedger
 */
@Slf4j
@Service
public class JwtBlacklistService {

  private final RedisService redisService;

  public JwtBlacklistService(RedisService redisService) {
    this.redisService = redisService;
  }

  /**
   * 将当前令牌加入黑名单，TTL 使用令牌剩余有效期，避免 Redis 保存无意义历史数据。
   *
   * @param tokenId JWT jti 标识
   * @param ttl 令牌剩余有效期
   */
  public void blacklist(String tokenId, Duration ttl) {
    if (!StringUtils.hasText(tokenId) || ttl == null || ttl.isZero() || ttl.isNegative()) {
      return;
    }
    try {
      redisService.requireSet(RedisKeys.authBlacklist(tokenId), "1", ttl);
    } catch (RuntimeException e) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录状态服务不可用，请稍后重试", e);
    }
  }

  /**
   * 判断令牌是否已退出登录。
   *
   * @param tokenId JWT jti 标识
   * @return 是否在黑名单中
   */
  public boolean isBlacklisted(String tokenId) {
    if (!StringUtils.hasText(tokenId)) {
      return true;
    }
    try {
      return redisService.requireGet(RedisKeys.authBlacklist(tokenId)).isPresent();
    } catch (RuntimeException e) {
      log.error("JWT blacklist check failed; token is treated as invalid", e);
      return true;
    }
  }
}
