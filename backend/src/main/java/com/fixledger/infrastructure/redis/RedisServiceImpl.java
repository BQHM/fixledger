package com.fixledger.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件功能说明：Redis 基础设施实现，统一封装缓存、去重和黑名单等临时状态访问。
 * </p>
 *
 * @Author FixLedger
 */
@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

  private final StringRedisTemplate stringRedisTemplate;
  private final AtomicBoolean connectionFailureLogged = new AtomicBoolean();

  public RedisServiceImpl(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：写入带 TTL 的去重 Key，用于提醒扫描、退出登录等短期幂等控制。
   * </p>
   * @param key 缓存键
   * @param value 缓存值
   * @param ttl 过期时间
   * @return 是否写入成功；Redis 不可用时允许后续数据库逻辑继续兜底
   */
  @Override
  public boolean setIfAbsent(String key, String value, Duration ttl) {
    try {
      Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
      return Boolean.TRUE.equals(success);
    } catch (RedisConnectionFailureException e) {
      logConnectionFailure("Redis set-if-absent failed; database dedupe guard will be used", e);
      return true;
    }
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：写入带 TTL 的缓存值；写入失败只记录降级日志，不阻断主流程。
   * </p>
   * @param key 缓存键
   * @param value 缓存值
   * @param ttl 过期时间
   */
  @Override
  public void set(String key, String value, Duration ttl) {
    try {
      stringRedisTemplate.opsForValue().set(key, value, ttl);
    } catch (RedisConnectionFailureException e) {
      logConnectionFailure("Redis set failed; cache-style data was not written", e);
    }
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：读取缓存值；Redis 不可用时按缓存未命中处理。
   * </p>
   * @param key 缓存键
   * @return 缓存值；Redis 不可用时返回空结果
   */
  @Override
  public Optional<String> get(String key) {
    try {
      return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    } catch (RedisConnectionFailureException e) {
      logConnectionFailure("Redis get failed; treating cached value as absent", e);
      return Optional.empty();
    }
  }

  @Override
  public void requireSet(String key, String value, Duration ttl) {
    stringRedisTemplate.opsForValue().set(key, value, ttl);
  }

  @Override
  public Optional<String> requireGet(String key) {
    return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
  }
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：删除缓存或去重 Key；Redis 不可用时依赖 TTL 或数据库状态兜底。
   * </p>
   * @param key 缓存键
   */
  @Override
  public void delete(String key) {
    try {
      stringRedisTemplate.delete(key);
    } catch (RedisConnectionFailureException e) {
      logConnectionFailure("Redis delete failed; cache-style data may expire by TTL", e);
    }
  }

  private void logConnectionFailure(String message, RedisConnectionFailureException e) {
    if (connectionFailureLogged.compareAndSet(false, true)) {
      log.warn("{}: {}", message, e.getMessage());
    }
    log.debug(message, e);
  }
}
