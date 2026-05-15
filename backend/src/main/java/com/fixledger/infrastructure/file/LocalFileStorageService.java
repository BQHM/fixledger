package com.fixledger.infrastructure.file;

import com.fixledger.common.exception.BusinessException;
import com.fixledger.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件功能说明：本地文件存储实现，主要用于测试环境和对象存储不可用时的兜底。
 * </p>
 *
 * @Author FixLedger
 */
@Service
@ConditionalOnProperty(
    prefix = "fixledger.file",
    name = "storage-type",
    havingValue = "local",
    matchIfMissing = true
)
public class LocalFileStorageService implements FileStorageService {

  private final FileStorageProperties properties;

  public LocalFileStorageService(FileStorageProperties properties) {
    this.properties = properties;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成存储上传文件基础设施操作。
   * </p>
   * @param familyId 家庭空间 ID
   * @param bizType 业务类型
   * @param file 上传文件
   * @return 处理结果
   */
  @Override
  public StoredFile store(Long familyId, String bizType, MultipartFile file) {
    try {
      String originalName = cleanOriginalName(file.getOriginalFilename());
      String extension = resolveExtension(originalName);
      String storageName = UUID.randomUUID() + (extension == null ? "" : "." + extension);
      LocalDate now = LocalDate.now();
      // 按家庭、业务类型和年月分目录，文件名使用 UUID 避免冲突。
      Path relativePath = Paths.get(
          String.valueOf(familyId),
          bizType.toLowerCase(),
          String.valueOf(now.getYear()),
          String.format("%02d", now.getMonthValue()),
          storageName
      );
      Path root = Paths.get(properties.localRoot()).toAbsolutePath().normalize();
      Path target = root.resolve(relativePath).normalize();
      // normalize 后再次校验根目录，阻断 ../ 形式的路径穿越。
      if (!target.startsWith(root)) {
        throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件路径非法");
      }
      Files.createDirectories(target.getParent());
      file.transferTo(target);
      return new StoredFile(
          storageName,
          relativePath.toString().replace('\\', '/'),
          extension,
          file.getSize(),
          file.getContentType()
      );
    } catch (IOException | IllegalStateException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件上传失败", e);
    }
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成读取文件资源基础设施操作。
   * </p>
   * @param storagePath 文件存储路径
   * @return 处理结果
   */
  @Override
  public Resource loadAsResource(String storagePath) {
    try {
      Path root = Paths.get(properties.localRoot()).toAbsolutePath().normalize();
      Path filePath = root.resolve(storagePath).normalize();
      // 下载同样限制在存储根目录内，避免读取任意本地文件。
      if (!filePath.startsWith(root)) {
        throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件路径非法");
      }
      Resource resource = new UrlResource(filePath.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
      }
      return resource;
    } catch (IOException | IllegalStateException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件读取失败", e);
    }
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
    return originalName.substring(index + 1).toLowerCase();
  }
}
