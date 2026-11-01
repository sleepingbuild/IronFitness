package com.homefit.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.repository.AuthRepository;
import com.homefit.app.data.repository.WorkoutLogRepository;
import com.homefit.app.util.Resource;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final MediatorLiveData<Resource<UserProfileEntity>> state = new MediatorLiveData<>();
    private LiveData<Resource<UserProfileEntity>> source;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        workoutLogRepository = new WorkoutLogRepository(application);
    }

    /** Trạng thái đăng nhập: null (chưa làm gì), LOADING, SUCCESS hoặc ERROR. */
    public LiveData<Resource<UserProfileEntity>> getState() {
        return state;
    }

    public void login(@NonNull String email, @NonNull String password) {
        Resource<UserProfileEntity> current = state.getValue();
        if (current != null && current.isLoading()) {
            return;
        }
        if (source != null) {
            state.removeSource(source);
        }
        source = authRepository.login(email, password);
        state.addSource(source, resource -> {
            state.setValue(resource);
            if (resource != null && resource.isSuccess()) {
                onLoggedIn();
            }
        });
    }

    /** Sau đăng nhập: đẩy các buổi tập chưa đồng bộ và khôi phục lịch sử từ server (W2-TV2-07). */
    private void onLoggedIn() {
        workoutLogRepository.syncPending();
        workoutLogRepository.restoreHistory();
    }
}
