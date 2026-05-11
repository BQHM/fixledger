package com.fixledger.modules.auth.response;

public record UserProfileResponse(
    Long id,
    String username,
    String nickname,
    String email
) {
}
