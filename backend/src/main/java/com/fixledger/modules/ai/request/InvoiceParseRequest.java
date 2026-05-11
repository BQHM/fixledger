package com.fixledger.modules.ai.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InvoiceParseRequest(
    @NotBlank(message = "票据文本不能为空")
    @Size(max = 5000, message = "票据文本最多 5000 个字符")
    String text
) {
}
