package com.fixledger.common.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PageQuery {

  @Min(value = 1, message = "页码必须大于等于 1")
  private long pageNum = 1;

  @Min(value = 1, message = "每页数量必须大于等于 1")
  @Max(value = 100, message = "每页数量不能超过 100")
  private long pageSize = 10;

  public long getPageNum() {
    return pageNum;
  }

  public void setPageNum(long pageNum) {
    this.pageNum = pageNum;
  }

  public long getPageSize() {
    return pageSize;
  }

  public void setPageSize(long pageSize) {
    this.pageSize = pageSize;
  }

  public <T> Page<T> toPage() {
    return Page.of(pageNum, pageSize);
  }
}
