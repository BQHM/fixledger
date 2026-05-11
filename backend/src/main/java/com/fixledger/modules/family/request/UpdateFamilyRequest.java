package com.fixledger.modules.family.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFamilyRequest(
    @NotBlank(message = "家庭空间名称不能为空")
    @Size(max = 128, message = "家庭空间名称最多 128 个字符")
    String name,

    @Size(max = 512, message = "家庭空间描述最多 512 个字符")
    String description
) {
}
