package com.homefit.app.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class OnboardingStore {
    private static final String PREF_NAME = "onboarding_prefs";
    private static final String KEY_COMPLETED = "onboarding_completed";
    private static final String KEY_GOAL = "user_goal"; // fatloss, muscle, abs, keepfit
    private static final String KEY_PRIORITY_AREA = "priority_area"; // fullbody, abs, chest_arms, legs_glutes
    private static final String KEY_FITNESS_LEVEL = "fitness_level"; // beginner, intermediate, advanced
    private static final String KEY_DAYS_PER_WEEK = "days_per_week"; // 3, 4, 5, 6
    private static final String KEY_GENDER = "gender"; // Nam, Nữ, Khác
    private static final String KEY_AGE = "age";
    private static final String KEY_HEIGHT_CM = "height_cm";
    private static final String KEY_WEIGHT_KG = "weight_kg";
    private static final String KEY_TARGET_WEIGHT_KG = "target_weight_kg";

    private final SharedPreferences prefs;

    public OnboardingStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_COMPLETED, completed).apply();
    }

    public void saveOnboardingData(String goal, String priorityArea, String fitnessLevel, int daysPerWeek,
                                  String gender, int age, float heightCm, float weightKg, float targetWeightKg) {
        prefs.edit()
                .putBoolean(KEY_COMPLETED, true)
                .putString(KEY_GOAL, goal)
                .putString(KEY_PRIORITY_AREA, priorityArea)
                .putString(KEY_FITNESS_LEVEL, fitnessLevel)
                .putInt(KEY_DAYS_PER_WEEK, daysPerWeek)
                .putString(KEY_GENDER, gender)
                .putInt(KEY_AGE, age)
                .putFloat(KEY_HEIGHT_CM, heightCm)
                .putFloat(KEY_WEIGHT_KG, weightKg)
                .putFloat(KEY_TARGET_WEIGHT_KG, targetWeightKg)
                .apply();
    }

    public String getGoal() {
        return prefs.getString(KEY_GOAL, "fatloss");
    }

    public String getPriorityArea() {
        return prefs.getString(KEY_PRIORITY_AREA, "fullbody");
    }

    public String getFitnessLevel() {
        return prefs.getString(KEY_FITNESS_LEVEL, "beginner");
    }

    public int getDaysPerWeek() {
        return prefs.getInt(KEY_DAYS_PER_WEEK, 4);
    }

    public String getGender() {
        return prefs.getString(KEY_GENDER, "Nam");
    }

    public int getAge() {
        return prefs.getInt(KEY_AGE, 25);
    }

    public float getHeightCm() {
        return prefs.getFloat(KEY_HEIGHT_CM, 170f);
    }

    public float getWeightKg() {
        return prefs.getFloat(KEY_WEIGHT_KG, 65f);
    }

    public float getTargetWeightKg() {
        return prefs.getFloat(KEY_TARGET_WEIGHT_KG, 60f);
    }

    public double calculateBmi() {
        float h = getHeightCm() / 100f;
        float w = getWeightKg();
        if (h <= 0) return 22.0;
        return w / (h * h);
    }

    public int calculateTargetDailyCalories() {
        float weight = getWeightKg();
        float height = getHeightCm();
        int age = getAge();
        String gender = getGender();

        // BMR Mifflin-St Jeor
        double bmr;
        if ("Nữ".equalsIgnoreCase(gender)) {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        }

        // Target adjustment
        String goal = getGoal();
        if ("fatloss".equalsIgnoreCase(goal)) {
            return (int) Math.round(bmr * 1.35 - 400);
        } else if ("muscle".equalsIgnoreCase(goal)) {
            return (int) Math.round(bmr * 1.45 + 300);
        } else {
            return (int) Math.round(bmr * 1.35);
        }
    }

    public int calculateEstimatedWeeks() {
        float current = getWeightKg();
        float target = getTargetWeightKg();
        float diff = Math.abs(current - target);
        if (diff < 0.5f) return 2;
        // ~0.5 kg per week safe rate
        return Math.max(2, (int) Math.ceil(diff / 0.5f));
    }
}
