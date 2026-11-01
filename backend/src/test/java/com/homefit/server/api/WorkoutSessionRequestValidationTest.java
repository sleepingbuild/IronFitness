package com.homefit.server.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.homefit.server.api.dto.WorkoutSessionRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;

class WorkoutSessionRequestValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final Instant START = Instant.parse("2026-10-24T08:00:00Z");
    private static final Instant END = Instant.parse("2026-10-24T08:20:00Z");
    private static final String UUID_OK = "7c1e0c52-3f3b-4c0e-9d6e-1b2a3c4d5e6f";

    private static Set<ConstraintViolation<WorkoutSessionRequest>> validate(
            String uuid, Integer seconds, Integer done) {
        return VALIDATOR.validate(new WorkoutSessionRequest(uuid, 1L, START, END, seconds, done));
    }

    @Test
    void validRequestHasNoViolations() {
        assertTrue(validate(UUID_OK, 1200, 8).isEmpty());
    }

    @Test
    void categoryIdMayBeNull() {
        assertTrue(VALIDATOR.validate(
                new WorkoutSessionRequest(UUID_OK, null, START, END, 1200, 8)).isEmpty());
    }

    @Test
    void invalidUuidIsRejected() {
        assertEquals(1, validate("not-a-uuid", 1200, 8).size());
    }

    @Test
    void negativeValuesAreRejected() {
        assertEquals(1, validate(UUID_OK, -1, 8).size());
        assertEquals(1, validate(UUID_OK, 1200, -1).size());
    }

    @Test
    void valuesOverLimitAreRejected() {
        assertEquals(1, validate(UUID_OK, 86401, 8).size());
        assertEquals(1, validate(UUID_OK, 1200, 1001).size());
    }

    @Test
    void missingFieldsAreRejected() {
        assertEquals(2, VALIDATOR.validate(
                new WorkoutSessionRequest(UUID_OK, null, START, END, null, null)).size());
        assertEquals(2, VALIDATOR.validate(
                new WorkoutSessionRequest(UUID_OK, null, null, null, 1200, 8)).size());
    }
}
