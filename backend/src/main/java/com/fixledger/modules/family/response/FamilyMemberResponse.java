package com.fixledger.modules.family.response;

import java.time.LocalDateTime;

public record FamilyMemberResponse(
    Long id,
    Long userId,
    String username,
    String nickname,
    String role,
    LocalDateTime joinedAt
) {
}
