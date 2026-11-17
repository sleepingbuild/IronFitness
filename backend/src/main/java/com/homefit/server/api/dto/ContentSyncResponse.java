package com.homefit.server.api.dto;

import java.time.Instant;
import java.util.List;

public record ContentSyncResponse(
        Instant lastUpdatedAt,
        long exerciseCount,
        long workoutCount,
        long categoryCount,
        long contentVersion,
        long workoutExercisesCount,
        List<WorkoutDto> workouts,
        List<ChallengeProgramDto> challenges
) {
    public ContentSyncResponse(Instant lastUpdatedAt, long exerciseCount, long workoutCount, long categoryCount, long contentVersion) {
        this(lastUpdatedAt, exerciseCount, workoutCount, categoryCount, contentVersion, 0L, List.of(), List.of());
    }
}
