package com.homefit.app;

import android.app.Application;

import com.homefit.app.ads.AdsManager;

import com.homefit.app.data.repository.AuthRepository;

/** Lớp Application của app. Chỉ khởi tạo các thành phần dùng chung. */
public class HomeFitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AdsManager.init(this);
        AuthRepository.ensureDefaultProAccount(this);
    }
}
