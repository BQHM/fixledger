package com.fixledger.modules.auth.response;

/**
 * <p>
 * 文件功能说明：认证响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record LoginResponse(
    String accessToken,
    long expiresIn,
    UserProfileResponse user,
    Long currentFamilyId
) {
}
