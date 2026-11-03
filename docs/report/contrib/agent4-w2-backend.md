# Báo cáo Đóng góp Tuần 2 - Agent 4 (Backend & Admin Web)

## 1. Các Task Đã Hoàn Thành
- **[W2-TV4-01] Hoàn thiện API Tuần 1**: Đã viết hoàn chỉnh logic cho `AuthController`, `MeController`, `LibraryController` và `GlobalExceptionHandler`.
- **[W2-TV4-02] API Đồng bộ Lịch sử Tập luyện**: Triển khai `WorkoutSessionController` và `WorkoutSessionService`. Đảm bảo tính idempotent theo `clientUuid`, cho phép push theo lô (`/batch`) và query list theo khoảng thời gian `from`/`to`.
- **[W2-TV4-03 & W2-TV4-04] Admin Web**: Hoàn thiện toàn bộ logic Web MVC (Thymeleaf) trong thư mục `admin/`. Cung cấp màn hình Dashboard hiển thị tổng quan số liệu, CRUD bài tập và quản lý quyền Premium của người dùng.
- **[W2-TV4-05] Xác thực Đăng nhập Google**: Tích hợp thư viện `google-api-client-gson`, xử lý xác thực `idToken` bằng `GoogleIdTokenVerifier` trong `AuthService`, tự động liên kết hoặc tạo tài khoản mới.
- **[W2-TV4-06] Kiểm thử Tích hợp**: Xây dựng 58 Test Cases cho cả API (JSON) và Admin Web (Thymeleaf). Tất cả đều chạy mượt mà và Passed 100% bằng lệnh `mvnw verify`.
- **[W2-TV4-07] Hướng dẫn Chạy Backend**: Cập nhật file `docs/SETUP.md`, bổ sung cách tạo cấu hình `application-local.yml`, tạo tài khoản Admin đầu tiên thông qua cấu hình, và giải thích về quá trình Migration của Flyway.
- **[W2-TV1-04] Triển khai Đám mây (Ops)**: Tạo tài liệu `docs/DEPLOY.md` đánh giá và chọn giải pháp Render (App) + Aiven (MySQL) để nhóm chuẩn bị triển khai lên môi trường Live. 

## 2. Các Task Đang Chờ / Chưa Thể Làm
- **[W2-TV4-08] V5__seed_more_exercises.sql**: Đang chờ file `docs/seed/exercises.csv` từ Agent 5 (Nghiên cứu thị trường / UI) để viết script tạo bài tập mở rộng.

## 3. Quá Trình Giải Quyết Lỗi & Ghi Chú
- **Lỗi Mạng Android Emulator (Cleartext HTTP)**: Khi gọi API từ Android Studio Emulator tới Spring Boot ở `http://localhost:8090`, đã gặp lỗi SocketTimeoutException do tường lửa Hyper-V WSL chặn truy cập Loopback từ Emulator. Giải pháp là hướng dẫn nhóm cấu hình chuyển sang IP LAN vật lý (VD: `192.168.x.x`) kết hợp với `network_security_config.xml` và cho phép cleartext traffic trên SDK 28+.
- **Idempotency trong Đồng bộ Lịch sử**: Khi một thiết bị gửi lịch sử lên nhiều lần (VD: do mạng chập chờn), `DataIntegrityViolationException` sẽ được bắt và `WorkoutSessionService` tự động kiểm tra lại `clientUuid` để trả về HTTP 200 thay vì tạo bản ghi trùng lặp (HTTP 201). Điều này đảm bảo tính nhất quán của lịch sử tập luyện bất kể tình trạng mạng của User.
