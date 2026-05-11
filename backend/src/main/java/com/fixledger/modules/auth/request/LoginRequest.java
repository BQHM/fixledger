package com.fixledger.modules.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "账号不能为空")
    @Size(max = 128, message = "账号最多 128 个字符")
    String account,

    @NotBlank(message = "密码不能为空")
    @Size(max = 64, message = "密码最多 64 个字符")
    String password
) {
}
