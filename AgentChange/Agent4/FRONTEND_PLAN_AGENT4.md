# Kế hoạch Frontend đợt 1 — Agent 4 (Face Attendance & Chấm công)

**Trạng thái:** chuẩn bị. Chưa viết code trong `frontend/` cho tới khi Agent 1 báo khung đã merge.
**Nguồn:** `NOTICE_FRONTEND_PHASE.md`, mục "Face Attendance & Chấm công (Agent 4)" trong `API_DESIGN.md`, các file code của Agent 4 trên `master`.
**Thư mục làm việc:** `frontend/src/app/<module>/` và `frontend/src/features/<module>/`.

---

## 1. Danh sách màn hình

| # | Màn hình | Vai trò | Endpoint |
|---|---|---|---|
| 1 | Đăng ký khuôn mặt của tôi | Member, Trainer, Receptionist, Sales | `POST /api/v1/face-profiles/me` (JSON descriptor, client tự trích) |
| 2 | Đăng ký khuôn mặt hộ | Receptionist, Admin (`SUPER_ADMIN`) | `POST /api/v1/face-profiles/{userId}` (JSON descriptor, client chọn ảnh và trích bằng face-api.js); `GET`, `DELETE` cùng đường dẫn |
| 3 | Kiosk điểm danh | Receptionist, Admin | `POST /api/v1/face-attendance/kiosk` (JSON descriptor, 1:N) |
| 4 | Tự điểm danh | Member, Trainer, Receptionist, Sales | `POST /api/v1/face-attendance/self` (JSON descriptor, 1:1) |
| 5 | Bảng chấm công của tôi | Receptionist, Sales | `GET /api/v1/staff-attendance/me` |
| 6 | Báo cáo chấm công theo ngày | Admin (Super Admin) | `GET /api/v1/staff-attendance?date=YYYY-MM-DD` |
| 7 | Lịch sử check-in của tôi | Member | `GET /api/v1/check-ins/me` (chỉ làm sau khi PR V8 merge) |

## 2. Hành vi theo từng màn hình

**Kiosk và tự điểm danh (màn 3, 4).** Response có `matchedUserName`, `action` (`CHECK_IN` / `CHECK_OUT` / `NONE`), `targetType` (`BOOKING` / `STAFF_ATTENDANCE` / `GYM_CHECK_IN` / `null`), `scanTime`, `bookingLinked`, `duplicateScan`, và với PT có thêm `bookingId`, `earlyLeave`, `reasonRequired`, `message`. Giao diện cần xử lý:
- `duplicateScan = true`: báo "quét lại quá sớm", không coi là lỗi.
- `action = NONE` kèm `message` (PT không có buổi nào cần điểm danh): hiển thị `message`, không phải lỗi.
- PT với `reasonRequired = true`: hiện ô nhập lý do rời buổi sớm và gửi qua `PUT /api/v1/bookings/{bookingId}/early-leave-reason` (endpoint của Agent 3).
- Hội viên không có buổi PT (`targetType = GYM_CHECK_IN`, sau khi PR V8 merge): hiển thị như lượt vào/ra phòng tập.
- Lỗi `MEMBERSHIP_NOT_FOUND` / `MEMBERSHIP_EXPIRED` (sau khi PR V8 merge): hiển thị `message`, hội viên cần gia hạn gói.

**Bảng chấm công (màn 5, 6).** Chỉ đọc. Màn 5 hiển thị lịch sử của chính mình kèm trạng thái đúng giờ / đi muộn / về sớm; màn 6 chọn ngày rồi xem toàn bộ nhân sự.

**Lịch sử check-in (màn 7).** Danh sách mới nhất trước. Mỗi dòng có `type` (`GYM_VISIT` lượt tự do, `PT_SESSION` buổi có PT), `checkInTime`, `checkOutTime` (có thể rỗng = chưa quét ra), `method`, `trainerName` (chỉ buổi PT). Chưa phân trang.

## 3. Trạng thái giao diện bắt buộc (theo thông báo của Agent 1)

Đang tải, danh sách rỗng, lỗi API (hiện `message`), không có quyền (403). Ẩn nút theo vai trò, nhưng backend vẫn là lớp bảo vệ thật.

## 4. Điểm cần chú ý kỹ thuật

1. **`FACE_NOT_RECOGNIZED` trả HTTP 401** (theo `API_DESIGN.md`), trong khi thông báo của Agent 1 nói người chưa đăng nhập nhận 403. Nếu bộ gọi API dùng chung coi mọi 401 là phiên hết hạn để làm mới token hoặc chuyển về trang đăng nhập, kiosk sẽ bị đẩy ra khỏi trang mỗi khi quét không nhận diện được. **Cần nhờ Agent 1 xử lý theo `errorCode`, không theo mã HTTP**, ít nhất cho các trang quét.
2. **Camera chỉ chạy ở ngữ cảnh an toàn:** `localhost` hoặc HTTPS. Kiosk chạy trên một máy khác truy cập bằng `http://<IP>` sẽ không mở được camera. Cần chốt cách chạy kiosk khi thử nghiệm.
3. **Đặc trưng khuôn mặt trích ở trình duyệt bằng face-api.js** (theo mô tả Swagger của `FaceProfileController`), gửi JSON `{"descriptor": [...]}`, không gửi ảnh gốc ở luồng quét. Backend (`FaceDescriptorRequest`) bắt buộc **đúng 128 số thực** (`@Size(min = 128, max = 128)`, không rỗng); sai thì trả `FACE_DESCRIPTOR_INVALID` (400). So khớp bằng **khoảng cách Euclid**, ngưỡng mặc định `0.45` (`gms.face-attendance.match-threshold`): khoảng cách nhỏ hơn ngưỡng mới nhận diện được, nếu không trả `FACE_NOT_RECOGNIZED`.
4. Thời gian là `LocalDateTime` giờ Việt Nam, không có múi giờ trong chuỗi: hiển thị nguyên, không cộng trừ múi giờ ở trình duyệt.
5. Kiosk và tự điểm danh là hai trang riêng.

## 5. Đã xác nhận và còn mở

**Đã xác nhận từ code (`FaceProfileController`):**
- Cả đăng ký của tôi (`POST /face-profiles/me`) lẫn đăng ký hộ (`POST /face-profiles/{userId}`) đều nhận **JSON descriptor**, không có multipart. Đăng ký hộ: Receptionist/Admin chọn ảnh trong trình duyệt, face-api.js trích descriptor, backend chỉ nhận JSON. Đăng ký lại ghi đè hồ sơ cũ.
- **Không có** `GET /face-profiles/me`: `GET` và `DELETE` theo `{userId}` chỉ dành cho Receptionist/Admin. Hội viên không tự biết mình đã đăng ký khuôn mặt chưa; muốn có màn "trạng thái đăng ký" cho hội viên thì cần Agent 4 thêm endpoint (thuộc phạm vi Agent 4, không phải Agent 1).
- Backend ghi nhận thư viện trích descriptor là **face-api.js** (128 chiều).

**Còn mở:**
- Ngưỡng `0.45` có hợp với face-api.js hay không chưa kiểm chứng. Theo hiểu biết chung của mình (chưa kiểm chứng trong dự án này), face-api.js thường dùng ngưỡng khoảng 0.6, nên 0.45 là chặt hơn; cần thử với vài khuôn mặt thật và có thể chỉnh `gms.face-attendance.match-threshold`.
- Mô hình của face-api.js phải được tải về từ chính frontend (file model nằm trong `public/`); chưa quyết định cách đặt file và dung lượng.
- `API_DESIGN.md` dòng 119 hiện ghi `/face-profiles/{userId}` là multipart ảnh tĩnh, lệch với code; cần sửa thành JSON descriptor.

## 6. Thứ tự làm khi khung frontend sẵn sàng

1. Màn 1, 4 (đăng ký khuôn mặt của tôi và tự điểm danh): dùng chung một thành phần camera và trích descriptor.
2. Màn 3 (kiosk), tái dùng thành phần camera.
3. Màn 5, 6 (bảng chấm công): chỉ đọc, đơn giản.
4. Màn 2 (đăng ký hộ).
5. Màn 7 (lịch sử check-in) sau khi PR V8 merge.
