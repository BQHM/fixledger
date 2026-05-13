package com.fixledger.modules.asset.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * 文件功能说明：设备档案请求对象，承载前端提交的参数并配合校验注解使用。
 * </p>
 *
 * @Author FixLedger
 */
public record CreateDeviceCategoryRequest(
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称最多 64 个字符")
    String name,

    @Size(max = 128, message = "分类图标最多 128 个字符")
    String icon,

    @Min(value = 0, message = "排序值不能小于 0")
    Integer sortOrder
) {
}
