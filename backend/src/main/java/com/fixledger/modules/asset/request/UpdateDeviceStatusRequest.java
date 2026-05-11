package com.fixledger.modules.asset.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDeviceStatusRequest(
    @NotBlank(message = "设备状态不能为空")
    String status,

    @Size(max = 255, message = "状态变更原因最多 255 个字符")
    String reason
) {
}
