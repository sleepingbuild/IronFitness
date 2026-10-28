package com.homefit.app.data.remote.dto;

public class ExerciseDto {
    public long id;
    public long categoryId;
    public String name;
    public String description;
    public String mediaUrl;
    /** "GIF" hoặc "VIDEO". */
    public String mediaType;
    public Integer durationSec;
    public Integer reps;
    public int restSec;
    public int sortOrder;
    /** false nghĩa là bài tập đã bị tắt: app phải xóa khỏi Room. */
    public Boolean isActive;
    /** ISO-8601 UTC. */
    public String updatedAt;
}
