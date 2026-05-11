package com.fixledger.modules.consumable.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReplaceRecordRequest(
    @NotNull(message = "更换日期不能为空")
    @PastOrPresent(message = "更换日期不能晚于今天")
    LocalDate replacedDate,

    @DecimalMin(value = "0.00", message = "更换费用不能小于 0")
    BigDecimal cost,

    @Size(max = 1024, message = "备注最多 1024 个字符")
    String note
) {
}
