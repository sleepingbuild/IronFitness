package com.homefit.app.ui.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.data.repository.ExerciseRepository;
import com.homefit.app.util.Resource;

import java.util.List;

/** Danh sách bài tập của một nhóm cơ, đọc từ Room. Gọi init(categoryId) trước khi quan sát. */
public class ExerciseListViewModel extends AndroidViewModel {

    private final ExerciseRepository repository;
    private final MediatorLiveData<LibraryState> state = new MediatorLiveData<>();

    @Nullable
    private LiveData<List<ExerciseEntity>> exercises;
    private long categoryId = -1;
    @Nullable
    private List<ExerciseEntity> latestList;
    @Nullable
    private Resource<Boolean> latestRefresh;
    private boolean refreshing;

    public ExerciseListViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application);
    }

    /** Gọi nhiều lần với cùng categoryId vẫn an toàn (xoay màn hình). */
    public void init(long categoryId) {
        if (exercises != null && this.categoryId == categoryId) {
            return;
        }
        this.categoryId = categoryId;
        exercises = repository.getExercises(categoryId);
        state.addSource(exercises, list -> {
            latestList = list;
            recompute();
        });
    }

    public LiveData<List<ExerciseEntity>> getExercises() {
        return exercises;
    }

    public LiveData<LibraryState> getState() {
        return state;
    }

    /** Dùng cho nút Thử lại khi danh sách rỗng: đồng bộ lại từ server. */
    public void refresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        latestRefresh = Resource.loading();
        recompute();
        LiveData<Resource<Boolean>> source = repository.refresh();
        state.addSource(source, resource -> {
            if (resource == null) {
                return;
            }
            latestRefresh = resource;
            if (!resource.isLoading()) {
                refreshing = false;
                state.removeSource(source);
            }
            recompute();
        });
    }

    private void recompute() {
        state.setValue(LibraryState.of(latestList, latestRefresh));
    }
}
