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
 * 文件功能说明：Redis 基础设施实现，封装外部依赖和技术细节。
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
   * 功能说明：完成写入 Redis 去重键基础设施操作。
   * </p>
   * @param key 缓存键
   * @param value 缓存值
   * @param ttl 过期时间
   * @return 是否处理成功
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
   * 功能说明：完成写入数据基础设施操作。
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
   * 功能说明：完成查询基础设施操作。
   * </p>
   * @param key 缓存键
   * @return 查询结果
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

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成删除基础设施操作。
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
