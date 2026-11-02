package com.homefit.app.ui.settings;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.repository.AuthRepository;

import java.lang.reflect.Method;

/**
 * ViewModel cho màn Cài đặt (W2-TV6-06 / W2-TV2-05).
 * Lưu cấu hình nhắc tập vào SharedPreferences ("homefit_settings") và kích hoạt
 * ReminderScheduler của Agent 2 (nếu đã merge) hoặc gửi Broadcast để đồng bộ lịch hẹn.
 */
public class SettingsViewModel extends AndroidViewModel {

    private static final String PREFS_FILE = "homefit_settings";
    private static final String KEY_ENABLED = "reminder_enabled";
    private static final String KEY_HOUR = "reminder_hour";
    private static final String KEY_MINUTE = "reminder_minute";
    private static final int DEFAULT_HOUR = 18;
    private static final int DEFAULT_MINUTE = 0;

    private final SharedPreferences prefs;
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> reminderEnabled = new MutableLiveData<>();
    private final MutableLiveData<Integer> reminderHour = new MutableLiveData<>();
    private final MutableLiveData<Integer> reminderMinute = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        this.prefs = application.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        this.authRepository = new AuthRepository(application);

        reminderEnabled.setValue(prefs.getBoolean(KEY_ENABLED, false));
        reminderHour.setValue(prefs.getInt(KEY_HOUR, DEFAULT_HOUR));
        reminderMinute.setValue(prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE));
    }

    public LiveData<Boolean> getReminderEnabled() {
        return reminderEnabled;
    }

    public LiveData<Integer> getReminderHour() {
        return reminderHour;
    }

    public LiveData<Integer> getReminderMinute() {
        return reminderMinute;
    }

    public LiveData<UserProfileEntity> getProfile() {
        return authRepository.getProfile();
    }

    public void setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
        reminderEnabled.setValue(enabled);
        int h = reminderHour.getValue() != null ? reminderHour.getValue() : DEFAULT_HOUR;
        int m = reminderMinute.getValue() != null ? reminderMinute.getValue() : DEFAULT_MINUTE;
        notifyReminderScheduleChanged(enabled, h, m);
    }

    public void setReminderTime(int hourOfDay, int minute) {
        prefs.edit()
                .putInt(KEY_HOUR, hourOfDay)
                .putInt(KEY_MINUTE, minute)
                .apply();
        reminderHour.setValue(hourOfDay);
        reminderMinute.setValue(minute);
        boolean enabled = Boolean.TRUE.equals(reminderEnabled.getValue());
        if (enabled) {
            notifyReminderScheduleChanged(true, hourOfDay, minute);
        }
    }

    public void logout() {
        setReminderEnabled(false);
        authRepository.logout();
    }

    /**
     * Nối mềm sang ReminderScheduler của Agent 2 (nếu có mặt trong classpath)
     * mà không gây lỗi biên dịch khi chạy độc lập.
     */
    private void notifyReminderScheduleChanged(boolean enabled, int hour, int minute) {
        Context app = getApplication();
        try {
            Class<?> schedulerClass = Class.forName("com.homefit.app.notification.ReminderScheduler");
            if (enabled) {
                Method schedule = schedulerClass.getMethod("scheduleDaily", Context.class, int.class, int.class);
                schedule.invoke(null, app, hour, minute);
            } else {
                Method cancel = schedulerClass.getMethod("cancel", Context.class);
                cancel.invoke(null, app);
            }
        } catch (Exception ignored) {
            Intent intent = new Intent("com.homefit.app.ACTION_REMINDER_CHANGED");
            intent.setPackage(app.getPackageName());
            intent.putExtra(KEY_ENABLED, enabled);
            intent.putExtra(KEY_HOUR, hour);
            intent.putExtra(KEY_MINUTE, minute);
            app.sendBroadcast(intent);
        }
    }
}