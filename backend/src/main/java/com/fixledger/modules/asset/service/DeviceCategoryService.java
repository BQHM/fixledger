package com.fixledger.modules.asset.service;

import com.fixledger.modules.asset.request.CreateDeviceCategoryRequest;
import com.fixledger.modules.asset.request.UpdateDeviceCategoryRequest;
import com.fixledger.modules.asset.response.DeviceCategoryResponse;
import java.util.List;

/**
 * 设备分类服务，用于家庭内设备档案的轻量分组。
 */
public interface DeviceCategoryService {

  /**
   * 查询家庭内可用设备分类。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @return 分类列表
   */
  List<DeviceCategoryResponse> listCategories(Long userId, Long familyId);

  /**
   * 创建家庭自定义设备分类。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 分类创建请求
   * @return 新分类信息
   */
  DeviceCategoryResponse createCategory(
      Long userId,
      Long familyId,
      CreateDeviceCategoryRequest request
  );

  /**
   * 更新家庭自定义设备分类。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param categoryId 分类 ID
   * @param request 分类更新请求
   * @return 更新后的分类信息
   */
  DeviceCategoryResponse updateCategory(
      Long userId,
      Long familyId,
      Long categoryId,
      UpdateDeviceCategoryRequest request
  );

  /**
   * 删除未被设备使用的自定义分类。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param categoryId 分类 ID
   * @return 是否删除成功
   */
  boolean deleteCategory(Long userId, Long familyId, Long categoryId);
}
