package com.homefit.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Cấu hình JWT, đọc từ homefit.jwt.* trong application-local.yml (không commit). */
@ConfigurationProperties(prefix = "homefit.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("60") int accessTokenMinutes,
        @DefaultValue("30") int refreshTokenDays) {
}
