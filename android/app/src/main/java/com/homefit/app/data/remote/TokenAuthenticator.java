package com.homefit.app.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.homefit.app.data.prefs.TokenStore;
import com.homefit.app.data.remote.dto.RefreshRequest;
import com.homefit.app.data.remote.dto.RefreshResponse;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Gặp 401 thì thử refresh token đúng một lần rồi gửi lại request.
 * Refresh thất bại (401/403) thì xóa phiên: nơi gọi thấy UNAUTHORIZED phải quay về màn Đăng nhập.
 */
public class TokenAuthenticator implements Authenticator {

    private final TokenStore tokenStore;
    /** ApiService KHÔNG có authenticator, tránh gọi đệ quy. */
    private final ApiService refreshApi;
    private final Object lock = new Object();

    public TokenAuthenticator(TokenStore tokenStore, ApiService refreshApi) {
        this.tokenStore = tokenStore;
        this.refreshApi = refreshApi;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        String sentHeader = response.request().header("Authorization");
        if (sentHeader == null || responseCount(response) >= 2) {
            return null; // request không cần token, hoặc đã thử lại rồi
        }
        synchronized (lock) {
            String sentToken = sentHeader.replace("Bearer ", "");
            String current = tokenStore.getAccessToken();
            if (current != null && !current.equals(sentToken)) {
                return withToken(response, current); // luồng khác đã refresh xong
            }
            String refreshToken = tokenStore.getRefreshToken();
            if (refreshToken == null) {
                return null;
            }
            retrofit2.Response<RefreshResponse> refreshed =
                    refreshApi.refresh(new RefreshRequest(refreshToken)).execute();
            RefreshResponse body = refreshed.body();
            if (refreshed.isSuccessful() && body != null && body.accessToken != null) {
                tokenStore.saveTokens(body.accessToken,
                        body.refreshToken != null ? body.refreshToken : refreshToken);
                return withToken(response, body.accessToken);
            }
            if (refreshed.code() == 401 || refreshed.code() == 403) {
                tokenStore.clear();
            }
            return null;
        }
    }

    private static Request withToken(Response response, String token) {
        return response.request().newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
    }

    private static int responseCount(Response response) {
        int count = 1;
        Response prior = response.priorResponse();
        while (prior != null) {
            count++;
            prior = prior.priorResponse();
        }
        return count;
    }
}
