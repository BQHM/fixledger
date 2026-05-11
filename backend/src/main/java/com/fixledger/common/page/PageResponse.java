package com.fixledger.common.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

public record PageResponse<T>(
    long pageNum,
    long pageSize,
    long total,
    long pages,
    List<T> records
) {

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
