package com.homefit.app.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Lưu JWT và id người dùng. KHÔNG lưu trong Room.
 * Hiện dùng SharedPreferences riêng (MODE_PRIVATE), chưa mã hóa vì chưa thêm thư viện security-crypto;
 * nâng cấp lên mã hóa (Android Keystore) là việc của Agent 2 nếu còn thời gian (xem ARCHITECTURE.md 2.3).
 */
public class TokenStore {

    private static final String FILE = "homefit_auth";
    private static final String KEY_ACCESS = "accessToken";
    private static final String KEY_REFRESH = "refreshToken";
    private static final String KEY_USER_ID = "userId";

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, @Nullable String refreshToken, long userId) {
        prefs.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
                .putLong(KEY_USER_ID, userId)
                .apply();
    }

    public void saveTokens(String accessToken, @Nullable String refreshToken) {
        prefs.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
                .apply();
    }

    @Nullable
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    @Nullable
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    /** Trả về -1 nếu chưa đăng nhập. */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public boolean hasSession() {
        return getAccessToken() != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
