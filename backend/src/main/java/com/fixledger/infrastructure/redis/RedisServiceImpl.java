package com.fixledger.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;
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
@Service
public class RedisServiceImpl implements RedisService {

  private final StringRedisTemplate stringRedisTemplate;

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
      // Redis is an optimization for cache/dedupe; database remains the source of truth.
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
      // Cache deletion failure must not block core business data.
    }
  }
}
