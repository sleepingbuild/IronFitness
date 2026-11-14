package com.homefit.app.data.remote.dto;

import java.util.List;

public class WorkoutDto {
    public Long id;
    public String type;
    public String titleVi;
    public String titleEn;
    public String primaryMuscle;
    public String level;
    public String goal;
    public int workSec;
    public int restSec;
    public int roundRestSec;
    public int rounds;
    public String equipment;
    public String coverImageUrl;
    public int sortOrder;
    public String status;
    public int version;
    public List<WorkoutItemDto> items;

    public static class WorkoutItemDto {
        public Long id;
        public Long workoutId;
        public String block;
        public int position;
        public Long exerciseId;
        public int roundNum;
        public String measure;
        public int value;
        public int restAfterSec;
        public String exerciseName;
        public String exerciseMediaUrl;
        public String exerciseMediaType;
    }
}
