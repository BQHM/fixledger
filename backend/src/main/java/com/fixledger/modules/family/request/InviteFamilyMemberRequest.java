package com.fixledger.modules.family.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 邀请已注册用户加入家庭空间的请求。
 */
public record InviteFamilyMemberRequest(
    @NotBlank(message = "账号不能为空")
    @Size(max = 128, message = "账号长度不能超过 128")
    String account,

    @NotBlank(message = "角色不能为空")
    @Size(max = 32, message = "角色长度不能超过 32")
    String role
) {
}
