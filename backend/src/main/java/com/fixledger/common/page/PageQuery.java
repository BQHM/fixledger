package com.fixledger.common.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * <p>
 * 文件功能说明：分页结构组件，为各业务模块提供可复用能力。
 * </p>
 *
 * @Author FixLedger
 */
public class PageQuery {

  @Min(value = 1, message = "页码必须大于等于 1")
  private long pageNum = 1;

  @Min(value = 1, message = "每页数量必须大于等于 1")
  @Max(value = 100, message = "每页数量不能超过 100")
  private long pageSize = 10;
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：查询。
   * </p>
   * @return 查询结果
   */
  public long getPageNum() {
    return pageNum;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：执行业务处理。
   * </p>
   * @param pageNum pageNum 参数
   */
  public void setPageNum(long pageNum) {
    this.pageNum = pageNum;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：查询。
   * </p>
   * @return 查询结果
   */
  public long getPageSize() {
    return pageSize;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：执行业务处理。
   * </p>
   * @param pageSize pageSize 参数
   */
  public void setPageSize(long pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * @Author FixLedger
   * <p>
   * 功能说明：执行业务处理。
   * </p>
   * @return 处理结果
   */
  public <T> Page<T> toPage() {
    return Page.of(pageNum, pageSize);
  }
}
