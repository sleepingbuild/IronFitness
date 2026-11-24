package com.homefit.server.api.dto;

import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.WorkoutItem;

public record WorkoutItemDto(
        Long id,
        String block,
        int position,
        int orderIndex,
        Long exerciseId,
        String name,
        String exerciseName,
        String description,
        String muscleGroup,
        String mediaUrl,
        String exerciseMediaUrl,
        String exerciseMediaType,
        int roundNum,
        String measure,
        String unit,
        int value,
        int quantity,
        int restAfterSec,
        int restSeconds
) {
    public static WorkoutItemDto from(WorkoutItem item, Exercise ex, String defaultMuscleGroup) {
        String exName = ex != null ? ex.getName() : "Bài tập #" + item.getExerciseId();
        String exMediaUrl = ex != null ? ex.getMediaUrl() : "";
        String exMediaType = ex != null && ex.getMediaType() != null ? ex.getMediaType().name() : "GIF";
        String exDesc = ex != null && ex.getDescription() != null ? ex.getDescription() : "";
        return from(item, exName, exMediaUrl, exMediaType, exDesc, defaultMuscleGroup);
    }

    public static WorkoutItemDto from(WorkoutItem item, String exName, String exMediaUrl, String exMediaType) {
        return from(item, exName, exMediaUrl, exMediaType, "", "");
    }

    public static WorkoutItemDto from(WorkoutItem item, String exName, String exMediaUrl, String exMediaType, String description, String muscleGroup) {
        String unitStr = item.getMeasure();
        if ("time".equalsIgnoreCase(unitStr)) {
            unitStr = "giây";
        } else if ("reps".equalsIgnoreCase(unitStr)) {
            unitStr = "lần";
        }

        return new WorkoutItemDto(
                item.getId(),
                item.getBlock(),
                item.getPosition(),
                item.getPosition(), // orderIndex
                item.getExerciseId(),
                exName, // name
                exName, // exerciseName
                description != null ? description : "",
                muscleGroup != null ? muscleGroup : "",
                exMediaUrl, // mediaUrl
                exMediaUrl, // exerciseMediaUrl
                exMediaType != null ? exMediaType : "GIF",
                item.getRoundNum(),
                item.getMeasure(),
                unitStr,
                item.getValue(),
                item.getValue(), // quantity
                item.getRestAfterSec(),
                item.getRestAfterSec() // restSeconds
        );
    }
}


