package com.fixledger.modules.maintenance.service;

import com.fixledger.common.page.PageResponse;
import com.fixledger.modules.maintenance.query.MaintenancePageQuery;
import com.fixledger.modules.maintenance.request.CreateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceRequest;
import com.fixledger.modules.maintenance.request.UpdateMaintenanceStatusRequest;
import com.fixledger.modules.maintenance.response.MaintenanceCostSummaryResponse;
import com.fixledger.modules.maintenance.response.MaintenanceResponse;
import java.time.LocalDate;

/**
 * 维修记录服务，负责故障登记、维修状态流转和费用统计。
 */
public interface MaintenanceService {

  /**
   * 为设备创建维修记录，初始状态为待处理。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param deviceId 设备 ID
   * @param request 维修创建请求
   * @return 新维修记录
   */
  MaintenanceResponse createMaintenance(
      Long userId,
      Long familyId,
      Long deviceId,
      CreateMaintenanceRequest request
  );

  /**
   * 分页查询维修记录，支持按设备和状态筛选。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param query 分页与筛选条件
   * @return 维修记录分页结果
   */
  PageResponse<MaintenanceResponse> pageMaintenance(
      Long userId,
      Long familyId,
      MaintenancePageQuery query
  );

  /**
   * 查询维修记录详情。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param maintenanceId 维修记录 ID
   * @return 维修记录详情
   */
  MaintenanceResponse getMaintenanceDetail(Long userId, Long familyId, Long maintenanceId);

  /**
   * 更新未完成、未取消的维修记录基础信息。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param maintenanceId 维修记录 ID
   * @param request 维修更新请求
   * @return 更新后的维修记录
   */
  MaintenanceResponse updateMaintenance(
      Long userId,
      Long familyId,
      Long maintenanceId,
      UpdateMaintenanceRequest request
  );

  /**
   * 通过状态机推进维修记录状态，必要时同步设备状态。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param maintenanceId 维修记录 ID
   * @param request 状态更新请求
   * @return 更新后的维修记录
   */
  MaintenanceResponse updateMaintenanceStatus(
      Long userId,
      Long familyId,
      Long maintenanceId,
      UpdateMaintenanceStatusRequest request
  );

  /**
   * 逻辑删除维修记录。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param maintenanceId 维修记录 ID
   * @return 是否删除成功
   */
  boolean deleteMaintenance(Long userId, Long familyId, Long maintenanceId);

  /**
   * 统计指定日期范围内的维修费用，已取消记录不计入。
   *
   * @param userId 当前用户 ID
   * @param familyId 家庭空间 ID
   * @param startDate 开始日期，可为空
   * @param endDate 结束日期，可为空
   * @return 费用合计和记录数量
   */
  MaintenanceCostSummaryResponse costSummary(
      Long userId,
      Long familyId,
      LocalDate startDate,
      LocalDate endDate
  );
}