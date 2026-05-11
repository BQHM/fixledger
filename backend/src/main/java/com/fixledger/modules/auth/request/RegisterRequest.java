package com.fixledger.modules.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名最多 64 个字符")
    String username,

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱最多 128 个字符")
    String email,

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须在 6 到 64 个字符之间")
    String password,

    @Size(max = 64, message = "昵称最多 64 个字符")
    String nickname
) {
}
