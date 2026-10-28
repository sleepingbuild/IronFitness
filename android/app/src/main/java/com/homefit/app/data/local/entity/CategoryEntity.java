package com.homefit.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Nhóm cơ (cache từ server). Khớp docs/DATABASE.md mục Room. */
@Entity(tableName = "categories")
public class CategoryEntity {
    /** Bằng id trên server. */
    @PrimaryKey
    public long id;

    @NonNull
    public String name = "";

    @NonNull
    public String slug = "";

    @Nullable
    public String iconUrl;

    public int sortOrder;

    /** Epoch millis (UTC), chuyển từ chuỗi ISO của server. */
    public long updatedAt;
}
