package com.homefit.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Buổi tập (cache từ server API). Khớp docs/DATABASE.md mục Room. */
@Entity(tableName = "workouts")
public class WorkoutEntity {
    @PrimaryKey
    public long id;

    @NonNull
    public String type = "";

    @NonNull
    public String titleVi = "";

    @Nullable
    public String titleEn;

    @NonNull
    public String primaryMuscle = "";

    @NonNull
    public String level = "";

    @Nullable
    public String goal;

    public int workSec;
    public int restSec;
    public int roundRestSec;
    public int rounds;

    @NonNull
    public String equipment = "none";

    @Nullable
    public String coverImageUrl;

    public int sortOrder;

    @NonNull
    public String status = "DRAFT";

    public int version = 1;

    public long updatedAt;
}
