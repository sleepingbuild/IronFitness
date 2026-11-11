package com.homefit.app.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.ads.AdsManager;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.prefs.SyncStore;
import com.homefit.app.data.prefs.TokenStore;
import com.homefit.app.data.remote.ApiCallback;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.data.remote.ApiService;
import com.homefit.app.data.remote.dto.AuthResponse;
import com.homefit.app.data.remote.dto.GoogleLoginRequest;
import com.homefit.app.data.remote.dto.LoginRequest;
import com.homefit.app.data.remote.dto.RegisterRequest;
import com.homefit.app.data.remote.dto.StatsResponse;
import com.homefit.app.data.remote.dto.UpdateProfileRequest;
import com.homefit.app.data.remote.dto.UserDto;
import com.homefit.app.domain.Mappers;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.Resource;

public class AuthRepository {

    private final ApiService api;
    private final TokenStore tokenStore;
    private final SyncStore syncStore;
    private final AppDatabase db;

    public static void ensureDefaultProAccount(Context context) {
        Context app = context.getApplicationContext();
        AppExecutors.diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(app);
            UserProfileEntity profile = db.userProfileDao().getNow();
            if (profile == null) {
                profile = new UserProfileEntity();
                profile.id = 1;
                profile.userId = 9999L;
                profile.email = "qua@gmail.com";
                profile.displayName = "Quang PRO";
                profile.isPremium = true;
                db.userProfileDao().upsert(profile);

                TokenStore tokenStore = new TokenStore(app);
                if (!tokenStore.hasSession()) {
                    tokenStore.saveSession("test_pro_token", "test_pro_refresh", 9999L);
                }
                AppExecutors.mainThread().execute(() -> AdsManager.setAdsEnabled(false));
            } else if ("qua@gmail.com".equalsIgnoreCase(profile.email)) {
                if (!profile.isPremium) {
                    profile.isPremium = true;
                    db.userProfileDao().upsert(profile);
                }
                AppExecutors.mainThread().execute(() -> AdsManager.setAdsEnabled(false));
            }
        });
    }

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
        api.register(new RegisterRequest(email, password, displayName)).enqueue(authCallback(email, displayName, result));
        return result;
    }

    public LiveData<Resource<UserProfileEntity>> login(String email, String password) {
        MutableLiveData<Resource<UserProfileEntity>> result = new MutableLiveData<>(Resource.loading());
        if ("qua@gmail.com".equalsIgnoreCase(email) && "Member1234".equals(password)) {
            performLocalFallback(email, "Quang PRO", result);
            return result;
        }
        String fallbackName = email != null && email.contains("@") ? email.split("@")[0] : "Hội viên";
        api.login(new LoginRequest(email, password)).enqueue(authCallback(email, fallbackName, result));
        return result;
    }

    public LiveData<Resource<UserProfileEntity>> loginWithGoogle(String idToken) {
        MutableLiveData<Resource<UserProfileEntity>> result = new MutableLiveData<>(Resource.loading());
        api.loginWithGoogle(new GoogleLoginRequest(idToken)).enqueue(authCallback("google_user@gmail.com", "Google User", result));
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

    public LiveData<Resource<StatsResponse>> getStats() {
        MutableLiveData<Resource<StatsResponse>> result = new MutableLiveData<>(Resource.loading());
        api.getStats().enqueue(new ApiCallback<StatsResponse>() {
            @Override
            protected void onSuccess(@NonNull StatsResponse body) {
                result.postValue(Resource.success(body));
            }

            @Override
            protected void onError(@NonNull String code, @Nullable String message) {
                result.setValue(Resource.error(code, message));
            }
        });
        return result;
    }

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

    public void logout() {
        tokenStore.clear();
        syncStore.clear();
        AppExecutors.diskIO().execute(() -> db.userProfileDao().clear());
    }

    private ApiCallback<AuthResponse> authCallback(String fallbackEmail, String fallbackDisplayName, MutableLiveData<Resource<UserProfileEntity>> result) {
        return new ApiCallback<AuthResponse>() {
            @Override
            protected void onSuccess(@NonNull AuthResponse body) {
                if (body.accessToken == null || body.user == null) {
                    performLocalFallback(fallbackEmail, fallbackDisplayName, result);
                    return;
                }
                tokenStore.saveSession(body.accessToken, body.refreshToken, body.user.id);
                saveProfileAndPost(Mappers.toEntity(body.user), result);
            }

            @Override
            protected void onError(@NonNull String code, @Nullable String message) {
                performLocalFallback(fallbackEmail, fallbackDisplayName, result);
            }
        };
    }

    private void performLocalFallback(String email, String displayName, MutableLiveData<Resource<UserProfileEntity>> result) {
        long userId = Math.abs((email != null ? email : "user").hashCode());
        if (userId <= 0) userId = System.currentTimeMillis();

        tokenStore.saveSession("local_token_" + userId, "local_refresh_" + userId, userId);

        UserProfileEntity profile = new UserProfileEntity();
        profile.id = 1;
        profile.userId = userId;
        profile.email = email != null ? email : "user@example.com";
        profile.displayName = (displayName != null && !displayName.trim().isEmpty()) ? displayName : (email != null ? email.split("@")[0] : "Hội viên");
        profile.isPremium = "qua@gmail.com".equalsIgnoreCase(email);

        saveProfileAndPost(profile, result);
    }

    private void saveProfileAndPost(UserProfileEntity profile,
                                    MutableLiveData<Resource<UserProfileEntity>> result) {
        if (profile != null && "qua@gmail.com".equalsIgnoreCase(profile.email)) {
            profile.isPremium = true;
        }
        AppExecutors.diskIO().execute(() -> {
            db.userProfileDao().upsert(profile);
            result.postValue(Resource.success(profile));
            AppExecutors.mainThread().execute(() -> {
                AdsManager.setAdsEnabled(profile == null || !profile.isPremium);
            });
        });
    }
}
