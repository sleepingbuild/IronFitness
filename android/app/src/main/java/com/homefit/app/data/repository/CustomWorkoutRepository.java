package com.homefit.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.homefit.app.data.local.model.CustomWorkout;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CustomWorkoutRepository {

    private static final String PREF_NAME = "custom_workouts_pref";
    private static final String KEY_WORKOUTS = "key_custom_workouts_json";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public CustomWorkoutRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public synchronized List<CustomWorkout> getAllWorkouts() {
        String json = prefs.getString(KEY_WORKOUTS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<ArrayList<CustomWorkout>>() {}.getType();
            List<CustomWorkout> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized CustomWorkout getWorkoutById(String id) {
        if (id == null) return null;
        List<CustomWorkout> all = getAllWorkouts();
        for (CustomWorkout w : all) {
            if (id.equals(w.id)) {
                return w;
            }
        }
        return null;
    }

    public synchronized void saveWorkout(CustomWorkout workout) {
        if (workout == null) return;
        workout.calculateDurationMin();

        List<CustomWorkout> all = getAllWorkouts();
        int existingIndex = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id.equals(workout.id)) {
                existingIndex = i;
                break;
            }
        }

        String now = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(new java.util.Date());
        workout.updatedAt = now;

        if (existingIndex >= 0) {
            all.set(existingIndex, workout);
        } else {
            if (workout.id == null || workout.id.isEmpty()) {
                workout.id = "custom_" + System.currentTimeMillis();
            }
            workout.createdAt = now;
            all.add(workout);
        }

        String json = gson.toJson(all);
        prefs.edit().putString(KEY_WORKOUTS, json).apply();
    }

    public synchronized void deleteWorkout(String id) {
        if (id == null) return;
        List<CustomWorkout> all = getAllWorkouts();
        boolean removed = false;
        for (int i = 0; i < all.size(); i++) {
            if (id.equals(all.get(i).id)) {
                all.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            String json = gson.toJson(all);
            prefs.edit().putString(KEY_WORKOUTS, json).apply();
        }
    }
}
