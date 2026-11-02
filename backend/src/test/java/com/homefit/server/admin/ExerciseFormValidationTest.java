package com.homefit.server.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.homefit.server.domain.MediaType;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class ExerciseFormValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static ExerciseForm valid() {
        ExerciseForm f = new ExerciseForm();
        f.setCategoryId(1L);
        f.setName("Plank");
        f.setDescription("Giữ thân người thẳng.");
        f.setMediaUrl("https://example.com/plank.gif");
        f.setMediaType(MediaType.GIF);
        f.setMode(ExerciseForm.Mode.DURATION);
        f.setAmount(30);
        f.setRestSec(15);
        f.setSortOrder(1);
        return f;
    }

    @Test
    void validFormHasNoViolations() {
        assertTrue(VALIDATOR.validate(valid()).isEmpty());
    }

    @Test
    void categoryAndNameAreRequired() {
        ExerciseForm f = valid();
        f.setCategoryId(null);
        f.setName(" ");
        assertEquals(2, VALIDATOR.validate(f).size());
    }

    @Test
    void mediaUrlMustBeHttpAndNotBlank() {
        ExerciseForm f = valid();
        f.setMediaUrl("");
        assertEquals(1, VALIDATOR.validate(f).size());
        f.setMediaUrl("plank.gif");
        assertEquals(1, VALIDATOR.validate(f).size());
        f.setMediaUrl("ftp://example.com/plank.gif");
        assertEquals(1, VALIDATOR.validate(f).size());
    }

    @Test
    void amountAndRestMustBeInRange() {
        ExerciseForm f = valid();
        f.setAmount(0);
        assertEquals(1, VALIDATOR.validate(f).size());
        f.setAmount(3601);
        assertEquals(1, VALIDATOR.validate(f).size());
        f = valid();
        f.setRestSec(-1);
        assertEquals(1, VALIDATOR.validate(f).size());
        f.setRestSec(601);
        assertEquals(1, VALIDATOR.validate(f).size());
    }

    @Test
    void repsHaveTighterLimitThanSeconds() {
        ExerciseForm f = valid();
        f.setMode(ExerciseForm.Mode.REPS);
        f.setAmount(1001);
        assertTrue(VALIDATOR.validate(f).isEmpty()); // chú thích cho phép tới 3600
        assertEquals("Số lần tối đa 1000", f.amountProblem());
        f.setAmount(15);
        assertNull(f.amountProblem());
        f.setMode(ExerciseForm.Mode.DURATION);
        f.setAmount(1001);
        assertNull(f.amountProblem());
    }

    @Test
    void missingModeOrAmountIsRejected() {
        ExerciseForm f = valid();
        f.setMode(null);
        f.setAmount(null);
        assertEquals(2, VALIDATOR.validate(f).size());
        assertNull(f.amountProblem());
    }
}
