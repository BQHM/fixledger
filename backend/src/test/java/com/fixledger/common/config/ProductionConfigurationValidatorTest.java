package com.fixledger.common.config;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fixledger.common.security.JwtProperties;
import com.fixledger.infrastructure.file.FileStorageProperties;
import com.fixledger.infrastructure.file.FileStorageProperties.S3Properties;
import com.fixledger.infrastructure.file.FileStorageProperties.StorageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

@DisplayName("生产配置启动检查")
class ProductionConfigurationValidatorTest {

  @Test
  @DisplayName("安全凭据完整时允许启动")
  void shouldAcceptStrongProductionSecrets() {
    assertThatNoException().isThrownBy(() -> createValidator(
        "database-password-with-32-characters",
        "redis-password-with-32-characters",
        "storage-secret-with-32-characters"
    ));
  }

  @Test
  @DisplayName("数据库仍使用开发密码时拒绝启动")
  void shouldRejectDevelopmentDatabasePassword() {
    assertThatThrownBy(() -> createValidator(
        "fixledger_dev_password",
        "redis-password-with-32-characters",
        "storage-secret-with-32-characters"
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MYSQL_PASSWORD");
  }

  @Test
  @DisplayName("密码通过空格补足长度时拒绝启动")
  void shouldRejectSecretPaddedWithWhitespace() {
    assertThatThrownBy(() -> createValidator(
        "weak-password                    ",
        "redis-password-with-32-characters",
        "storage-secret-with-32-characters"
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MYSQL_PASSWORD");
  }

  @Test
  @DisplayName("对象存储密钥过短时拒绝启动")
  void shouldRejectWeakObjectStorageSecret() {
    assertThatThrownBy(() -> createValidator(
        "database-password-with-32-characters",
        "redis-password-with-32-characters",
        "short"
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("FILE_S3_SECRET_KEY");
  }

  @Test
  @DisplayName("对象存储访问键仍是示例值时拒绝启动")
  void shouldRejectExampleObjectStorageAccessKey() {
    assertThatThrownBy(() -> createValidator(
        "database-password-with-32-characters",
        "redis-password-with-32-characters",
        "storage-secret-with-32-characters",
        "replace-with-a-production-access-key"
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("FILE_S3_ACCESS_KEY");
  }

  private ProductionConfigurationValidator createValidator(
      String databasePassword,
      String redisPassword,
      String storageSecret
  ) {
    return createValidator(
        databasePassword,
        redisPassword,
        storageSecret,
        "production-access-key"
    );
  }

  private ProductionConfigurationValidator createValidator(
      String databasePassword,
      String redisPassword,
      String storageSecret,
      String storageAccessKey
  ) {
    DataSourceProperties dataSourceProperties = new DataSourceProperties();
    dataSourceProperties.setPassword(databasePassword);

    RedisProperties redisProperties = new RedisProperties();
    redisProperties.setPassword(redisPassword);

    FileStorageProperties fileStorageProperties = new FileStorageProperties(
        StorageType.RUSTFS,
        "/app/uploads",
        new S3Properties(
            "http://rustfs:9000",
            storageAccessKey,
            storageSecret,
            "fixledger-files",
            "us-east-1",
            true,
            false
        )
    );

    return new ProductionConfigurationValidator(
        new JwtProperties("jwt-secret-with-at-least-32-characters", 3600),
        dataSourceProperties,
        redisProperties,
        fileStorageProperties
    );
  }
}
