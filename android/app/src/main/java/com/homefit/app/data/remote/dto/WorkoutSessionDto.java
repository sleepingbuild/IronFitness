package com.homefit.app.data.remote.dto;

/** Một phần tử của GET /workout-sessions. */
public class WorkoutSessionDto {
    public long id;
    public String clientUuid;
    public Long categoryId;
    public String startedAt;
    public String completedAt;
    public int totalSeconds;
    public int exercisesDone;
}
