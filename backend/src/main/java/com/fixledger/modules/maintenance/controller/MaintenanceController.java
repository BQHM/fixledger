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

/**
 * <p>
 * 文件功能说明：维修接口控制器，负责参数校验、认证上下文读取和调用业务服务。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@RestController
@RequestMapping("/api/families/{familyId}")
public class MaintenanceController {

  private final MaintenanceService maintenanceService;

  public MaintenanceController(MaintenanceService maintenanceService) {
    this.maintenanceService = maintenanceService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理维修分页查询接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @GetMapping("/maintenance-records")
  public Result<PageResponse<MaintenanceResponse>> pageMaintenance(
      @PathVariable Long familyId,
      @Valid MaintenancePageQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(maintenanceService.pageMaintenance(userId, familyId, query));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理维修创建接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
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

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理维修查询接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param maintenanceId 维修记录 ID
   * @return 查询结果
   */
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

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理维修更新接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param maintenanceId 维修记录 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
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

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理维修更新接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param maintenanceId 维修记录 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
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

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理维修删除接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param maintenanceId 维修记录 ID
   * @return 删除结果
   */
  @DeleteMapping("/maintenance-records/{maintenanceId}")
  public Result<Boolean> deleteMaintenance(
      @PathVariable Long familyId,
      @PathVariable Long maintenanceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(maintenanceService.deleteMaintenance(userId, familyId, maintenanceId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理维修执行业务处理接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param startDate startDate 参数
   * @param endDate endDate 参数
   * @return 统一响应结果
   */
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
