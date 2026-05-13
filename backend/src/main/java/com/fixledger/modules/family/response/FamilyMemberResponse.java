package com.fixledger.modules.family.response;

import java.time.LocalDateTime;

/**
 * <p>
 * 文件功能说明：家庭空间响应对象，封装返回给前端展示的数据结构。
 * </p>
 *
 * @Author FixLedger
 */
public record FamilyMemberResponse(
    Long id,
    Long userId,
    String username,
    String nickname,
    String role,
    LocalDateTime joinedAt
) {
}
