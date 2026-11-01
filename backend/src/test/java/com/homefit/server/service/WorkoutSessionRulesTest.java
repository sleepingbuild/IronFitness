package com.homefit.server.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.homefit.server.api.ApiException;
import com.homefit.server.api.dto.WorkoutSessionRequest;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class WorkoutSessionRulesTest {

    private static final Instant NOW = Instant.parse("2026-10-31T12:00:00Z");
    private static final String UUID_OK = "7c1e0c52-3f3b-4c0e-9d6e-1b2a3c4d5e6f";

    private static WorkoutSessionRequest request(String started, String completed) {
        return new WorkoutSessionRequest(UUID_OK, 1L, Instant.parse(started), Instant.parse(completed), 600, 5);
    }

    @Test
    void normalSessionIsAccepted() {
        assertDoesNotThrow(() -> WorkoutSessionService.checkBusinessRules(
                request("2026-10-31T11:00:00Z", "2026-10-31T11:20:00Z"), NOW));
    }

    @Test
    void completedBeforeStartedIsRejected() {
        ApiException e = assertThrows(ApiException.class, () -> WorkoutSessionService.checkBusinessRules(
                request("2026-10-31T11:20:00Z", "2026-10-31T11:00:00Z"), NOW));
        assertEquals("VALIDATION_ERROR", e.getCode());
    }

    @Test
    void farFutureIsRejectedButSmallClockSkewIsAllowed() {
        assertThrows(ApiException.class, () -> WorkoutSessionService.checkBusinessRules(
                request("2026-12-01T10:00:00Z", "2026-12-01T10:20:00Z"), NOW));
        assertDoesNotThrow(() -> WorkoutSessionService.checkBusinessRules(
                request("2026-10-31T12:30:00Z", "2026-10-31T12:50:00Z"), NOW));
    }

    @Test
    void uuidIsNormalizedToLowerCase() {
        assertEquals("7c1e0c52-3f3b-4c0e-9d6e-1b2a3c4d5e6f",
                WorkoutSessionService.normalizeUuid(" 7C1E0C52-3F3B-4C0E-9D6E-1B2A3C4D5E6F "));
    }
}
