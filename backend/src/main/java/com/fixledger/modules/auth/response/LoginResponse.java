package com.fixledger.modules.auth.response;

public record LoginResponse(
    String accessToken,
    long expiresIn,
    UserProfileResponse user,
    Long currentFamilyId
) {
}
