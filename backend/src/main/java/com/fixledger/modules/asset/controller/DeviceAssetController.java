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
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/families/{familyId}/devices")
public class DeviceAssetController {

  private final DeviceAssetService deviceAssetService;

  public DeviceAssetController(DeviceAssetService deviceAssetService) {
    this.deviceAssetService = deviceAssetService;
  }

  @GetMapping
  public Result<PageResponse<DeviceListResponse>> pageDevices(
      @PathVariable Long familyId,
      @Valid DevicePageQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.pageDevices(userId, familyId, query));
  }

  @PostMapping
  public Result<CreateDeviceResponse> createDevice(
      @PathVariable Long familyId,
      @Valid @RequestBody CreateDeviceRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.createDevice(userId, familyId, request));
  }

  @GetMapping("/{deviceId}")
  public Result<DeviceDetailResponse> getDeviceDetail(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.getDeviceDetail(userId, familyId, deviceId));
  }

  @PutMapping("/{deviceId}")
  public Result<DeviceDetailResponse> updateDevice(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @Valid @RequestBody UpdateDeviceRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.updateDevice(userId, familyId, deviceId, request));
  }

  @DeleteMapping("/{deviceId}")
  public Result<Boolean> deleteDevice(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceAssetService.deleteDevice(userId, familyId, deviceId));
  }

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
