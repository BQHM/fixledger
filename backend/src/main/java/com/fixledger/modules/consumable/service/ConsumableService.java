package com.fixledger.modules.consumable.service;

import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.consumable.query.ConsumableDueSoonQuery;
import com.fixledger.modules.consumable.request.CreateConsumableRequest;
import com.fixledger.modules.consumable.request.CreateReplaceRecordRequest;
import com.fixledger.modules.consumable.request.UpdateConsumableRequest;
import com.fixledger.modules.consumable.response.ConsumableReplaceRecordResponse;
import com.fixledger.modules.consumable.response.ConsumableResponse;
import java.util.List;

/**
 * 耗材服务，维护耗材更换周期、提醒日期和更换历史。
 */
public interface ConsumableService {

  /**
   * 查询设备下的耗材项目。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 耗材列表
   */
  List<ConsumableResponse> listDeviceConsumables(Long userId, Long familyId, Long deviceId);

  /**
   * 为设备创建耗材项目，并根据最近更换日期计算下次提醒日期。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 耗材创建请求
   * @return 新耗材信息
   */
  ConsumableResponse createConsumable(
      Long userId,
      Long familyId,
      Long deviceId,
      CreateConsumableRequest request
  );

  /**
   * 更新耗材配置，周期或最近更换日期变化时重新计算提醒状态。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @param request 耗材更新请求
   * @return 更新后的耗材信息
   */
  ConsumableResponse updateConsumable(
      Long userId,
      Long familyId,
      Long consumableId,
      UpdateConsumableRequest request
  );

  /**
   * 逻辑删除耗材项目。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @return 是否删除成功
   */
  boolean deleteConsumable(Long userId, Long familyId, Long consumableId);

  /**
   * 记录一次耗材更换，并同步刷新最近更换日期和下次提醒日期。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @param request 更换记录请求
   * @return 新更换记录
   */
  ConsumableReplaceRecordResponse createReplaceRecord(
      Long userId,
      Long familyId,
      Long consumableId,
      CreateReplaceRecordRequest request
  );

  /**
   * 查询耗材更换历史。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param consumableId 耗材 ID
   * @return 更换记录列表
   */
  List<ConsumableReplaceRecordResponse> listReplaceRecords(
      Long userId,
      Long familyId,
      Long consumableId
  );

  /**
   * 分页查询即将到期或已逾期的耗材。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 分页与提醒窗口条件
   * @return 耗材分页结果
   */
  PageResponse<ConsumableResponse> pageDueSoon(
      Long userId,
      Long familyId,
      ConsumableDueSoonQuery query
  );
}