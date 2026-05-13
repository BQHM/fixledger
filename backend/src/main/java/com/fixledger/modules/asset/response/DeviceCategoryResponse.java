package com.fixledger.modules.asset.response;

/**
 * <p>
 * 文件功能说明：设备档案响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record DeviceCategoryResponse(
    Long id,
    String name,
    String icon,
    Integer sortOrder,
    Boolean systemDefault
) {
}
