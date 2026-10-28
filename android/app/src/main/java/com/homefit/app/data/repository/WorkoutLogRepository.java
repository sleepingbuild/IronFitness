package com.homefit.app.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.WorkoutLogEntity;
import com.homefit.app.data.prefs.TokenStore;
import com.homefit.app.data.remote.ApiCallback;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.data.remote.ApiService;
import com.homefit.app.data.remote.ErrorCodes;
import com.homefit.app.data.remote.dto.BatchSessionsRequest;
import com.homefit.app.data.remote.dto.BatchSessionsResponse;
import com.homefit.app.data.remote.dto.WorkoutSessionDto;
import com.homefit.app.data.remote.dto.WorkoutSessionRequest;
import com.homefit.app.domain.Mappers;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.DateUtils;
import com.homefit.app.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lịch sử buổi tập: ghi Room trước (synced = false), đồng bộ lên server sau bằng clientUuid (idempotent).
 * syncPending() cần được gọi khi: vừa lưu buổi tập (đã tự gọi), sau đăng nhập, và khi mở app.
 */
public class WorkoutLogRepository {

    private static final int BATCH_SIZE = 100;
    private static final long ONE_DAY_MS = 24L * 60L * 60L * 1000L;

    private final ApiService api;
    private final AppDatabase db;
    private final TokenStore tokenStore;

    public WorkoutLogRepository(@NonNull Context context) {
        Context app = context.getApplicationContext();
        this.api = ApiClient.get(app);
        this.db = AppDatabase.getInstance(app);
        this.tokenStore = new TokenStore(app);
    }

    /**
     * Lưu buổi tập vừa hoàn thành. SUCCESS chứa clientUuid. Sau khi lưu sẽ tự thử đồng bộ.
     */
    public LiveData<Resource<String>> saveWorkout(@Nullable Long categoryId, long startedAt, long completedAt,
                                                  int totalSeconds, int exercisesDone) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>(Resource.loading());
        AppExecutors.diskIO().execute(() -> {
            long userId = tokenStore.getUserId();
            if (userId <= 0) {
                result.postValue(Resource.error(ErrorCodes.UNAUTHORIZED, null));
                return;
            }
            WorkoutLogEntity log = new WorkoutLogEntity();
            log.userId = userId;
            log.clientUuid = UUID.randomUUID().toString();
            log.categoryId = categoryId;
            log.startedAt = startedAt;
            log.completedAt = completedAt;
            log.totalSeconds = totalSeconds;
            log.exercisesDone = exercisesDone;
            log.synced = false;
            db.workoutLogDao().insert(log);
            result.postValue(Resource.success(log.clientUuid));
            syncPendingInternal();
        });
        return result;
    }

    /** Đẩy các buổi tập chưa đồng bộ lên server. Lỗi mạng thì giữ nguyên để thử lại lần sau. */
    public void syncPending() {
        AppExecutors.diskIO().execute(this::syncPendingInternal);
    }

    /** Chạy trên diskIO. */
    private void syncPendingInternal() {
        long userId = tokenStore.getUserId();
        if (userId <= 0) {
            return;
        }
        List<WorkoutLogEntity> pending = db.workoutLogDao().getUnsynced(userId, BATCH_SIZE);
        if (pending.isEmpty()) {
            return;
        }
        List<WorkoutSessionRequest> requests = new ArrayList<>();
        for (WorkoutLogEntity log : pending) {
            requests.add(Mappers.toRequest(log));
        }
        api.syncBatch(new BatchSessionsRequest(requests)).enqueue(new ApiCallback<BatchSessionsResponse>() {
            @Override
            protected void onSuccess(@NonNull BatchSessionsResponse body) {
                if (body.syncedUuids != null && !body.syncedUuids.isEmpty()) {
                    List<String> uuids = new ArrayList<>(body.syncedUuids);
                    AppExecutors.diskIO().execute(() -> db.workoutLogDao().markSynced(uuids));
                }
            }

            @Override
            protected void onError(@NonNull String code, @Nullable String message) {
                // Giữ synced = false, lần gọi syncPending() sau sẽ thử lại.
            }
        });
    }

    /**
     * Đăng nhập trên máy mới: tải lịch sử từ server vào Room. SUCCESS chứa số bản ghi nhận được.
     */
    public LiveData<Resource<Integer>> restoreHistory() {
        MutableLiveData<Resource<Integer>> result = new MutableLiveData<>(Resource.loading());
        long userId = tokenStore.getUserId();
        if (userId <= 0) {
            result.setValue(Resource.error(ErrorCodes.UNAUTHORIZED, null));
            return result;
        }
        String from = "2000-01-01T00:00:00Z";
        String to = DateUtils.toIso(System.currentTimeMillis() + ONE_DAY_MS);
        api.getSessions(from, to).enqueue(new ApiCallback<List<WorkoutSessionDto>>() {
            @Override
            protected void onSuccess(@NonNull List<WorkoutSessionDto> body) {
                AppExecutors.diskIO().execute(() -> {
                    List<WorkoutLogEntity> logs = new ArrayList<>();
                    for (WorkoutSessionDto d : body) {
                        logs.add(Mappers.toEntity(d, userId));
                    }
                    db.workoutLogDao().insertAll(logs);
                    result.postValue(Resource.success(logs.size()));
                });
            }

            @Override
            protected void onError(@NonNull String code, @Nullable String message) {
                result.setValue(Resource.error(code, message));
            }
        });
        return result;
    }

    // ---- Truy vấn cho màn Lịch sử / Thống kê (luôn theo người dùng đang đăng nhập) ----

    public LiveData<List<WorkoutLogEntity>> getByMonth(long fromMillis, long toMillis) {
        return db.workoutLogDao().getByMonth(tokenStore.getUserId(), fromMillis, toMillis);
    }

    /** Thời điểm hoàn thành (epoch millis) để đánh dấu ngày trên Calendar. */
    public LiveData<List<Long>> getDatesWithWorkout(long fromMillis, long toMillis) {
        return db.workoutLogDao().getDatesWithWorkout(tokenStore.getUserId(), fromMillis, toMillis);
    }

    public LiveData<Integer> getTotalCount() {
        return db.workoutLogDao().countAll(tokenStore.getUserId());
    }

    public LiveData<Integer> getTotalSeconds() {
        return db.workoutLogDao().sumSeconds(tokenStore.getUserId());
    }
}
