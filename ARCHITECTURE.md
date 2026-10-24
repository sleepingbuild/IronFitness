# ARCHITECTURE – HomeFit

## 1. Tổng quan

```
┌────────────────────┐        HTTPS/JSON (JWT)        ┌─────────────────────────────┐
│   Android App      │ ─────────────────────────────▶ │  Spring Boot Backend        │
│  Java + MVVM       │ ◀───────────────────────────── │  /api/v1  (REST)            │
│  Room (local DB)   │                                │  /admin   (Thymeleaf)       │
└────────────────────┘                                │  Security + JWT + JPA       │
                                                      └──────────────┬──────────────┘
┌────────────────────┐        Browser (session)                      │ JDBC
│  Admin (trình      │ ─────────────────────────────▶ /admin         ▼
│  duyệt)            │                                         ┌───────────┐
└────────────────────┘                                         │  MySQL 8  │
                                                               └───────────┘
Media GIF/Video: lưu URL trong DB, file host ngoài (Cloudinary/CDN/GitHub). Backend KHÔNG lưu file.
```

Nguyên tắc: **Một backend duy nhất** phục vụ cả app (REST/JSON) và admin (Thymeleaf), dùng chung service và repository.

## 2. Android App

### 2.1 Mẫu kiến trúc: MVVM + Repository

```
UI (Activity/Fragment, XML)
   │ observe LiveData
ViewModel
   │
Repository  ──▶ Room (DAO)        nguồn dữ liệu local, là "source of truth" cho UI
            ──▶ Retrofit (API)    đồng bộ với server
```

- UI chỉ nói chuyện với ViewModel, ViewModel chỉ nói chuyện với Repository.
- Repository quyết định lấy từ Room hay API.
- Dùng `LiveData`, `ViewBinding`, `Navigation Component`, một Activity + nhiều Fragment.

### 2.2 Cấu trúc package

```
com.homefit.app
├── data
│   ├── local          # Room: AppDatabase, entity, dao
│   ├── remote         # Retrofit: ApiService, dto, AuthInterceptor
│   ├── repository     # AuthRepository, ExerciseRepository, WorkoutLogRepository
│   └── prefs          # TokenStore (EncryptedSharedPreferences), Settings
├── domain             # model thuần Java (nếu cần), mapper
├── ui
│   ├── auth           # Splash, Login, Register
│   ├── profile        # Profile, BMI
│   ├── library        # Category list, Exercise list
│   ├── player         # WorkoutPlayerFragment + ViewModel + timer
│   ├── history        # Calendar, Stats
│   └── settings       # Nhắc tập
├── notification       # ReminderScheduler, ReminderReceiver
├── util               # BmiCalculator, DateUtils, Resource<T>
└── di                 # (tùy chọn) khởi tạo thủ công singleton hoặc Hilt
```

### 2.3 Chiến lược dữ liệu: offline-first

| Dữ liệu | Nguồn gốc | Cách xử lý |
|---|---|---|
| Danh mục, bài tập | Server | App tải và lưu vào Room, UI đọc từ Room. Làm mới khi mở app (so `updatedAt`). Mất mạng vẫn xem được bài đã cache |
| Lịch sử buổi tập | Tạo ở app | Ghi vào Room trước (`synced=false`), worker đẩy lên server khi có mạng, sau đó `synced=true`. Dùng `clientUuid` để server chống trùng |
| Hồ sơ (chiều cao, cân nặng) | App + server | Lưu Room, đồng bộ qua `PUT /me/profile` |
| Token JWT | Server | Lưu `EncryptedSharedPreferences`, không lưu trong Room |
| Giờ nhắc tập | Chỉ local | Lưu SharedPreferences, không cần server |

> GIF/Video chỉ cache bởi Glide/ExoPlayer (cache HTTP), không lưu file vào Room.

### 2.4 Workout Player

State machine trong `WorkoutPlayerViewModel`:

```
IDLE → EXERCISE(i) ⇄ PAUSED
          │ hết giờ / Skip
          ▼
        REST → EXERCISE(i+1) ... → FINISHED (ghi log vào Room)
```

- Đếm ngược bằng `CountDownTimer` (hoặc `Handler`), giữ `remainingMs` trong ViewModel để xoay màn hình không mất.
- Bài tính theo **reps** (không có `durationSec`): hiển thị số reps và nút "Xong" thay cho đếm ngược.
- Âm thanh: `SoundPool` cho tiếng bíp ngắn khi chuyển bài và hết giờ.
- Giữ màn hình sáng trong lúc tập (`FLAG_KEEP_SCREEN_ON`).

### 2.5 Nhắc tập hằng ngày

- `AlarmManager` (inexact, hoặc `WorkManager` periodic) tạo notification hằng ngày; đăng ký lại sau khi khởi động máy (`BOOT_COMPLETED`).
- Android 13+ cần xin quyền `POST_NOTIFICATIONS`.
- Tránh dùng exact alarm trừ khi thật sự cần (cần quyền riêng từ Android 12+).

### 2.6 Xác thực trên app

- Email/mật khẩu: `POST /auth/register`, `POST /auth/login` → nhận `accessToken` (và `refreshToken`).
- Google: lấy **ID token** từ Google Sign-In/Credential Manager, gửi `POST /auth/google`, backend xác thực token với Google.
- `AuthInterceptor` gắn `Authorization: Bearer <token>`; gặp 401 thì thử refresh một lần, lỗi thì quay về Login.

## 3. Backend (Spring Boot)

### 3.1 Cấu trúc package

```
com.homefit.server
├── config        # SecurityConfig, JwtProperties, WebMvcConfig
├── security      # JwtService, JwtAuthFilter, UserDetailsServiceImpl
├── domain        # Entity JPA: User, Category, Exercise, WorkoutSession
├── repository    # Spring Data JPA
├── service       # AuthService, ExerciseService, StatsService...
├── api           # @RestController /api/v1/** + dto + GlobalExceptionHandler
├── admin         # @Controller /admin/** (Thymeleaf)
└── util
resources
├── db/migration  # Flyway: V1__init.sql, V2__seed.sql ...
├── templates/admin
├── static/admin
└── application.yml, application-example.yml
```

### 3.2 Bảo mật

- Hai chuỗi `SecurityFilterChain`:
  - `/api/**`: stateless, JWT. `/api/v1/auth/**` công khai, còn lại cần token. Endpoint ghi nội dung bài tập chỉ role `ADMIN`.
  - `/admin/**`: form login + session, chỉ role `ADMIN`, bật CSRF.
- Mật khẩu băm bằng BCrypt. JWT secret đọc từ biến môi trường/config, không commit.
- Đăng nhập Google: xác thực ID token bằng `google-api-client`, tạo hoặc liên kết user theo email.
- Mọi dữ liệu người dùng (`/me/**`) lọc theo `userId` lấy từ token, không nhận `userId` từ client.

### 3.3 Quy ước

- DTO tách khỏi Entity, không trả Entity ra API.
- Lỗi trả JSON thống nhất: `{ "code": "...", "message": "..." }` (xem docs/API.md).
- Thay đổi schema **chỉ qua Flyway**, không dùng `ddl-auto=update`.
- Xóa mềm cho `exercises` và `categories` (`is_active`) để không làm hỏng lịch sử.

## 4. Admin Web

- Thymeleaf + Bootstrap (CDN hoặc file tĩnh), không SPA.
- Trang: Dashboard (tổng user, tổng lượt tập), Danh mục (CRUD), Bài tập (CRUD, lọc theo danh mục), Người dùng (xem, bật/tắt Premium, tùy chọn).
- Form bài tập: tên, mô tả, danh mục, `mediaUrl`, `mediaType` (GIF/VIDEO), `durationSec` hoặc `reps`, `restSec`, thứ tự, bật/tắt.
- Có nút xem trước media từ URL.

## 5. Quyết định kiến trúc (ADR rút gọn)

| # | Quyết định | Lý do |
|---|---|---|
| 1 | Một backend Spring Boot cho cả app và admin | Cùng ngôn ngữ Java, ít thành phần phải vận hành |
| 2 | MySQL + Flyway | Yêu cầu dự án, migration có kiểm soát |
| 3 | Room làm source of truth ở app | Xem bài tập và ghi lịch sử khi offline |
| 4 | Media lưu bằng URL | Không cần lưu trữ file, đơn giản và rẻ |
| 5 | JWT cho app, session cho admin | Phù hợp từng loại client |
| 6 | Bỏ đăng nhập SĐT khỏi MVP | OTP SMS tốn phí và phức tạp |
| 7 | `clientUuid` cho buổi tập | Đồng bộ lại nhiều lần không bị trùng |

## 6. Rủi ro và lưu ý

- **Hosting MySQL miễn phí** thường giới hạn hoặc không ổn định. Giai đoạn đầu chạy local/LAN; chốt chỗ deploy ở tuần 3–4 (kiểm tra gói miễn phí tại thời điểm đó).
- Android emulator gọi máy host qua `10.0.2.2`; máy thật dùng IP LAN, và nếu dùng HTTP thường (dev) cần cấu hình `networkSecurityConfig` cho phép cleartext **chỉ ở build debug**.
- Link media của bên thứ ba có thể chết: admin nên có nút kiểm tra link.
