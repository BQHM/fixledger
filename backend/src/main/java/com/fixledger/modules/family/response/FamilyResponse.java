package com.fixledger.modules.family.response;

public record FamilyResponse(
    Long id,
    String name,
    String description,
    String role,
    Long ownerUserId
) {
}
