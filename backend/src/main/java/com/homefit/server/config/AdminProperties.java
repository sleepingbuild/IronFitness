package com.homefit.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Tài khoản admin đầu tiên, đọc từ homefit.admin.* (không lưu mật khẩu trong migration). */
@ConfigurationProperties(prefix = "homefit.admin")
public record AdminProperties(String email, String password) {
}
