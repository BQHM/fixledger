package com.fixledger.modules.ai.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * 文件功能说明：AI 辅助请求对象，承载前端提交的参数并配合校验注解使用。
 * </p>
 *
 * @Author FixLedger
 */
public record InvoiceParseRequest(
    @NotBlank(message = "票据文本不能为空")
    @Size(max = 5000, message = "票据文本最多 5000 个字符")
    String text
) {
}
