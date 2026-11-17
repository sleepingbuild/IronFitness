package com.homefit.app.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * Tiện ích theo dõi sự kiện Analytics & Crashlytics (an toàn try-catch).
 * Không làm crash ứng dụng ngay cả khi chưa có google-services.json.
 */
public final class AnalyticsTracker {

    private static final String TAG = "AnalyticsTracker";

    private AnalyticsTracker() {
    }

    public static void logLogin(Context context, String method) {
        if (context == null) return;
        try {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.METHOD, method != null ? method : "email");
            analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
        } catch (Throwable e) {
            Log.w(TAG, "Analytics logLogin skipped", e);
        }
    }

    public static void logSignUp(Context context, String method) {
        if (context == null) return;
        try {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.METHOD, method != null ? method : "email");
            analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
        } catch (Throwable e) {
            Log.w(TAG, "Analytics logSignUp skipped", e);
        }
    }

    public static void logWorkoutStart(Context context, String workoutName, String groupKey, String difficulty) {
        if (context == null) return;
        try {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, workoutName != null ? workoutName : "Workout");
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, groupKey != null ? groupKey : "general");
            bundle.putString("difficulty", difficulty != null ? difficulty : "medium");
            analytics.logEvent("workout_start", bundle);
        } catch (Throwable e) {
            Log.w(TAG, "Analytics logWorkoutStart skipped", e);
        }
    }

    public static void logWorkoutComplete(Context context, String workoutName, int activeDurationSec, double caloriesBurned) {
        if (context == null) return;
        try {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, workoutName != null ? workoutName : "Workout");
            bundle.putInt("active_duration_sec", activeDurationSec);
            bundle.putDouble("calories_burned", caloriesBurned);
            analytics.logEvent("workout_complete", bundle);
        } catch (Throwable e) {
            Log.w(TAG, "Analytics logWorkoutComplete skipped", e);
        }
    }

    public static void logCustomWorkoutCreated(Context context, int exerciseCount) {
        if (context == null) return;
        try {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Bundle bundle = new Bundle();
            bundle.putInt("exercise_count", exerciseCount);
            analytics.logEvent("custom_workout_created", bundle);
        } catch (Throwable e) {
            Log.w(TAG, "Analytics logCustomWorkoutCreated skipped", e);
        }
    }

    public static void recordNonFatalException(Throwable throwable) {
        if (throwable == null) return;
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable);
        } catch (Throwable e) {
            Log.w(TAG, "Crashlytics recordException skipped", e);
        }
    }
}
