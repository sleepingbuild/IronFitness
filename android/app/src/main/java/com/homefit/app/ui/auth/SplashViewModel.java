package com.homefit.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.homefit.app.data.repository.AuthRepository;

public class SplashViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    public SplashViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    /** Đã có token đăng nhập hay chưa. */
    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }
}
