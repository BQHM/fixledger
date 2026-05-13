package com.fixledger.infrastructure.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * 文件功能说明：文件存储配置对象，集中绑定配置文件中的相关参数。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@ConfigurationProperties(prefix = "fixledger.file")
public record FileStorageProperties(
    @NotNull(message = "文件存储类型不能为空") StorageType storageType,
    @NotBlank(message = "本地文件存储路径不能为空") String localRoot
) {

  public enum StorageType {
    LOCAL,
    MINIO
  }
}
