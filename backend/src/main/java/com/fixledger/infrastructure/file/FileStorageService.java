package com.fixledger.infrastructure.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象，屏蔽本地文件系统和后续 MinIO 实现差异。
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
   * @param storagePath 数据库中保存的相对存储路径
   * @return 可下载资源
   */
  Resource loadAsResource(String storagePath);
}