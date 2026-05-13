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

/**
 * <p>
 * 文件功能说明：保修接口控制器，负责参数校验、认证上下文读取和调用业务服务。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@RestController
@RequestMapping("/api/families/{familyId}")
public class WarrantyController {

  private final WarrantyService warrantyService;

  public WarrantyController(WarrantyService warrantyService) {
    this.warrantyService = warrantyService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理保修查询列表接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 列表结果
   */
  @GetMapping("/devices/{deviceId}/warranties")
  public Result<List<WarrantyResponse>> listDeviceWarranties(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.listDeviceWarranties(userId, familyId, deviceId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理保修创建接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @PostMapping("/devices/{deviceId}/warranties")
  public Result<WarrantyResponse> createWarranty(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @Valid @RequestBody CreateWarrantyRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.createWarranty(userId, familyId, deviceId, request));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理保修更新接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param warrantyId 保修记录 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @PutMapping("/warranties/{warrantyId}")
  public Result<WarrantyResponse> updateWarranty(
      @PathVariable Long familyId,
      @PathVariable Long warrantyId,
      @Valid @RequestBody UpdateWarrantyRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.updateWarranty(userId, familyId, warrantyId, request));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理保修删除接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param warrantyId 保修记录 ID
   * @return 删除结果
   */
  @DeleteMapping("/warranties/{warrantyId}")
  public Result<Boolean> deleteWarranty(
      @PathVariable Long familyId,
      @PathVariable Long warrantyId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.deleteWarranty(userId, familyId, warrantyId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理保修分页查询接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @GetMapping("/warranties/expiring")
  public Result<PageResponse<WarrantyResponse>> pageExpiring(
      @PathVariable Long familyId,
      @Valid WarrantyExpiringQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(warrantyService.pageExpiring(userId, familyId, query));
  }
}
