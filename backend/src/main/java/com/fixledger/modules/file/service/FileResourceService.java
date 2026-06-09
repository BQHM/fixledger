package com.fixledger.modules.file.service;

import com.fixledger.modules.file.response.CredentialBoxResponse;
import com.fixledger.modules.file.response.FileResourceResponse;
import com.fixledger.modules.file.response.ManualSearchResponse;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件资源服务，统一管理设备、保修、维修和耗材相关凭证。
 */
public interface FileResourceService {

  /**
   * 上传并绑定附件到业务对象。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param bizType 附件业务类型
   * @param bizId 业务对象 ID
   * @param file 上传文件
   * @return 附件元数据
   */
  FileResourceResponse uploadFile(
      Long userId,
      Long familyId,
      String bizType,
      Long bizId,
      MultipartFile file
  );

  /**
   * 查询业务对象下挂载的附件列表。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param bizType 附件业务类型
   * @param bizId 业务对象 ID
   * @return 附件列表
   */
  List<FileResourceResponse> listFiles(Long userId, Long familyId, String bizType, Long bizId);

  /**
   * 按设备聚合查询凭证盒，包含设备、说明书、保修、维修和耗材附件。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 凭证盒聚合数据
   */
  CredentialBoxResponse getCredentialBox(Long userId, Long familyId, Long deviceId);

  /**
   * 按设备搜索已索引的说明书内容。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param keyword 搜索关键词
   * @return 搜索结果
   */
  List<ManualSearchResponse> searchManuals(
      Long userId,
      Long familyId,
      Long deviceId,
      String keyword
  );

  /**
   * 下载附件，读取前会校验家庭空间访问权限。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param fileId 附件 ID
   * @return 下载资源和响应头所需元数据
   */
  FileDownloadResource downloadFile(Long userId, Long familyId, Long fileId);

  /**
   * 逻辑删除附件元数据，文件内容可由后续清理任务延迟处理。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param fileId 附件 ID
   * @return 是否删除成功
   */
  boolean deleteFile(Long userId, Long familyId, Long fileId);

  /**
   * 控制器下载文件时需要的资源与元数据聚合。
   */
  record FileDownloadResource(
      Resource resource,
      String originalName,
      String contentType,
      Long fileSize
  ) {
  }
}
