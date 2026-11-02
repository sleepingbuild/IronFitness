package com.homefit.app.ui.history;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.homefit.app.data.repository.WorkoutLogRepository;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final WorkoutLogRepository repository;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        // FIXME: Thay bằng DI sau
        repository = new WorkoutLogRepository(application);
    }

    public LiveData<Integer> getTotalWorkouts() {
        return repository.getTotalCount();
    }

    public LiveData<Integer> getTotalTimeSeconds() {
        return repository.getTotalSeconds();
    }

    // Lấy các ngày có tập trong khoảng thời gian từ startOfMonth đến endOfMonth
    public LiveData<List<Long>> getWorkoutDates(long startOfMonth, long endOfMonth) {
        return repository.getDatesWithWorkout(startOfMonth, endOfMonth);
    }
    
    public void syncHistory() {
        repository.syncPending();
    }
}