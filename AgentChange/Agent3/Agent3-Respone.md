# Phản hồi cho Agent 3 (từ Agent 1 + Phi)

## 1. Bảng giả định (mục 1 trong README của bạn) — TẤT CẢ ĐÚNG

- `BaseEntity`: `id (UUID)`, `createdAt`/`updatedAt` kiểu `LocalDateTime` (không phải `Instant`), auditing tự động. Getter chuẩn Lombok.
- `User.getId()`, `getFullName()` — đúng.
- `UserRepository`, `BranchRepository` đã tồn tại sẵn — đúng, và đã sửa xong lỗi Agent 2 từng ghi đè mất 2 method của `UserRepository`.
- `security/CurrentUserRole.java` — **giờ đã có bản thật**, push cùng đợt với `CurrentUserId`. Trả về tên role **không** có tiền tố `ROLE_` (vd `"MEMBER"`, `"TRAINER"`), đúng như bạn giả định.
- `@PreAuthorize("hasRole('TRAINER')")` với prefix `ROLE_` chuẩn Spring Security — đúng.
- `ApiResponse.success(data)` / `success(message, data)` — đúng.
- `BusinessException(ErrorCode, String)` — đúng.
- 8 mã lỗi `SCHEDULE_TIME_INVALID`/`BOOKING_*` — đã có sẵn trong `ErrorCode.java`, không cần PR `[shared]`.

## 2. Việc phải làm ngay

Xoá file tạm `security/CurrentUserId.java` (bản của Agent 2, bạn đang tái sử dụng) và tạo file tạm `CurrentUserRole.java` nếu có — thay bằng 2 file thật đã push lên `master`. Cách gọi (`CurrentUserId.get()`, `CurrentUserRole.get()`) **giữ nguyên**, không phải sửa chỗ gọi nào.

## 3. Trả lời 4 điểm cần Phi xác nhận (mục 2 trong README của bạn)

1. **Bắt buộc có gói ACTIVE mới đặt được lịch PT** — Duyệt.
2. **`NO_SHOW` ưu tiên hơn `PT_NO_SHOW`** khi cả 2 điều kiện cùng đúng — Duyệt, đúng logic (lỗi thuộc về member chưa check-in thì không thể đổ cho PT).
3. **Slot mặc định 60 phút**, qua `gms.booking.slot-duration-minutes` — Duyệt. Key đã có trong `application.yml.example` (`gms.booking.*`), dùng đúng tên đó.
4. **`updateBooking` cho đổi cả trainer/ngày/giờ, chỉ giữ `memberId`** — Duyệt.

## 4. Điểm tích hợp với Agent 4 và Agent 5 — cả 2 agent đã đọc và xác nhận

- **Agent 4** gọi `BookingService.recordFaceScan(UUID memberId, LocalDateTime scanTime)`, tin tưởng hoàn toàn `memberId` do Agent 4 truyền vào (không verify lại danh tính ở tầng Booking) — đúng kiến trúc, giữ nguyên.
- **Agent 5** dùng `BookingCompletedEvent` (push qua Spring `ApplicationEvent`) — xác nhận cách này, không cần thêm `findCompletedSince()`.

## 5. Cấu hình — dùng đúng khối `gms.booking` đã có sẵn

4 key bạn đề xuất (`min-lead-minutes`, `slot-duration-minutes`, `pending-cancel-threshold-minutes`, `pt-no-show-grace-minutes`) đã được thêm đúng tên vào `application.yml.example`, nằm trong khối `gms:` sẵn có — không cần tự thêm, chỉ cần đọc đúng key khi `@Value`.

## 6. Việc còn thiếu trước khi coi Issue 3.x là "Done"

- Test riêng từng rule (đặc biệt lazy auto-status với `Clock` injectable để giả lập thời gian, và rule giới hạn buổi/tuần).
- `mvn clean verify` thật sau khi ghép — làm sau khi xoá xong file tạm ở mục 2.