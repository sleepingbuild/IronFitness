package com.homefit.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Buổi tập đã hoàn thành, ghi local trước (synced = false) rồi đồng bộ lên server.
 * Khớp docs/DATABASE.md mục Room.
 */
@Entity(tableName = "workout_logs",
        indices = {@Index(value = {"clientUuid"}, unique = true), @Index("userId")})
public class WorkoutLogEntity {
    @PrimaryKey(autoGenerate = true)
    public long localId;

    /** Id người dùng trên server; mọi truy vấn lịch sử lọc theo cột này. */
    public long userId;

    /** UUID do app sinh, gửi lên server để chống trùng. */
    @NonNull
    public String clientUuid = "";

    @Nullable
    public Long categoryId;

    /** Epoch millis. */
    public long startedAt;

    /** Epoch millis. */
    public long completedAt;

    public int totalSeconds;

    public int exercisesDone;

    /** false cho đến khi server xác nhận. */
    public boolean synced;
}
