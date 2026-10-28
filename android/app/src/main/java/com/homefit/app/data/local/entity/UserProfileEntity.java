package com.homefit.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Hồ sơ người dùng hiện tại, luôn đúng 1 dòng (id = 1). */
@Entity(tableName = "user_profile")
public class UserProfileEntity {
    @PrimaryKey
    public int id = 1;

    /** Id người dùng trên server. */
    public long userId;

    @NonNull
    public String email = "";

    @NonNull
    public String displayName = "";

    @Nullable
    public Double heightCm;

    @Nullable
    public Double weightKg;

    public boolean isPremium;
}
