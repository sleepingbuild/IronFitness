package com.homefit.server.api.dto;

public record AuthResponse(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
}
