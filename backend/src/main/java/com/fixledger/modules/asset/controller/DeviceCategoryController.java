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

@RestController
@RequestMapping("/api/families/{familyId}/device-categories")
public class DeviceCategoryController {

  private final DeviceCategoryService deviceCategoryService;

  public DeviceCategoryController(DeviceCategoryService deviceCategoryService) {
    this.deviceCategoryService = deviceCategoryService;
  }

  @GetMapping
  public Result<List<DeviceCategoryResponse>> listCategories(@PathVariable Long familyId) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceCategoryService.listCategories(userId, familyId));
  }

  @PostMapping
  public Result<DeviceCategoryResponse> createCategory(
      @PathVariable Long familyId,
      @Valid @RequestBody CreateDeviceCategoryRequest request
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceCategoryService.createCategory(userId, familyId, request));
  }

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

  @DeleteMapping("/{categoryId}")
  public Result<Boolean> deleteCategory(
      @PathVariable Long familyId,
      @PathVariable Long categoryId
  ) {
    Long userId = CurrentUserContext.getUserId();
    return Result.success(deviceCategoryService.deleteCategory(userId, familyId, categoryId));
  }
}
