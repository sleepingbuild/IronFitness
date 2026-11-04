package com.homefit.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class StatsResponse {
    @SerializedName("totalSessions")
    public int totalSessions;

    @SerializedName("totalDurationSec")
    public long totalDurationSec;

    @SerializedName("currentStreakDays")
    public int currentStreakDays;

    @SerializedName("sessionsLast7Days")
    public int sessionsLast7Days;

    @SerializedName("sessionsLast30Days")
    public int sessionsLast30Days;
}
