> **CẬP NHẬT 2026-10-07:** nội dung bên dưới mô tả bản trước change request
> `CHANGE_PT_ATTENDANCE_AND_PAY.md`. Luật no-show (ân hạn 15 phút), huỷ muộn, PT điểm danh theo buổi và
> `BookingSettledEvent` (thay `BookingCompletedEvent`) đã thay các mục tương ứng. Xem
> `AGENT3_REPLY_PT_CHANGE.md` để biết trạng thái mới nhất.

# Agent 3 — Lịch làm việc PT & Booking Engine — Ghi chú bàn giao
### (Bản cuối — đã build thành công trên máy thật, gửi Agent 1 review)

**Trạng thái: `mvn clean verify` → BUILD SUCCESS** (Agent 2 chạy trên máy thật, 2026-09-30,
9.4s, ghép cùng code của Agent 1 + Agent 2). `verify` chạy cả test phase nên toàn bộ
`BookingServiceImplTest` (17 test) cũng đã PASS trong lần build này — không chỉ compile qua.

Code trong `src/` nằm đúng khu vực sở hữu theo `TASK_ASSIGNMENT.md`: `entity/TrainerSchedule`,
`entity/Booking`, `controller/ScheduleController`, `controller/BookingController`,
`service/*Schedule*`, `service/*Booking*`, migration `V3__schedule_booking.sql` — không tạo/sửa
file SHARED nào (`User`, `BaseEntity`, `Branch`, `SecurityConfig`, `JwtTokenProvider`,
`GlobalExceptionHandler`, `ErrorCode`, `ApiResponse`, migration V1/V2/V2_1).

## 1. Lịch sử 2 lỗi build thật đã fix — chỉ nằm ở 1 file (`MembershipLookupPortImpl.java`)

Đây là điểm chạm DUY NHẤT nối sang module Membership của Agent 2 (đúng như thiết kế ban đầu —
toàn bộ logic Booking không phụ thuộc trực tiếp `MembershipService`, chỉ qua interface
`MembershipLookupPort`), nên khi giả định sai, lỗi chỉ khoanh vùng đúng 1 file, không lan ra chỗ
khác.

| Lần | Lỗi | Nguyên nhân | Đã sửa |
|---|---|---|---|
| 1 | `cannot find symbol: getPackage() location: Optional<MemberPackage>` | Giả định `getCurrentMembership()` trả thẳng object/`null` — thực tế trả `Optional<MemberPackage>` | Unwrap bằng `.orElseThrow(...)` |
| 2 | `cannot find symbol: getPackage() location: MemberPackage` | Giả định field liên kết sang `Package` tên là `package` — sai, vì `package` là từ khoá dành riêng của Java. Field thật (Agent 2 xác nhận qua source): `private Package gymPackage;` | Đổi `getPackage()` → `getGymPackage()` |

Sau 2 lần sửa, build pass hoàn toàn — nghĩa là `getGymPackage().getMaxSessionsPerWeek()` đúng cả
2 vế, không còn giả định treo nào ở điểm tích hợp Agent 2.

## 2. Toàn bộ giả định khác (bảng gốc ở bản giao đầu tiên) — nay coi là ĐÃ XÁC NHẬN THẬT

Vì build (bao gồm compile + chạy test) pass toàn bộ, các giả định sau — vốn chỉ được Agent 1 xác
nhận bằng lời trước đó — nay đã được xác nhận bằng chính trình biên dịch:
`BaseEntity` (id/createdAt/updatedAt + setter), `User.getId()`/`getFullName()`, `UserRepository`/
`BranchRepository` có sẵn, `CurrentUserId`/`CurrentUserRole` thật, `ApiResponse.success(...)`,
`BusinessException(ErrorCode, String)` + có getter `errorCode` (test dùng
`.extracting("errorCode")` cũng đã pass), 8 mã lỗi `SCHEDULE_TIME_INVALID`/`BOOKING_*` có sẵn
trong `ErrorCode`, `@PreAuthorize` prefix `ROLE_`.

## 3. Đã implement + test (khớp `ISSUES.md` 3.1 → 3.5) — TẤT CẢ ĐÃ PASS BUILD

| Issue | Trạng thái |
|---|---|
| 3.1 CRUD TrainerSchedule theo `work_date` | ✅ |
| 3.2 Tạo/sửa Booking + validate đầy đủ 5 rule | ✅ + test riêng từng rule (đã pass) |
| 3.3 Xác nhận/Hoàn thành/Huỷ Booking | ✅ + test (đã pass) |
| 3.4 Lazy auto-status (không cron) | ✅ + test với `Clock` giả lập (đã pass) |
| 3.5 Migration `V3__schedule_booking.sql` | ✅ |

## 4. Việc CHƯA xong / cần quyết định thêm — Agent 1 xem xét

1. **Trừ `member_packages.remaining_sessions` khi Booking COMPLETED — CHƯA implement ở đâu cả**
   (phát hiện khi trả lời câu hỏi Agent 4, xem `AGENT3_RESPONSE_TO_AGENT4.md` mục 4). Đề xuất:
   Agent 2 lắng nghe `BookingCompletedEvent` đã có sẵn (Agent 3 đang phát cho Agent 5 nghe tính
   hoa hồng) để tự trừ buổi — không cần Agent 3 sửa gì thêm nếu Agent 2 đồng ý. **Cần Agent 1
   hoặc Phi chốt hướng này trước khi Agent 2 code.**
2. Pagination chuẩn (`?page=&size=&sort=`) cho `GET /bookings`, `GET /trainer-schedules` — để
   dành, cùng tình trạng với Agent 2 (`AGENT2_README.md` mục 5), chưa ai yêu cầu ưu tiên.
3. Race-condition lock cho `recordFaceScan` — hiện dùng lock in-memory phía Agent 4 (đã Agent 3
   xác nhận đủ dùng ở `AGENT3_RESPONSE_TO_AGENT4.md` mục 2, vì dự án chưa chạy nhiều instance).
4. `BookingClockConfig.java` (bean `Clock`, file mới trong package `service/`) — nếu Agent 1 sau
   này định nghĩa bean `Clock` khác ở `config/` gây duplicate bean, chỉ cần xoá file này của
   Agent 3, không ảnh hưởng logic (`BookingServiceImpl`/`TrainerScheduleServiceImpl` chỉ inject
   theo type `Clock`).

## 5. File đính kèm trong bản giao này

- Toàn bộ source `src/main/java/...` + migration `V3__schedule_booking.sql`
- `src/test/java/.../BookingServiceImplTest.java` — 17 test, đã pass trong lần build thật
- `AGENT3_RESPONSE_TO_AGENT4.md` — trả lời 4 câu hỏi tích hợp từ Agent 4 (đính kèm riêng)
