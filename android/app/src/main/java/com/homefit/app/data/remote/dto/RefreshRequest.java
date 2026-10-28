package com.homefit.app.data.remote.dto;

public class RefreshRequest {
    public final String refreshToken;

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
