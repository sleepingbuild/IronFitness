package com.homefit.server.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

/** Một buổi tập gửi từ app (POST /workout-sessions và từng phần tử trong batch). Thời gian ISO-8601 UTC. */
public record WorkoutSessionRequest(
        @NotBlank(message = "Thiếu clientUuid")
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "clientUuid phải là UUID hợp lệ")
        String clientUuid,

        Long categoryId,

        @NotNull(message = "Thiếu startedAt")
        Instant startedAt,

        @NotNull(message = "Thiếu completedAt")
        Instant completedAt,

        @NotNull(message = "Thiếu totalSeconds")
        @Min(value = 0, message = "totalSeconds không được âm")
        @Max(value = 86400, message = "totalSeconds tối đa 86400 (24 giờ)")
        Integer totalSeconds,

        @NotNull(message = "Thiếu exercisesDone")
        @Min(value = 0, message = "exercisesDone không được âm")
        @Max(value = 1000, message = "exercisesDone tối đa 1000")
        Integer exercisesDone) {
}
