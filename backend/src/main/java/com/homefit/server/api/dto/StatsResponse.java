package com.homefit.server.api.dto;

public record StatsResponse(
        int totalSessions,
        long totalDurationSec,
        int currentStreakDays,
        int sessionsLast7Days,
        int sessionsLast30Days) {
}
