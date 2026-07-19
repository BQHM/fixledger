package com.fixledger.modules.family.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 调整家庭成员角色的请求。
 */
public record UpdateFamilyMemberRoleRequest(
    @NotBlank(message = "角色不能为空")
    @Size(max = 32, message = "角色长度不能超过 32")
    String role
) {
}
