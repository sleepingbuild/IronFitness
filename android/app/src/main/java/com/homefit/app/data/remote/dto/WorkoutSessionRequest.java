package com.homefit.app.data.remote.dto;

/** Gửi lên POST /workout-sessions và trong batch. Thời gian ISO-8601 UTC. */
public class WorkoutSessionRequest {
    public String clientUuid;
    public Long categoryId;
    public String startedAt;
    public String completedAt;
    public int totalSeconds;
    public int exercisesDone;
}
