package com.homefit.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.repository.AuthRepository;
import com.homefit.app.util.Resource;

public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MediatorLiveData<Resource<UserProfileEntity>> state = new MediatorLiveData<>();
    private LiveData<Resource<UserProfileEntity>> source;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    /** Trạng thái đăng ký: null (chưa làm gì), LOADING, SUCCESS hoặc ERROR. */
    public LiveData<Resource<UserProfileEntity>> getState() {
        return state;
    }

    public void register(@NonNull String email, @NonNull String password, @NonNull String displayName) {
        Resource<UserProfileEntity> current = state.getValue();
        if (current != null && current.isLoading()) {
            return;
        }
        if (source != null) {
            state.removeSource(source);
        }
        source = authRepository.register(email, password, displayName);
        state.addSource(source, state::setValue);
    }
}
