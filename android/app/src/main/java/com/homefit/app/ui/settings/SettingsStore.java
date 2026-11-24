package com.homefit.app.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStore {
    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    private static final String KEY_APP_LANGUAGE = "app_language";
    private static final String KEY_NIGHT_MODE = "night_mode";

    private final SharedPreferences prefs;

    public SettingsStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getNightMode() {
        return prefs.getInt(KEY_NIGHT_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setNightMode(int mode) {
        prefs.edit().putInt(KEY_NIGHT_MODE, mode).apply();
    }

    public String getLanguageCode() {
        return prefs.getString(KEY_APP_LANGUAGE, "vi");
    }

    public void setLanguageCode(String code) {
        prefs.edit().putString(KEY_APP_LANGUAGE, code).apply();
    }

    public boolean isReminderEnabled() {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false);
    }

    public void setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
    }

    public int getReminderHour() {
        return prefs.getInt(KEY_REMINDER_HOUR, 18); // Default 6 PM
    }

    public int getReminderMinute() {
        return prefs.getInt(KEY_REMINDER_MINUTE, 0);
    }

    public void setReminderTime(int hour, int minute) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply();
    }

    public boolean isVoiceCoachEnabled() {
        return prefs.getBoolean("voice_coach_enabled", true);
    }

    public void setVoiceCoachEnabled(boolean enabled) {
        prefs.edit().putBoolean("voice_coach_enabled", enabled).apply();
    }

    public boolean isSoundEffectsEnabled() {
        return prefs.getBoolean("sound_effects_enabled", true);
    }

    public void setSoundEffectsEnabled(boolean enabled) {
        prefs.edit().putBoolean("sound_effects_enabled", enabled).apply();
    }

    public float getDifficultyMultiplier() {
        return prefs.getFloat("difficulty_multiplier", 1.0f);
    }

    public void setDifficultyMultiplier(float multiplier) {
        prefs.edit().putFloat("difficulty_multiplier", multiplier).apply();
    }
}