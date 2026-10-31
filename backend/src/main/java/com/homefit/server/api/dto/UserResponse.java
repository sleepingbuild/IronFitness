package com.homefit.server.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.homefit.server.domain.User;

/** Cấu trúc User trong docs/API.md (GET /me và trường user của các API auth). */
public record UserResponse(
        Long id,
        String email,
        String displayName,
        @JsonProperty("isPremium") boolean premium,
        Double heightCm,
        Double weightKg) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.isPremium(), user.getHeightCm(), user.getWeightKg());
    }
}
