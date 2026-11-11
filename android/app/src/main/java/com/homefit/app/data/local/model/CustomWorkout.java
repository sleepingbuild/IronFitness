package com.homefit.app.data.local.model;

import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;

import java.util.ArrayList;
import java.util.List;

public class CustomWorkout {
    public String id;
    public String title = "Buổi tập của tôi 1";
    public String createdAt;
    public String updatedAt;
    public Settings settings = new Settings();
    public List<CustomWorkoutItem> items = new ArrayList<>();
    public int durationMin;
    public int exerciseCount;

    public static class Settings {
        public int restSec = 20;
        public int rounds = 1;
        public int roundRestSec = 60;
        public boolean autoWarmupCooldown = true;
    }

    public int calculateDurationMin() {
        if (items == null || items.isEmpty()) {
            this.exerciseCount = 0;
            this.durationMin = 0;
            return 0;
        }

        int totalSec = 0;
        int roundsCount = (settings != null && settings.rounds > 0) ? settings.rounds : 1;
        int defaultRestSec = (settings != null) ? settings.restSec : 20;
        int roundRestSec = (settings != null) ? settings.roundRestSec : 60;

        for (CustomWorkoutItem item : items) {
            int exerciseSec = 0;
            if (item.timeSec != null && item.timeSec > 0) {
                exerciseSec = item.timeSec;
            } else if (item.reps != null && item.reps > 0) {
                exerciseSec = item.reps * 3;
            } else if ("time".equalsIgnoreCase(item.measure) && item.value > 0) {
                exerciseSec = item.value;
            } else if (item.value > 0) {
                exerciseSec = item.value * 3;
            } else {
                exerciseSec = 30; // 30s estimate for free mode
            }

            int setsCount = (item.sets != null && item.sets > 0) ? item.sets : 1;
            int restAfter = item.restAfterSec >= 0 ? item.restAfterSec : defaultRestSec;
            totalSec += (exerciseSec + restAfter + 5) * setsCount;
        }

        totalSec *= roundsCount;
        if (roundsCount > 1) {
            totalSec += roundRestSec * (roundsCount - 1);
        }

        this.exerciseCount = items.size();
        this.durationMin = (int) Math.round(totalSec / 60.0);
        if (this.durationMin == 0 && totalSec > 0) {
            this.durationMin = 1;
        }
        return this.durationMin;
    }

    public List<LocalWorkoutDto> toLocalWorkoutDtos() {
        List<LocalWorkoutDto> list = new ArrayList<>();
        if (items == null) return list;

        for (CustomWorkoutItem item : items) {
            LocalWorkoutDto dto = new LocalWorkoutDto();
            dto.id = item.exerciseId != null ? item.exerciseId : ("custom_ex_" + System.currentTimeMillis());
            dto.name = item.exerciseName != null ? item.exerciseName : "Bài tập";
            dto.target = item.target != null ? item.target : "Toàn thân";
            dto.reps_sets = item.getFormattedSubtitle();
            dto.instructions = item.instructions != null ? item.instructions : new ArrayList<>();
            dto.tips = item.tips != null ? item.tips : "";
            list.add(dto);
        }

        return list;
    }
}
