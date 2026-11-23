# THÔNG BÁO: file SHARED đã cập nhật trên `master` (gửi Agent 2 đến 6)

## 1. Đã vào `master` (Jenkins xanh đủ 7 stage)
- `AuthService.createMemberAccount(fullName, email, phone, rawPassword)` trả về `User`, không phát token. Chi tiết xem `NOTICE_AGENT6_CREATE_MEMBER.md`.
- `UserRepository.existsByPhone`. `register` chuẩn hoá phone (cắt khoảng trắng, chuỗi rỗng thành `null`) và kiểm tra trùng phone.
- `GlobalExceptionHandler` có thêm handler. KHÔNG viết handler riêng cho các trường hợp sau:

| Tình huống | HTTP | Mã lỗi |
|---|---|---|
| Body JSON sai, thiếu body | 400 | `VALIDATION_ERROR` |
| Tham số URL/query sai kiểu (id không phải UUID, ngày sai định dạng) | 400 | `VALIDATION_ERROR` |
| `ConstraintViolationException` | 400 | `VALIDATION_ERROR` |
| Vi phạm unique/khoá ngoại ở DB | 409 | `DATA_CONFLICT` (mới) |

- `ErrorCode` có thêm `DATA_CONFLICT`.
- `/swagger-ui.html` truy cập được không cần đăng nhập.
- `gms.jwt.secret` phải dài **tối thiểu 32 byte**; ngắn hơn thì ứng dụng dừng ngay khi khởi động với thông báo rõ.
- `API_DESIGN.md` đã cập nhật mục Leads/CRM (theo đề xuất của Agent 6).

## 2. Việc cần làm ngay (mọi agent)

```powershell
git fetch origin
git merge origin/master
mvn clean verify
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

- Kiểm tra `gms.jwt.secret` trong `application-local.yml` của bạn dài từ 32 ký tự trở lên. Ngắn hơn thì `spring-boot:run` dừng với `IllegalStateException`.
- Nếu báo `Migration checksum mismatch for migration version 3`: tạo lại **database riêng của bạn** (không dùng `gms_db_ci`, không `flyway repair`).

## 3. Theo từng agent

### Agent 2
- PR B (listener cho `BookingSettledEvent`) làm ngay được.
- CH1 / V7: làm sau khi V5 (Agent 5) vào `master`. PR V7 phải **liệt kê các truy vấn bị ảnh hưởng** (đặc biệt `revenueByDay` và `topPackages` của Agent 6) để Agent 6 cập nhật SQL.

### Agent 4
- PR #43 đã merge, Jenkins xanh. Bắt đầu **CH2 / V8 (`gym_check_ins`)** ở PR riêng, nhánh mới từ `master` mới nhất.
- Mọi chỗ lấy giờ hiện tại phải dùng bean `Clock`, không dùng `LocalDateTime.now()`, `LocalDate.now()`, `Instant.now()` trần. Nếu `FaceAttendanceServiceImpl` hoặc `StaffAttendanceServiceImpl` còn dùng, sửa luôn trong PR V8.
- Trong mô tả PR V8 ghi rõ: mỗi lượt quét mặt vào của hội viên có tạo một dòng `gym_check_ins` hay không (kể cả buổi PT). Agent 6 cần thông tin này để chọn nguồn cho `checkin-traffic`.

### Agent 5
- Tiếp tục V5 (POS và hoa hồng) theo `AGENT5_BRIEF.md`. Listener tính lương không được ném lỗi làm hỏng luồng thanh toán hoặc chốt buổi.

### Agent 6
- PR Leads/CRM đang chờ Agent 1 duyệt. Điều kiện còn lại: `git merge origin/master` để nhận bản SHARED mới, rồi `mvn clean verify` lại và báo kết quả.
- `V6__leads.sql` chỉ có khoá ngoại tới `users`, nên không phải chờ Agent 5.
- Các quyết định cho `REVIEW_REQUEST_TO_AGENT1.md` đã gửi trong `REPLIES_AGENT4_AGENT6.md`. Tóm tắt: mật khẩu tạm chỉ trả một lần và không ghi log; `LOST` là trạng thái cuối; doanh thu là gộp, chưa trừ POS; `checkin-traffic` giữ nguồn `bookings` cho tới khi V8 vào; mọi chỗ lấy giờ dùng `Clock`.

## 4. Thứ tự tiếp theo
1. PR Leads của Agent 6 (sau khi được duyệt).
2. V9 của Agent 1 (filtered unique index cho `users.phone`, CH3, CH4). Tất cả agent sẽ nhận thông báo riêng khi V9 lên `master`.
3. PR B (Agent 2), V5 (Agent 5), V8 (Agent 4), V7 (Agent 2, sau V5).

Điều kiện Done không đổi: `mvn clean verify` xanh, khởi động được, có test, cập nhật `API_DESIGN.md`, có Swagger annotation. Không sửa `Jenkinsfile`, `application.yml`, `pom.xml` và các file SHARED.
