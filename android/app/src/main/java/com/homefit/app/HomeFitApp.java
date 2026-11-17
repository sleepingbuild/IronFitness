package com.homefit.app;

import android.app.Application;
import android.util.Log;

import com.homefit.app.ads.AdsManager;
import com.homefit.app.util.AppExecutors;

/** Lớp Application của app. Khởi tạo các tác vụ bất đồng bộ ngoài Main Thread. */
public class HomeFitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppExecutors.diskIO().execute(() -> {
            try {
                AdsManager.init(this);
            } catch (Throwable e) {
                Log.w("HomeFitApp", "Khởi tạo AdsManager gặp cảnh báo", e);
            }
        });
    }
}
