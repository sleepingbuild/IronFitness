package com.homefit.app.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.data.prefs.SyncStore;
import com.homefit.app.data.remote.ApiCallback;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.data.remote.ApiService;
import com.homefit.app.data.remote.dto.CategoryDto;
import com.homefit.app.data.remote.dto.ExerciseDto;
import com.homefit.app.domain.Mappers;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.DateUtils;
import com.homefit.app.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Danh mục và bài tập. Room là nguồn dữ liệu cho UI (offline-first);
 * refresh() tải từ server (chỉ phần mới đổi nhờ updatedAfter) rồi ghi vào Room.
 */
public class ExerciseRepository {

    private final ApiService api;
    private final AppDatabase db;
    private final SyncStore syncStore;

    public ExerciseRepository(@NonNull Context context) {
        Context app = context.getApplicationContext();
        this.api = ApiClient.get(app);
        this.db = AppDatabase.getInstance(app);
        this.syncStore = new SyncStore(app);
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return db.categoryDao().getAll();
    }

    public LiveData<List<ExerciseEntity>> getExercises(long categoryId) {
        return db.exerciseDao().getByCategory(categoryId);
    }

    /** Nạp danh sách bài của một nhóm một lần (cho Workout Player). Consumer chạy ở main thread. */
    public void getExercisesOnce(long categoryId, @NonNull Consumer<List<ExerciseEntity>> consumer) {
        AppExecutors.diskIO().execute(() -> {
            List<ExerciseEntity> list = db.exerciseDao().getByCategoryNow(categoryId);
            AppExecutors.mainThread().execute(() -> consumer.accept(list));
        });
    }

    /**
     * Đồng bộ danh mục rồi bài tập. SUCCESS(true) khi cả hai xong; ERROR nếu một bước lỗi
     * (dữ liệu cũ trong Room vẫn dùng được).
     */
    public LiveData<Resource<Boolean>> refresh() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>(Resource.loading());
        syncCategories(result);
        return result;
    }

    private void syncCategories(MutableLiveData<Resource<Boolean>> result) {
        final String since = syncStore.getCategoriesSince();
        api.getCategories(since).enqueue(new ApiCallback<List<CategoryDto>>() {
            @Override
            protected void onSuccess(@NonNull List<CategoryDto> body) {
                AppExecutors.diskIO().execute(() -> {
                    applyCategories(body, since);
                    syncExercises(result);
                });
            }

            @Override
            protected void onError(@NonNull String code, @Nullable String message) {
                result.setValue(Resource.error(code, message));
            }
        });
    }

    private void syncExercises(MutableLiveData<Resource<Boolean>> result) {
        final String since = syncStore.getExercisesSince();
        api.getExercises(null, since).enqueue(new ApiCallback<List<ExerciseDto>>() {
            @Override
            protected void onSuccess(@NonNull List<ExerciseDto> body) {
                AppExecutors.diskIO().execute(() -> {
                    applyExercises(body, since);
                    result.postValue(Resource.success(true));
                });
            }

            @Override
            protected void onError(@NonNull String code, @Nullable String message) {
                result.setValue(Resource.error(code, message));
            }
        });
    }

    /** Chạy trên diskIO. */
    private void applyCategories(List<CategoryDto> dtos, @Nullable String since) {
        final List<CategoryEntity> active = new ArrayList<>();
        final List<Long> activeIds = new ArrayList<>();
        final List<Long> inactiveIds = new ArrayList<>();
        String newest = since;
        long newestMs = DateUtils.parseIso(since);
        for (CategoryDto d : dtos) {
            if (Boolean.FALSE.equals(d.isActive)) {
                inactiveIds.add(d.id);
            } else {
                active.add(Mappers.toEntity(d));
                activeIds.add(d.id);
            }
            long ms = DateUtils.parseIso(d.updatedAt);
            if (ms > newestMs) {
                newestMs = ms;
                newest = d.updatedAt;
            }
        }
        final boolean fullSync = since == null;
        db.runInTransaction(() -> {
            if (!active.isEmpty()) {
                db.categoryDao().upsertAll(active);
            }
            if (!inactiveIds.isEmpty()) {
                db.categoryDao().deleteByIds(inactiveIds);
            }
            if (fullSync && !activeIds.isEmpty()) {
                db.categoryDao().deleteMissing(activeIds);
            }
        });
        if (newest != null) {
            syncStore.setCategoriesSince(newest);
        }
    }

    /** Chạy trên diskIO. */
    private void applyExercises(List<ExerciseDto> dtos, @Nullable String since) {
        final List<ExerciseEntity> active = new ArrayList<>();
        final List<Long> activeIds = new ArrayList<>();
        final List<Long> inactiveIds = new ArrayList<>();
        String newest = since;
        long newestMs = DateUtils.parseIso(since);
        for (ExerciseDto d : dtos) {
            if (Boolean.FALSE.equals(d.isActive)) {
                inactiveIds.add(d.id);
            } else {
                active.add(Mappers.toEntity(d));
                activeIds.add(d.id);
            }
            long ms = DateUtils.parseIso(d.updatedAt);
            if (ms > newestMs) {
                newestMs = ms;
                newest = d.updatedAt;
            }
        }
        final boolean fullSync = since == null;
        db.runInTransaction(() -> {
            if (!active.isEmpty()) {
                db.exerciseDao().upsertAll(active);
            }
            if (!inactiveIds.isEmpty()) {
                db.exerciseDao().deleteByIds(inactiveIds);
            }
            if (fullSync && !activeIds.isEmpty()) {
                db.exerciseDao().deleteMissing(activeIds);
            }
        });
        if (newest != null) {
            syncStore.setExercisesSince(newest);
        }
    }
}
