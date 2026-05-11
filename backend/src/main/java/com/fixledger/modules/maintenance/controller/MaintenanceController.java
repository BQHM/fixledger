package com.fixledger.modules.maintenance.controller;

import com.fixledger.common.page.PageResponse;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.maintenance.query.MaintenancePageQuery;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceStatusRequest;
import com.fixledger.modules.maintenance.response.MaintenanceCostSummaryResponse;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import com.fixledger.modules.maintenance.service.MaintenanceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
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

@Validated
@RestController
@RequestMapping("/api/families/{familyId}")
public class MaintenanceController {

  private final MaintenanceService maintenanceService;

  public MaintenanceController(MaintenanceService maintenanceService) {
    this.maintenanceService = maintenanceService;
  }

  @GetMapping("/maintenance-records")
  public Result<PageResponse<MaintenanceResponse>> pageMaintenance(
      @PathVariable Long familyId,
      @Valid MaintenancePageQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(maintenanceService.pageMaintenance(userId, familyId, query));
  }

  @PostMapping("/devices/{deviceId}/maintenance-records")
  public Result<MaintenanceResponse> createMaintenance(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @Valid @RequestBody CreateMaintenanceRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        maintenanceService.createMaintenance(userId, familyId, deviceId, request)
    );
  }

  @GetMapping("/maintenance-records/{maintenanceId}")
  public Result<MaintenanceResponse> getMaintenanceDetail(
      @PathVariable Long familyId,
      @PathVariable Long maintenanceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        maintenanceService.getMaintenanceDetail(userId, familyId, maintenanceId)
    );
  }

  @PutMapping("/maintenance-records/{maintenanceId}")
  public Result<MaintenanceResponse> updateMaintenance(
      @PathVariable Long familyId,
      @PathVariable Long maintenanceId,
      @Valid @RequestBody UpdateMaintenanceRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        maintenanceService.updateMaintenance(userId, familyId, maintenanceId, request)
    );
  }

  @PatchMapping("/maintenance-records/{maintenanceId}/status")
  public Result<MaintenanceResponse> updateMaintenanceStatus(
      @PathVariable Long familyId,
      @PathVariable Long maintenanceId,
      @Valid @RequestBody UpdateMaintenanceStatusRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        maintenanceService.updateMaintenanceStatus(userId, familyId, maintenanceId, request)
    );
  }

  @DeleteMapping("/maintenance-records/{maintenanceId}")
  public Result<Boolean> deleteMaintenance(
      @PathVariable Long familyId,
      @PathVariable Long maintenanceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(maintenanceService.deleteMaintenance(userId, familyId, maintenanceId));
  }

  @GetMapping("/maintenance-records/cost-summary")
  public Result<MaintenanceCostSummaryResponse> costSummary(
      @PathVariable Long familyId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate endDate
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(maintenanceService.costSummary(userId, familyId, startDate, endDate));
  }
}
