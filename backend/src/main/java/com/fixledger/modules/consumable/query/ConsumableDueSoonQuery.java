package com.fixledger.modules.consumable.query;

import com.fixledger.common.page.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * <p>
 * 文件功能说明：耗材查询对象，封装分页、筛选和排序条件。
 * </p>
 *
 * @Author FixLedger
 */
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
