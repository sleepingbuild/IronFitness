package com.homefit.server.api.dto;

import java.util.List;

/** syncedUuids: đã ghi nhận (gồm cả bản đã có từ trước). failed: bản ghi bị từ chối kèm lý do. */
public record BatchSessionsResponse(List<String> syncedUuids, List<Failure> failed) {

    public record Failure(String clientUuid, String reason) {
    }
}
