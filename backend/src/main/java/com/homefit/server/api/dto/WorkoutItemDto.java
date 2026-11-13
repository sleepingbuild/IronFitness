package com.homefit.server.api.dto;

import com.homefit.server.domain.WorkoutItem;

public record WorkoutItemDto(
        Long id,
        String block,
        int position,
        Long exerciseId,
        String exerciseName,
        String exerciseMediaUrl,
        String exerciseMediaType,
        int roundNum,
        String measure,
        int value,
        int restAfterSec
) {
    public static WorkoutItemDto from(WorkoutItem item, String exName, String exMediaUrl, String exMediaType) {
        return new WorkoutItemDto(
                item.getId(),
                item.getBlock(),
                item.getPosition(),
                item.getExerciseId(),
                exName,
                exMediaUrl,
                exMediaType,
                item.getRoundNum(),
                item.getMeasure(),
                item.getValue(),
                item.getRestAfterSec()
        );
    }
}
