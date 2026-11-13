package com.homefit.server.api.dto;

import java.time.Instant;

public record ContentSyncResponse(
        Instant lastUpdatedAt,
        long exerciseCount,
        long workoutCount,
        long categoryCount
) {}
