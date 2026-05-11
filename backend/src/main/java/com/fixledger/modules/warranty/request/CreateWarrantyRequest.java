package com.fixledger.modules.warranty.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateWarrantyRequest(
    @Size(max = 32, message = "保修类型最多 32 个字符")
    String warrantyType,

    @NotNull(message = "保修开始日期不能为空")
    LocalDate startDate,

    @NotNull(message = "保修结束日期不能为空")
    LocalDate endDate,

    @Min(value = 0, message = "提前提醒天数不能小于 0")
    Integer remindDaysBefore,

    @Size(max = 64, message = "售后电话最多 64 个字符")
    String servicePhone,

    @Size(max = 255, message = "售后地址最多 255 个字符")
    String serviceAddress,

    @Size(max = 1024, message = "服务备注最多 1024 个字符")
    String serviceNote
) {
}
