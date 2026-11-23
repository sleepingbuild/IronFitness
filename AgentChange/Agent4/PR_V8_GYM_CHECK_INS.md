# PR: CH2 / V8 — `gym_check_ins` và lịch sử check-in (M17)

**Nhánh:** `feature/agent4-ch2-gym-check-ins` → `master`
**Tác giả:** Agent 4
**Tham chiếu:** `REPLY_AGENT4_V8.md` (Agent 1 đã chốt 3 điểm), `DATABASE_SCHEMA.md` mục V8, `docs/DB_DESIGN_REVIEW.md` CH2, `USER_STORIES.md` M16/M17

---

## 1. Phạm vi

- Migration `V8__gym_check_ins.sql`, không sửa migration nào đã có.
- Entity `GymCheckIn`, `GymCheckInRepository`, `GymCheckInService` / `GymCheckInServiceImpl`.
- `FaceAttendanceServiceImpl`: MEMBER quét mặt mà **không có booking phù hợp** thì ghi vào `gym_check_ins`.
- M17: `GET /api/v1/check-ins/me` (`CheckInController`, `CheckInHistoryService`, `CheckInHistoryItem`).
- Đổi mọi chỗ lấy giờ trong `FaceAttendanceServiceImpl` sang bean `Clock`.

## 2. Mỗi lượt quét của MEMBER tạo dòng `gym_check_ins` khi nào (theo yêu cầu của Agent 1)

| Tình huống | `bookings` | `gym_check_ins` |
|---|---|---|
| Có booking phù hợp trong ngày (kể cả hội viên đang có buổi PT) | Cập nhật như cũ | **Không tạo dòng** |
| Không có booking phù hợp | Không đổi | Tạo hoặc đóng dòng (xem mục 3) |

Hệ quả: `gym_check_ins` và `bookings` **loại trừ nhau**, tổng lưu lượng = cộng hai nguồn, không đếm trùng. Agent 6 dùng cách này cho `checkin-traffic`.

## 3. Quy tắc lượt vào/ra (`GymCheckInServiceImpl`)

- Dòng gần nhất của hội viên **trong cùng ngày** (theo `Clock`, Asia/Ho_Chi_Minh) chưa có `check_out_time`: lần quét này là **check-out** của dòng đó. Không kiểm tra gói, để hội viên vẫn ra được khi gói vừa hết hạn lúc đang tập.
- Dòng gần nhất đã có check-out, hoặc chưa có dòng nào hôm nay: **tạo dòng mới** (check-in), có kiểm tra gói.
- Dòng mở từ **ngày trước** (quên check-out): không coi là check-out của hôm nay, tạo dòng mới và **để nguyên dòng cũ**. Việc dọn các dòng này để sau.
- Chống quét đúp: dùng lại khóa dedup Redis sẵn có (120 giây cho MEMBER), không viết cơ chế mới.

## 4. Kiểm tra gói (chỉ cho lượt vào mới, không có booking)

Dùng `MembershipService.getCurrentMembership` (không tự viết truy vấn gói):

| Trường hợp | Mã lỗi |
|---|---|
| Không còn gói ACTIVE và chưa từng có gói nào (`getHistory` rỗng) | `MEMBERSHIP_NOT_FOUND` |
| Không còn gói ACTIVE nhưng đã từng có gói | `MEMBERSHIP_EXPIRED` |

Cả hai mã đã có trong `ErrorCode`, không thêm mã mới.

## 5. Khác biệt với bản thiết kế DB và điểm cần Agent 1 duyệt

- **DDL V8 khớp `DATABASE_SCHEMA.md`** (cột, kiểu, mặc định, chỉ mục `idx_gym_check_ins_member_time`). `branch_id` nullable, có khóa ngoại tới `branches(id)` như đã chốt. V8 không tham chiếu cột nào của V9.
- Entity lưu `member_id` và `branch_id` dạng `UUID` thô (không map quan hệ sang `User`/`Branch`), cùng cách với `Booking.branchId`.
- **Hội viên chỉ có gói `PENDING` hoặc `CANCELLED`** (không có ACTIVE, không có gói nào hết hạn) cũng nhận `MEMBERSHIP_EXPIRED`, vì mã chỉ dựa vào việc `getHistory` có phần tử hay không. Cần Agent 1 xác nhận hành vi này chấp nhận được.
- **Khóa dedup Redis được đặt trước khi kiểm tra gói.** Hội viên bị từ chối `MEMBERSHIP_EXPIRED` rồi gia hạn xong phải đợi tối đa 120 giây mới quét lại được. Không đổi hành vi này trong PR; đề nghị xử lý riêng nếu cần.
- Quét tự phục vụ (`selfScan`) của MEMBER đi cùng luồng với kiosk nên cũng ghi `gym_check_ins` và cũng kiểm tra gói. Tài liệu CH2 chỉ nhắc kiosk; cần xác nhận hai đường quét dùng chung quy tắc.
- `GET /api/v1/check-ins/me` trả danh sách đầy đủ, chưa phân trang (giống `/staff-attendance/me`). Dễ đổi sang phân trang nếu Agent 1 yêu cầu.
- `BookingService.findMyBookings` được gọi với vai trò `"MEMBER"`; xác nhận chuỗi này đúng với cách `BookingController` truyền vào.

## 6. Dùng `Clock`

- `FaceAttendanceServiceImpl`: bỏ 2 chỗ `LocalDateTime.now()` trần, dùng `LocalDateTime.now(clock)`.
- `StaffAttendanceServiceImpl`: không còn `now()` trần, không cần sửa.
- Còn `now()` trần ở file **không thuộc Agent 4**, không sửa trong PR này: `ApiResponse` (`Instant.now()`), `TokenBlacklistService` (`Instant.now()`), `PaymentServiceImpl` (2 chỗ `LocalDate.now()`).

## 7. Kiểm thử

- `GymCheckInServiceImplTest` (6 test): lượt 1 tạo check-in; hội viên chưa có gói → `MEMBERSHIP_NOT_FOUND`; gói đã hết hạn → `MEMBERSHIP_EXPIRED`; lượt 2 là check-out và không gọi kiểm tra gói; lượt 3 tạo dòng mới; dòng mở từ hôm trước không bị đóng.
- `FaceAttendanceServiceImplTest`: quét có booking **không** gọi `GymCheckInService`; quét không booking ghi `GYM_CHECK_IN`.
- `CheckInHistoryServiceImplTest` (3 test): gộp hai nguồn đúng thứ tự, loại booking chưa quét vào, danh sách rỗng.

## 8. Kết quả kiểm tra (điền trước khi gửi)

- [ ] `git merge origin/master` không conflict: ______
- [ ] `mvn clean verify`: ______
- [ ] `spring-boot:run` (profile `local`, database riêng `gms_db_agent4`) khởi động được, Flyway áp V8: ______ (đã xác nhận V8 `success = 1` ở lần chạy trước khi thêm M17)
- [ ] Swagger có nhóm `Check-in` với `GET /api/v1/check-ins/me`: đã kiểm tra
- [ ] `API_DESIGN.md` đã cập nhật: ______
