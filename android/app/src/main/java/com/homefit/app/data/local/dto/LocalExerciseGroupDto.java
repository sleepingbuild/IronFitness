package com.homefit.app.data.local.dto;

import java.util.List;

public class LocalExerciseGroupDto {
    public String group;
    public String group_name;
    public String difficulty;
    public String description;
    public int duration_min; // Thêm trường duration_min nếu có sẵn
    public List<LocalWorkoutDto> workouts;

    public static class LocalWorkoutDto {
        public String id;
        public String name;
        public String target;
        public String reps_sets;
        public List<String> instructions;
        public String tips;
        public String mediaUrl;
    }
}
