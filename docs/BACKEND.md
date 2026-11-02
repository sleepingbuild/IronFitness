# BACKEND – Chạy, cấu hình và kiểm tra máy chủ HomeFit

Tài liệu cho người chạy và phát triển backend (`backend/`). Cập nhật lần cuối: 2026-11-01.

## 1. Yêu cầu
- JDK 17 trở lên (dự án biên dịch ở mức Java 17, chạy được trên JDK mới hơn).
- MySQL 8 đang chạy (Windows: dịch vụ `MySQL80` hoặc tên tương tự).
- Không cần cài Maven: dùng `mvnw.cmd` có sẵn trong `backend/`.

## 2. Chuẩn bị một lần

### 2.1 Tạo database
```sql
CREATE DATABASE IF NOT EXISTS homefit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Chạy trong MySQL Workbench, hoặc `mysql -u root -p` (nếu lệnh `mysql` chưa nhận ra, thêm thư mục `bin` của MySQL vào PATH của cửa sổ terminal hiện tại).

### 2.2 Tạo `application-local.yml`
Sao chép `backend/src/main/resources/application-example.yml` thành `application-local.yml` cùng thư mục và điền giá trị thật. File này **không được commit** (đã nằm trong `.gitignore`); kiểm tra bằng `git check-ignore -v backend/src/main/resources/application-local.yml` (phải in ra một dòng).

Quy tắc bắt buộc:
- **Mọi giá trị đặt trong nháy kép.** Trong YAML ký tự `#` bắt đầu chú thích: `password: #abc` bị đọc thành rỗng và gây lỗi `using password: NO`.
- Không đặt `spring.jpa.hibernate.ddl-auto` (đã là `none` trong `application.properties`). Đặt `validate` sẽ báo lệch kiểu cột `DECIMAL`.
- `homefit.jwt.secret` dài ít nhất 32 ký tự; không dùng giá trị mẫu.
- `homefit.admin.email` và `homefit.admin.password` (từ 8 ký tự, không phải `CHANGE_ME...`) để tạo tài khoản quản trị đầu tiên khi khởi động.

## 3. Chạy
```
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```
- Cổng mặc định **8090** (`server.port` trong `application.properties`). Đổi tạm bằng biến môi trường: `$env:SERVER_PORT = "8091"`.
- Lần đầu Flyway tạo bảng và dữ liệu mẫu; log có `Successfully applied N migrations`.
- Trang quản trị: `http://localhost:8090/admin`. API: `http://localhost:8090/api/v1/...`.

Hai cách bật profile khác nhau, đừng nhầm:

| Lệnh | Cú pháp bật profile `local` |
|---|---|
| `spring-boot:run` (chạy máy chủ) | `"-Dspring-boot.run.profiles=local"` |
| `verify` (chạy test) | `"-Dspring.profiles.active=local"` (các test tích hợp đã tự bật profile `local`) |

## 4. Kiểm thử
```
cd backend
.\mvnw.cmd verify "-Dspring.profiles.active=local"
```
- **Kiểm thử đơn vị** (không cần database): JWT, kiểm tra dữ liệu buổi tập, quy tắc nghiệp vụ, biểu mẫu admin.
- **Kiểm thử tích hợp** (`*IntegrationTest`): khởi động máy chủ thật trên cổng ngẫu nhiên, kết nối MySQL local và gọi API bằng HTTP thật. Cần MySQL đang chạy và `application-local.yml` đúng.
- Dữ liệu của test dùng email bắt đầu bằng `it-` và tự xóa sau mỗi test (lịch sử và token xóa theo). Nếu một lần chạy bị dừng giữa chừng, dọn tay:
  ```sql
  DELETE FROM users WHERE email LIKE 'it-%@example.test';
  ```
- Kiểm thử đăng nhập trang quản trị bằng form cần mật khẩu admin nên thử tay.

### Đo hiệu năng (phục vụ chương Kiểm thử của báo cáo)
```
powershell -ExecutionPolicy Bypass -File scripts\perf-api.ps1 -BaseUrl http://localhost:8090 -Runs 30 -Concurrency 10
```
Script đăng ký một người dùng `perf-...@example.test`, đo thời gian đăng nhập, đọc danh mục, đọc bài tập, ghi buổi tập, đồng bộ lô 100 buổi và tải song song. Dọn dữ liệu đo:
```sql
DELETE FROM users WHERE email LIKE 'perf-%@example.test';
```
Kết quả chỉ có giá trị tham khảo trên máy phát triển.

## 5. API đã có
| Nhóm | Endpoint | Ghi chú |
|---|---|---|
| Xác thực (công khai) | `POST /api/v1/auth/register`, `/login`, `/refresh` | Làm mới có xoay vòng token. `/auth/google` chưa làm |
| Hồ sơ | `GET /api/v1/me`, `PUT /api/v1/me/profile` | userId lấy từ token |
| Thư viện | `GET /api/v1/categories`, `GET /api/v1/exercises` | `updatedAfter` trả cả bản ghi vừa ẩn (`isActive:false`) |
| Lịch sử | `POST /api/v1/workout-sessions`, `/batch`, `GET /api/v1/workout-sessions` | Chống trùng theo `clientUuid` |
| Quản trị | `/admin`, `/admin/categories`, `/admin/exercises`, `/admin/users` | Đăng nhập form, phiên, CSRF, chỉ ADMIN |

Hợp đồng chi tiết nằm ở `docs/API.md`; schema ở `docs/DATABASE.md`.

## 6. Migration (Flyway)
| File | Nội dung |
|---|---|
| `V1__init.sql` | 5 bảng: users, categories, exercises, workout_sessions, refresh_tokens |
| `V2__seed_categories.sql` | 4 nhóm cơ |
| `V3__seed_sample_exercises.sql` | 12 bài **thử** (ảnh giữ chỗ), sẽ được `V4` thay bằng bài thật |

Chỉ thêm file `V{n}__mo_ta.sql` mới; **không sửa** file đã chạy (Flyway kiểm tra mã băm và báo lỗi nếu file thay đổi).

## 7. Bảo mật tóm tắt
- Hai chuỗi bộ lọc: `/api/**` dùng JWT (HS256, không phiên); `/admin/**` dùng đăng nhập form, phiên và CSRF, chỉ vai trò ADMIN. Mọi đường dẫn khác bị từ chối.
- Mật khẩu băm BCrypt; refresh token chỉ lưu băm SHA-256, mỗi lần làm mới thì token cũ bị thu hồi.
- Không đưa mật khẩu, khóa JWT hay ID thật vào Git.
- Không dùng `jwt.getIssuer()` (claim `iss` là chuỗi, không phải URL); dùng `jwt.getClaimAsString("iss")`.

## 8. Lỗi thường gặp
| Dấu hiệu | Nguyên nhân | Cách sửa |
|---|---|---|
| `using password: NO`, `Access denied` | Mật khẩu trong YAML bắt đầu bằng `#` hoặc sai | Đặt trong nháy kép, kiểm tra bằng `mysql -u root -p -e "SELECT 1;"` |
| `Failed to configure a DataSource ... no profiles are currently active` | Chưa bật profile `local` | Dùng đúng cú pháp ở mục 3 |
| `wrong column type ... found [decimal]` | `ddl-auto: validate` trong `application-local.yml` | Xóa dòng đó |
| `homefit.jwt.secret phải có ít nhất 32 ký tự` | Thiếu hoặc ngắn khóa JWT | Khai báo trong `application-local.yml` |
| `Chưa tạo tài khoản admin` | Thiếu hoặc dùng giá trị mẫu cho `homefit.admin.*` | Điền email và mật khẩu từ 8 ký tự |
| `Unknown database 'homefit'` | Chưa tạo database | Mục 2.1 |
| `Communications link failure` | MySQL không chạy | `Get-Service MySQL*` phải `Running` |
| `Port 8090 was already in use` | Cổng bị chiếm | Dừng chương trình khác hoặc đặt `SERVER_PORT` |
| App báo "không kết nối được máy chủ" | Backend không chạy, sai cổng, hoặc điện thoại thật | Kiểm tra backend; `api_base_url` phải khớp cổng; máy thật dùng IP LAN |
| `cannot find symbol` ở lớp `ApiException`, `CategoryRepository` | Áp dụng gói code khi PR của gói trước chưa merge | Merge gói trước rồi áp dụng lại |
| Test tích hợp lỗi `Connection refused` | MySQL tắt hoặc thiếu `application-local.yml` | Kiểm tra mục 2 |
