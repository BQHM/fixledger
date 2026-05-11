package com.fixledger.modules.reminder.query;

import com.fixledger.common.page.PageQuery;

public class ReminderPageQuery extends PageQuery {

  private String status;

  private String type;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
