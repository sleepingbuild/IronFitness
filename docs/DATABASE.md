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

### refresh_tokens (tùy chọn, nếu dùng refresh token)
`id`, `user_id` FK, `token_hash` UNIQUE, `expires_at`, `revoked` BOOLEAN.

### Làm sau (Premium)
`custom_plans`, `custom_plan_items` – lịch tập cá nhân. **Chưa tạo ở MVP.**

### Migration
- `V1__init.sql`: tạo bảng trên.
- `V2__seed.sql`: 4 danh mục, 12–16 bài mẫu (do TV5 cung cấp), 1 tài khoản admin (mật khẩu băm BCrypt, bắt buộc đổi).

## 2. Room (Android)

Database: `AppDatabase` (version 1). Entity phản ánh dữ liệu server cần cache cộng thêm dữ liệu local.

### CategoryEntity
`id` (PK, = id server), `name`, `slug`, `iconUrl`, `sortOrder`, `updatedAt`.

### ExerciseEntity
`id` (PK, = id server), `categoryId` (index), `name`, `description`, `mediaUrl`, `mediaType`, `durationSec` (nullable), `reps` (nullable), `restSec`, `sortOrder`, `updatedAt`.

### WorkoutLogEntity
| Trường | Ghi chú |
|---|---|
| `localId` | PK autoGenerate |
| `clientUuid` | UUID, unique, gửi lên server |
| `categoryId` | nullable |
| `startedAt`, `completedAt` | epoch millis |
| `totalSeconds` | |
| `exercisesDone` | |
| `synced` | boolean, `false` đến khi server xác nhận |

### UserProfileEntity (1 dòng)
`id` (cố định = 1), `email`, `displayName`, `heightCm`, `weightKg`, `isPremium`.

### DAO chính
- `CategoryDao`: `getAll()` (LiveData), `upsertAll(list)`, `deleteMissing(ids)`.
- `ExerciseDao`: `getByCategory(categoryId)` (LiveData), `upsertAll(list)`.
- `WorkoutLogDao`: `insert`, `getUnsynced()`, `markSynced(uuids)`, `getByMonth(from, to)`, `countAll()`, `sumSeconds()`, `getDatesWithWorkout(from, to)` cho Calendar.
- `UserProfileDao`: `get()` (LiveData), `upsert`.

### Quy ước
- Ngày tập trên Calendar tính theo múi giờ máy (từ `completedAt`).
- Đổi schema Room phải tăng version và viết `Migration` (không dùng `fallbackToDestructiveMigration` ở bản release).
