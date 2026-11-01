package com.homefit.server.api.dto;

import com.homefit.server.domain.WorkoutSession;

import java.time.Instant;

/** Phần tử của GET /workout-sessions. */
public record WorkoutSessionItem(
        Long id,
        String clientUuid,
        Long categoryId,
        Instant startedAt,
        Instant completedAt,
        int totalSeconds,
        int exercisesDone) {

    public static WorkoutSessionItem from(WorkoutSession s) {
        return new WorkoutSessionItem(s.getId(), s.getClientUuid(), s.getCategoryId(), s.getStartedAt(),
                s.getCompletedAt(), s.getTotalSeconds(), s.getExercisesDone());
    }
}
