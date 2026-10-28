package com.homefit.app.data.remote.dto;

public class RegisterRequest {
    public final String email;
    public final String password;
    public final String displayName;

    public RegisterRequest(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }
}
