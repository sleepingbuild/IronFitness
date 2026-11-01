package com.homefit.app.ui.auth;

import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.homefit.app.R;

/**
 * Kiểm tra dữ liệu nhập ở màn Đăng nhập/Đăng ký (khớp quy tắc ở docs/API.md mục 1).
 * Mỗi hàm trả id chuỗi lỗi, hoặc 0 nếu hợp lệ.
 */
public final class AuthValidator {

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_NAME_LENGTH = 100;

    private AuthValidator() {
    }

    @StringRes
    public static int emailError(@NonNull String email) {
        if (email.isEmpty()) {
            return R.string.auth_error_email_required;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return R.string.auth_error_email_invalid;
        }
        return 0;
    }

    /** Đăng nhập chỉ cần có mật khẩu; độ dài do server kiểm tra. */
    @StringRes
    public static int loginPasswordError(@NonNull String password) {
        return password.isEmpty() ? R.string.auth_error_password_required : 0;
    }

    @StringRes
    public static int registerPasswordError(@NonNull String password) {
        if (password.isEmpty()) {
            return R.string.auth_error_password_required;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return R.string.auth_error_password_short;
        }
        return 0;
    }

    @StringRes
    public static int nameError(@NonNull String name) {
        if (name.isEmpty()) {
            return R.string.auth_error_name_required;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return R.string.auth_error_name_long;
        }
        return 0;
    }

    @StringRes
    public static int confirmError(@NonNull String password, @NonNull String confirm) {
        return password.equals(confirm) ? 0 : R.string.auth_error_confirm_mismatch;
    }
}
