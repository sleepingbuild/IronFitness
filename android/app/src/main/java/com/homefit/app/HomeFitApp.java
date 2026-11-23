package com.homefit.app;

import android.app.Application;
import android.util.Log;

import com.homefit.app.ads.AdsManager;
import com.homefit.app.util.AppExecutors;

/** Lớp Application của app. Khởi tạo các tác vụ bất đồng bộ ngoài Main Thread. */
public class HomeFitApp extends Application {

    private static final String TAG = "HomeFitApp";

    @Override
    public void onCreate() {
        super.onCreate();

        setupCrashHandler();

        AppExecutors.diskIO().execute(() -> {
            try {
                AdsManager.init(this);
            } catch (Throwable e) {
                Log.w(TAG, "Khởi tạo AdsManager gặp cảnh báo", e);
            }
        });
    }

    private void setupCrashHandler() {
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Lỗi Uncaught Exception trên luồng: " + (thread != null ? thread.getName() : "unknown"), throwable);
            try {
                Class<?> crashlyticsClass = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics");
                Object instance = crashlyticsClass.getMethod("getInstance").invoke(null);
                if (instance != null) {
                    crashlyticsClass.getMethod("recordException", Throwable.class).invoke(instance, throwable);
                }
            } catch (Throwable ignored) {
            }
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }
}
