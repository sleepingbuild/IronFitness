package com.homefit.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Bài tập (cache từ server). Đúng một trong durationSec hoặc reps có giá trị.
 * Khớp docs/DATABASE.md mục Room.
 */
@Entity(tableName = "exercises", indices = {@Index("categoryId")})
public class ExerciseEntity {
    /** Bằng id trên server. */
    @PrimaryKey
    public long id;

    public long categoryId;

    @NonNull
    public String name = "";

    @Nullable
    public String description;

    @NonNull
    public String mediaUrl = "";

    /** "GIF" hoặc "VIDEO". */
    @NonNull
    public String mediaType = "GIF";

    /** Tập theo thời gian (giây); null nếu tập theo số lần. */
    @Nullable
    public Integer durationSec;

    /** Tập theo số lần; null nếu tập theo thời gian. */
    @Nullable
    public Integer reps;

    public int restSec;

    public int sortOrder;

    /** Epoch millis (UTC). */
    public long updatedAt;
}
