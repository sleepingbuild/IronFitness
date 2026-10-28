package com.homefit.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BmiCalculatorTest {

    private static final double DELTA = 0.001;

    @Test
    public void underweight() {
        double bmi = BmiCalculator.calculate(175, 50);
        assertEquals(16.3, bmi, DELTA);
        assertEquals(BmiCalculator.Category.UNDERWEIGHT, BmiCalculator.classify(bmi));
    }

    @Test
    public void normal() {
        double bmi = BmiCalculator.calculate(170, 65);
        assertEquals(22.5, bmi, DELTA);
        assertEquals(BmiCalculator.Category.NORMAL, BmiCalculator.classify(bmi));
    }

    @Test
    public void overweight() {
        double bmi = BmiCalculator.calculate(170, 80);
        assertEquals(27.7, bmi, DELTA);
        assertEquals(BmiCalculator.Category.OVERWEIGHT, BmiCalculator.classify(bmi));
    }

    @Test
    public void obese() {
        double bmi = BmiCalculator.calculate(170, 100);
        assertEquals(34.6, bmi, DELTA);
        assertEquals(BmiCalculator.Category.OBESE, BmiCalculator.classify(bmi));
    }

    @Test
    public void boundaryValues() {
        // 74 / 2^2 = 18.5 exactly: thuộc nhóm bình thường
        assertEquals(BmiCalculator.Category.NORMAL,
                BmiCalculator.classify(BmiCalculator.calculate(200, 74)));
        // 100 / 2^2 = 25.0 exactly: bắt đầu thừa cân
        assertEquals(BmiCalculator.Category.OVERWEIGHT,
                BmiCalculator.classify(BmiCalculator.calculate(200, 100)));
        // 120 / 2^2 = 30.0 exactly: bắt đầu béo phì
        assertEquals(BmiCalculator.Category.OBESE,
                BmiCalculator.classify(BmiCalculator.calculate(200, 120)));
    }

    @Test
    public void rangeLimitsAreAccepted() {
        assertTrue(BmiCalculator.isValidHeight(50));
        assertTrue(BmiCalculator.isValidHeight(250));
        assertTrue(BmiCalculator.isValidWeight(20));
        assertTrue(BmiCalculator.isValidWeight(300));
    }

    @Test
    public void outOfRangeIsInvalid() {
        assertFalse(BmiCalculator.isValidHeight(49.9));
        assertFalse(BmiCalculator.isValidHeight(250.1));
        assertFalse(BmiCalculator.isValidHeight(0));
        assertFalse(BmiCalculator.isValidHeight(-170));
        assertFalse(BmiCalculator.isValidHeight(Double.NaN));
        assertFalse(BmiCalculator.isValidWeight(19.9));
        assertFalse(BmiCalculator.isValidWeight(300.1));
        assertFalse(BmiCalculator.isValidWeight(-5));
        assertFalse(BmiCalculator.isValidWeight(Double.NaN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroHeightThrows() {
        BmiCalculator.calculate(0, 65);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeWeightThrows() {
        BmiCalculator.calculate(170, -65);
    }
}
