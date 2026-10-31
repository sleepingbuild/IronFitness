package com.homefit.server.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Lỗi thống nhất theo docs/API.md: { "code", "message", "details" }. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, Map<String, String> details) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }
}
