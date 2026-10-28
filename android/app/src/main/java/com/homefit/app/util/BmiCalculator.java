package com.homefit.app.util;

import androidx.annotation.NonNull;

/**
 * BMI = cân nặng (kg) / (chiều cao (m))^2, làm tròn 1 chữ số thập phân.
 * Phân loại theo ngưỡng WHO: dưới 18,5 gầy; 18,5 đến dưới 25 bình thường;
 * 25 đến dưới 30 thừa cân; từ 30 béo phì.
 * Phạm vi hợp lệ khớp docs/API.md: chiều cao 50 đến 250 cm, cân nặng 20 đến 300 kg.
 */
public final class BmiCalculator {

    public static final double MIN_HEIGHT_CM = 50.0;
    public static final double MAX_HEIGHT_CM = 250.0;
    public static final double MIN_WEIGHT_KG = 20.0;
    public static final double MAX_WEIGHT_KG = 300.0;

    public enum Category { UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE }

    private BmiCalculator() {
    }

    public static boolean isValidHeight(double heightCm) {
        return !Double.isNaN(heightCm) && heightCm >= MIN_HEIGHT_CM && heightCm <= MAX_HEIGHT_CM;
    }

    public static boolean isValidWeight(double weightKg) {
        return !Double.isNaN(weightKg) && weightKg >= MIN_WEIGHT_KG && weightKg <= MAX_WEIGHT_KG;
    }

    /**
     * @throws IllegalArgumentException nếu chiều cao hoặc cân nặng nằm ngoài phạm vi hợp lệ
     */
    public static double calculate(double heightCm, double weightKg) {
        if (!isValidHeight(heightCm)) {
            throw new IllegalArgumentException("heightCm ngoài phạm vi: " + heightCm);
        }
        if (!isValidWeight(weightKg)) {
            throw new IllegalArgumentException("weightKg ngoài phạm vi: " + weightKg);
        }
        double meters = heightCm / 100.0;
        double bmi = weightKg / (meters * meters);
        return Math.round(bmi * 10.0) / 10.0;
    }

    @NonNull
    public static Category classify(double bmi) {
        if (bmi < 18.5) {
            return Category.UNDERWEIGHT;
        } else if (bmi < 25.0) {
            return Category.NORMAL;
        } else if (bmi < 30.0) {
            return Category.OVERWEIGHT;
        }
        return Category.OBESE;
    }
}
