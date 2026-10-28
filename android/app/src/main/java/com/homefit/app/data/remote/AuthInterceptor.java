package com.homefit.app.data.remote;

import androidx.annotation.NonNull;

import com.homefit.app.data.prefs.TokenStore;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Gắn "Authorization: Bearer ..." cho mọi request trừ nhóm /auth/*. */
public class AuthInterceptor implements Interceptor {

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String token = tokenStore.getAccessToken();
        if (token == null || request.url().encodedPath().contains("/auth/")) {
            return chain.proceed(request);
        }
        return chain.proceed(request.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build());
    }
}
