package com.homefit.server.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "idToken không được để trống")
        String idToken) {
}
