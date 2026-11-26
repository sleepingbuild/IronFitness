# TỔNG HỢP DỰ ÁN HOMEFIT (IRONFITNESS)

Báo cáo tổng hợp toàn bộ kiến trúc, chức năng, cơ sở dữ liệu và cấu trúc nguồn của dự án **HomeFit (IronFitness)** bao gồm cả ứng dụng **Android Client** và máy chủ **Spring Boot Backend / Trang quản trị Admin Web**.

---

## 1. TỔNG QUAN DỰ ÁN

- **Tên dự án**: HomeFit (IronFitness)
- **Mục tiêu**: Ứng dụng hỗ trợ **luyện tập thể thao tại nhà** (Home Workout) không cần thiết bị phức tạp, kết hợp ứng dụng di động Android Offline-First cho người dùng cuối và hệ thống máy chủ Quản trị nội dung bài tập, người dùng.
- **Mô hình kiến trúc tổng thể**:
  - **Client (Android)**: Xây dựng bằng Java 17, kiến trúc MVVM, Room Database (Offline-first), Retrofit REST Client, Glide (Media/GIF player), AdMob (Tích hợp quảng cáo).
  - **Backend (Spring Boot)**: Java 17+, Spring Data JPA, Spring Security + JWT, Flyway Database Migration, Thymeleaf Admin Dashboard.
  - **Database**: MySQL 8.0+ (utf8mb4).
  - **Media Storage**: Lưu đường dẫn URL ngoài (CDN/GitHub/Cloudinary), backend không lưu trữ file tĩnh nặng.

```
┌─────────────────────────┐         REST API (JSON + JWT)         ┌─────────────────────────────┐
│   Android App (Client)  │ ────────────────────────────────────► │  Spring Boot Backend        │
│   - Java 17 + MVVM      │ ◄──────────────────────────────────── │  - REST API /api/v1         │
│   - Room (Offline First)│                                       │  - Admin Web /admin         │
└─────────────────────────┘                                       │  - Spring Security + JPA    │
                                                                  └──────────────┬──────────────┘
┌─────────────────────────┐         Browser Session (CSRF)                       │ JDBC
│   Admin Web Browser     │ ────────────────────────────────────► /admin         ▼
└─────────────────────────┘                                                ┌───────────┐
                                                                           │  MySQL 8  │
                                                                           └───────────┘
```

---

## 2. CẤU TRÚC THƯ MỤC DỰ ÁN

```
IronFitness/
├── PROJECT_SUMMARY.md           # [FILE NÀY] File tổng hợp toàn bộ dự án
├── README.md                    # Hướng dẫn tổng quan dự án
├── ARCHITECTURE.md              # Thiết kế kiến trúc chi tiết
├── CONTRIBUTING.md              # Quy tắc đóng góp và làm việc Git
├── AGENT.md                     # Hướng dẫn dành cho AI Agent
├── android/                     # MODULE ỨNG DỤNG ANDROID (Java 17, MVVM, Room)
│   ├── app/
│   │   ├── build.gradle         # Cấu hình build & dependencies Android
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── assets/exercises/ # Dữ liệu bài tập Markdown khởi tạo local
│   │       ├── java/com/homefit/app/
│   │       │   ├── ads/         # Quản lý quảng cáo Google AdMob (AdsManager)
│   │       │   ├── data/        # Tầng Data: Local (Room), Remote (Retrofit), Repositories, Prefs
│   │       │   ├── domain/      # Mappers chuyển đổi DTO ↔ Entity
│   │       │   ├── notification/# Nhắc nhở tập luyện (AlarmManager, BootReceiver)
│   │       │   ├── ui/          # Tầng Giao diện (Auth, Discover, Player, History, Profile, Custom)
│   │       │   └── util/        # Utility helpers (BmiCalculator, DateUtils, Resource, ...)
│   │       └── res/             # Layouts XML, Resources, Values, NavGraph
├── backend/                     # MODULE MÁY CHỦ SPRING BOOT (Java 17, REST API & Admin Web)
│   ├── pom.xml                  # Cấu hình Maven dependencies
│   ├── Dockerfile               # Cấu hình đóng gói Docker container
│   └── src/main/
│       ├── java/com/homefit/server/
│       │   ├── admin/           # Controllers & Forms cho trang Quản trị (Thymeleaf)
│       │   ├── api/             # REST Controllers (/api/v1) & Global Exception Handler
│       │   ├── config/          # Spring Security, WebMvc, JWT configuration
│       │   ├── domain/          # Entities JPA (User, Exercise, Category, WorkoutSession, ...)
│       │   ├── repository/      # Spring Data JPA Repositories
│       │   ├── security/        # Service xử lý Token JWT, UserDetailsService
│       │   └── service/         # Business Logic Services
│       └── resources/
│           ├── application.yml  # Cấu hình môi trường
│           ├── db/migration/    # Flyway SQL migrations (V1 đến V15)
│           └── templates/admin/ # Giao diện HTML Thymeleaf cho trang Admin
├── docs/                        # THƯ MỤC TÀI LIỆU KỸ THUẬT
│   ├── API.md                   # Quy ước và chi tiết Hợp đồng REST API v1
│   ├── DATABASE.md              # Schema CSDL MySQL & Room Database
│   ├── BACKEND.md               # Hướng dẫn vận hành & troubleshooting backend
│   ├── ADS.md                   # Thiết kế & Kịch bản tích hợp AdMob
│   ├── SETUP.md                 # Hướng dẫn cài đặt môi trường phát triển
│   ├── DEPLOY.md                # Hướng dẫn triển khai Cloud (Render & Aiven)
│   └── seed/                    # Dữ liệu CSV mẫu cho hệ thống
└── scripts/
    └── perf-api.ps1             # Script PowerShell kiểm thử hiệu năng API
```

---

## 3. CHI TIẾT MODULE ANDROID CLIENT (`android/`)

### 3.1 Nền tảng Công nghệ
- **Ngôn ngữ**: Java 17
- **Giao diện**: Android XML Views với `ViewBinding` & `Navigation Component` (Single Activity Architecture: `MainActivity`).
- **Mô hình kiến trúc**: **MVVM** (Model-View-ViewModel) + **Repository Pattern**.
- **Quản lý trạng thái**: `LiveData` & `Observer` pattern.
- **Cơ sở dữ liệu cục bộ**: `Room Database` (Offline-First local source of truth).
- **Mạng REST Client**: `Retrofit 2` + `Gson` + `OkHttp 4` logging interceptor.
- **Tải ảnh & media**: `Glide 4` (hỗ trợ hiển thị ảnh GIF động bài tập).
- **Quảng cáo**: Google Play Services Ads (`AdMob`).

### 3.2 Các Tính năng & Màn hình Chính
1. **Đăng nhập / Đăng ký & Xác thực (`ui/auth`)**:
   - `SplashFragment`: Kiểm tra trạng thái đăng nhập, điều hướng khởi động.
   - `LoginFragment` & `RegisterFragment`: Đăng nhập/Đăng ký qua Email + Password. Tích hợp validation đầu vào (`AuthValidator`).
   - Quản lý phiên làm việc bằng JWT AccessToken & RefreshToken lưu trong SharedPreferences (`TokenStore`).
2. **Khám phá & Thư viện bài tập (`ui/discover`)**:
   - Hiển thị danh mục theo nhóm cơ: **Bụng, Ngực, Chân, Tay, Vai & Lưng, Toàn thân**.
   - Xem danh sách bài tập kèm theo mức độ (Dễ, Trung bình, Khó).
   - Màn hình Chi tiết bài tập: Mô tả động tác, số hiệp/thời gian tập, ảnh GIF minh họa.
3. **Trình phát Luyện tập - Workout Player (`ui/player`)**:
   - Quản lý trạng thái bài tập: `IDLE` → `EXERCISE` ⇄ `PAUSED` → `REST` → `FINISHED`.
   - Hỗ trợ cả 2 dạng bài tập: Theo **thời gian đếm ngược** (Duration in Seconds) và theo **số lần lặp** (Reps).
   - Đếm ngược thời gian nghỉ (`restSec`) giữa các bài tập.
   - Giữ màn hình luôn sáng trong lúc tập (`FLAG_KEEP_SCREEN_ON`).
   - Phát âm thanh hiệu ứng bằng `SoundPool` khi chuyển bài và hoàn thành.
4. **Tùy chỉnh Bài tập cá nhân (`ui/custom`)**:
   - Cho phép người dùng tự tạo lộ trình tập luyện riêng (`CustomWorkout`), thêm/bớt bài tập (`CustomWorkoutItem`).
5. **Lịch sử & Thống kê (`ui/history`)**:
   - Giao diện Calendar dạng lưới (`CalendarAdapter`, `CalendarDayCell`) hiển thị các ngày đã hoàn thành buổi tập.
   - Thống kê tổng số buổi tập, tổng số phút luyện tập, số ngày tập liên tục (Streak).
6. **Hồ sơ Cá nhân & Chỉ số BMI (`ui/profile`)**:
   - Cập nhật chiều cao, cân nặng.
   - Tự động tính chỉ số **BMI** (`BmiCalculator`) và hiển thị phân loại (Thiếu cân, Bình thường, Thừa cân, Béo phì).
   - Hiển thị trạng thái tài khoản (Thường / Premium).
7. **Nhắc nhở Tập luyện (`notification`)**:
   - Đặt lịch thông báo nhắc tập hàng ngày qua `ReminderScheduler` & `AlarmManager`.
   - Tự động đăng ký lại lịch báo thức khi thiết bị khởi động lại (`BootReceiver`).
8. **Tích hợp Quảng cáo AdMob (`ads`)**:
   - Lớp quản lý tập trung `AdsManager`: Cấu hình Banner Ads & Interstitial Ads.
   - **Banner Ad**: Hiển thị ở cuối màn hình Danh mục, Bài tập, Thống kê.
   - **Interstitial Ad**: Tự động hiển thị sau khi hoàn thành buổi tập (với giới hạn giãn cách tối thiểu 3 phút/lần).
   - **Tắt quảng cáo**: Người dùng có cờ `isPremium = true` sẽ tự động không bị làm phiền bởi bất kỳ quảng cáo nào.

---

## 4. CHI TIẾT MODULE SPRING BOOT BACKEND (`backend/`)

### 4.1 Nền tảng Công nghệ
- **Ngôn ngữ**: Java 17+
- **Framework**: Spring Boot 3.x / 4.x
- **Bảo mật**: Spring Security (Dual Filter Chain: REST JWT stateless & Admin Web Session/CSRF).
- **ORM / Database**: Spring Data JPA (Hibernate), MySQL 8.
- **Migration Tool**: Flyway Migration.
- **Admin Template Engine**: Thymeleaf + Bootstrap HTML5.

### 4.2 Cấu trúc Cơ sở dữ liệu (Database Schema)

Hệ thống CSDL bao gồm 15 Flyway Migrations (`V1__init.sql` đến `V15__expand_media_type_enum.sql`):

| Tên Bảng | Mô Tả | Cột Chính |
|---|---|---|
| `users` | Thông tin tài khoản người dùng | `id`, `email`, `password_hash`, `google_id`, `display_name`, `role`, `is_premium`, `height_cm`, `weight_kg` |
| `categories` | Danh mục nhóm cơ | `id`, `name`, `slug`, `icon_url`, `sort_order`, `is_active`, `updated_at` |
| `exercises` | Thư viện bài tập | `id`, `category_id`, `name`, `description`, `media_url`, `media_type` (GIF/VIDEO), `duration_sec`, `reps`, `rest_sec`, `is_active` |
| `workouts` | Lộ trình tập gói gọn | `id`, `name`, `description`, `category_id`, `difficulty_level`, `cover_image_url`, `is_active` |
| `workout_items` | Chi tiết các bài tập trong lộ trình | `id`, `workout_id`, `exercise_id`, `sort_order`, `duration_sec`, `reps`, `rest_sec` |
| `workout_sessions` | Lịch sử các buổi tập đã hoàn thành | `id`, `user_id`, `client_uuid` (UNIQUE), `category_id`, `started_at`, `completed_at`, `total_seconds`, `exercises_done` |
| `refresh_tokens` | Token làm mới phiên JWT | `id`, `user_id`, `token_hash`, `expires_at`, `revoked` |
| `challenge_programs` | Chương trình thử thách (30 ngày) | `id`, `title`, `description`, `banner_url`, `total_days` |
| `challenge_sessions` | Tiến độ thử thách của người dùng | `id`, `user_id`, `program_id`, `day_number`, `completed_at` |
| `discover_items` | Các mục nổi bật ở màn Trang chủ | `id`, `title`, `subtitle`, `image_url`, `target_type`, `target_id`, `sort_order` |
| `home_sections` | Bố cục các phần trên trang chủ | `id`, `title`, `section_type`, `sort_order` |
| `app_configs` | Cấu hình động ứng dụng | `config_key`, `config_value`, `description` |
| `audit_logs` | Nhật ký hoạt động của Admin | `id`, `admin_username`, `action`, `target_type`, `target_id`, `created_at` |

### 4.3 REST API Endpoints (`/api/v1`)

Mọi phản hồi lỗi REST API tuân thủ định dạng chuẩn JSON: `{ "code": "...", "message": "..." }`.

1. **Nhóm Xác thực (`/api/v1/auth`)**:
   - `POST /auth/register`: Đăng ký tài khoản mới.
   - `POST /auth/login`: Đăng nhập bằng Email/Password, trả về `accessToken` & `refreshToken`.
   - `POST /auth/refresh`: Làm mới Access Token bằng Refresh Token (Token Rotation).
   - `POST /auth/google`: Đăng nhập/Đăng ký nhanh qua Google ID Token.
2. **Nhóm Hồ sơ Cá nhân (`/api/v1/me`)**:
   - `GET /me`: Lấy thông tin người dùng hiện tại từ Token JWT.
   - `PUT /me/profile`: Cập nhật `displayName`, `heightCm`, `weightKg`.
3. **Nhóm Thư viện & Nội dung (`/api/v1`)**:
   - `GET /categories`: Lấy danh sách nhóm cơ (Hỗ trợ `updatedAfter` cho Delta Sync).
   - `GET /exercises`: Lấy danh sách bài tập (Hỗ trợ lọc theo `categoryId` và `updatedAfter`).
   - `GET /discover`: Lấy dữ liệu các mục Banner/Khám phá cho trang chủ.
   - `GET /content-sync`: Tải đồng bộ toàn bộ schema nội dung bài tập.
4. **Nhóm Lịch sử Buổi tập (`/api/v1/workout-sessions`)**:
   - `POST /workout-sessions`: Đẩy 1 buổi tập hoàn thành lên server (Idempotent theo `clientUuid`).
   - `POST /workout-sessions/batch`: Đồng bộ lô nhiều buổi tập khi có mạng trở lại.
   - `GET /workout-sessions`: Lấy lịch sử tập luyện của người dùng trong khoảng thời gian `from` → `to`.
5. **Nhóm Thử thách (`/api/v1/challenges`)**:
   - `GET /challenges`: Lấy danh sách các chương trình thử thách 30 ngày.
   - `GET /challenges/{id}`: Xem chi tiết lộ trình thử thách.

### 4.4 Trang Quản trị Web Admin (`/admin`)
- **Đăng nhập & Bảo mật**: Form-based authentication, quản lý phiên qua HTTP Session, bảo vệ chống giả mạo request với CSRF Token.
- **Trang Dashboard (`/admin`)**: Hiển thị tổng quan số lượng người dùng, tổng số buổi tập đã hoàn thành, lượt tập trong ngày.
- **Quản lý Danh mục (`/admin/categories`)**: Thêm, sửa, ẩn/hiện danh mục nhóm cơ.
- **Quản lý Bài tập (`/admin/exercises`)**: CRUD bài tập, tải/gắn URL ảnh GIF minh họa, thiết lập số lần/thời gian nghỉ.
- **Quản lý Workouts & Challenges (`/admin/workouts`, `/admin/challenges`)**: Tùy chỉnh danh sách bài tập theo từng lộ trình.
- **Quản lý Người dùng (`/admin/users`)**: Xem danh sách người dùng, cập nhật trạng thái tài khoản, bật/tắt quyền **Premium**.
- **Quản lý Cấu hình & Nhật ký (`/admin/app-configs`, `/admin/audit-logs`)**: Theo dõi lịch sử thao tác quản trị và thay đổi tham số hệ thống.

---

## 5. CHIẾN LƯỢC DỮ LIỆU VÀ ĐỒNG BỘ (OFFLINE-FIRST)

1. **Source of Truth tại Client**:
   - Ứng dụng lưu toàn bộ Danh mục, Bài tập và Lịch sử tập vào CSDL `Room` trên điện thoại. Khi không có mạng, người dùng vẫn có thể xem danh mục, đọc bài tập và thực hiện tập luyện bình thường.
2. **Đồng bộ Lịch sử (Push Sync)**:
   - Khi hoàn thành buổi tập offline, ứng dụng sinh một chuỗi `clientUuid` (UUID v4) và lưu vào `WorkoutLogEntity` với cờ `synced = false`.
   - Khi thiết bị nối mạng, dịch vụ đồng bộ gọi `POST /workout-sessions/batch`.
   - Máy chủ kiểm tra ràng buộc `UNIQUE(user_id, client_uuid)` để ngăn chặn trùng lặp dữ liệu.
   - Nhận phản hồi thành công (`syncedUuids`), app cập nhật cờ `synced = true` trong Room.
3. **Đồng bộ Nội dung (Pull Sync / Delta Sync)**:
   - Client lưu lại thời điểm đồng bộ gần nhất `lastSyncAt`.
   - Khi mở app có mạng, app gọi `/categories?updatedAfter=...` và `/exercises?updatedAfter=...`.
   - Backend trả về các bản ghi mới hoặc thay đổi kể từ mốc đó (kể cả các bản ghi bị ẩn `isActive = false` để app xóa khỏi Room).

---

## 6. QUY TRÌNH DỰNG VÀ KIỂM THỬ (BUILD & TESTING)

### 6.1 Chạy Backend Local
1. Tạo CSDL MySQL: `CREATE DATABASE homefit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. Tạo cấu hình `backend/src/main/resources/application-local.yml` (chứa thông tin DB username/password và `jwt.secret`).
3. Chạy lệnh:
   ```cmd
   cd backend
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
   ```
4. Truy cập Admin Web tại `http://localhost:8090/admin` và REST API tại `http://localhost:8090/api/v1/`.

### 6.2 Chạy Android App
1. Mở thư mục `android/` bằng **Android Studio**.
2. Kiểm tra `build.gradle` (Gradle 8.x, Java 17 compatibility).
3. Đảm bảo file `res/xml/network_security_config.xml` cho phép giao tiếp HTTP trong môi trường debug (với IP `10.0.2.2` trên Android Emulator).
4. Run app trên Android Emulator (API 26 trở lên).

### 6.3 Đo hiệu năng REST API
Thực thi script PowerShell có sẵn để kiểm thử độ trễ và khả năng chịu tải API:
```powershell
powershell -ExecutionPolicy Bypass -File scripts\perf-api.ps1 -BaseUrl http://localhost:8090 -Runs 30 -Concurrency 10
```

---

## 7. ĐÓNG GÓI VÀ TRIỂN KHAI CLOUD (DEPLOYMENT)

- **Đóng gói Docker Container**:
  Thư mục `backend/Dockerfile` sử dụng cơ chế Multi-Stage build tối ưu:
  - Stage 1: Compile source code với JDK 25 Alpine.
  - Stage 2: Chạy ứng dụng với JRE 25 Alpine và giới hạn heap memory `-Xmx256m`.
- **Nền tảng Hosting Đề xuất (Free Tier)**:
  - **Backend**: Triển khai Web Service trên **Render.com** (Tự động trigger build khi push code vào nhánh `main`).
  - **Database**: Sử dụng **Aiven MySQL Cloud** (5GB Storage, hỗ trợ SSL).

---
*Báo cáo được tổng hợp tự động từ cấu trúc nguồn dữ liệu HomeFit.*
