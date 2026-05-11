package com.fixledger.modules.warranty.controller;

import com.fixledger.common.page.PageResponse;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.warranty.query.WarrantyExpiringQuery;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.request.UpdateWarrantyRequest;
import com.fixledger.modules.warranty.response.WarrantyResponse;
import com.fixledger.modules.warranty.service.WarrantyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/families/{familyId}")
public class WarrantyController {

  private final WarrantyService warrantyService;

  public WarrantyController(WarrantyService warrantyService) {
    this.warrantyService = warrantyService;
  }

  @GetMapping("/devices/{deviceId}/warranties")
  public Result<List<WarrantyResponse>> listDeviceWarranties(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.listDeviceWarranties(userId, familyId, deviceId));
  }

  @PostMapping("/devices/{deviceId}/warranties")
  public Result<WarrantyResponse> createWarranty(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @Valid @RequestBody CreateWarrantyRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.createWarranty(userId, familyId, deviceId, request));
  }

  @PutMapping("/warranties/{warrantyId}")
  public Result<WarrantyResponse> updateWarranty(
      @PathVariable Long familyId,
      @PathVariable Long warrantyId,
      @Valid @RequestBody UpdateWarrantyRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.updateWarranty(userId, familyId, warrantyId, request));
  }

  @DeleteMapping("/warranties/{warrantyId}")
  public Result<Boolean> deleteWarranty(
      @PathVariable Long familyId,
      @PathVariable Long warrantyId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.deleteWarranty(userId, familyId, warrantyId));
  }

  @GetMapping("/warranties/expiring")
  public Result<PageResponse<WarrantyResponse>> pageExpiring(
      @PathVariable Long familyId,
      @Valid WarrantyExpiringQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.pageExpiring(userId, familyId, query));
  }
}
