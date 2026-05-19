package com.fixledger.modules.consumable.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：耗材请求对象，承载前端提交的参数并配合校验注解使用。
 * </p>
 *
 * @Author FixLedger
 */
public record CreateReplaceRecordRequest(
    @NotNull(message = "更换日期不能为空")
    @PastOrPresent(message = "更换日期不能晚于今天")
    LocalDate replacedDate,

    @DecimalMin(value = "0.00", message = "更换费用不能小于 0")
    @Digits(integer = 10, fraction = 2, message = "更换费用最多 10 位整数和 2 位小数")
    BigDecimal cost,

    @Size(max = 1024, message = "备注最多 1024 个字符")
    String note
) {
}
