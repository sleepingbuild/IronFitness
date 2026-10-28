package com.homefit.app.data.remote.dto;

import java.util.Map;

/** Lỗi thống nhất: { "code": "...", "message": "...", "details": {...} }. */
public class ApiErrorDto {
    public String code;
    public String message;
    public Map<String, String> details;
}
