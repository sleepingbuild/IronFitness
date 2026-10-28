package com.homefit.app.domain;

import androidx.annotation.NonNull;

import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.local.entity.WorkoutLogEntity;
import com.homefit.app.data.remote.dto.CategoryDto;
import com.homefit.app.data.remote.dto.ExerciseDto;
import com.homefit.app.data.remote.dto.UserDto;
import com.homefit.app.data.remote.dto.WorkoutSessionDto;
import com.homefit.app.data.remote.dto.WorkoutSessionRequest;
import com.homefit.app.util.DateUtils;

/** Chuyển đổi giữa DTO (mạng) và Entity (Room). Không có logic nghiệp vụ. */
public final class Mappers {

    private Mappers() {
    }

    @NonNull
    public static CategoryEntity toEntity(@NonNull CategoryDto d) {
        CategoryEntity e = new CategoryEntity();
        e.id = d.id;
        e.name = d.name != null ? d.name : "";
        e.slug = d.slug != null ? d.slug : "";
        e.iconUrl = d.iconUrl;
        e.sortOrder = d.sortOrder;
        e.updatedAt = DateUtils.parseIso(d.updatedAt);
        return e;
    }

    @NonNull
    public static ExerciseEntity toEntity(@NonNull ExerciseDto d) {
        ExerciseEntity e = new ExerciseEntity();
        e.id = d.id;
        e.categoryId = d.categoryId;
        e.name = d.name != null ? d.name : "";
        e.description = d.description;
        e.mediaUrl = d.mediaUrl != null ? d.mediaUrl : "";
        e.mediaType = d.mediaType != null ? d.mediaType : "GIF";
        e.durationSec = d.durationSec;
        e.reps = d.reps;
        e.restSec = d.restSec;
        e.sortOrder = d.sortOrder;
        e.updatedAt = DateUtils.parseIso(d.updatedAt);
        return e;
    }

    @NonNull
    public static UserProfileEntity toEntity(@NonNull UserDto d) {
        UserProfileEntity e = new UserProfileEntity();
        e.userId = d.id;
        e.email = d.email != null ? d.email : "";
        e.displayName = d.displayName != null ? d.displayName : "";
        e.heightCm = d.heightCm;
        e.weightKg = d.weightKg;
        e.isPremium = Boolean.TRUE.equals(d.isPremium);
        return e;
    }

    @NonNull
    public static WorkoutSessionRequest toRequest(@NonNull WorkoutLogEntity e) {
        WorkoutSessionRequest r = new WorkoutSessionRequest();
        r.clientUuid = e.clientUuid;
        r.categoryId = e.categoryId;
        r.startedAt = DateUtils.toIso(e.startedAt);
        r.completedAt = DateUtils.toIso(e.completedAt);
        r.totalSeconds = e.totalSeconds;
        r.exercisesDone = e.exercisesDone;
        return r;
    }

    /** Bản ghi khôi phục từ server luôn là synced = true. */
    @NonNull
    public static WorkoutLogEntity toEntity(@NonNull WorkoutSessionDto d, long userId) {
        WorkoutLogEntity e = new WorkoutLogEntity();
        e.userId = userId;
        e.clientUuid = d.clientUuid != null ? d.clientUuid : "";
        e.categoryId = d.categoryId;
        e.startedAt = DateUtils.parseIso(d.startedAt);
        e.completedAt = DateUtils.parseIso(d.completedAt);
        e.totalSeconds = d.totalSeconds;
        e.exercisesDone = d.exercisesDone;
        e.synced = true;
        return e;
    }
}
