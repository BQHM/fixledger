package com.fixledger.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 访问门面，集中控制 Key 写入和 TTL 规则。
 */
public interface RedisService {

  /**
   * 仅当 Key 不存在时写入，常用于提醒去重和幂等控制。
   *
   * @param key Redis Key
   * @param value Redis Value
   * @param ttl 过期时间
   * @return 是否写入成功
   */
  boolean setIfAbsent(String key, String value, Duration ttl);

  /**
   * 写入带过期时间的缓存。
   *
   * @param key Redis Key
   * @param value Redis Value
   * @param ttl 过期时间
   */
  void set(String key, String value, Duration ttl);

  /**
   * 读取缓存值。
   *
   * @param key Redis Key
   * @return 缓存值
   */
  Optional<String> get(String key);

  /**
   * 写入必须成功的安全状态，调用方负责处理 Redis 异常。
   *
   * @param key Redis Key
   * @param value Redis Value
   * @param ttl 过期时间
   */
  default void requireSet(String key, String value, Duration ttl) {
    set(key, value, ttl);
  }

  /**
   * 读取必须成功的安全状态，调用方负责处理 Redis 异常。
   *
   * @param key Redis Key
   * @return 缓存值
   */
  default Optional<String> requireGet(String key) {
    return get(key);
  }

  /**
   * 删除缓存。
   *
   * @param key Redis Key
   */
  void delete(String key);
}
