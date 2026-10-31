package com.homefit.server.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Thiếu refresh token") String refreshToken) {
}
