package com.homefit.app.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.homefit.app.data.remote.dto.ApiErrorDto;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback Retrofit gom mọi lỗi về (code, message). Retrofit trên Android gọi lại ở main thread.
 * Việc ghi Room trong onSuccess phải chuyển sang AppExecutors.diskIO().
 * An toàn tuyệt đối với ngoại lệ mạng và dữ liệu rỗng.
 */
public abstract class ApiCallback<T> implements Callback<T> {

    private static final String TAG = "ApiCallback";
    private static final Gson GSON = new Gson();

    protected abstract void onSuccess(@NonNull T body);

    protected abstract void onError(@NonNull String code, @Nullable String message);

    @Override
    public final void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        try {
            T body = response.body();
            if (response.isSuccessful() && body != null) {
                onSuccess(body);
                return;
            }
            ApiErrorDto error = parseError(response);
            onError(error.code != null ? error.code : ErrorCodes.UNKNOWN, error.message);
        } catch (Throwable t) {
            Log.e(TAG, "Lỗi ngoài ý muốn trong onResponse", t);
            try {
                onError(ErrorCodes.UNKNOWN, t.getMessage());
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public final void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        try {
            String msg = t != null ? t.getMessage() : "Mất kết nối mạng";
            onError(ErrorCodes.NETWORK_ERROR, msg);
        } catch (Throwable e) {
            Log.e(TAG, "Lỗi ngoài ý muốn trong onFailure", e);
        }
    }

    @NonNull
    static ApiErrorDto parseError(@NonNull Response<?> response) {
        ApiErrorDto dto = null;
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                dto = GSON.fromJson(errorBody.string(), ApiErrorDto.class);
            }
        } catch (Exception ignored) {
            // Thân lỗi không phải JSON: dùng mã HTTP bên dưới.
        }
        if (dto == null) {
            dto = new ApiErrorDto();
        }
        if (dto.code == null) {
            dto.code = codeForHttp(response.code());
        }
        return dto;
    }

    @NonNull
    private static String codeForHttp(int http) {
        switch (http) {
            case 400:
                return ErrorCodes.VALIDATION_ERROR;
            case 401:
                return ErrorCodes.UNAUTHORIZED;
            case 403:
                return ErrorCodes.FORBIDDEN;
            case 404:
                return ErrorCodes.NOT_FOUND;
            case 409:
                return ErrorCodes.CONFLICT;
            default:
                return http >= 500 ? ErrorCodes.INTERNAL_ERROR : ErrorCodes.UNKNOWN;
        }
    }
}
