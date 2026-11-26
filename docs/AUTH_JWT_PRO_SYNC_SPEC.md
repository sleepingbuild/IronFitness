# ĐẶC TẢ VÀ HƯỚNG DẪN NGHIỆP VỤ: XÁC THỰC JWT, PHÂN QUYỀN PRO & ĐỒNG BỘ HỒ SƠ 2 CHIỀU

**Nhiệm vụ**: Xác thực JWT, Phân quyền PRO & Đồng bộ Hồ sơ 2 chiều  
**Người phụ trách**: Thành viên 1 (Tech Lead)  
**Phạm vi thành phần chính**: `AuthRepository`, `TokenStore`, `ApiClient`, `AdsManager`, `TokenAuthenticator`, `AuthInterceptor`, `UserProfileDao`, `UserProfileEntity`.

---

## 1. DANH SÁCH TOÀN BỘ FILE THUỘC PHẠM VI NHIỆM VỤ

| STT | File / Đường dẫn | Vai trò & Trách nhiệm |
|---|---|---|
| **1** | `android/app/src/main/java/com/homefit/app/data/prefs/TokenStore.java` | Quản lý lưu trữ `accessToken`, `refreshToken`, `userId` trong SharedPreferences (`MODE_PRIVATE`). |
| **2** | `android/app/src/main/java/com/homefit/app/data/remote/AuthInterceptor.java` | OkHttp Interceptor tự động thêm Header `Authorization: Bearer <token>` vào mọi Request (bỏ qua nhóm `/auth/*`). |
| **3** | `android/app/src/main/java/com/homefit/app/data/remote/TokenAuthenticator.java` | OkHttp Authenticator tự động bắt mã HTTP `401 Unauthorized`, gọi API `/auth/refresh` và gửi lại request lỗi không để gián đoạn người dùng. |
| **4** | `android/app/src/main/java/com/homefit/app/data/remote/ApiClient.java` | Khởi tạo cấu hình Retrofit và OkHttpClient phân tách giữa `refreshApi` (Plain client) và `authedApi` để tránh đệ quy vô tận. |
| **5** | `android/app/src/main/java/com/homefit/app/data/remote/ApiService.java` | Khai báo các REST Endpoints: `/auth/login`, `/auth/register`, `/auth/refresh`, `/me`, `/me/profile`, `/me/upgrade-pro`. |
| **6** | `android/app/src/main/java/com/homefit/app/data/local/entity/UserProfileEntity.java` | Entity Room lưu trữ hồ sơ người dùng hiện tại ở local (luôn cố định `id = 1`, lưu `userId`, `email`, `displayName`, `heightCm`, `weightKg`, `isPremium`, `proExpiryDate`). |
| **7** | `android/app/src/main/java/com/homefit/app/data/local/dao/UserProfileDao.java` | Interface DAO truy vấn (`get()`, `getNow()`), cập nhật (`upsert()`) và xóa (`clear()`) hồ sơ người dùng trong Room. |
| **8** | `android/app/src/main/java/com/homefit/app/domain/Mappers.java` | Lớp ánh xạ DTO từ mạng (`UserDto`) thành Entity Room (`UserProfileEntity`), đồng thời tính toán quyền PRO/Premium và hạn dùng. |
| **9** | `android/app/src/main/java/com/homefit/app/ads/AdsManager.java` | Lớp quản lý quảng cáo AdMob tập trung, điều khiển việc bật/tắt quảng cáo (`setAdsEnabled(!isPremium)`). |
| **10** | `android/app/src/main/java/com/homefit/app/data/repository/AuthRepository.java` | **Trung tâm điều phối nghiệp vụ**: Đăng nhập, đăng ký, refresh profile, nâng cấp PRO, cập nhật hồ sơ 2 chiều và đăng xuất. |
| **11** | *Các DTO Remote* (`data/remote/dto/`) | `AuthResponse.java`, `UserDto.java`, `LoginRequest.java`, `RegisterRequest.java`, `RefreshRequest.java`, `RefreshResponse.java`, `UpdateProfileRequest.java`. |
| **12** | *Các ViewModel/UI liên quan* (`ui/auth/`, `ui/profile/`) | `SplashViewModel`, `LoginViewModel`, `RegisterViewModel`, `ProfileFragment` (Trigger gọi Repository và lắng nghe LiveData). |

---

## 2. LUỒNG HOẠT ĐỘNG NGHIỆP VỤ (BUSINESS WORKFLOWS)

```
                       ┌──────────────────────────────────────────────────────────┐
                       │                     NGƯỜI DÙNG                           │
                       └────────────────────────────┬─────────────────────────────┘
                                                    │
                 ┌──────────────────────────────────┼──────────────────────────────────┐
                 ▼                                  ▼                                  ▼
      [LUỒNG 1: XÁC THỰC JWT]            [LUỒNG 2: PHÂN QUYỀN PRO]          [LUỒNG 3: ĐỒNG BỘ 2 CHIỀU]
┌──────────────────────────────────┐ ┌──────────────────────────────────┐ ┌──────────────────────────────────┐
│ 1. Đăng nhập / Đăng ký           │ │ 1. Nhấn "Nâng cấp PRO" / Nạp tiền│ │ 1. Sửa Chiều cao / Cân nặng / Tên│
│ 2. Server trả accessToken &      │ │ 2. Repository gọi API            │ │ 2. Repository ghi trước vào Room │
│    refreshToken                  │ │    `POST /me/upgrade-pro`         │ │    (Optimistic UI update)        │
│ 3. TokenStore lưu session        │ │ 3. Server xác nhận, trả UserDto  │ │ 3. Đồng thời gọi REST API        │
│ 4. Mappers lưu UserProfileEntity │ │    với `isPremium = true`         │ │    `PUT /me/profile`             │
│    vào Room DB                   │ │ 4. Room DB cập nhật `isPremium`  │ │ 4. Thành công: Room lưu dữ liệu  │
│ 5. AuthInterceptor đính token vào│ │ 5. Callback báo AdsManager:      │ │    chuẩn từ Server               │
│    mọi Request về sau            │ │    `setAdsEnabled(false)`        │ │ 5. Thất bại: Giữ dữ liệu local,  │
│ 6. Hết hạn Token (HTTP 401) →    │ │ 6. Toàn bộ Banner & Interstitial │ │    báo lỗi người dùng qua UI     │
│    TokenAuthenticator tự refresh │ │    Ad Mob tự động ẩn hoàn toàn   │ │                                  │
└──────────────────────────────────┘ └──────────────────────────────────┘ └──────────────────────────────────┘
```

### Luồng 1: Xác thực JWT & Tự động Gia hạn Token (Auto Refresh JWT)
1. **Khởi tạo phiên**: Khi đăng nhập/đăng ký thành công, server trả về `accessToken` (ngắn hạn) và `refreshToken` (dài hạn). `AuthRepository` lưu các token này vào `TokenStore` (SharedPreferences).
2. **Gắn Header tự động**: Với mọi HTTP request gửi đi, `AuthInterceptor` kiểm tra đường dẫn (nếu không phải `/auth/`) thì tự động đính kèm `Authorization: Bearer <accessToken>`.
3. **Xử lý Hết hạn (HTTP 401)**:
   - Khi `accessToken` hết hạn, Server trả về lỗi HTTP 401.
   - `TokenAuthenticator` chặn response 401 này. Sử dụng `synchronized` khóa luồng để tránh việc nhiều request cùng lúc đi refresh token.
   - Gọi đồng bộ `refreshApi.refresh(...)` với `refreshToken`.
   - Nếu cấp lại token thành công: Lưu token mới vào `TokenStore` và **gửi lại ngay request bị lỗi trước đó** kèm token mới. Người dùng không hề cảm nhận ứng dụng bị ngắt quãng.
   - Nếu Refresh Token cũng hết hạn (HTTP 401/403): Gọi `tokenStore.clear()`. Ứng dụng điều hướng người dùng về lại màn hình Đăng nhập.

---

### Luồng 2: Phân quyền PRO & Kiểm soát Quảng cáo (PRO & Ad Control)
1. **Kiểm tra trạng thái PRO**: Khi nhận thông tin người dùng (`UserDto`), `Mappers.toEntity(userDto)` chuyển đổi thành `UserProfileEntity`, đồng thời kiểm tra cờ `isPremium` hoặc `isPro`.
2. **Lưu trữ Local**: Cập nhật bản ghi `UserProfileEntity` (cố định `id = 1`) vào Room Database.
3. **Tắt Quảng cáo**:
   - Khi `saveProfileAndPost(...)` được thực thi, ứng dụng chạy lệnh trên Main Thread:
     ```java
     AdsManager.setAdsEnabled(profile == null || !profile.isPremium);
     ```
   - Nếu `isPremium == true`: `AdsManager` đặt cờ `adsEnabled = false`, giải phóng biến `interstitial = null`.
   - Khi đó, mọi lời gọi hiển thị Quảng cáo Banner (`attachBanner`) hoặc Interstitial Ad (`showInterstitialIfReady`) sẽ tự động bỏ qua, mang lại trải nghiệm không quảng cáo cho tài khoản PRO.

---

### Luồng 3: Đồng bộ Hồ sơ 2 chiều (2-Way Profile Synchronization)
1. **Chiều Local → Server (Cập nhật Hồ sơ với Optimistic UI)**:
   - Khi người dùng thay đổi chiều cao, cân nặng hoặc tên trong màn hình Profile, `AuthRepository.updateProfile(...)` được gọi.
   - **Bước 1 (Local First)**: Lập tức cập nhật `UserProfileEntity` trong Room DB trước trên `diskIO` thread và phát LiveData tới UI. Người dùng thấy giao diện phản hồi ngay lập tức mà không phải chờ mạng.
   - **Bước 2 (Server Sync)**: Gọi API `PUT /me/profile`.
   - **Bước 3 (Reconciliation)**: Khi API trả về dữ liệu chuẩn từ Server (`UserDto`), repository cập nhật lại Room DB lần 2 để đồng bộ chính xác hoàn toàn. Nếu có lỗi mạng, dữ liệu local tạm thời vẫn được giữ lại kèm báo lỗi UI.
2. **Chiều Server → Local (Làm mới Hồ sơ)**:
   - Khi khởi động app hoặc kéo để làm mới (Swipe to Refresh), `AuthRepository.refreshProfile()` gọi `GET /me`.
   - Lấy thông tin `UserDto` mới nhất từ Server (bao gồm trạng thái PRO vừa nạp/nâng cấp), chuyển thành `UserProfileEntity` và ghi đè vào Room DB.

---

## 3. CÁC ĐOẠN CODE CỐT LÕI (CORE CODE SNIPPETS)

### 3.1 `TokenStore.java` - Lưu trữ phiên đăng nhập
```java
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

    @Nullable
    public String getAccessToken() { return prefs.getString(KEY_ACCESS, null); }

    @Nullable
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH, null); }

    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1L); }

    public boolean hasSession() { return getAccessToken() != null; }

    public void clear() { prefs.edit().clear().apply(); }
}
```

---

### 3.2 `AuthInterceptor.java` - Đính kèm JWT Header
```java
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
        // Không gắn token nếu chưa đăng nhập hoặc là các request thuộc nhóm /auth/
        if (token == null || request.url().encodedPath().contains("/auth/")) {
            return chain.proceed(request);
        }
        return chain.proceed(request.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build());
    }
}
```

---

### 3.3 `TokenAuthenticator.java` - Xử lý Tự động Refresh Token khi gặp 401
```java
public class TokenAuthenticator implements Authenticator {
    private final TokenStore tokenStore;
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
            return null; // Tránh thử lại vô tận
        }
        synchronized (lock) {
            String sentToken = sentHeader.replace("Bearer ", "");
            String current = tokenStore.getAccessToken();
            if (current != null && !current.equals(sentToken)) {
                return withToken(response, current); // Luồng khác đã refresh thành công
            }
            String refreshToken = tokenStore.getRefreshToken();
            if (refreshToken == null) return null;

            // Gọi API refresh token đồng bộ
            retrofit2.Response<RefreshResponse> refreshed =
                    refreshApi.refresh(new RefreshRequest(refreshToken)).execute();
            RefreshResponse body = refreshed.body();
            if (refreshed.isSuccessful() && body != null && body.accessToken != null) {
                tokenStore.saveTokens(body.accessToken,
                        body.refreshToken != null ? body.refreshToken : refreshToken);
                return withToken(response, body.accessToken);
            }
            // Thất bại do hết hạn cả refresh token -> Xóa phiên
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
}
```

---

### 3.4 `ApiClient.java` - Phân tách Client tránh đệ quy
```java
public final class ApiClient {
    private static ApiService create(Context app) {
        String baseUrl = app.getString(R.string.api_base_url);
        TokenStore tokenStore = new TokenStore(app);

        OkHttpClient plain = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .build();

        // Refresh API dùng Plain Client (không đính Interceptor hay Authenticator)
        ApiService refreshApi = retrofit(baseUrl, plain).create(ApiService.class);

        // Client chính chứa AuthInterceptor và TokenAuthenticator
        OkHttpClient authed = plain.newBuilder()
                .addInterceptor(new AuthInterceptor(tokenStore))
                .authenticator(new TokenAuthenticator(tokenStore, refreshApi))
                .build();

        return retrofit(baseUrl, authed).create(ApiService.class);
    }
}
```

---

### 3.5 `AuthRepository.java` - Tích hợp Đồng bộ Hồ sơ & Tắt Quảng cáo PRO
```java
public class AuthRepository {
    // ... Khởi tạo các thành phần DB, Api, TokenStore ...

    /** Cập nhật hồ sơ 2 chiều (Optimistic UI Local + Server Sync) */
    public LiveData<Resource<UserProfileEntity>> updateProfile(
            @NonNull String displayName, @Nullable Double heightCm, @Nullable Double weightKg) {
        MutableLiveData<Resource<UserProfileEntity>> result = new MutableLiveData<>(Resource.loading());
        AppExecutors.diskIO().execute(() -> {
            // Bước 1: Cập nhật ngay vào Room DB (Local First)
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

            // Bước 2: Gọi API đồng bộ Server
            api.updateProfile(new UpdateProfileRequest(displayName, heightCm, weightKg))
                    .enqueue(new ApiCallback<UserDto>() {
                        @Override
                        protected void onSuccess(@NonNull UserDto body) {
                            saveProfileAndPost(Mappers.toEntity(body), result);
                        }

                        @Override
                        protected void onError(@NonNull String code, @Nullable String message) {
                            result.postValue(Resource.error(code, message, saved));
                        }
                    });
        });
        return result;
    }

    /** Lưu Hồ sơ vào Room DB & Đồng bộ trạng thái Quảng cáo AdMob với cờ Premium */
    private void saveProfileAndPost(UserProfileEntity profile,
                                    MutableLiveData<Resource<UserProfileEntity>> result) {
        AppExecutors.diskIO().execute(() -> {
            if (profile != null) {
                db.userProfileDao().upsert(profile);
            }
            result.postValue(Resource.success(profile));
            // Cập nhật bật/tắt Quảng cáo trên Main Thread dựa theo cờ Premium
            AppExecutors.mainThread().execute(() -> {
                AdsManager.setAdsEnabled(profile == null || !profile.isPremium);
            });
        });
    }

    /** Đăng xuất: Xóa toàn bộ Token & Hồ sơ người dùng */
    public void logout() {
        tokenStore.clear();
        syncStore.clear();
        AppExecutors.diskIO().execute(() -> db.userProfileDao().clear());
    }
}
```

---

## 4. TỔNG KẾT
Tất cả 12 file trên phối hợp chặt chẽ tạo nên một giải pháp xác thực và quản lý tài khoản hoàn chỉnh:
- **JWT & Token Refresh**: Tự động, ngầm định, bảo mật cao.
- **Phân quyền PRO**: Tự động đồng bộ cờ `isPremium` từ Server xuống Room DB và vô hiệu hóa toàn bộ AdMob.
- **Đồng bộ 2 chiều**: Đảm bảo tốc độ phản hồi tức thì trên UI (Local-first) đồng thời giữ toàn vẹn dữ liệu chuẩn trên Cloud Server.
