package com.fixledger.modules.asset.response;

public record DeviceCategoryResponse(
    Long id,
    String name,
    String icon,
    Integer sortOrder,
    Boolean systemDefault
) {
}
