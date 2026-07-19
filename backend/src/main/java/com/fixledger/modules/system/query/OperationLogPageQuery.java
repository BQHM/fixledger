package com.fixledger.modules.system.query;

import com.fixledger.common.page.PageQuery;
import jakarta.validation.constraints.Size;

/**
 * 操作日志分页查询条件。
 */
public class OperationLogPageQuery extends PageQuery {

  private Long familyId;

  @Size(max = 64, message = "模块长度不能超过 64")
  private String module;

  @Size(max = 64, message = "动作长度不能超过 64")
  private String action;

  public Long getFamilyId() {
    return familyId;
  }

  public void setFamilyId(Long familyId) {
    this.familyId = familyId;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
