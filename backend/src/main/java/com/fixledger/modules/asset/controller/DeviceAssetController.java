package com.fixledger.modules.asset.controller;

import com.fixledger.common.page.PageResponse;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.asset.query.DevicePageQuery;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.request.UpdateDeviceRequest;
import com.fixledger.modules.asset.request.UpdateDeviceStatusRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceDetailResponse;
import com.fixledger.modules.asset.response.DeviceListResponse;
import com.fixledger.modules.asset.service.DeviceAssetService;
import com.fixledger.modules.file.response.CredentialBoxResponse;
import com.fixledger.modules.file.response.ManualSearchResponse;
import com.fixledger.modules.file.service.FileResourceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 文件功能说明：设备档案接口控制器，负责参数校验、认证上下文读取和调用业务服务。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@RestController
@RequestMapping("/api/families/{familyId}/devices")
public class DeviceAssetController {

  private final DeviceAssetService deviceAssetService;
  private final FileResourceService fileResourceService;

  public DeviceAssetController(
      DeviceAssetService deviceAssetService,
      FileResourceService fileResourceService
  ) {
    this.deviceAssetService = deviceAssetService;
    this.fileResourceService = fileResourceService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案分页查询接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @GetMapping
  public Result<PageResponse<DeviceListResponse>> pageDevices(
      @PathVariable Long familyId,
      @Valid DevicePageQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.pageDevices(userId, familyId, query));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案创建接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @PostMapping
  public Result<CreateDeviceResponse> createDevice(
      @PathVariable Long familyId,
      @Valid @RequestBody CreateDeviceRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.createDevice(userId, familyId, request));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案查询接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 查询结果
   */
  @GetMapping("/{deviceId}")
  public Result<DeviceDetailResponse> getDeviceDetail(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.getDeviceDetail(userId, familyId, deviceId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备凭证盒聚合查询接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 凭证盒聚合数据
   */
  @GetMapping("/{deviceId}/credential-box")
  public Result<CredentialBoxResponse> getCredentialBox(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(fileResourceService.getCredentialBox(userId, familyId, deviceId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备说明书全文搜索接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param keyword 搜索关键词
   * @return 说明书搜索结果
   */
  @GetMapping("/{deviceId}/manuals/search")
  public Result<List<ManualSearchResponse>> searchManuals(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @RequestParam("keyword") String keyword
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(fileResourceService.searchManuals(userId, familyId, deviceId, keyword));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案更新接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @PutMapping("/{deviceId}")
  public Result<DeviceDetailResponse> updateDevice(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @Valid @RequestBody UpdateDeviceRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.updateDevice(userId, familyId, deviceId, request));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案删除接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 删除结果
   */
  @DeleteMapping("/{deviceId}")
  public Result<Boolean> deleteDevice(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.deleteDevice(userId, familyId, deviceId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案更新接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @PatchMapping("/{deviceId}/status")
  public Result<DeviceDetailResponse> updateDeviceStatus(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @Valid @RequestBody UpdateDeviceStatusRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        deviceAssetService.updateDeviceStatus(userId, familyId, deviceId, request)
    );
  }
}
