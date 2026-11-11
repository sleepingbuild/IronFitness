package com.homefit.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChallengeRepository {

    private static final String PREF_NAME = "challenge_progress_pref";
    private static final String KEY_SELECTED_LEVEL = "key_selected_level";

    private final SharedPreferences prefs;

    public ChallengeRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getSelectedLevel() {
        return prefs.getString(KEY_SELECTED_LEVEL, "easy");
    }

    public void setSelectedLevel(String level) {
        if (level == null) return;
        prefs.edit().putString(KEY_SELECTED_LEVEL, level).apply();
    }

    public String getLevelTitleVi(String level) {
        if ("hard".equalsIgnoreCase(level)) return "Khó";
        if ("medium".equalsIgnoreCase(level)) return "Trung bình";
        return "Dễ";
    }

    public int getCompletedCount(String level) {
        Set<String> completedSet = prefs.getStringSet(getCompletedKey(level), new HashSet<>());
        return completedSet != null ? completedSet.size() : 0;
    }

    public boolean isDayCompleted(String level, int dayIndex) {
        Set<String> completedSet = prefs.getStringSet(getCompletedKey(level), new HashSet<>());
        return completedSet != null && completedSet.contains(String.valueOf(dayIndex));
    }

    public void markDayCompleted(String level, int dayIndex) {
        Set<String> completedSet = new HashSet<>(prefs.getStringSet(getCompletedKey(level), new HashSet<>()));
        completedSet.add(String.valueOf(dayIndex));
        prefs.edit().putStringSet(getCompletedKey(level), completedSet).apply();
    }

    public void resetLevelProgress(String level) {
        prefs.edit().remove(getCompletedKey(level)).apply();
    }

    public int getCurrentDayIndex(String level) {
        for (int day = 1; day <= 28; day++) {
            if (isRestDay(day)) {
                continue; // Ngày nghỉ tự động trôi qua
            }
            if (!isDayCompleted(level, day)) {
                return day;
            }
        }
        return 28; // Hoàn thành tất cả
    }

    public boolean isRestDay(int dayIndex) {
        return (dayIndex % 7) == 0;
    }

    public String getDayTypeName(int dayIndex) {
        if (isRestDay(dayIndex)) return "Ngày nghỉ";
        int posInWeek = (dayIndex - 1) % 7 + 1;
        switch ((posInWeek - 1) % 3) {
            case 0: return "Ngực & Tay";
            case 1: return "Lưng & Bụng";
            case 2: return "Chân";
            default: return "Toàn thân";
        }
    }

    public LocalExerciseGroupDto getSessionForDay(String level, int dayIndex) {
        if (isRestDay(dayIndex)) return null;

        int week = (dayIndex - 1) / 7 + 1;
        int posInWeek = (dayIndex - 1) % 7 + 1;
        String typeName = getDayTypeName(dayIndex);
        String levelName = getLevelTitleVi(level);

        LocalExerciseGroupDto dto = new LocalExerciseGroupDto();
        dto.group = "challenge_" + level;
        dto.group_name = "Ngày " + dayIndex + " - " + typeName;
        dto.difficulty = levelName;

        int workSec = "hard".equalsIgnoreCase(level) ? 45 : ("medium".equalsIgnoreCase(level) ? 40 : 30);
        int restSec = "hard".equalsIgnoreCase(level) ? 15 : 20;

        // Số bài khối chính theo tuần & độ khó
        int baseMainCount = 8;
        if ("medium".equalsIgnoreCase(level)) baseMainCount = 9;
        if ("hard".equalsIgnoreCase(level)) baseMainCount = 10;
        int mainCount = baseMainCount + (week - 1);

        dto.workouts = buildWorkoutsForDay(typeName, mainCount, workSec, restSec, posInWeek > 3);
        dto.duration_min = calculateDuration(dto.workouts.size(), workSec, restSec);

        return dto;
    }

    private List<LocalWorkoutDto> buildWorkoutsForDay(String typeName, int mainCount, int workSec, int restSec, boolean isCycle2) {
        List<LocalWorkoutDto> list = new ArrayList<>();
        String timeTag = workSec + "s";

        // Khởi động (3 bài)
        addWorkout(list, "Jumping Jack", "Warm-up", "30s");
        addWorkout(list, "Arm Circles", "Warm-up", "30s");
        addWorkout(list, "Cat-Cow", "Warm-up", "30s");

        // Khối chính
        if ("Ngực & Tay".equals(typeName)) {
            if (!isCycle2) {
                addWorkout(list, "Chống đẩy vào tường", "Main", timeTag);
                addWorkout(list, "Chống đẩy quỳ gối", "Main", timeTag);
                addWorkout(list, "Triceps Dips trên sàn", "Main", timeTag);
                addWorkout(list, "Chống đẩy tay hẹp", "Main", timeTag);
                addWorkout(list, "Pike Push-up", "Main", timeTag);
                addWorkout(list, "Plank chạm vai", "Main", timeTag);
                addWorkout(list, "Chống đẩy tay rộng", "Main", timeTag);
                addWorkout(list, "Plank cao giữ", "Main", timeTag);
            } else {
                addWorkout(list, "Chống đẩy chuẩn", "Main", timeTag);
                addWorkout(list, "Chống đẩy lệch tay", "Main", timeTag);
                addWorkout(list, "Dips sàn chân duỗi", "Main", timeTag);
                addWorkout(list, "Chống đẩy kim cương", "Main", timeTag);
                addWorkout(list, "Pike Push-up chậm", "Main", timeTag);
                addWorkout(list, "Plank up-down", "Main", timeTag);
                addWorkout(list, "Chống đẩy nâng một chân", "Main", timeTag);
                addWorkout(list, "Plank khuỷu tay", "Main", timeTag);
            }
        } else if ("Lưng & Bụng".equals(typeName)) {
            if (!isCycle2) {
                addWorkout(list, "Bird Dog", "Main", timeTag);
                addWorkout(list, "Plank quỳ gối", "Main", timeTag);
                addWorkout(list, "Superman", "Main", timeTag);
                addWorkout(list, "Dead Bug", "Main", timeTag);
                addWorkout(list, "Heel Taps", "Main", timeTag);
                addWorkout(list, "Crunch gập bụng", "Main", timeTag);
                addWorkout(list, "Reverse Crunch", "Main", timeTag);
                addWorkout(list, "Plank nghiêng", "Main", timeTag);
            } else {
                addWorkout(list, "Bicycle Crunch", "Main", timeTag);
                addWorkout(list, "Mountain Climber", "Main", timeTag);
                addWorkout(list, "Plank thẳng tay", "Main", timeTag);
                addWorkout(list, "Reverse Snow Angel", "Main", timeTag);
                addWorkout(list, "Leg Raise gập gối", "Main", timeTag);
                addWorkout(list, "Plank nghiêng nâng hông", "Main", timeTag);
                addWorkout(list, "Hollow Hold", "Main", timeTag);
                addWorkout(list, "V-up gập bụng", "Main", timeTag);
            }
        } else {
            if (!isCycle2) {
                addWorkout(list, "Squat chậm", "Main", timeTag);
                addWorkout(list, "Sumo Squat", "Main", timeTag);
                addWorkout(list, "Reverse Lunge", "Main", timeTag);
                addWorkout(list, "Đá chân sau", "Main", timeTag);
                addWorkout(list, "Nhón gót", "Main", timeTag);
                addWorkout(list, "Donkey Kick", "Main", timeTag);
                addWorkout(list, "Cầu mông", "Main", timeTag);
                addWorkout(list, "Cầu mông giữ", "Main", timeTag);
            } else {
                addWorkout(list, "Squat chuẩn", "Main", timeTag);
                addWorkout(list, "Jump Squat", "Main", timeTag);
                addWorkout(list, "Lateral Lunge", "Main", timeTag);
                addWorkout(list, "Single-leg RDL", "Main", timeTag);
                addWorkout(list, "Nhón gót 1 chân", "Main", timeTag);
                addWorkout(list, "Wall Sit", "Main", timeTag);
                addWorkout(list, "Cầu mông 1 chân", "Main", timeTag);
                addWorkout(list, "Split Squat", "Main", timeTag);
            }
        }

        // Nếu cần thêm bài cho các tuần sau
        while (list.size() < (3 + mainCount + 3)) {
            addWorkout(list, "Plank giữ", "Main", timeTag);
        }

        // Giãn cơ (3 bài)
        addWorkout(list, "Chest Opener", "Cool-down", "30s");
        addWorkout(list, "Child's Pose", "Cool-down", "30s");
        addWorkout(list, "Standing Quad Stretch", "Cool-down", "30s");

        return list;
    }

    private void addWorkout(List<LocalWorkoutDto> list, String name, String target, String repsSets) {
        LocalWorkoutDto w = new LocalWorkoutDto();
        w.id = name.toLowerCase().replace(" ", "_").replace("-", "_");
        w.name = name;
        w.target = target;
        w.reps_sets = repsSets;
        w.instructions = new ArrayList<>();
        w.tips = "";
        list.add(w);
    }

    private int calculateDuration(int count, int workSec, int restSec) {
        int totalSec = 210 + (count - 6) * (workSec + restSec + 5);
        return (int) Math.round(totalSec / 60.0);
    }

    private String getCompletedKey(String level) {
        return "completed_days_" + (level != null ? level : "easy");
    }
}
