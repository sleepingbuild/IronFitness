package com.homefit.server.api.dto;

import com.homefit.server.domain.Workout;

import java.util.List;

public record WorkoutDto(
        Long id,
        String type,
        String titleVi,
        String titleEn,
        String primaryMuscle,
        String level,
        String goal,
        int workSec,
        int restSec,
        int roundRestSec,
        int rounds,
        String equipment,
        String coverImageUrl,
        int sortOrder,
        String status,
        int version,
        List<WorkoutItemDto> items
) {
    public static WorkoutDto from(Workout w, List<WorkoutItemDto> items) {
        return new WorkoutDto(
                w.getId(),
                w.getType(),
                w.getTitleVi(),
                w.getTitleEn(),
                w.getPrimaryMuscle(),
                w.getLevel(),
                w.getGoal(),
                w.getWorkSec(),
                w.getRestSec(),
                w.getRoundRestSec(),
                w.getRounds(),
                w.getEquipment(),
                w.getCoverImageUrl(),
                w.getSortOrder(),
                w.getStatus(),
                w.getVersion(),
                items
        );
    }
}

