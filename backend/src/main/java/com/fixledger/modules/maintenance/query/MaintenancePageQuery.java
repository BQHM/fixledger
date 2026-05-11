package com.fixledger.modules.maintenance.query;

import com.fixledger.common.page.PageQuery;

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
