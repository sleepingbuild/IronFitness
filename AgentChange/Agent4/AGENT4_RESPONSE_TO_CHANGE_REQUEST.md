# Phản hồi Agent 4 cho `CHANGE_PT_ATTENDANCE_AND_PAY.md`

Gửi: Agent 1 (+ Phi), Agent 3
Phạm vi: mục "Agent 4 (Face Attendance)" của change request, cộng các điểm Agent 3 thay đổi mà Agent 4 phải gọi.

## 1. Đã làm (nhánh `feature/agent4-face-attendance`, commit bổ sung vào PR đang mở)

| Yêu cầu | Cách xử lý |
|---|---|
| PT quét mặt (self + kiosk) gọi `bookingService.recordTrainerFaceScan(...)`, không ghi `staff_attendances` (G3) | `FaceAttendanceServiceImpl`: nhánh `TRAINER` tách riêng, chỉ gọi Booking. `RECEPTIONIST` vẫn ghi `staff_attendances`. Test xác nhận `staffAttendanceService` không bị gọi cho PT |
| Response thêm `earlyLeave`, `reasonRequired` | `FaceAttendanceResponse` thêm 2 field. `earlyLeave` chỉ bật khi `type == CHECK_OUT` (phòng thủ nếu Booking trả cờ này cho CHECK_IN); `reasonRequired = earlyLeave` |
| PT không có buổi phù hợp → thông báo, không phải lỗi | `Optional.empty()` → `action = "NONE"`, `bookingLinked = false`, `message = "Không có buổi nào cần điểm danh"`. Không ném exception |
| Sửa test `StaffAttendanceServiceImplTest` dính TRAINER | Test này vốn không kiểm tra role nào (service không phân biệt role), chỉ đổi tên user mẫu `"PT Test"` → `"Receptionist Test"` và ghi chú G3. Phần test thực sự đổi nằm ở `FaceAttendanceServiceImplTest` (test cũ `staffScan_trainer_...` bị thay bằng 6 test luồng PT mới) |
| PT dạy 2 buổi liền: chống quét đúp Redis không được chặn lượt thứ 2 | PT dùng cửa sổ riêng `gms.face-attendance.trainer-min-scan-interval-seconds` (mặc định **15s**, member/receptionist vẫn 120s). Có test kiểm tra đúng `Duration` 15s cho PT, 120s cho member |

## 2. Thêm ngoài change request (cần xác nhận, dễ gỡ nếu không đồng ý)

1. **Response thêm `bookingId` và `message`.** Change request chỉ nêu `earlyLeave`/`reasonRequired`, nhưng FE cần `bookingId` để gọi
   `PUT /api/v1/bookings/{id}/early-leave-reason` — nếu không có, FE phải tự đoán buổi vừa quét ra. `message` chứa thông báo "không có buổi nào cần điểm danh".
2. **`GET /api/v1/staff-attendance/me` đổi từ `TRAINER, RECEPTIONIST` → chỉ `RECEPTIONIST`.** Vì PT không còn bản ghi `staff_attendances`, để PT gọi chỉ trả danh sách rỗng gây hiểu nhầm.
   Cần Agent 1 cập nhật `API_DESIGN.md` mục Face Attendance & Chấm công cho khớp.

## 3. Giả định về code Agent 3 — ĐÃ ĐỐI CHIẾU VỚI CODE THẬT (2026-10-12), không cần Agent 3 xác nhận thêm

Đối chiếu với `origin/feature/agent3-schedule-booking`: `recordTrainerFaceScan`, `TrainerScanResult(booking, type, earlyLeave)`
và `BookingResponse.getId()` đều đúng như giả định, **trừ một điểm**: `TrainerScanResult` là record riêng ở
`com.gym.management.dto.response`, không lồng trong `BookingService`. Đã sửa import trong file test, code chính không bị ảnh hưởng.
Merge tạm nhánh Agent 3 vào local: `mvn clean verify` BUILD SUCCESS, 111 file compile, 95 test pass
(gồm 16 test của `FaceAttendanceServiceImplTest`). Đã push commit `8153200` lên PR.

Bảng giả định ban đầu, giữ lại để tham khảo:

| Giả định | Chỗ dùng | Nếu sai |
|---|---|---|
| `TrainerScanResult` là record **lồng trong `BookingService`** (`BookingService.TrainerScanResult`) | Chỉ ở file test (`import ...BookingService.TrainerScanResult`). Code chính dùng `var` nên không phụ thuộc vị trí | Sửa 1 dòng import trong `FaceAttendanceServiceImplTest` |
| Accessor của record là `booking()`, `type()`, `earlyLeave()`; `type()` trả đúng chuỗi `"CHECK_IN"` / `"CHECK_OUT"` | `FaceAttendanceServiceImpl.processTrainerScan` | Sửa tên method gọi |
| `BookingResponse` có `getId()` | `result.booking().getId()` | Sửa 1 chỗ |
| `recordTrainerFaceScan(UUID, LocalDateTime)` trả `Optional<TrainerScanResult>` | như trên | Sửa chữ ký gọi |

**Lưu ý hệ quả:** nhánh Agent 4 sẽ **không compile** cho đến khi PR Agent 3 (bản đã sửa theo change request) merge vào `master`, vì `recordTrainerFaceScan` chưa tồn tại. Tương tự lần trước, Agent 4 sẽ tự merge tạm nhánh Agent 3 để test local, không push bản merge tạm.

## 4. Cấu hình (Agent 1 thêm vào `application*.yml` khi tiện — code có sẵn giá trị mặc định nên không chặn build)

```yaml
gms:
  face-attendance:
    trainer-min-scan-interval-seconds: 15   # mới: cửa sổ chống quét đúp riêng cho PT
```

**Cần Phi xác nhận 15s:** đủ ngắn để PT quét ra rồi quét vào buổi liền sau, đủ dài để lọc việc FE gửi trùng 1 lần quét. Nếu quy trình thực tế có PT quét 2 lần liên tiếp nhanh hơn 15s, giảm giá trị này.

## 5. Điểm Agent 4 chuyển tiếp, không tự xử lý

- Ràng buộc G6 (PT chỉ check-in được trong `[S-30, S+15]`), chọn booking theo thứ tự check-out trước check-in, `applyLazyStatus` trước khi chọn: toàn bộ do Agent 3 thực hiện trong `recordTrainerFaceScan`. Agent 4 chỉ chuyển kết quả ra response.
- Khi PT quét ngoài khoảng cho phép → Agent 3 trả `Optional.empty()` → Agent 4 hiện thông báo, không phân biệt được "quá sớm" với "không có buổi". Nếu FE cần phân biệt hai trường hợp này, Agent 3 cần trả thêm lý do trong kết quả.

## 6. Lưu ý reset DB local

`V3__schedule_booking.sql` (Agent 3) và `V4__face_attendance.sql` (Agent 4, vừa thêm `updated_at` cho `staff_attendances`) đều được sửa trực tiếp khi chưa merge. Ai đã chạy Flyway local với bản cũ sẽ lỗi checksum → `docker compose down -v` rồi chạy lại. Unit test không cần DB nên không bị ảnh hưởng.

## 7. Chưa làm / ngoài phạm vi

- Swagger annotation trên 3 controller (vẫn là nợ từ trước).
- Test tích hợp với `@SpringBootTest` (chưa có — nên có để bắt các lỗi entity/migration không khớp như vụ `updated_at`).
