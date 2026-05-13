package com.fixledger.modules.maintenance.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件功能说明：维修请求对象，承载前端提交的参数并配合校验注解使用。
 * </p>
 *
 * @Author FixLedger
 */
public record UpdateMaintenanceStatusRequest(
    @NotBlank(message = "维修状态不能为空")
    String status,

    @Size(max = 2048, message = "处理结果最多 2048 个字符")
    String resultDescription,

    @DecimalMin(value = "0.00", message = "维修费用不能小于 0")
    BigDecimal repairCost,

    @PastOrPresent(message = "完成时间不能晚于当前时间")
    LocalDateTime completedAt,

    Boolean syncDeviceRepaired
) {
}
