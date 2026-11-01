package com.homefit.server.api.dto;

/** Kết quả POST /workout-sessions: 201 nếu mới, 200 nếu clientUuid đã có. */
public record WorkoutSessionResponse(Long id, String clientUuid) {
}
