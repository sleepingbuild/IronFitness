package com.homefit.app.ui.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.data.repository.ExerciseRepository;
import com.homefit.app.util.Resource;

import java.util.List;

/**
 * Danh mục nhóm cơ. Đọc từ Room, đồng bộ từ server khi mở màn (ExerciseRepository.refresh).
 */
public class CategoryViewModel extends AndroidViewModel {

    private final ExerciseRepository repository;
    private final LiveData<List<CategoryEntity>> categories;
    private final MediatorLiveData<LibraryState> state = new MediatorLiveData<>();

    @Nullable
    private List<CategoryEntity> latestList;
    @Nullable
    private Resource<Boolean> latestRefresh = Resource.loading();
    private boolean refreshing;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application);
        categories = repository.getCategories();
        state.addSource(categories, list -> {
            latestList = list;
            recompute();
        });
        refresh();
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public LiveData<LibraryState> getState() {
        return state;
    }

    /** Tải lại từ server. Bỏ qua nếu đang tải. */
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
