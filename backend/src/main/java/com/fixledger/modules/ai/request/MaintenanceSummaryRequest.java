package com.fixledger.modules.ai.request;

import jakarta.validation.constraints.NotNull;

/**
 * <p>
 * 文件功能说明：AI 辅助请求对象，承载前端提交的参数并配合校验注解使用。
 * </p>
 *
 * @Author FixLedger
 */
public record MaintenanceSummaryRequest(
    @NotNull(message = "设备 ID 不能为空")
    Long deviceId
) {
}
