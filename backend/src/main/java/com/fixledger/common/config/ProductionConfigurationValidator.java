package com.fixledger.common.config;

import com.fixledger.common.security.JwtProperties;
import com.fixledger.infrastructure.file.FileStorageProperties;
import com.fixledger.infrastructure.file.FileStorageProperties.StorageType;
import java.util.Locale;
import java.util.Set;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 生产配置启动检查，阻止空凭据和仓库中的已知演示值进入运行环境。
 */
@Component
@Profile("prod")
public class ProductionConfigurationValidator {

  private static final Set<String> FORBIDDEN_SECRETS = Set.of(
      "fixledger_dev_password",
      "root_password",
      "fixledger123",
      "fixledger-local-dev-secret-must-be-replaced",
      "fixledger-docker-dev-secret-change-me-32-bytes",
      "replace-with-at-least-32-byte-development-secret",
      "replace-with-a-strong-production-secret"
  );

  public ProductionConfigurationValidator(
      JwtProperties jwtProperties,
      DataSourceProperties dataSourceProperties,
      RedisProperties redisProperties,
      FileStorageProperties fileStorageProperties
  ) {
    requireSecret("JWT_SECRET", jwtProperties.secret(), 32);
    requireSecret("MYSQL_PASSWORD", dataSourceProperties.getPassword(), 16);
    requireSecret("REDIS_PASSWORD", redisProperties.getPassword(), 16);
    validateFileStorage(fileStorageProperties);
  }

  private void validateFileStorage(FileStorageProperties properties) {
    if (properties.storageType() == StorageType.LOCAL) {
      return;
    }
    if (properties.s3() == null) {
      throw new IllegalStateException("Production object storage configuration is required");
    }
    requireSecret("FILE_S3_ACCESS_KEY", properties.s3().accessKey(), 8);
    requireSecret("FILE_S3_SECRET_KEY", properties.s3().secretKey(), 16);
  }

  private void requireSecret(String name, String value, int minimumLength) {
    String normalized = StringUtils.hasText(value)
        ? value.trim().toLowerCase(Locale.ROOT)
        : "";
    if (normalized.length() < minimumLength) {
      throw new IllegalStateException(
          name + " must contain at least " + minimumLength + " characters in production");
    }
    if (FORBIDDEN_SECRETS.contains(normalized)
        || normalized.contains("change-me")
        || normalized.contains("replace-with")) {
      throw new IllegalStateException(name + " still uses a development or example value");
    }
  }
}
