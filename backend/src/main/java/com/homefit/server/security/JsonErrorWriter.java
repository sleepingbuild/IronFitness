package com.homefit.server.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

/** Ghi lỗi JSON {"code","message"} từ tầng bộ lọc bảo mật (ngoài controller). */
public final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    public static void write(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"code\":\"" + escape(code) + "\",\"message\":\"" + escape(message) + "\"}");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
