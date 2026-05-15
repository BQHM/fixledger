package com.fixledger.infrastructure.file;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * 文件功能说明：文件存储配置对象，集中绑定本地和 S3 兼容对象存储参数。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@ConfigurationProperties(prefix = "fixledger.file")
public record FileStorageProperties(
    @NotNull(message = "文件存储类型不能为空") StorageType storageType,
    @NotBlank(message = "本地文件存储路径不能为空") String localRoot,
    @Valid S3Properties s3
) {

  /**
   * <p>
   * 文件功能说明：文件存储类型，RUSTFS、S3、MINIO 走同一套 S3 兼容实现。
   * </p>
   *
   * @Author FixLedger
   */
  public enum StorageType {
    LOCAL,
    RUSTFS,
    S3,
    MINIO
  }

  /**
   * <p>
   * 文件功能说明：S3 兼容对象存储配置，当前用于 RustFS。
   * </p>
   *
   * @Author FixLedger
   */
  public record S3Properties(
      @NotBlank(message = "对象存储 endpoint 不能为空") String endpoint,
      @NotBlank(message = "对象存储 accessKey 不能为空") String accessKey,
      @NotBlank(message = "对象存储 secretKey 不能为空") String secretKey,
      @NotBlank(message = "对象存储 bucket 不能为空") String bucket,
      @NotBlank(message = "对象存储 region 不能为空") String region,
      boolean pathStyleAccess,
      boolean createBucket
  ) {
  }
}
