package com.fixledger.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImpl implements RedisService {

  private final StringRedisTemplate stringRedisTemplate;

  public RedisServiceImpl(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean setIfAbsent(String key, String value, Duration ttl) {
    try {
      Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
      return Boolean.TRUE.equals(success);
    } catch (RedisConnectionFailureException e) {
      return true;
    }
  }

  @Override
  public void set(String key, String value, Duration ttl) {
    try {
      stringRedisTemplate.opsForValue().set(key, value, ttl);
    } catch (RedisConnectionFailureException e) {
      // Redis is an optimization for cache/dedupe; database remains the source of truth.
    }
  }

  @Override
  public Optional<String> get(String key) {
    try {
      return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    } catch (RedisConnectionFailureException e) {
      return Optional.empty();
    }
  }

  @Override
  public void delete(String key) {
    try {
      stringRedisTemplate.delete(key);
    } catch (RedisConnectionFailureException e) {
      // Cache deletion failure must not block core business data.
    }
  }
}
