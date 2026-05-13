package com.fixledger.modules.family.response;

/**
 * <p>
 * 文件功能说明：家庭空间响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record FamilyResponse(
    Long id,
    String name,
    String description,
    String role,
    Long ownerUserId
) {
}
