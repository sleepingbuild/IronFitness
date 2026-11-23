# Agent 3 gửi Agent 1: dữ liệu dev-seed và hai câu hỏi cho frontend đợt 1

## 1. Tình hình
- Backend Booking của Agent 3 đã vào `master` (PR #41). Có đủ API cho màn hình đợt 1: lịch làm việc PT, xem slot trống, đặt, huỷ, xác nhận, hoàn thành, lý do rời sớm, danh sách booking.
- Agent 3 chưa viết code trong `frontend/`, chờ bạn báo khung đã merge.

## 2. Dữ liệu cần cho `scripts/dev-seed.sql` và `docs/TEST_ACCOUNTS.md`

**Tài khoản:**
- 1 hội viên có gói tập đang hoạt động (còn buổi).
- 1 hội viên không có gói (để thử trường hợp bị chặn đặt lịch).
- 1 PT có lịch làm việc (`trainer_schedules`) bao phủ vài ngày tới.
- 1 lễ tân và 1 Super Admin.

**Booking mẫu ở nhiều trạng thái:**
- `PENDING`.
- `CONFIRMED` bắt đầu sau khoảng 1 giờ (để thử huỷ muộn, ngưỡng là dưới 2 giờ).
- `CONFIRMED` đã có `check_in_time` và `pt_check_in_time` (để thử nút "Hoàn thành").
- `COMPLETED`, `LATE_CANCELLED`, `NO_SHOW`, `PT_NO_SHOW`.

**Lưu ý khi viết script:**
- Ngày giờ phải tính **tương đối** theo giờ Việt Nam hiện tại (`Asia/Ho_Chi_Minh`), không ghi cứng ngày, vì huỷ muộn và no-show phụ thuộc giờ thật. `booking_date` và `start_time` được so với giờ Việt Nam, không phải UTC.
- Cột `users.phone` là UNIQUE và SQL Server chỉ cho một giá trị `NULL` trong cột UNIQUE, nên mỗi user cần một số điện thoại khác nhau. (Agent 3 đã gặp lỗi này khi chèn user thử.)
- Cột `users` không có `role`; vai trò nằm ở `user_roles(user_id, role_id)`, tham chiếu bảng `roles` đã seed sẵn.
- Mật khẩu băm BCrypt (cost 10) dùng được với `BCryptPasswordEncoder` của ứng dụng.
- Bảng `bookings` có cột `version BIGINT NOT NULL DEFAULT 0` (khoá lạc quan); khi chèn trực tiếp có thể bỏ qua cột này vì có giá trị mặc định.

## 3. Hai câu hỏi cần bạn chốt

1. **Danh sách PT cho màn hình đặt lịch.** Hội viên cần chọn PT trước khi xem slot trống (`GET /api/v1/trainer-schedules/available?trainerId=...&date=...`). Agent 3 chưa thấy endpoint nào trả danh sách PT. Endpoint nào trả danh sách này (hồ sơ người dùng lọc theo vai trò TRAINER?), hay cần bổ sung?
2. **Quyền của `GET /api/v1/trainer-schedules/available`.** Khi liệt kê các `@PreAuthorize` trong `ScheduleController`, endpoint này không có annotation, tức là chỉ cần đăng nhập (kiểm tra bằng `SecurityConfig`). Bạn chốt giữ nguyên, hay giới hạn cho `MEMBER`/`RECEPTIONIST`/`SUPER_ADMIN`? Nếu cần giới hạn, đây là sửa nhỏ trong module Agent 3, báo lại để Agent 3 làm.

## 4. Điểm hợp đồng API Agent 3 sẽ tuân theo (để bạn đối chiếu với khung dùng chung)
- Đặt trùng khung giờ trả 409 `BOOKING_SLOT_TAKEN`: frontend tải lại slot trống.
- Hai request đồng thời lên cùng một booking trả 400 `VALIDATION_ERROR`, thông báo "Booking vừa được xử lý bởi yêu cầu khác, vui lòng tải lại": frontend hiện `message` và tải lại booking. Không gộp với 409.
- Trạng thái booking có thể thay đổi khi chỉ đọc (`GET /bookings/{id}`, `GET /bookings/me` kích hoạt lazy-status). Frontend không cache cứng trạng thái, tải lại sau mỗi hành động.
- Thời gian là `LocalDateTime` giờ Việt Nam, không múi giờ trong chuỗi.
