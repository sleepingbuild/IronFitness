package com.homefit.server.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.homefit.server.domain.Category;

import java.time.Instant;

/** Phần tử của GET /categories (docs/API.md). */
public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String iconUrl,
        int sortOrder,
        @JsonProperty("isActive") boolean active,
        Instant updatedAt) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getIconUrl(),
                c.getSortOrder(), c.isActive(), c.getUpdatedAt());
    }
}
