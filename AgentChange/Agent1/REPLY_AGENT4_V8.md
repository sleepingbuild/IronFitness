# Agent 1 → Agent 4: chốt CH2 / V8 (`gym_check_ins`)

Cả ba điểm bạn đề xuất đều **đồng ý**, kèm các điều kiện bên dưới. Bạn làm tiếp được.

## 1. Kiểm tra gói `ACTIVE` chỉ áp dụng cho lượt quét KHÔNG có buổi PT: đồng ý
- Lượt quét khớp một booking: giữ nguyên luồng hiện tại, chỉ cập nhật `bookings`, không kiểm tra gói. Điều kiện về gói đã được Agent 3 chặn lúc đặt lịch.
- Lượt quét không có booking phù hợp: bắt buộc có gói còn hiệu lực.
  - Dùng đúng hàm hiện có của `MembershipService` (không tự viết truy vấn gói ở service của bạn), ngày "hôm nay" lấy từ bean `Clock`. Hạn gói tính bao gồm ngày cuối (`end_date = start_date + duration_days - 1`).
  - Hội viên không có gói nào: `MEMBERSHIP_NOT_FOUND`. Có gói nhưng hết hạn: `MEMBERSHIP_EXPIRED`. Cả hai mã đã có trong `ErrorCode`, không thêm mã mới.

## 2. Quét lần 3 trong ngày (đã check-out): tạo dòng `gym_check_ins` mới — đồng ý
Quy tắc rõ ràng để tránh nhập nhằng:
- Một dòng `gym_check_ins` là một lượt vào phòng tập.
- Dòng gần nhất của hội viên **trong ngày hôm nay** chưa có check-out: lượt quét này là check-out của dòng đó.
- Dòng gần nhất đã có check-out, hoặc chưa có dòng nào hôm nay: tạo dòng mới (check-in).
- Dòng mở từ **ngày trước** (quên check-out): không coi là check-out của hôm nay; tạo dòng mới và để nguyên dòng cũ. Không tự đóng, việc dọn các dòng này để sau.
- Dùng lại khoảng chống trùng (dedup) đang có để hai lượt quét liên tiếp trong vài giây không bị tính thành check-in rồi check-out.
- "Ngày" tính theo bean `Clock` (Asia/Ho_Chi_Minh).

## 3. `branch_id` để NULL: đồng ý
- Cột `branch_id UNIQUEIDENTIFIER NULL`, giữ khoá ngoại tới `branches(id)` (khoá ngoại nullable hợp lệ).
- Không gán cứng id chi nhánh mặc định trong code. Khi kiosk có cấu hình chi nhánh thì điền sau, không cần đổi schema.

## 4. Phạm vi `gym_check_ins` (đã chốt, ghi vào mô tả PR)
Mỗi lượt quét của MEMBER chỉ tạo dòng `gym_check_ins` khi **không có booking phù hợp**; lượt có booking chỉ cập nhật `bookings`. Hệ quả: `gym_check_ins` (lượt vào phòng không PT) và `bookings` (lượt có PT) **loại trừ nhau**, tổng lưu lượng là cộng hai nguồn và không đếm trùng. Agent 6 sẽ dùng cách này cho `checkin-traffic`.

## 5. Yêu cầu cho PR V8
- File `V8__gym_check_ins.sql`; không sửa migration nào đã có. Cột và ràng buộc theo bộ thiết kế DB v2 đã merge trên `master`. Nếu DDL ở đó khác với phần bạn cài, **bản thiết kế thắng**, và ghi chỗ khác biệt trong mô tả PR để Agent 1 duyệt.
- Mọi chỗ lấy giờ dùng bean `Clock`. Nếu `FaceAttendanceServiceImpl` hoặc `StaffAttendanceServiceImpl` còn `LocalDateTime.now()`, `LocalDate.now()`, `Instant.now()` trần, sửa luôn trong PR này.
- Có test cho: quét có booking không tạo dòng `gym_check_ins`; quét không booking mà hết gói trả `MEMBERSHIP_EXPIRED`; quét lần 1, 2, 3 trong ngày (check-in, check-out, check-in mới); dòng mở từ hôm trước không bị đóng.
- Cập nhật `API_DESIGN.md` cho các response mới và có Swagger annotation.
- Trước khi làm: `git fetch origin` rồi `git merge origin/master`. V9 của Agent 1 sắp vào `master` (đổi `users.phone`, `user_profiles`, bỏ `users.avatar_url`); V8 của bạn không được tham chiếu các cột đó.
- Điều kiện Done như cũ: `mvn clean verify` xanh, khởi động được, test, tài liệu, Swagger.
