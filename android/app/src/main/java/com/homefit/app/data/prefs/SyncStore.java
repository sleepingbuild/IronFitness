package com.homefit.app.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Lưu mốc đồng bộ và chuỗi JSON cache dữ liệu bài tập / buổi tập cho chế độ Offline.
 */
public class SyncStore {

    private static final String FILE = "homefit_sync";
    private static final String KEY_CATEGORIES = "categoriesSince";
    private static final String KEY_EXERCISES = "exercisesSince";
    private static final String KEY_CATEGORIES_JSON = "categoriesJson";
    private static final String KEY_EXERCISES_JSON = "exercisesJson";
    private static final String KEY_WORKOUTS_JSON = "workoutsJson";

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

    @Nullable
    public String getCategoriesJson() {
        return prefs.getString(KEY_CATEGORIES_JSON, null);
    }

    public void saveCategoriesJson(String json) {
        prefs.edit().putString(KEY_CATEGORIES_JSON, json).apply();
    }

    @Nullable
    public String getExercisesJson() {
        return prefs.getString(KEY_EXERCISES_JSON, null);
    }

    public void saveExercisesJson(String json) {
        prefs.edit().putString(KEY_EXERCISES_JSON, json).apply();
    }

    @Nullable
    public String getWorkoutsJson() {
        return prefs.getString(KEY_WORKOUTS_JSON, null);
    }

    public void saveWorkoutsJson(String json) {
        prefs.edit().putString(KEY_WORKOUTS_JSON, json).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
