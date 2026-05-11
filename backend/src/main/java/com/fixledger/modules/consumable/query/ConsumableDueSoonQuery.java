package com.fixledger.modules.consumable.query;

import com.fixledger.common.page.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ConsumableDueSoonQuery extends PageQuery {

  @Min(value = 0, message = "查询天数不能小于 0")
  @Max(value = 3650, message = "查询天数不能超过 3650")
  private Integer days = 7;

  public Integer getDays() {
    return days;
  }

  public void setDays(Integer days) {
    this.days = days;
  }
}
