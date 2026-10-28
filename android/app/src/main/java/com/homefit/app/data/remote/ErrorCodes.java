package com.homefit.app.data.remote;

/** Mã lỗi thống nhất, khớp docs/API.md, cộng thêm lỗi phía client. */
public final class ErrorCodes {
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    /** Chỉ phát sinh ở client: mất mạng, hết thời gian chờ. */
    public static final String NETWORK_ERROR = "NETWORK_ERROR";
    public static final String UNKNOWN = "UNKNOWN";

    private ErrorCodes() {
    }
}
