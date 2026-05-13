package com.fixledger.modules.maintenance.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件功能说明：维修请求对象，承载前端提交的参数并配合校验注解使用。
 * </p>
 *
 * @Author FixLedger
 */
public record CreateMaintenanceRequest(
    @NotBlank(message = "维修标题不能为空")
    @Size(max = 128, message = "维修标题最多 128 个字符")
    String title,

    @NotBlank(message = "故障描述不能为空")
    @Size(max = 2048, message = "故障描述最多 2048 个字符")
    String faultDescription,

    @PastOrPresent(message = "故障发生时间不能晚于当前时间")
    LocalDateTime occurredAt,

    @Size(max = 128, message = "维修渠道最多 128 个字符")
    String repairChannel,

    @Size(max = 128, message = "维修联系方式最多 128 个字符")
    String repairContact
) {
}
