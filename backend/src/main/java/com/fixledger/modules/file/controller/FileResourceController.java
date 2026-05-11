package com.fixledger.modules.file.controller;

import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.file.response.FileResourceResponse;
import com.fixledger.modules.file.service.FileResourceService;
import com.fixledger.modules.file.service.FileResourceService.FileDownloadResource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/families/{familyId}/files")
public class FileResourceController {

  private final FileResourceService fileResourceService;

  public FileResourceController(FileResourceService fileResourceService) {
    this.fileResourceService = fileResourceService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Result<FileResourceResponse> uploadFile(
      @PathVariable Long familyId,
      @RequestParam("file") MultipartFile file,
      @RequestParam("bizType") String bizType,
      @RequestParam("bizId") Long bizId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(fileResourceService.uploadFile(userId, familyId, bizType, bizId, file));
  }

  @GetMapping
  public Result<List<FileResourceResponse>> listFiles(
      @PathVariable Long familyId,
      @RequestParam("bizType") String bizType,
      @RequestParam("bizId") Long bizId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(fileResourceService.listFiles(userId, familyId, bizType, bizId));
  }

  @GetMapping("/{fileId}/download")
  public ResponseEntity<Resource> downloadFile(
      @PathVariable Long familyId,
      @PathVariable Long fileId
  ) {
    Long userId = CurrentUserContext.getUserId();
    FileDownloadResource download = fileResourceService.downloadFile(userId, familyId, fileId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(download.contentType()))
        .contentLength(download.fileSize())
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(download.originalName()))
        .body(download.resource());
  }

  @DeleteMapping("/{fileId}")
  public Result<Boolean> deleteFile(
      @PathVariable Long familyId,
      @PathVariable Long fileId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(fileResourceService.deleteFile(userId, familyId, fileId));
  }

  private String contentDisposition(String filename) {
    return ContentDisposition.attachment()
        .filename(filename, StandardCharsets.UTF_8)
        .build()
        .toString();
  }
}
