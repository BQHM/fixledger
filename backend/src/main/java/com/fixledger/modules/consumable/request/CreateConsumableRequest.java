package com.fixledger.modules.consumable.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateConsumableRequest(
    @NotBlank(message = "耗材名称不能为空")
    @Size(max = 128, message = "耗材名称最多 128 个字符")
    String name,

    @Size(max = 128, message = "品牌最多 128 个字符")
    String brand,

    @Size(max = 128, message = "型号最多 128 个字符")
    String model,

    @NotNull(message = "更换周期不能为空")
    @Min(value = 1, message = "更换周期必须大于 0")
    Integer cycleDays,

    @PastOrPresent(message = "上次更换日期不能晚于今天")
    LocalDate lastReplacedDate,

    @Min(value = 0, message = "提前提醒天数不能小于 0")
    Integer remindDaysBefore,

    @Size(max = 1024, message = "备注最多 1024 个字符")
    String remark
) {
}
