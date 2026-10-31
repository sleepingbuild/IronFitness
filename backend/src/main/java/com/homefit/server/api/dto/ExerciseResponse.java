package com.homefit.server.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.MediaType;

import java.time.Instant;

/** Phần tử của GET /exercises (docs/API.md). Đúng một trong durationSec hoặc reps khác null. */
public record ExerciseResponse(
        Long id,
        Long categoryId,
        String name,
        String description,
        String mediaUrl,
        MediaType mediaType,
        Integer durationSec,
        Integer reps,
        int restSec,
        int sortOrder,
        @JsonProperty("isActive") boolean active,
        Instant updatedAt) {

    public static ExerciseResponse from(Exercise e) {
        return new ExerciseResponse(e.getId(), e.getCategoryId(), e.getName(), e.getDescription(),
                e.getMediaUrl(), e.getMediaType(), e.getDurationSec(), e.getReps(), e.getRestSec(),
                e.getSortOrder(), e.isActive(), e.getUpdatedAt());
    }
}
