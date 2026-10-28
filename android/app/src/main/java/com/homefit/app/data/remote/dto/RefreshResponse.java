package com.homefit.app.data.remote.dto;

/** Trả về bởi /auth/refresh. */
public class RefreshResponse {
    public String accessToken;
    public String refreshToken;
    public long expiresIn;
}
