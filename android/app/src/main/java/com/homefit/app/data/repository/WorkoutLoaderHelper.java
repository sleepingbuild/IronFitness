package com.homefit.app.data.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.local.entity.WorkoutEntity;
import com.homefit.app.data.local.entity.WorkoutItemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WorkoutLoaderHelper {

    private WorkoutLoaderHelper() {
    }

    @Nullable
    public static LocalExerciseGroupDto loadWorkoutFromRoom(@NonNull Context context,
                                                              @Nullable String groupKey,
                                                              @Nullable String difficulty,
                                                              @Nullable String groupName) {
        AppDatabase db = AppDatabase.getInstance(context);
        List<WorkoutEntity> allWorkouts = db.workoutDao().getAllNow();
        if (allWorkouts == null || allWorkouts.isEmpty()) return null;

        WorkoutEntity matched = findMatchingWorkout(allWorkouts, groupKey, difficulty, groupName);
        if (matched == null) return null;

        List<WorkoutItemEntity> items = db.workoutItemDao().getByWorkoutIdNow(matched.id);
        if (items == null || items.isEmpty()) return null;

        LocalExerciseGroupDto dto = new LocalExerciseGroupDto();
        dto.group = matched.goal != null && !matched.goal.isEmpty() ? matched.goal : (matched.type != null ? matched.type : "");
        dto.group_name = matched.titleVi != null ? matched.titleVi : "";
        dto.difficulty = matched.level != null ? matched.level : "";
        dto.coverImageUrl = matched.coverImageUrl != null ? matched.coverImageUrl : "";

        List<LocalExerciseGroupDto.LocalWorkoutDto> workouts = new ArrayList<>();
        for (WorkoutItemEntity item : items) {
            if (item == null) continue;
            LocalExerciseGroupDto.LocalWorkoutDto w = new LocalExerciseGroupDto.LocalWorkoutDto();
            w.id = String.valueOf(item.exerciseId);
            w.name = item.exerciseName != null ? item.exerciseName : "";
            w.mediaUrl = item.exerciseMediaUrl != null ? item.exerciseMediaUrl : "";

            if ("time".equalsIgnoreCase(item.measure) || "giây".equalsIgnoreCase(item.measure)) {
                w.reps_sets = item.value + " giây";
            } else {
                w.reps_sets = item.value + " lần";
            }
            w.target = "Nghỉ " + item.restAfterSec + "s";
            w.instructions = new ArrayList<>();
            w.tips = "Giữ nhịp thở đều.";
            workouts.add(w);
        }
        dto.workouts = workouts;
        dto.duration_min = Math.max(1, (matched.workSec + matched.restSec) * workouts.size() / 60);
        return dto;
    }

    @Nullable
    public static WorkoutEntity findMatchingWorkout(@NonNull List<WorkoutEntity> workouts,
                                                      @Nullable String groupKey,
                                                      @Nullable String difficulty,
                                                      @Nullable String groupName) {
        if (workouts == null || workouts.isEmpty()) return null;
        String g = groupKey != null ? groupKey.toLowerCase(Locale.US) : "";
        String d = difficulty != null ? difficulty.toLowerCase(Locale.US) : "";
        String n = groupName != null ? groupName.toLowerCase(Locale.US) : "";

        // 1. Match by exact titleVi or title containing groupName
        for (WorkoutEntity w : workouts) {
            if (w == null) continue;
            String title = w.titleVi != null ? w.titleVi.toLowerCase(Locale.US) : "";
            String level = w.level != null ? w.level : "";
            if (!n.isEmpty() && (title.equals(n) || title.contains(n) || n.contains(title))) {
                if (d.isEmpty() || level.equalsIgnoreCase(d)) {
                    return w;
                }
            }
        }

        // 2. Match challenge days if groupKey is challenge_level
        if (g.startsWith("challenge_")) {
            String targetLevel = g.replace("challenge_", "");
            String lvlVi = targetLevel.contains("easy") ? "dễ" : (targetLevel.contains("hard") ? "khó" : "trung bình");
            for (WorkoutEntity w : workouts) {
                if (w == null) continue;
                String title = w.titleVi != null ? w.titleVi.toLowerCase(Locale.US) : "";
                String level = w.level != null ? w.level : "";
                if (title.contains("ngày " + d) && (title.contains(lvlVi) || level.equalsIgnoreCase(targetLevel))) {
                    return w;
                }
            }
        }

        // 3. Match category workouts if groupKey is category_ID
        if (g.startsWith("category_")) {
            try {
                long catId = Long.parseLong(g.replace("category_", ""));
                for (WorkoutEntity w : workouts) {
                    if (w != null && w.id == catId) return w;
                }
            } catch (Exception ignored) {}
        }

        // 4. Match by goal and level
        for (WorkoutEntity w : workouts) {
            if (w == null) continue;
            String goal = w.goal != null ? w.goal.toLowerCase(Locale.US) : "";
            String level = w.level != null ? w.level : "";
            if (!g.isEmpty() && goal.equalsIgnoreCase(g)) {
                if (d.isEmpty() || level.equalsIgnoreCase(d)) {
                    return w;
                }
            }
        }

        // 5. Match by primaryMuscle and level
        for (WorkoutEntity w : workouts) {
            if (w == null) continue;
            String muscle = w.primaryMuscle != null ? w.primaryMuscle.toLowerCase(Locale.US) : "";
            String level = w.level != null ? w.level : "";
            if (!g.isEmpty() && (muscle.equalsIgnoreCase(g) || g.contains(muscle) || muscle.contains(g))) {
                if (d.isEmpty() || level.equalsIgnoreCase(d)) {
                    return w;
                }
            }
        }

        // 6. Match by type and level
        for (WorkoutEntity w : workouts) {
            if (w == null) continue;
            String type = w.type != null ? w.type.toLowerCase(Locale.US) : "";
            String level = w.level != null ? w.level : "";
            if (!g.isEmpty() && type.equalsIgnoreCase(g)) {
                if (d.isEmpty() || level.equalsIgnoreCase(d)) {
                    return w;
                }
            }
        }

        // 7. Fallback fuzzy match
        for (WorkoutEntity w : workouts) {
            if (w == null) continue;
            String goal = w.goal != null ? w.goal.toLowerCase(Locale.US) : "";
            String title = w.titleVi != null ? w.titleVi.toLowerCase(Locale.US) : "";
            if ((!g.isEmpty() && (goal.contains(g) || g.contains(goal)))
                    || (!n.isEmpty() && (title.contains(n) || n.contains(title)))) {
                return w;
            }
        }

        return null;
    }
}
