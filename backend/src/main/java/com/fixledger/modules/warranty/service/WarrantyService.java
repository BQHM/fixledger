package com.fixledger.modules.warranty.service;

import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.warranty.query.WarrantyExpiringQuery;
import com.fixledger.modules.warranty.request.CreateWarrantyRequest;
import com.fixledger.modules.warranty.request.UpdateWarrantyRequest;
import com.fixledger.modules.warranty.response.WarrantyResponse;
import java.util.List;

/**
 * 保修记录服务，负责设备保修信息维护和即将过保查询。
 */
public interface WarrantyService {

  /**
   * 查询指定设备下的保修记录。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 保修记录列表
   */
  List<WarrantyResponse> listDeviceWarranties(Long userId, Long familyId, Long deviceId);

  /**
   * 为设备创建保修记录，并校验保修日期边界。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 保修创建请求
   * @return 新保修记录
   */
  WarrantyResponse createWarranty(
      Long userId,
      Long familyId,
      Long deviceId,
      CreateWarrantyRequest request
  );

  /**
   * 更新保修记录。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param warrantyId 保修记录 ID
   * @param request 保修更新请求
   * @return 更新后的保修记录
   */
  WarrantyResponse updateWarranty(
      Long userId,
      Long familyId,
      Long warrantyId,
      UpdateWarrantyRequest request
  );

  /**
   * 逻辑删除保修记录。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param warrantyId 保修记录 ID
   * @return 是否删除成功
   */
  boolean deleteWarranty(Long userId, Long familyId, Long warrantyId);

  /**
   * 分页查询即将过保的记录，用于提醒页和首页看板。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 分页与提醒窗口条件
   * @return 即将过保记录分页结果
   */
  PageResponse<WarrantyResponse> pageExpiring(
      Long userId,
      Long familyId,
      WarrantyExpiringQuery query
  );
}