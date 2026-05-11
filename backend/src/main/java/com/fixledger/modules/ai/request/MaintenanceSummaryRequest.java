package com.fixledger.modules.ai.request;

import jakarta.validation.constraints.NotNull;

public record MaintenanceSummaryRequest(
    @NotNull(message = "设备 ID 不能为空")
    Long deviceId
) {
}
