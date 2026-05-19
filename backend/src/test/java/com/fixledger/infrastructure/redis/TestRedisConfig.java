package com.fixledger.infrastructure.redis;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestRedisConfig {

  @Bean
  @Primary
  public RedisService testRedisService() {
    return new InMemoryRedisService();
  }

  private static class InMemoryRedisService implements RedisService {

    private final Map<String, ValueHolder> values = new ConcurrentHashMap<>();

    @Override
    public boolean setIfAbsent(String key, String value, Duration ttl) {
      evictIfExpired(key);
      long expiresAt = System.currentTimeMillis() + ttl.toMillis();
      return values.putIfAbsent(key, new ValueHolder(value, expiresAt)) == null;
    }

    @Override
    public void set(String key, String value, Duration ttl) {
      long expiresAt = System.currentTimeMillis() + ttl.toMillis();
      values.put(key, new ValueHolder(value, expiresAt));
    }

    @Override
    public Optional<String> get(String key) {
      evictIfExpired(key);
      ValueHolder holder = values.get(key);
      return holder == null ? Optional.empty() : Optional.of(holder.value());
    }

    @Override
    public void delete(String key) {
      values.remove(key);
    }

    private void evictIfExpired(String key) {
      ValueHolder holder = values.get(key);
      if (holder != null && holder.expiresAt() <= System.currentTimeMillis()) {
        values.remove(key);
      }
    }
  }

  private record ValueHolder(String value, long expiresAt) {
  }
}
