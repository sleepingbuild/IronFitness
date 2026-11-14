package com.homefit.app.domain;

import androidx.annotation.NonNull;

import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.local.entity.WorkoutEntity;
import com.homefit.app.data.local.entity.WorkoutLogEntity;
import com.homefit.app.data.remote.dto.CategoryDto;
import com.homefit.app.data.remote.dto.ExerciseDto;
import com.homefit.app.data.remote.dto.UserDto;
import com.homefit.app.data.remote.dto.WorkoutDto;
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

    @NonNull
    public static WorkoutEntity toEntity(@NonNull WorkoutDto d) {
        WorkoutEntity e = new WorkoutEntity();
        if (d.id != null) e.id = d.id;
        e.type = d.type != null ? d.type : "";
        e.titleVi = d.titleVi != null ? d.titleVi : "";
        e.titleEn = d.titleEn;
        e.primaryMuscle = d.primaryMuscle != null ? d.primaryMuscle : "";
        e.level = d.level != null ? d.level : "";
        e.goal = d.goal;
        e.workSec = d.workSec;
        e.restSec = d.restSec;
        e.roundRestSec = d.roundRestSec;
        e.rounds = d.rounds;
        e.equipment = d.equipment != null ? d.equipment : "none";
        e.coverImageUrl = d.coverImageUrl;
        e.sortOrder = d.sortOrder;
        e.status = d.status != null ? d.status : "DRAFT";
        e.version = d.version;
        return e;
    }

    @NonNull
    public static LocalExerciseGroupDto toLocalDto(@NonNull WorkoutDto d) {
        LocalExerciseGroupDto dto = new LocalExerciseGroupDto();
        dto.group = d.goal != null && !d.goal.isEmpty() ? d.goal : (d.primaryMuscle != null ? d.primaryMuscle.toLowerCase() : "");
        dto.group_name = d.titleVi;
        dto.difficulty = d.level;
        int count = d.items != null ? d.items.size() : 10;
        dto.duration_min = Math.max(1, (d.workSec + d.restSec) * count / 60);
        dto.coverImageUrl = d.coverImageUrl;
        return dto;
    }
}
