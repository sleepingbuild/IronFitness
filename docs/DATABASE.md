# DATABASE – HomeFit

Nguồn sự thật cho schema. Sửa schema thì cập nhật file này **và** thêm migration Flyway mới.

## 1. MySQL (backend)

Charset: `utf8mb4`, collation `utf8mb4_unicode_ci`. Thời gian lưu UTC (`DATETIME`).

### users
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| email | VARCHAR(255) UNIQUE NOT NULL | |
| password_hash | VARCHAR(100) NULL | NULL nếu chỉ đăng nhập Google |
| google_id | VARCHAR(64) UNIQUE NULL | |
| display_name | VARCHAR(100) | |
| role | ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' | |
| is_premium | BOOLEAN NOT NULL DEFAULT FALSE | |
| height_cm | DECIMAL(5,1) NULL | |
| weight_kg | DECIMAL(5,1) NULL | |
| created_at | DATETIME NOT NULL | |
| updated_at | DATETIME NOT NULL | |

BMI **không lưu**, tính khi hiển thị: `BMI = weight_kg / (height_cm/100)²`.

### categories (nhóm cơ)
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| name | VARCHAR(100) NOT NULL | Bụng, Ngực, Chân, Toàn thân |
| slug | VARCHAR(100) UNIQUE NOT NULL | abs, chest, legs, full_body |
| icon_url | VARCHAR(500) NULL | |
| sort_order | INT NOT NULL DEFAULT 0 | |
| is_active | BOOLEAN NOT NULL DEFAULT TRUE | xóa mềm |
| updated_at | DATETIME NOT NULL | dùng cho đồng bộ |

### exercises
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| category_id | BIGINT NOT NULL FK → categories.id | INDEX |
| name | VARCHAR(150) NOT NULL | |
| description | TEXT NULL | hướng dẫn động tác |
| media_url | VARCHAR(500) NOT NULL | link GIF/Video |
| media_type | ENUM('GIF','VIDEO') NOT NULL | |
| duration_sec | INT NULL | tập theo thời gian |
| reps | INT NULL | tập theo số lần |
| rest_sec | INT NOT NULL DEFAULT 15 | nghỉ sau bài |
| sort_order | INT NOT NULL DEFAULT 0 | |
| is_active | BOOLEAN NOT NULL DEFAULT TRUE | xóa mềm |
| updated_at | DATETIME NOT NULL | |

Ràng buộc: **đúng một** trong `duration_sec` hoặc `reps` có giá trị (kiểm tra ở service và form admin).

### workout_sessions (buổi tập hoàn thành)
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| user_id | BIGINT NOT NULL FK → users.id | INDEX |
| client_uuid | CHAR(36) NOT NULL | do app sinh, chống trùng |
| category_id | BIGINT NULL FK → categories.id | |
| started_at | DATETIME NOT NULL | |
| completed_at | DATETIME NOT NULL | |
| total_seconds | INT NOT NULL | |
| exercises_done | INT NOT NULL | |
| created_at | DATETIME NOT NULL | |

UNIQUE (`user_id`, `client_uuid`). INDEX (`completed_at`) cho thống kê.

### refresh_tokens (bắt buộc, phục vụ `POST /auth/refresh`)
`id`, `user_id` FK, `token_hash` UNIQUE (lưu băm, không lưu token gốc), `expires_at`, `revoked` BOOLEAN NOT NULL DEFAULT FALSE, `created_at`.

### Làm sau (Premium)
`custom_plans`, `custom_plan_items` – lịch tập cá nhân. **Chưa tạo ở MVP.**

### Migration
- `V1__init.sql`: tạo các bảng `users`, `categories`, `exercises`, `workout_sessions`, `refresh_tokens`.
- `V2__seed_categories.sql`: 4 danh mục (Bụng, Ngực, Chân, Toàn thân).
- `V3__seed_exercises.sql`: 12–16 bài mẫu, dựa trên `docs/seed/exercises.csv` do TV5 cung cấp.
- **Tài khoản admin không nằm trong migration** (không lưu mật khẩu trong Git). Backend tạo admin đầu tiên khi khởi động từ cấu hình (`application-local.yml` hoặc biến môi trường), chỉ tạo nếu chưa có user nào có role `ADMIN`.

## 2. Room (Android)

Database: `AppDatabase` (version 1, file `homefit.db`). Entity phản ánh dữ liệu server cần cache cộng thêm dữ liệu local.

Quy ước kiểu dữ liệu: mọi trường thời gian trong Room lưu **epoch millis (UTC, kiểu `long`)**; chuỗi ISO-8601 của server được chuyển bằng `DateUtils`. Entity nằm ở `com.homefit.app.data.local.entity`, DAO ở `data.local.dao`.

### CategoryEntity
`id` (PK, = id server), `name`, `slug`, `iconUrl`, `sortOrder`, `updatedAt`. Chỉ lưu danh mục đang `isActive`.

### ExerciseEntity
`id` (PK, = id server), `categoryId` (index), `name`, `description`, `mediaUrl`, `mediaType`, `durationSec` (nullable), `reps` (nullable), `restSec`, `sortOrder`, `updatedAt`.

### WorkoutLogEntity
| Trường | Ghi chú |
|---|---|
| `localId` | PK autoGenerate |
| `userId` | id người dùng trên server (lấy từ `TokenStore`). **Mọi truy vấn lịch sử lọc theo cột này** để hai tài khoản trên cùng một máy không lẫn lịch sử. Có INDEX |
| `clientUuid` | UUID, UNIQUE index, gửi lên server |
| `categoryId` | nullable |
| `startedAt`, `completedAt` | epoch millis |
| `totalSeconds` | |
| `exercisesDone` | |
| `synced` | boolean, `false` đến khi server xác nhận |

### UserProfileEntity (1 dòng)
`id` (cố định = 1), `userId` (id trên server), `email`, `displayName`, `heightCm` (nullable), `weightKg` (nullable), `isPremium`.

### DAO chính
- `CategoryDao`: `getAll()` (LiveData), `upsertAll(list)`, `deleteByIds(ids)` (bản ghi server đã tắt), `deleteMissing(ids)` (sau đồng bộ đầy đủ).
- `ExerciseDao`: `getByCategory(categoryId)` (LiveData), `getByCategoryNow(categoryId)` (đồng bộ, cho Player), `upsertAll(list)`, `deleteByIds(ids)`, `deleteMissing(ids)`.
- `WorkoutLogDao` (đều nhận `userId`): `insert` (IGNORE khi trùng `clientUuid`), `insertAll`, `getUnsynced(userId, limit)`, `markSynced(uuids)`, `getByMonth(userId, from, to)`, `countAll(userId)`, `sumSeconds(userId)`, `getDatesWithWorkout(userId, from, to)` (trả danh sách `completedAt`, app đổi sang ngày theo múi giờ máy để đánh dấu Calendar).
- `UserProfileDao`: `get()` (LiveData), `getNow()`, `upsert`, `clear()`.

### Quy ước
- Ngày tập trên Calendar tính theo múi giờ máy (từ `completedAt`).
- Đổi schema Room phải tăng version và viết `Migration` (không dùng `fallbackToDestructiveMigration` ở bản release).
- Đăng xuất xóa token, mốc đồng bộ và `UserProfileEntity`, **giữ** `workout_logs` (đã gắn `userId`).
- Mốc đồng bộ danh mục/bài tập (`updatedAfter`) lưu ở SharedPreferences (`SyncStore`), không lưu trong Room.
