package com.homefit.server.api.dto;

public record RefreshResponse(String accessToken, String refreshToken, long expiresIn) {
}
