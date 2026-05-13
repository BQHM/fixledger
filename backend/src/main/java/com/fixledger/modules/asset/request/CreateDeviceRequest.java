package com.fixledger.modules.asset.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 文件功能说明：设备档案请求对象，承载前端提交的参数并配合校验注解使用。
 * </p>
 *
 * @Author FixLedger
 */
public record CreateDeviceRequest(
    Long categoryId,

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 128, message = "设备名称最多 128 个字符")
    String name,

    @Size(max = 128, message = "品牌最多 128 个字符")
    String brand,

    @Size(max = 128, message = "型号最多 128 个字符")
    String model,

    @Size(max = 128, message = "序列号最多 128 个字符")
    String serialNumber,

    @NotNull(message = "购买日期不能为空")
    @PastOrPresent(message = "购买日期不能晚于今天")
    LocalDate purchaseDate,

    @Size(max = 128, message = "购买渠道最多 128 个字符")
    String purchaseChannel,

    @DecimalMin(value = "0.00", message = "购买价格不能小于 0")
    BigDecimal purchasePrice,

    @Size(max = 128, message = "存放位置最多 128 个字符")
    String location,

    @Size(max = 1024, message = "备注最多 1024 个字符")
    String remark
) {
}
