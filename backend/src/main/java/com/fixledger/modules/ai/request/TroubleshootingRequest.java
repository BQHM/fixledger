package com.fixledger.modules.ai.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TroubleshootingRequest(
    @NotNull(message = "设备 ID 不能为空")
    Long deviceId,

    Long maintenanceId,

    @NotBlank(message = "故障描述不能为空")
    @Size(max = 1000, message = "故障描述最多 1000 个字符")
    String faultDescription
) {
}
