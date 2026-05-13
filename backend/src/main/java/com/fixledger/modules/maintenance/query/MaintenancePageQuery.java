package com.fixledger.modules.maintenance.query;

import com.fixledger.common.page.PageQuery;

/**
 * <p>
 * 文件功能说明：维修查询对象，封装分页、筛选和排序条件。
 * </p>
 *
 * @Author FixLedger
 */
public class MaintenancePageQuery extends PageQuery {

  private Long deviceId;

  private String status;

  public Long getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(Long deviceId) {
    this.deviceId = deviceId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
