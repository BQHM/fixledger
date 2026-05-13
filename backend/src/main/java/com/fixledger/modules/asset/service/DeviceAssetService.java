package com.fixledger.modules.asset.service;

import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.asset.query.DevicePageQuery;
import com.fixledger.modules.asset.request.CreateDeviceRequest;
import com.fixledger.modules.asset.request.UpdateDeviceRequest;
import com.fixledger.modules.asset.request.UpdateDeviceStatusRequest;
import com.fixledger.modules.asset.response.CreateDeviceResponse;
import com.fixledger.modules.asset.response.DeviceDetailResponse;
import com.fixledger.modules.asset.response.DeviceListResponse;

/**
 * 设备档案服务，维护家庭设备从创建到状态流转的核心记录。
 */
public interface DeviceAssetService {

  /**
   * 分页查询家庭设备列表，支持关键字、分类、状态和品牌筛选。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 分页与筛选条件
   * @return 设备列表分页结果
   */
  PageResponse<DeviceListResponse> pageDevices(Long userId, Long familyId, DevicePageQuery query);

  /**
   * 创建设备档案，默认设备状态为正常。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param request 创建设备请求
   * @return 新设备 ID
   */
  CreateDeviceResponse createDevice(Long userId, Long familyId, CreateDeviceRequest request);

  /**
   * 查询设备详情，详情聚合字段由后续业务模块逐步补齐。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 设备详情
   */
  DeviceDetailResponse getDeviceDetail(Long userId, Long familyId, Long deviceId);

  /**
   * 更新设备基础资料，不直接修改设备状态。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 更新设备请求
   * @return 更新后的设备详情
   */
  DeviceDetailResponse updateDevice(
      Long userId,
      Long familyId,
      Long deviceId,
      UpdateDeviceRequest request
  );

  /**
   * 逻辑删除设备档案，保留生命周期历史数据。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @return 是否删除成功
   */
  boolean deleteDevice(Long userId, Long familyId, Long deviceId);

  /**
   * 通过受控入口修改设备状态，避免绕过状态流转规则。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 状态更新请求
   * @return 更新后的设备详情
   */
  DeviceDetailResponse updateDeviceStatus(
      Long userId,
      Long familyId,
      Long deviceId,
      UpdateDeviceStatusRequest request
  );
}
