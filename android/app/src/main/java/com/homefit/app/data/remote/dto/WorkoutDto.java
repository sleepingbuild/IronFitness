package com.homefit.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WorkoutDto {
    @SerializedName("id")
    public Long id;

    @SerializedName("type")
    public String type;

    @SerializedName(value = "titleVi", alternate = {"title_vi"})
    public String titleVi;

    @SerializedName(value = "titleEn", alternate = {"title_en"})
    public String titleEn;

    @SerializedName(value = "primaryMuscle", alternate = {"primary_muscle"})
    public String primaryMuscle;

    @SerializedName("level")
    public String level;

    @SerializedName("goal")
    public String goal;

    @SerializedName(value = "workSec", alternate = {"work_sec"})
    public int workSec;

    @SerializedName(value = "restSec", alternate = {"rest_sec"})
    public int restSec;

    @SerializedName(value = "roundRestSec", alternate = {"round_rest_sec"})
    public int roundRestSec;

    @SerializedName("rounds")
    public int rounds;

    @SerializedName("equipment")
    public String equipment;

    @SerializedName(value = "coverImageUrl", alternate = {"cover_image_url"})
    public String coverImageUrl;

    @SerializedName(value = "sortOrder", alternate = {"sort_order"})
    public int sortOrder;

    @SerializedName("status")
    public String status;

    @SerializedName("version")
    public int version;

    @SerializedName("items")
    public List<WorkoutItemDto> items;

    public static class WorkoutItemDto {
        @SerializedName("id")
        public Long id;

        @SerializedName(value = "workoutId", alternate = {"workout_id"})
        public Long workoutId;

        @SerializedName("block")
        public String block;

        @SerializedName("position")
        public int position;

        @SerializedName(value = "exerciseId", alternate = {"exercise_id"})
        public Long exerciseId;

        @SerializedName(value = "roundNum", alternate = {"round_num"})
        public int roundNum;

        @SerializedName("measure")
        public String measure;

        @SerializedName("value")
        public int value;

        @SerializedName(value = "restAfterSec", alternate = {"rest_after_sec"})
        public int restAfterSec;

        @SerializedName(value = "exerciseName", alternate = {"exercise_name"})
        public String exerciseName;

        @SerializedName(value = "exerciseMediaUrl", alternate = {"exercise_media_url"})
        public String exerciseMediaUrl;

        @SerializedName(value = "exerciseMediaType", alternate = {"exercise_media_type"})
        public String exerciseMediaType;
    }
}
