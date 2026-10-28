package com.homefit.app.data.remote.dto;

public class GoogleLoginRequest {
    public final String idToken;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }
}
