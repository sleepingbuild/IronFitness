package com.homefit.app.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.prefs.SyncStore;
import com.homefit.app.data.prefs.TokenStore;
import com.homefit.app.data.remote.ApiCallback;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.data.remote.ApiService;
import com.homefit.app.data.remote.ErrorCodes;
import com.homefit.app.data.remote.dto.AuthResponse;
import com.homefit.app.data.remote.dto.GoogleLoginRequest;
import com.homefit.app.data.remote.dto.LoginRequest;
import com.homefit.app.data.remote.dto.RegisterRequest;
import com.homefit.app.data.remote.dto.UpdateProfileRequest;
import com.homefit.app.data.remote.dto.UserDto;
import com.homefit.app.domain.Mappers;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.Resource;

/**
 * Đăng ký, đăng nhập, hồ sơ. ViewModel gọi lớp này; Fragment không gọi trực tiếp Retrofit/Room.
 * Trả về LiveData<Resource<...>>: LOADING rồi SUCCESS hoặc ERROR.
 */
public class AuthRepository {

    private final ApiService api;
    private final TokenStore tokenStore;
    private final SyncStore syncStore;
    private final AppDatabase db;

    public AuthRepository(@NonNull Context context) {
        Context app = context.getApplicationContext();
        this.api = ApiClient.get(app);
        this.tokenStore = new TokenStore(app);
        this.syncStore = new SyncStore(app);
        this.db = AppDatabase.getInstance(app);
    }

    public boolean isLoggedIn() {
        return tokenStore.hasSession();
    }

    /** Hồ sơ hiện tại trong Room (null nếu chưa có). */
    public LiveData<UserProfileEntity> getProfile() {
        return db.userProfileDao().get();
    }

    public LiveData<Resource<UserProfileEntity>> register(String email, String password, String displayName) {
        MutableLiveData<Resource<UserProfileEntity>> result = new MutableLiveData<>(Resource.loading());
        api.register(new RegisterRequest(email, password, displayName)).enqueue(authCallback(result));
        return result;
    }

    public LiveData<Resource<UserProfileEntity>> login(String email, String password) {
        MutableLiveData<Resource<UserProfileEntity>> result = new MutableLiveData<>(Resource.loading());
        api.login(new LoginRequest(email, password)).enqueue(authCallback(result));
        return result;
    }

    public LiveData<Resource<UserProfileEntity>> loginWithGoogle(String idToken) {
        MutableLiveData<Resource<UserProfileEntity>> result = new MutableLiveData<>(Resource.loading());
        api.loginWithGoogle(new GoogleLoginRequest(idToken)).enqueue(authCallback(result));
        return result;
    }

    /** Làm mới hồ sơ từ server (GET /me). */
    public LiveData<Resource<UserProfileEntity>> refreshProfile() {
        MutableLiveData<Resource<UserProfileEntity>> result = new MutableLiveData<>(Resource.loading());
        api.getMe().enqueue(new ApiCallback<UserDto>() {
            @Override
            protected void onSuccess(@NonNull UserDto body) {
                saveProfileAndPost(Mappers.toEntity(body), result);
            }

            @Override
            protected void onError(@NonNull String code, @Nullable String message) {
                result.setValue(Resource.error(code, message));
            }
        });
        return result;
    }

    /**
     * Lưu vào Room trước (offline-first) rồi gọi PUT /me/profile.
     * Nếu server lỗi (ví dụ mất mạng), kết quả là ERROR kèm dữ liệu đã lưu local.
     */
    public LiveData<Resource<UserProfileEntity>> updateProfile(
            @NonNull String displayName, @Nullable Double heightCm, @Nullable Double weightKg) {
        MutableLiveData<Resource<UserProfileEntity>> result = new MutableLiveData<>(Resource.loading());
        AppExecutors.diskIO().execute(() -> {
            UserProfileEntity local = db.userProfileDao().getNow();
            if (local == null) {
                local = new UserProfileEntity();
                local.userId = tokenStore.getUserId();
            }
            local.displayName = displayName;
            local.heightCm = heightCm;
            local.weightKg = weightKg;
            db.userProfileDao().upsert(local);
            final UserProfileEntity saved = local;

            api.updateProfile(new UpdateProfileRequest(displayName, heightCm, weightKg))
                    .enqueue(new ApiCallback<UserDto>() {
                        @Override
                        protected void onSuccess(@NonNull UserDto body) {
                            saveProfileAndPost(Mappers.toEntity(body), result);
                        }

                        @Override
                        protected void onError(@NonNull String code, @Nullable String message) {
                            result.setValue(Resource.error(code, message, saved));
                        }
                    });
        });
        return result;
    }

    /**
     * Đăng xuất: xóa token và hồ sơ, mốc đồng bộ. Lịch sử tập (workout_logs) được giữ lại
     * vì đã gắn userId và không bao giờ hiển thị cho tài khoản khác.
     */
    public void logout() {
        tokenStore.clear();
        syncStore.clear();
        AppExecutors.diskIO().execute(() -> db.userProfileDao().clear());
    }

    private ApiCallback<AuthResponse> authCallback(MutableLiveData<Resource<UserProfileEntity>> result) {
        return new ApiCallback<AuthResponse>() {
            @Override
            protected void onSuccess(@NonNull AuthResponse body) {
                if (body.accessToken == null || body.user == null) {
                    result.setValue(Resource.error(ErrorCodes.UNKNOWN, null));
                    return;
                }
                tokenStore.saveSession(body.accessToken, body.refreshToken, body.user.id);
                saveProfileAndPost(Mappers.toEntity(body.user), result);
            }

            @Override
            protected void onError(@NonNull String code, @Nullable String message) {
                result.setValue(Resource.error(code, message));
            }
        };
    }

    private void saveProfileAndPost(UserProfileEntity profile,
                                    MutableLiveData<Resource<UserProfileEntity>> result) {
        AppExecutors.diskIO().execute(() -> {
            db.userProfileDao().upsert(profile);
            result.postValue(Resource.success(profile));
        });
    }
}
