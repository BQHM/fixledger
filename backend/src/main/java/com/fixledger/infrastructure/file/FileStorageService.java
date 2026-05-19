package com.fixledger.infrastructure.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件功能说明：文件存储抽象，屏蔽本地文件系统和 S3 兼容对象存储实现差异。
 * </p>
 *
 * @Author FixLedger
 */
public interface FileStorageService {

  /**
   * 存储上传文件并返回可持久化的文件元数据。
   *
   * @param familyId 家庭空间 ID
   * @param bizType 附件业务类型
   * @param file 上传文件
   * @return 存储后的文件元数据
   */
  StoredFile store(Long familyId, String bizType, MultipartFile file);

  /**
   * 按存储路径读取文件资源。
   *
   * @param storagePath 数据库中保存的相对存储路径或对象 Key
   * @return 可下载资源
   */
  Resource loadAsResource(String storagePath);

  /**
   * 删除已写入的文件内容，用于元数据写入失败后的补偿清理。
   *
   * @param storagePath 数据库中保存的相对存储路径或对象 Key
   */
  void delete(String storagePath);
}
