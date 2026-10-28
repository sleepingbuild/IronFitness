package com.homefit.app.data.remote.dto;

import java.util.List;

public class BatchSessionsRequest {
    public final List<WorkoutSessionRequest> sessions;

    public BatchSessionsRequest(List<WorkoutSessionRequest> sessions) {
        this.sessions = sessions;
    }
}
