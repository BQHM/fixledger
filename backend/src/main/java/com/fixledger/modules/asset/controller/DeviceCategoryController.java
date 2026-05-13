package com.fixledger.modules.asset.controller;

import com.fixledger.common.result.Result;
import com.fixledger.common.security.CurrentUserContext;
import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.UpdateDeviceCategoryRequest;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import com.fixledger.modules.asset.service.DeviceCategoryService;
import jakarta.validation.Valid;
import java.util.List;
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
 * 文件功能说明：设备档案接口控制器，负责参数校验、认证上下文读取和调用业务服务。
 * </p>
 *
 * @Author FixLedger
 */
@RestController
@RequestMapping("/api/families/{familyId}/device-categories")
public class DeviceCategoryController {

  private final DeviceCategoryService deviceCategoryService;

  public DeviceCategoryController(DeviceCategoryService deviceCategoryService) {
    this.deviceCategoryService = deviceCategoryService;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案查询列表接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @return 列表结果
   */
  @GetMapping
  public Result<List<DeviceCategoryResponse>> listCategories(@PathVariable Long familyId) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceCategoryService.listCategories(userId, familyId));
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
  public Result<DeviceCategoryResponse> createCategory(
      @PathVariable Long familyId,
      @Valid @RequestBody CreateDeviceCategoryRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceCategoryService.createCategory(userId, familyId, request));
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案更新接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param categoryId 设备分类 ID
   * @param request 请求参数
   * @return 更新后的数据
   */
  @PutMapping("/{categoryId}")
  public Result<DeviceCategoryResponse> updateCategory(
      @PathVariable Long familyId,
      @PathVariable Long categoryId,
      @Valid @RequestBody UpdateDeviceCategoryRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(
        deviceCategoryService.updateCategory(userId, familyId, categoryId, request)
    );
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：处理设备档案删除接口请求。
   * </p>
   * @param familyId 家庭空间 ID
   * @param categoryId 设备分类 ID
   * @return 删除结果
   */
  @DeleteMapping("/{categoryId}")
  public Result<Boolean> deleteCategory(
      @PathVariable Long familyId,
      @PathVariable Long categoryId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceCategoryService.deleteCategory(userId, familyId, categoryId));
  }
}
