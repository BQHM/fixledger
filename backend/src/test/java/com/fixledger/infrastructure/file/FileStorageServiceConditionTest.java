package com.fixledger.infrastructure.file;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

class FileStorageServiceConditionTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
      .withUserConfiguration(TestConfig.class)
      .withPropertyValues(
          "fixledger.file.local-root=./target/test-uploads",
          "fixledger.file.s3.endpoint=http://localhost:9000",
          "fixledger.file.s3.access-key=fixledger",
          "fixledger.file.s3.secret-key=fixledger123",
          "fixledger.file.s3.bucket=fixledger-files",
          "fixledger.file.s3.region=us-east-1",
          "fixledger.file.s3.path-style-access=true",
          "fixledger.file.s3.create-bucket=true"
      );

  @Test
  @DisplayName("文件存储类型为 local 时启用本地存储实现")
  void localStorageTypeUsesLocalService() {
    contextRunner
        .withPropertyValues("fixledger.file.storage-type=local")
        .run(context -> {
          assertThat(context).hasSingleBean(FileStorageService.class);
          assertThat(context).hasSingleBean(LocalFileStorageService.class);
          assertThat(context).doesNotHaveBean(S3FileStorageService.class);
        });
  }

  @Test
  @DisplayName("文件存储类型为 rustfs 时启用 S3 兼容存储实现")
  void rustfsStorageTypeUsesS3Service() {
    contextRunner
        .withPropertyValues("fixledger.file.storage-type=rustfs")
        .run(context -> {
          assertThat(context).hasSingleBean(FileStorageService.class);
          assertThat(context).hasSingleBean(S3FileStorageService.class);
          assertThat(context).doesNotHaveBean(LocalFileStorageService.class);
        });
  }

  @Test
  @DisplayName("文件存储类型为 minio 时复用 S3 兼容存储实现")
  void minioStorageTypeUsesS3Service() {
    contextRunner
        .withPropertyValues("fixledger.file.storage-type=minio")
        .run(context -> {
          assertThat(context).hasSingleBean(FileStorageService.class);
          assertThat(context).hasSingleBean(S3FileStorageService.class);
          assertThat(context).doesNotHaveBean(LocalFileStorageService.class);
        });
  }

  @Configuration
  @ConfigurationPropertiesScan(basePackageClasses = FileStorageProperties.class)
  @Import({LocalFileStorageService.class, S3FileStorageService.class})
  static class TestConfig {
  }
}
