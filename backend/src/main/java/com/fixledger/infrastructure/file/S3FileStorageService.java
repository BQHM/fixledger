package com.fixledger.infrastructure.file;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>
 * 文件功能说明：S3 兼容对象存储实现，当前用于 RustFS 文件上传和下载。
 * </p>
 *
 * @Author FixLedger
 */
@Service
@ConditionalOnExpression(
    "'${fixledger.file.storage-type:local}'.equalsIgnoreCase('rustfs')"
        + " || '${fixledger.file.storage-type:local}'.equalsIgnoreCase('s3')"
        + " || '${fixledger.file.storage-type:local}'.equalsIgnoreCase('minio')"
)
public class S3FileStorageService implements FileStorageService {

  private final FileStorageProperties properties;
  private final S3Client s3Client;
  private volatile boolean bucketReady;

  public S3FileStorageService(FileStorageProperties properties) {
    this.properties = properties;
    FileStorageProperties.S3Properties s3 = s3Properties();
    this.s3Client = S3Client.builder()
        .endpointOverride(URI.create(s3.endpoint()))
        .region(Region.of(s3.region()))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(s3.accessKey(), s3.secretKey())
        ))
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(s3.pathStyleAccess())
            .build())
        .build();
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：将上传文件写入 RustFS Bucket，并返回对象存储元数据。
   * </p>
   * @param familyId 家庭空间 ID
   * @param bizType 业务类型
   * @param file 上传文件
   * @return 存储后的文件元数据
   */
  @Override
  public StoredFile store(Long familyId, String bizType, MultipartFile file) {
    try {
      ensureBucketReady();
      String originalName = cleanOriginalName(file.getOriginalFilename());
      String extension = resolveExtension(originalName);
      String storageName = UUID.randomUUID() + (extension == null ? "" : "." + extension);
      String objectKey = buildObjectKey(familyId, bizType, storageName);
      FileStorageProperties.S3Properties s3 = s3Properties();
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(s3.bucket())
          .key(objectKey)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();
      try (InputStream input = file.getInputStream()) {
        RequestBody body = RequestBody.fromInputStream(input, file.getSize());
        s3Client.putObject(request, body);
      }
      return new StoredFile(
          storageName,
          objectKey,
          extension,
          file.getSize(),
          file.getContentType()
      );
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件读取失败", e);
    } catch (S3Exception | IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "RustFS 文件上传失败", e);
    }
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：从 RustFS 按对象 Key 读取文件资源。
   * </p>
   * @param storagePath 文件对象 Key
   * @return 可下载资源
   */
  @Override
  public Resource loadAsResource(String storagePath) {
    try {
      if (!isValidObjectKey(storagePath)) {
        throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件路径非法");
      }
      FileStorageProperties.S3Properties s3 = s3Properties();
      byte[] bytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
              .bucket(s3.bucket())
              .key(storagePath)
              .build())
          .asByteArray();
      return new ByteArrayResource(bytes);
    } catch (NoSuchKeyException | NoSuchBucketException e) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在", e);
    } catch (S3Exception | IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "RustFS 文件读取失败", e);
    }
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：应用关闭时释放 S3 Client 底层 HTTP 资源。
   * </p>
   */
  @PreDestroy
  public void closeClient() {
    s3Client.close();
  }

  private void ensureBucketReady() {
    if (bucketReady) {
      return;
    }
    synchronized (this) {
      if (bucketReady) {
        return;
      }
      FileStorageProperties.S3Properties s3 = s3Properties();
      try {
        s3Client.headBucket(HeadBucketRequest.builder().bucket(s3.bucket()).build());
        bucketReady = true;
      } catch (NoSuchBucketException e) {
        createBucketIfAllowed(s3);
        bucketReady = true;
      } catch (S3Exception e) {
        if (e.statusCode() == 404) {
          createBucketIfAllowed(s3);
          bucketReady = true;
          return;
        }
        throw e;
      }
    }
  }

  private void createBucketIfAllowed(FileStorageProperties.S3Properties s3) {
    if (!s3.createBucket()) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "RustFS Bucket 不存在");
    }
    try {
      s3Client.createBucket(CreateBucketRequest.builder().bucket(s3.bucket()).build());
    } catch (BucketAlreadyOwnedByYouException ignored) {
      // 并发首次上传时其他请求可能已经创建 Bucket，可安全忽略。
    }
  }

  private FileStorageProperties.S3Properties s3Properties() {
    FileStorageProperties.S3Properties s3 = properties.s3();
    if (s3 == null) {
      throw new BusinessException(ErrorCode.CONFIG_NOT_FOUND, "对象存储配置不能为空");
    }
    return s3;
  }

  private String buildObjectKey(Long familyId, String bizType, String storageName) {
    LocalDate now = LocalDate.now();
    return String.join(
        "/",
        "families",
        String.valueOf(familyId),
        bizType.toLowerCase(Locale.ROOT),
        String.valueOf(now.getYear()),
        String.format("%02d", now.getMonthValue()),
        storageName
    );
  }

  private boolean isValidObjectKey(String storagePath) {
    return StringUtils.hasText(storagePath)
        && !storagePath.startsWith("/")
        && !storagePath.contains("..")
        && storagePath.startsWith("families/");
  }

  private String cleanOriginalName(String originalName) {
    return StringUtils.hasText(originalName) ? StringUtils.cleanPath(originalName) : "";
  }

  private String resolveExtension(String originalName) {
    if (!StringUtils.hasText(originalName)) {
      return null;
    }
    int index = originalName.lastIndexOf('.');
    if (index < 0 || index == originalName.length() - 1) {
      return null;
    }
    return originalName.substring(index + 1).toLowerCase(Locale.ROOT);
  }
}
