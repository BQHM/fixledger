package com.fixledger.modules.family.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * 文件功能说明：家庭空间请求对象，承载前端提交的参数并配合校验注解使用。
 * </p>
 *
 * @Author FixLedger
 */
public record CreateFamilyRequest(
    @NotBlank(message = "家庭空间名称不能为空")
    @Size(max = 128, message = "家庭空间名称最多 128 个字符")
    String name,

    @Size(max = 512, message = "家庭空间描述最多 512 个字符")
    String description
) {
}
