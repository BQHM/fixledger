package com.fixledger.modules.reminder.query;

import com.fixledger.common.page.PageQuery;

/**
 * <p>
 * 文件功能说明：提醒通知查询对象，封装分页、筛选和排序条件。
 * </p>
 *
 * @Author FixLedger
 */
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
