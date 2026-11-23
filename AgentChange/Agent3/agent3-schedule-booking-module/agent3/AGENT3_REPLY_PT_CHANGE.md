# Agent 3 → Agent 1: phản hồi CHANGE_PT_ATTENDANCE_AND_PAY

## 1. Đã làm xong (bước 2-7 của yêu cầu)

| Việc | Ghi chú |
|---|---|
| V3: thêm 5 cột `pt_*` | sửa thẳng `V3__schedule_booking.sql`. **Ai đã chạy V3 cũ ở local phải `docker compose down -v`**, nếu không Flyway báo checksum mismatch |
| `Booking`, `BookingResponse` + `fullDuration` | `fullDuration` kiểu `Boolean`: **null = PT chưa quét ra** (chưa xác định được), true/false như spec |
| `BookingStatus.LATE_CANCELLED` | cột VARCHAR, không cần migration |
| `applyLazyStatus` viết lại | ân hạn tính từ **giờ bắt đầu**, `no-show-grace-minutes` mặc định 15. So sánh nghiêm ngặt: đúng S+15 thì chưa lật |
| `cancelBooking` | huỷ muộn khi `start - now < 2h`, đúng 2h chưa muộn. PT huỷ → `CANCELLED`. Huỷ `PENDING` luôn miễn phí |
| `BookingRepository` | slot theo khoảng chồng, loại `CANCELLED/PT_NO_SHOW/LATE_CANCELLED`. `countActiveInWeek` loại `CANCELLED/PT_NO_SHOW`. `findTodayPendingCheckIn` chỉ `CONFIRMED` |
| `BookingSettledEvent(outcome, fullDuration)` | thay `BookingCompletedEvent` (đã xoá). Phát ở `completeBooking`, lazy-flip `NO_SHOW`, huỷ muộn |
| `recordTrainerFaceScan` | theo đúng thứ tự chọn booking trong spec. Kết quả là `TrainerScanResult` ở **`com.gym.management.dto.response`** (Agent 4 import từ đây) |
| `TrainerScheduleServiceImpl.findAvailableSlots` | cũng đổi sang chồng khoảng + loại 3 status, nếu không FE thấy slot trống nhưng đặt thì báo trùng |
| Test | `BookingServiceImplTest` viết lại, 55 test, gồm đủ các ca spec yêu cầu (ân hạn 15 phút, mốc 2 giờ, huỷ PENDING, `fullDuration` có/không lý do) |

## 2. CHƯA làm, đang chờ — PR chưa được merge khi các mục này còn mở

1. **`completeBooking` chốt chặn PT đã quét vào/ra** + **endpoint `PUT /bookings/{id}/early-leave-reason`**:
   chờ `ErrorCode.BOOKING_PT_NOT_CHECKED_IN` và `BOOKING_EARLY_LEAVE_REASON_NOT_ALLOWED` lên master.
   Hiện `completeBooking` là **bản tạm**: đã chặn status khác `CONFIRMED` và học viên chưa quét,
   nhưng **chưa chặn PT chưa quét** (có `TODO` ngay trong code). Anh báo khi merge, em làm nốt.
2. **G7: chặn `createBooking` khi gói hết buổi.** Cần anh/Phi/Agent 2 chốt 2 điểm trước khi em
   đụng vào (em không có source `MemberPackage` để tự kiểm):
   - Tên getter của `remaining_sessions` trên `MemberPackage`. Nhờ chạy:
     `Select-String -Path "src\main\java\com\gym\management\entity\MemberPackage.java" -Pattern "remaining"`
   - "Hướng A" nghĩa là chặn khi `remaining == 0`, hay khi `remaining <= số booking PENDING/CONFIRMED chưa chốt`?
     Vì `remaining_sessions` chỉ giảm lúc chốt, nếu chỉ chặn `== 0` thì học viên còn 1 buổi vẫn đặt được 5 buổi
     tương lai cùng lúc. Em đề xuất cách thứ hai; gói theo thời gian (`remaining` null) thì không giới hạn.

## 3. Điểm spec chưa nói rõ, em tự quyết — cần anh/Phi xác nhận

1. **Cả hai đã quét vào nhưng PT không bấm hoàn thành**: booking nằm ở `CONFIRMED` mãi, không trừ buổi,
   không trả công. Trước đây nhánh `PT_NO_SHOW` 30 phút cũ che trường hợp này, spec mới bỏ nó. Cần
   quy tắc (vd quá `E + n giờ` thì tự chốt thế nào?). Hiện em để nguyên.
2. **PT đã quét vào, học viên vắng → lazy-flip `NO_SHOW`**: booking rời `CONFIRMED` nên lượt PT quét ra
   sau đó trả `empty` ("không có buổi cần điểm danh"), `pt_check_out_time` không được ghi. PT vẫn được
   trả công (quy tắc 10). Agent 4 nên hiện thông báo phù hợp, không coi là lỗi.
3. **`LATE_CANCELLED` nhả slot** (đúng theo query spec). Học viên khác đặt lại được slot đó, PT có thể
   được trả hai lần cho cùng khung giờ. Đúng ý Phi không?
4. PT check-in sớm tối đa 30 phút: spec không có config key nên em để hằng số trong code.

## 4. Lỗi tồn đọng trong code cũ của em, đã sửa luôn (không có trong spec)

- `getById` đang `@Transactional(readOnly = true)` nhưng lại gọi `applyLazyStatus` (ghi DB + phát event).
- `updateBooking` không kiểm tra quyền: bất kỳ member nào cũng sửa được booking của người khác → đã
  chặn, chỉ chủ booking hoặc lễ tân/admin. Thêm: chỉ sửa được booking `PENDING/CONFIRMED`.
- `cancelBooking` huỷ lại booking đã `LATE_CANCELLED` sẽ phát event lần 2, học viên mất buổi 2 lần
  → chỉ huỷ được booking `PENDING/CONFIRMED`.
- Query slot dùng `(:excludeId is null or ...)` với UUID null: trên SQL Server dễ bị lỗi chuyển kiểu
  varbinary → uniqueidentifier. Đổi sang UUID sentinel `BookingRepository.NO_EXCLUDE`.

## 5. Cần anh làm / xác nhận

- `application.yml.example`: thêm `gms.booking.no-show-grace-minutes: 15` và `gms.booking.late-cancel-hours: 2`,
  bỏ `pt-no-show-grace-minutes`. Code đã có default đúng nên chưa có key vẫn chạy được.
- Spec nói kiểm tra `CurrentUserRole.get()` trả `"RECEPTIONIST"` chứ không phải `"ROLE_RECEPTIONIST"`. Em
  chưa mở được file thật (42 dòng, dài hơn bản tạm của em). Nhờ anh xác nhận, hoặc chạy:
  `Get-Content src\main\java\com\gym\management\security\CurrentUserRole.java`
- Agent 2 đổi listener sang `BookingSettledEvent` (package `com.gym.management.service.event`, giữ nguyên chỗ cũ).
  Agent 5 listen cùng event này.

## 6. Cảnh báo về mức độ kiểm chứng

Em chưa chạy được `mvn verify` cho đợt này (sẽ chạy trong `gms-agent3` sau khi merge master và áp file).
Cú pháp đã kiểm bằng `javac`, logic test đã lần tay từng ca, nhưng **các query JPQL (chồng khoảng giờ,
loại status) chưa từng chạy trên SQL Server thật** — unit test dùng mock không bắt được lỗi ở tầng này.
Cần chạy app thật một lần (đặt 2 booking chồng giờ qua Swagger) trước khi coi là xong.
