package com.homefit.app.ui.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.data.remote.ErrorCodes;
import com.homefit.app.data.repository.ExerciseRepository;
import com.homefit.app.util.Resource;

import java.util.List;

/**
 * Chi tiết một bài tập. Tìm bài trong danh sách của nhóm cơ (đọc từ Room) nên tự cập nhật
 * khi dữ liệu đổi. Gọi init(categoryId, exerciseId) trước khi quan sát.
 */
public class ExerciseDetailViewModel extends AndroidViewModel {

    private final ExerciseRepository repository;
    private final MediatorLiveData<Resource<ExerciseEntity>> exercise = new MediatorLiveData<>();
    private boolean initialized;

    public ExerciseDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application);
        exercise.setValue(Resource.loading());
    }

    public void init(long categoryId, long exerciseId) {
        if (initialized) {
            return;
        }
        initialized = true;
        LiveData<List<ExerciseEntity>> source = repository.getExercises(categoryId);
        exercise.addSource(source, list -> {
            if (list == null) {
                return;
            }
            ExerciseEntity found = find(list, exerciseId);
            exercise.setValue(found != null
                    ? Resource.success(found)
                    : Resource.error(ErrorCodes.NOT_FOUND, null));
        });
    }

    /** LOADING, SUCCESS (có bài) hoặc ERROR/NOT_FOUND. */
    public LiveData<Resource<ExerciseEntity>> getExercise() {
        return exercise;
    }

    @Nullable
    private static ExerciseEntity find(@NonNull List<ExerciseEntity> list, long id) {
        for (ExerciseEntity e : list) {
            if (e.id == id) {
                return e;
            }
        }
        return null;
    }
}
