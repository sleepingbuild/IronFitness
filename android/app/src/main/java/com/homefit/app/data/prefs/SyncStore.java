package com.homefit.app.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Lưu mốc đồng bộ (chuỗi ISO-8601 lấy từ updatedAt lớn nhất đã nhận) để lần sau gọi API với updatedAfter.
 */
public class SyncStore {

    private static final String FILE = "homefit_sync";
    private static final String KEY_CATEGORIES = "categoriesSince";
    private static final String KEY_EXERCISES = "exercisesSince";

    private final SharedPreferences prefs;

    public SyncStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    @Nullable
    public String getCategoriesSince() {
        return prefs.getString(KEY_CATEGORIES, null);
    }

    public void setCategoriesSince(String iso) {
        prefs.edit().putString(KEY_CATEGORIES, iso).apply();
    }

    @Nullable
    public String getExercisesSince() {
        return prefs.getString(KEY_EXERCISES, null);
    }

    public void setExercisesSince(String iso) {
        prefs.edit().putString(KEY_EXERCISES, iso).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
