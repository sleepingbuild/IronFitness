package com.homefit.app.data.remote.dto;

/** Trả về bởi /auth/register, /auth/login, /auth/google. */
public class AuthResponse {
    public String accessToken;
    public String refreshToken;
    public long expiresIn;
    public UserDto user;
}
