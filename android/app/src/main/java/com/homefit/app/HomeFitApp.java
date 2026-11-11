package com.homefit.app;

import android.app.Application;

import com.homefit.app.ads.AdsManager;

/** Lớp Application của app. Chỉ khởi tạo các thành phần dùng chung. */
public class HomeFitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AdsManager.init(this);
    }
}
