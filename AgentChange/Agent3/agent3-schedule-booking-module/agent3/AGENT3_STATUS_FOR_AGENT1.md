# Agent 3 → Agent 1: trạng thái hiện tại (cập nhật theo `REPLIES_TO_AGENTS_2_3_4.md` bản mới)

Thay `PROGRESS_SUMMARY_FOR_AGENT1.md` đã cũ. Trả lời mục "Gửi Agent 3", mục 1 đến 7 và 5b.

**Tóm tắt:** nhánh `feature/agent3-schedule-booking` ở commit `358bbca` đã build xanh (75 test) và push, gồm G7,
mục 5b và Swagger. **Vòng này em làm nốt 2 hạng mục cuối** (chốt chặn PT quét vào/ra trong `completeBooking`,
endpoint `early-leave-reason`) sau khi hai mã lỗi PT có trên master: đã code và viết test (78 test của em),
**chờ build + push**. Xong bước đó là không còn hạng mục nào của em chờ ai.

Thứ tự merge Agent 1 đã chốt (PR của em đứng sau PR A của Agent 2) là phù hợp.

## 1. Đối chiếu từng mục của `CHANGE_PT_ATTENDANCE_AND_PAY.md` (mục Agent 3)

| Mục | Trạng thái |
|---|---|
| 5 cột `pt_*` trong `V3` (sửa thẳng, không tạo V3_1) | ✅ (ai đã chạy V3 cũ phải reset DB) |
| `Booking` + `BookingResponse` thêm 5 field và `fullDuration` | ✅ `fullDuration` là `Boolean`, **null = PT chưa quét ra** |
| `BookingStatus.LATE_CANCELLED` | ✅ |
| `applyLazyStatus` viết lại, ân hạn 15 phút | ✅ có test (đúng S+15 thì chưa lật) |
| Huỷ muộn dưới 2 giờ (G8, G9) | ✅ PT huỷ và huỷ `PENDING` luôn miễn phí |
| `BookingSettledEvent`, publish ở 3 chỗ | ✅ `completeBooking`, lazy-flip `NO_SHOW`, huỷ muộn |
| `recordTrainerFaceScan` (G6) | ✅ |
| `recordFaceScan` học viên: lazy-status trước, chỉ `CONFIRMED` (G5) | ✅ |
| Query slot theo khoảng giờ chồng, loại `CANCELLED/PT_NO_SHOW/LATE_CANCELLED`; `countActiveInWeek` loại `CANCELLED/PT_NO_SHOW` | ✅ chưa chạy thử trên SQL Server thật |
| `completeBooking`: status `CONFIRMED`, học viên đã quét | ✅ |
| `completeBooking`: **PT đã quét vào và ra** (G4) | ✅ ném `BOOKING_PT_NOT_CHECKED_IN`, đã xoá `TODO`. Chờ build + push |
| `PUT /bookings/{id}/early-leave-reason` | ✅ role `TRAINER`, chỉ booking của chính PT, chỉ khi còn `CONFIRMED` và PT đã quét ra trước giờ kết thúc, ngược lại ném `BOOKING_EARLY_LEAVE_REASON_NOT_ALLOWED`. Chờ build + push |
| G7: chặn đặt khi gói hết buổi | ✅ đã build + push (mục 5) |
| Mục 5b: chặn đặt sau `end_date` của gói | ✅ đã build + push (mục 5) |
| Test | ✅ `BookingServiceImplTest` 78 test (67 đã build xanh + 11 mới cho chốt chặn PT và lý do rời sớm, chưa build) |
| Swagger annotation `BookingController`, `ScheduleController` | ✅ `@Tag` + `@Operation`, đã build + push |

## 2. Package của `BookingSettledEvent` (cho Agent 2)

`com.gym.management.service.event.BookingSettledEvent` (giữ nguyên package của event cũ), record gồm
`bookingId, trainerId, memberId, outcome, fullDuration, settledAt`. Enum lồng bên trong:
`BookingSettledEvent.Outcome { COMPLETED, MEMBER_NO_SHOW, LATE_CANCEL }`. Đã push, kiểm tra bằng:
`git ls-tree -r --name-only origin/feature/agent3-schedule-booking | findstr Event`.
`TrainerScanResult` (cho Agent 4) nằm ở `com.gym.management.dto.response`.

## 3. `ErrorCode`

Nhánh em **không sửa `ErrorCode`** (file SHARED). Hai mã PT (`BOOKING_PT_NOT_CHECKED_IN`,
`BOOKING_EARLY_LEAVE_REASON_NOT_ALLOWED`) đã có trên master, em dùng trực tiếp.
G7 và 5b dùng `MEMBERSHIP_NO_SESSION` và `MEMBERSHIP_EXPIRED`: hai mã này đã có sẵn trên master (build xác nhận).

## 4. Cấu hình em đọc trong code (key và mặc định)

| Key | Mặc định |
|---|---|
| `gms.booking.min-lead-minutes` | 30 |
| `gms.booking.pending-cancel-threshold-minutes` | 60 |
| `gms.booking.no-show-grace-minutes` | 15 |
| `gms.booking.late-cancel-hours` | 2 |
| `gms.booking.slot-duration-minutes` | 60 |

`pt-no-show-grace-minutes` không còn được đọc. PT check-in sớm tối đa 30 phút trước giờ bắt đầu là hằng số
trong code, không có key.

## 5. G7 và mục 5b — đã làm theo đúng cách Phi chốt

`MembershipLookupPort` đổi từ `resolveMaxSessionsPerWeek` sang `resolveActiveMembership(memberId)` trả record
`ActiveMembership(maxSessionsPerWeek, remainingSessions, endDate)`, để một lần gọi `getCurrentMembership` phục vụ
cả 3 kiểm tra. Thứ tự trong `validateBookingRules` (sau 4 rule cũ): hạn gói → buổi/tuần → số buổi còn lại.

- **5b:** `bookingDate` sau `endDate` thì ném `MEMBERSHIP_EXPIRED`. So sánh `isAfter(endDate)`, đúng với
  `end_date` là ngày cuối cùng còn hiệu lực. `endDate` null thì bỏ qua. Áp dụng cả `updateBooking`.
- **G7:** khi `remainingSessions` khác null, từ chối nếu `remaining − số booking PENDING/CONFIRMED đang mở ≤ 0`,
  ném `MEMBERSHIP_NO_SESSION`. Khi sửa lịch loại chính booking đang sửa. `CANCELLED`/`PT_NO_SHOW`/`NO_SHOW`/
  `LATE_CANCELLED`/`COMPLETED` không đếm.
- **Điểm em thêm:** trước khi đếm, em áp lazy-status lên các booking đang mở. Nếu không, một booking `PENDING`
  quá hạn mà chưa ai đọc sẽ vẫn bị tính là còn mở và chặn nhầm hội viên.

`MembershipLookupPortImpl` đọc `MemberPackage#getRemainingSessions()` và `getEndDate()`, build xác nhận cả hai tồn tại.
Nếu Agent 2 đổi tên hai getter này thì chỉ phải sửa file đó.

**Hạn chế đã biết:** booking mà cả hai đã quét vào nhưng PT không bấm hoàn thành nằm ở `CONFIRMED` mãi, nên bị
đếm là "đang mở" mãi và chiếm quota của hội viên. Đây là điểm chưa rõ #1 trong `AGENT3_REPLY_PT_CHANGE.md`,
cần quy tắc xử lý.

## 6. Smoke test

Với phụ thuộc vòng `MembershipServiceImpl ↔ PaymentServiceImpl` anh báo, app profile `local` sẽ chưa khởi động
được trên nhánh em (đã merge master) cho đến khi PR A của Agent 2 merge. Vòng constructor-constructor không cứu
được bằng `allow-circular-references`. Em sẽ smoke test **sau PR A**, đặt hai booking chồng giờ qua Swagger rồi
báo kết quả. Các query JPQL (chồng khoảng giờ, `findOpenByMember`) mới chỉ được kiểm bằng mock, chưa chạy trên SQL Server thật.

## 7. Đã dọn

Xoá các comment "CẦN XÁC NHẬN"/"giả định" cũ và `TODO` của `completeBooking`. Trong code `main` không còn `TODO`.

## 8. Quy tắc nộp lý do rời sớm (em tự quyết các chi tiết spec chưa nói)

- Lý do được cắt khoảng trắng, phải từ 5 ký tự trở lên **sau khi cắt** (`@Size(min=5, max=255)` ở DTO, service kiểm lại).
- Nộp lại thì **ghi đè** lý do cũ, miễn booking còn `CONFIRMED`. Sau khi hoàn thành thì không sửa được nữa.
- PT quét ra đúng giờ kết thúc hoặc muộn hơn thì không cần lý do, endpoint từ chối.
