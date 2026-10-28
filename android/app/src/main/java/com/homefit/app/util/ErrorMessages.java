package com.homefit.app.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.homefit.app.R;
import com.homefit.app.data.remote.ErrorCodes;

/** Đổi mã lỗi thành chuỗi tiếng Việt từ strings_errors.xml. */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    @NonNull
    public static String toText(@NonNull Context context, @NonNull Resource<?> resource) {
        return toText(context, resource.code, resource.message);
    }

    /**
     * Ưu tiên thông điệp server (đã là tiếng Việt, ví dụ lỗi validate) trừ khi là lỗi mạng.
     */
    @NonNull
    public static String toText(@NonNull Context context, @Nullable String code, @Nullable String serverMessage) {
        if (!ErrorCodes.NETWORK_ERROR.equals(code) && serverMessage != null && !serverMessage.trim().isEmpty()) {
            return serverMessage;
        }
        if (code == null) {
            return context.getString(R.string.error_unknown);
        }
        switch (code) {
            case ErrorCodes.NETWORK_ERROR:
                return context.getString(R.string.error_network);
            case ErrorCodes.UNAUTHORIZED:
                return context.getString(R.string.error_unauthorized);
            case ErrorCodes.FORBIDDEN:
                return context.getString(R.string.error_forbidden);
            case ErrorCodes.NOT_FOUND:
                return context.getString(R.string.error_not_found);
            case ErrorCodes.CONFLICT:
                return context.getString(R.string.error_conflict);
            case ErrorCodes.VALIDATION_ERROR:
                return context.getString(R.string.error_validation);
            case ErrorCodes.INTERNAL_ERROR:
                return context.getString(R.string.error_server);
            default:
                return context.getString(R.string.error_unknown);
        }
    }
}
