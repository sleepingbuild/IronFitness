# Gửi Phi + Agent 1 — Các điểm cần xác nhận (từ Agent 4)

> Từ nay các câu hỏi thuộc thẩm quyền Phi (quyết định thiết kế/phạm vi) và các câu hỏi cần
> Agent 1 (xem file nền/shared) được gộp chung 1 file, xử lý cùng lúc.

---

## A. Cần Phi quyết định (thiết kế/phạm vi)

### A.1 `FaceProfileService.registerForWithImage()` — đăng ký hộ bằng ảnh tĩnh
`REQUIREMENTS.md` mục 4 quy định trích descriptor luôn làm ở client (JS), nhưng luồng "Đăng
ký hộ" (Lễ tân upload ảnh tĩnh) đòi hỏi trích descriptor **phía server (Java)** — hiện chưa
có thư viện face-detection nào xác nhận có trong `pom.xml`.

**Chọn 1 trong 2:**
- (a) Bỏ luồng ảnh tĩnh khỏi v1 — Lễ tân đăng ký hộ cũng dùng webcam + JS như luồng tự đăng ký
- (b) Thêm thư viện Java face-detection (JavaCV/OpenCV binding, hoặc gọi service riêng) —
  tăng độ phức tạp đáng kể

**Trạng thái code:** `registerForWithImage()` đang tạm ném `UnsupportedOperationException`.

### A.2 Chấm công theo ca mặc định TOÀN HỆ THỐNG, không theo từng nhân sự
Đã chọn dùng 1 cặp giờ mặc định qua config (`gms.face-attendance.default-shift-start/end`,
07:00–21:00), không phân biệt theo người — đơn giản hoá cho quy mô đồ án.
**Xác nhận:** có đúng ý đồ không, hay cần ca riêng theo từng nhân sự (phải thêm migration
`V4_1` + PR `[shared]` nếu chọn thêm cột vào `users`)?

### A.3 Race-condition — lock in-memory, chỉ đúng khi chạy 1 instance
Đã dùng `ConcurrentHashMap` + `synchronized` theo `userId`, không phải lock phân tán.
**Xác nhận:** phạm vi đồ án chạy 1 instance là đủ, giữ nguyên giải pháp này?

### A.4 `face_profiles.reference_image` — lưu path/URL hay BLOB trong DB?
Đang theo convention `User.avatarUrl` (String path) + `gms.upload.dir: uploads` đã có sẵn.
**Xác nhận cuối cùng** hướng đi này (bạn từng nêu ý muốn lưu DB ở lượt trước).

---

## B. Cần Agent 1 xem file thật (không phải quyết định, chỉ cần xác nhận thông tin)

### B.1 `pom.xml` — liên quan trực tiếp đến quyết định A.1
Cần biết đã có sẵn dependency xử lý ảnh/ML nào chưa.

### B.2 `V1__init_schema.sql` (seed data) — giá trị `roles.name` lưu thật trong DB
Agent 4 đang giả định `Role.getName()` trả đúng `"MEMBER"`/`"TRAINER"`/`"RECEPTIONIST"`
(không dấu `ROLE_`, đúng chữ hoa) — dùng để route quét mặt sang Booking hay StaffAttendance.

### B.3 `UserRepository.java` — xác nhận có sẵn `findById(UUID)` và `getReferenceById(UUID)`
Đang giả định chuẩn Spring Data JPA, chưa xem file thật.

---

## Thứ tự nên xử lý
1. A.1 trước — ảnh hưởng lớn nhất đến khối lượng code còn lại
2. B.1 đi kèm A.1 (cùng 1 quyết định)
3. A.2/A.3/A.4 — không chặn code tiếp nhưng cần biết để review đúng hướng
4. B.2/B.3 — gửi kèm file thật khi tiện, không gấp
