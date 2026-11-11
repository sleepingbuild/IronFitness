package com.homefit.app.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class WorkoutProgressStore {

    private static final String PREF_NAME = "workout_progress_pref";
    private final SharedPreferences prefs;

    public static class SavedProgress {
        public String workoutKey;
        public String title;
        public int completedIndex; // index to resume at (e.g. 1)
        public int totalExercises;
        public int percentCompleted;

        public SavedProgress(String workoutKey, String title, int completedIndex, int totalExercises) {
            this.workoutKey = workoutKey;
            this.title = title;
            this.completedIndex = completedIndex;
            this.totalExercises = totalExercises;
            this.percentCompleted = totalExercises > 0 ? (int) Math.round(((double) completedIndex / totalExercises) * 100) : 0;
        }
    }

    public WorkoutProgressStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveProgress(String workoutKey, String title, int completedIndex, int totalExercises) {
        if (workoutKey == null || workoutKey.isEmpty()) return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(workoutKey + "_title", title);
        editor.putInt(workoutKey + "_index", completedIndex);
        editor.putInt(workoutKey + "_total", totalExercises);
        editor.apply();
    }

    public SavedProgress getProgress(String workoutKey) {
        if (workoutKey == null || !prefs.contains(workoutKey + "_index")) {
            return null;
        }
        String title = prefs.getString(workoutKey + "_title", "");
        int index = prefs.getInt(workoutKey + "_index", 0);
        int total = prefs.getInt(workoutKey + "_total", 0);
        if (index <= 0 && total <= 0) return null;
        return new SavedProgress(workoutKey, title, index, total);
    }

    public void clearProgress(String workoutKey) {
        if (workoutKey == null) return;
        prefs.edit()
                .remove(workoutKey + "_title")
                .remove(workoutKey + "_index")
                .remove(workoutKey + "_total")
                .apply();
    }
}
