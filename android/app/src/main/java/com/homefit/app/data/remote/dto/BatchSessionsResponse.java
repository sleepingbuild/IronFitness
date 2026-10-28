package com.homefit.app.data.remote.dto;

import java.util.List;

public class BatchSessionsResponse {
    public List<String> syncedUuids;
    public List<Failed> failed;

    public static class Failed {
        public String clientUuid;
        public String reason;
    }
}
