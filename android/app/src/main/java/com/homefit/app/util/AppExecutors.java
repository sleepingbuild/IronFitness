package com.homefit.app.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Luồng dùng chung: diskIO (một luồng, ghi Room tuần tự) và mainThread. */
public final class AppExecutors {

    private static final Executor DISK_IO = Executors.newSingleThreadExecutor();
    private static final Executor MAIN_THREAD = new Executor() {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    };

    private AppExecutors() {
    }

    public static Executor diskIO() {
        return DISK_IO;
    }

    public static Executor mainThread() {
        return MAIN_THREAD;
    }
}
