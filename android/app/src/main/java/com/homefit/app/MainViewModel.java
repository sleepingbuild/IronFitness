package com.homefit.app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.remote.ErrorCodes;
import com.homefit.app.data.repository.AuthRepository;
import com.homefit.app.data.repository.WorkoutLogRepository;
import com.homefit.app.ui.common.Event;
import com.homefit.app.util.Resource;

/**
 * ViewModel của MainActivity (W2-TV6-03, gồm phần W2-TV2-07):
 * khi mở app đã đăng nhập thì đẩy buổi tập chưa đồng bộ và làm mới hồ sơ; nếu server báo hết phiên
 * (401 sau khi thử refresh) thì xóa phiên và báo Activity đưa người dùng về Đăng nhập.
 */
public class MainViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final LiveData<UserProfileEntity> profile;
    private final MediatorLiveData<Event<Boolean>> sessionExpired = new MediatorLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        profile = authRepository.getProfile();

        if (authRepository.isLoggedIn()) {
            new WorkoutLogRepository(application).syncPending();
            LiveData<Resource<UserProfileEntity>> refresh = authRepository.refreshProfile();
            sessionExpired.addSource(refresh, resource -> {
                if (resource == null || resource.isLoading()) {
                    return;
                }
                sessionExpired.removeSource(refresh);
                if (resource.isError() && ErrorCodes.UNAUTHORIZED.equals(resource.code)) {
                    authRepository.logout();
                    sessionExpired.setValue(new Event<>(true));
                }
            });
        }
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    /** Hồ sơ trong Room; dùng để bật/tắt quảng cáo theo cờ Premium. */
    public LiveData<UserProfileEntity> getProfile() {
        return profile;
    }

    /** Phát một lần khi phiên đăng nhập hết hạn. */
    public LiveData<Event<Boolean>> getSessionExpired() {
        return sessionExpired;
    }
}
