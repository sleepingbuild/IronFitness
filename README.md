# HomeFit – Ứng dụng luyện tập tại nhà

Ứng dụng Android giúp người dùng tập luyện tại nhà theo nhóm cơ, kèm Admin Web để quản trị nội dung bài tập.

## Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Mobile App | Android, **Java**, XML layout, kiến trúc **MVVM** |
| Lưu trữ local | **Room** (SQLite) – cache bài tập, lịch sử tập, hồ sơ |
| Gọi API | Retrofit + OkHttp + Gson |
| Hiển thị GIF/Video | Glide (GIF), Media3 ExoPlayer (video) |
| Backend + Admin Web | **Spring Boot 3** (Java 17), Spring Security + JWT, Spring Data JPA |
| Database backend | **MySQL 8** + Flyway (migration) |
| Giao diện Admin | Thymeleaf (render phía server, cùng project backend) |

## Cấu trúc repo (monorepo)

```
homefit/
├── android/        # Project Android Studio (Java)
├── backend/        # Spring Boot: REST API (/api/v1) + Admin Web (/admin)
├── docs/           # Tài liệu: SETUP, DATABASE, API, TASKS
├── IronFitnessChanges/  # Hàng chờ thay đổi của 5 agent (mỗi agent 1 folder)
├── scripts/        # apply-changes.sh / .ps1: đưa file từ hàng chờ vào repo
├── README.md
├── ARCHITECTURE.md
├── AGENT.md        # Hướng dẫn cho AI agent / thành viên mới
└── CONTRIBUTING.md # Quy tắc Git
```

## Phạm vi MVP

**Mobile App**
1. Tài khoản: đăng ký/đăng nhập Email, Google; nhập chiều cao, cân nặng và tính BMI; cờ người dùng Premium (tính năng tự xếp lịch làm sau).
2. Thư viện bài tập theo nhóm cơ (Bụng, Ngực, Chân, Toàn thân); Workout Player (GIF/Video, đếm ngược bài tập và nghỉ, Pause/Resume/Skip, âm thanh).
3. Lịch sử: đánh dấu buổi tập trên Calendar, thống kê số buổi và tổng phút, nhắc tập hằng ngày (local notification).

**Admin Web**
1. CRUD danh mục và bài tập (link GIF/Video, thời gian giây hoặc số reps).
2. Báo cáo: tổng user đăng ký, tổng lượt tập hoàn thành.

**Ngoài MVP (làm sau):** đăng nhập SĐT/OTP (tốn phí SMS), lịch tập cá nhân Premium, thanh toán.

## Chạy dự án (môi trường dev)

### Yêu cầu
- JDK 17, Android Studio (bản ổn định mới nhất), MySQL 8 (cài trực tiếp, XAMPP hoặc Docker), Maven hoặc Gradle wrapper.

### Backend
```bash
# 1. Tạo database
mysql -u root -p -e "CREATE DATABASE homefit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Cấu hình
cd backend
cp src/main/resources/application-example.yml src/main/resources/application-local.yml
# sửa: spring.datasource.*, app.jwt.secret, app.google.client-id

# 3. Chạy (Flyway tự tạo bảng và seed dữ liệu mẫu)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
- API: `http://localhost:8080/api/v1`
- Admin: `http://localhost:8080/admin` (tài khoản admin mặc định tạo bởi seed, đổi mật khẩu ngay)

### Android
1. Mở thư mục `android/` bằng Android Studio.
2. Trong `local.properties` hoặc `build.gradle` đặt `BASE_URL`:
   - Emulator: `http://10.0.2.2:8080/api/v1/`
   - Máy thật: `http://<IP-LAN-của-máy-chạy-backend>:8080/api/v1/`
3. Thêm `GOOGLE_WEB_CLIENT_ID` (lấy từ Google Cloud Console).
4. Run app.

> Không commit file chứa secret (`application-local.yml`, `local.properties`).

## Tài liệu

- [docs/SETUP.md](docs/SETUP.md) – hướng dẫn khởi tạo dự án
- [IronFitnessChanges/README.md](IronFitnessChanges/README.md) – quy trình giao file của agent
- [ARCHITECTURE.md](ARCHITECTURE.md) – kiến trúc tổng thể
- [docs/DATABASE.md](docs/DATABASE.md) – schema MySQL và Room
- [docs/API.md](docs/API.md) – hợp đồng REST API
- [docs/TASKS_WEEK1.md](docs/TASKS_WEEK1.md) – phân công tuần 1
- [AGENT.md](AGENT.md) – quy tắc cho agent
- [CONTRIBUTING.md](CONTRIBUTING.md) – quy tắc Git

## Thành viên

| TV | Vai trò |
|---|---|
| TV1 | Tech Lead |
| TV2 | Android Dev A (Tài khoản, Profile, Lịch sử) |
| TV3 | Android Dev B (Bài tập, Player) |
| TV4 | Backend + Admin Web (Spring Boot, MySQL) |
| TV5 | UI/UX, QA, Content |
