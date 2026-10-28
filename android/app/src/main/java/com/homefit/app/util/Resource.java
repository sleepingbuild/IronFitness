package com.homefit.app.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Kết quả bọc trạng thái cho LiveData: LOADING, SUCCESS hoặc ERROR.
 * Khi ERROR, {@code code} là mã lỗi (xem ErrorCodes); dùng ErrorMessages.toText để ra chuỗi hiển thị.
 */
public final class Resource<T> {

    public enum Status { LOADING, SUCCESS, ERROR }

    @NonNull
    public final Status status;
    @Nullable
    public final T data;
    @Nullable
    public final String code;
    @Nullable
    public final String message;

    private Resource(@NonNull Status status, @Nullable T data,
                     @Nullable String code, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null, null);
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null, null);
    }

    public static <T> Resource<T> error(@NonNull String code, @Nullable String message) {
        return new Resource<>(Status.ERROR, null, code, message);
    }

    /** Lỗi nhưng vẫn còn dữ liệu local để hiển thị. */
    public static <T> Resource<T> error(@NonNull String code, @Nullable String message, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, code, message);
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
