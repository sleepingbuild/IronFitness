package com.homefit.server.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.homefit.server.domain.ChallengeSession;

public record ChallengeSessionDto(
        Long id,
        int weekNum,
        int dayIndex,
        @JsonProperty("isRestDay") boolean restDay,
        Long workoutId,
        WorkoutDto workout
) {
    public static ChallengeSessionDto from(ChallengeSession s, WorkoutDto workout) {
        return new ChallengeSessionDto(
                s.getId(),
                s.getWeekNum(),
                s.getDayIndex(),
                s.isRestDay(),
                s.getWorkoutId(),
                workout
        );
    }
}
