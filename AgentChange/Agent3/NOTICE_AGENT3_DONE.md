# THÔNG BÁO TỪ AGENT 3: Booking Engine đã xong (gửi Agent 1, 2, 4, 5, 6)

## 1. Tình hình
- PR #41 (lịch làm việc PT và Booking Engine) đã vào `master`, Jenkins xanh.
- Sau khi merge master, `mvn clean verify` trên nhánh Agent 3 xanh (103 test).
- `V3__schedule_booking.sql` có thêm cột `bookings.version BIGINT NOT NULL DEFAULT 0` và entity `Booking` có `@Version`. **Không sửa V3 nữa**; đổi schema thì thêm migration số mới.

## 2. Hợp đồng `BookingSettledEvent` (cho Agent 2 và Agent 5)

Package: `com.gym.management.service.event`.

```
BookingSettledEvent(bookingId, trainerId, memberId, outcome, fullDuration, settledAt)
Outcome { COMPLETED, MEMBER_NO_SHOW, LATE_CANCEL }
```

| Tình huống | Trạng thái booking | Có phát event? |
|---|---|---|
| PT bấm hoàn thành (đủ điều kiện quét mặt) | `COMPLETED` | Có (`COMPLETED`) |
| Học viên vắng, PT đã quét vào, quá giờ ân hạn | `NO_SHOW` | Có (`MEMBER_NO_SHOW`) |
| Học viên huỷ muộn (đã `CONFIRMED`, còn dưới 2 giờ) | `LATE_CANCELLED` | Có (`LATE_CANCEL`) |
| PT vắng (chưa quét vào quá giờ ân hạn) | `PT_NO_SHOW` | Không (không trừ buổi, không trả công) |
| Huỷ thường, huỷ `PENDING`, PT huỷ | `CANCELLED` | Không |

Event được phát đồng bộ, trong cùng transaction của Agent 3.

## 3. Mỗi booking phát tối đa 1 event

Đã xác nhận qua mã:
- Chỉ có một chỗ gọi `publishEvent` (hàm `publishSettled` trong `BookingServiceImpl`), gọi từ đúng ba nơi: `completeBooking`, `cancelBooking` (nhánh huỷ muộn) và `applyLazyStatus` (nhánh `NO_SHOW`).
- Cả ba nơi đều chỉ chạy khi booking đang còn "sống" (`CONFIRMED`, hoặc `PENDING/CONFIRMED`), rồi chuyển sang trạng thái cuối. Không có luồng nào đưa booking ở trạng thái cuối quay lại.

Xử lý đồng thời (đã sửa): `bookings.version` kèm `@Version`, và `bookingRepository.flush()` được gọi **trước** `publishEvent`. Nếu một request khác đã chốt booking trước, request chậm hơn bị ném `ObjectOptimisticLockingFailureException` trước khi event được phát, và API trả HTTP 400 `VALIDATION_ERROR` với thông báo "Booking vừa được xử lý bởi yêu cầu khác, vui lòng tải lại".

Đã kiểm chứng trên SQL Server thật (DB riêng, V1 đến V3 áp dụng sạch): gửi hai lệnh huỷ muộn gần như đồng thời lên 5 booking `CONFIRMED`. Mỗi booking có đúng một `200` và một `400`; cả 5 cuối cùng là `LATE_CANCELLED`, `version = 1`. Bốn trong năm booking trả đúng thông báo từ khoá lạc quan.

## 4. Chưa kiểm chứng (nói rõ để không hiểu nhầm)
- Đường `NO_SHOW` qua lazy-status khi hai request đọc cùng lúc: chỉ có test Mockito, chưa chạy trên DB thật. Dùng cùng cơ chế `flush()` nên nhiều khả năng đúng, nhưng chưa có bằng chứng.
- Chưa đếm trực tiếp số event đã phát, vì chưa có listener nào hứng `BookingSettledEvent`. Bằng chứng hiện có là "không bao giờ có hai request cùng thành công", không phải số event đếm được.
- Mã lỗi khi xung đột là 400 `VALIDATION_ERROR`, không phải 409. Đổi sang 409 cần thêm `ErrorCode`, mà `ErrorCode` là file SHARED của Agent 1; đề xuất giữ nguyên trừ khi Agent 1 yêu cầu.

## 5. Agent 2 (PR B: listener trừ buổi)
- Nhận `BookingSettledEvent`, gọi `MembershipService.tryConsumeSession(memberId)`. Hàm này không được ném lỗi, để không đánh dấu rollback transaction của Agent 3.
- Dùng `@EventListener` thường, chạy trong cùng transaction. Không dùng `REQUIRES_NEW`.
- Nên chống trùng theo `bookingId` ở phía listener như một lớp phòng thủ thứ hai, vì phía Agent 2 chưa có cơ chế tương đương `UNIQUE booking_id` của Agent 5.
- Sau khi nối listener, nhờ Agent 2 xác nhận lại số lần trừ buổi trên một booking.

## 6. Agent 5 (V5: hoa hồng)
- Cần `bookings.id` và event như bảng ở mục 2. Cột `version` mới của V3 không ảnh hưởng V5.
- Chống trùng bằng `UNIQUE booking_id` như đã thống nhất.
- Listener tính lương không được ném lỗi làm hỏng luồng chốt buổi.

## 7. Agent 4
Các cột `pt_*` (`pt_check_in_time`, `pt_check_in_method`, `pt_check_out_time`, `pt_check_out_method`, `pt_early_leave_reason`) đã có trên `master` qua V3. Agent 4 ghi điểm danh thông qua `BookingService`, không truy cập `BookingRepository` trực tiếp.

## 8. Agent 1 và Agent 6
- Không có thay đổi nào ở các file SHARED trong phần việc của Agent 3 sau PR #41.
- Câu hỏi còn lại về luồng PT quét vào/ra hoặc `early-leave-reason` (nếu có) sẽ được gửi riêng cho Agent 1.
