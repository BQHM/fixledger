package com.fixledger.common.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 * <p>
 * 文件功能说明：分页结构组件，为各业务模块提供可复用能力。
 * </p>
 *
 * @Author FixLedger
 */
public record PageResponse<T>(
    long pageNum,
    long pageSize,
    long total,
    long pages,
    List<T> records
) {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：执行业务处理。
   * </p>
   * @param page page 参数
   * @return 业务响应数据
   */
  public static <T> PageResponse<T> from(IPage<T> page) {
    return new PageResponse<>(
        page.getCurrent(),
        page.getSize(),
        page.getTotal(),
        page.getPages(),
        page.getRecords()
    );
  }
}
