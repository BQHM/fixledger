package com.fixledger.modules.consumable.controller;

import com.fixledger.common.page.PageResponse;
import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.consumable.query.ConsumableDueSoonQuery;
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.request.CreateReplaceRecordRequest;
import com.fixledger.modules.consumable.request.UpdateConsumableRequest;
import com.fixledger.modules.consumable.response.ConsumableReplaceRecordResponse;
import com.fixledger.modules.consumable.response.ConsumableResponse;
import com.fixledger.modules.consumable.service.ConsumableService;
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
 * 文件功能说明：耗材接口控制器，负责参数校验、认证上下文读取和调用业务服务。
 * </p>
 *
 * @Author FixLedger
 */
@Validated
@RestController
@RequestMapping("/api/families/{familyId}")
public class ConsumableController {

  private final ConsumableService consumableService;

  public ConsumableController(ConsumableService consumableService) {
    this.consumableService = consumableService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理耗材查询列表接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 列表结果
   */
  @GetMapping("/devices/{deviceId}/consumables")
  public Result<List<ConsumableResponse>> listDeviceConsumables(
      @PathVariable Long familyId,
      @PathVariable Long deviceId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(consumableService.listDeviceConsumables(userId, familyId, deviceId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理耗材创建接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @PostMapping("/devices/{deviceId}/consumables")
  public Result<ConsumableResponse> createConsumable(
      @PathVariable Long familyId,
      @PathVariable Long deviceId,
      @Valid @RequestBody CreateConsumableRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        consumableService.createConsumable(userId, familyId, deviceId, request)
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理耗材更新接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @PutMapping("/consumables/{consumableId}")
  public Result<ConsumableResponse> updateConsumable(
      @PathVariable Long familyId,
      @PathVariable Long consumableId,
      @Valid @RequestBody UpdateConsumableRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        consumableService.updateConsumable(userId, familyId, consumableId, request)
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理耗材删除接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @return 删除结果
   */
  @DeleteMapping("/consumables/{consumableId}")
  public Result<Boolean> deleteConsumable(
      @PathVariable Long familyId,
      @PathVariable Long consumableId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(consumableService.deleteConsumable(userId, familyId, consumableId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理耗材创建接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @param request 请求参数
   * @return 创建后的数据
   */
  @PostMapping("/consumables/{consumableId}/replace-records")
  public Result<ConsumableReplaceRecordResponse> createReplaceRecord(
      @PathVariable Long familyId,
      @PathVariable Long consumableId,
      @Valid @RequestBody CreateReplaceRecordRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        consumableService.createReplaceRecord(userId, familyId, consumableId, request)
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理耗材查询列表接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @return 列表结果
   */
  @GetMapping("/consumables/{consumableId}/replace-records")
  public Result<List<ConsumableReplaceRecordResponse>> listReplaceRecords(
      @PathVariable Long familyId,
      @PathVariable Long consumableId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(consumableService.listReplaceRecords(userId, familyId, consumableId));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理耗材分页查询接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param query 查询条件
   * @return 分页结果
   */
  @GetMapping("/consumables/due-soon")
  public Result<PageResponse<ConsumableResponse>> pageDueSoon(
      @PathVariable Long familyId,
      @Valid ConsumableDueSoonQuery query
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(consumableService.pageDueSoon(userId, familyId, query));
  }
}
