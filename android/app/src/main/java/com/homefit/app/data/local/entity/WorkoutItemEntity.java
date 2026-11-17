package com.homefit.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Bài tập con trong một buổi tập (cache từ server API). */
@Entity(tableName = "workout_items")
public class WorkoutItemEntity {
    @PrimaryKey
    public long id;

    public long workoutId;
    public long exerciseId;
    public int position;

    @NonNull
    public String block = "B";

    @NonNull
    public String measure = "reps";

    public int value;
    public int restAfterSec;

    @NonNull
    public String exerciseName = "";

    @Nullable
    public String exerciseMediaUrl;

    @NonNull
    public String exerciseMediaType = "GIF";
}
