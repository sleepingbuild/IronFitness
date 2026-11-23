# Agent 4 — Face Attendance & Staff Attendance — Ghi chú bàn giao

**Trạng thái: `mvn clean verify` → BUILD SUCCESS (2026-09-30, 10.071s).** Sẵn sàng để Agent 1 review PR.

Code viết dựa trên file thật đã nhận: `BaseEntity`, `User`, `Role`, `Booking`, `ErrorCode`,
`ApiResponse`, `BusinessException`, `GlobalExceptionHandler`, `CurrentUserId`,
`CurrentUserRole`, `BookingService`, `BookingController`, `application-local.yml`.
**Không tạo/sửa bất kỳ file SHARED nào.**

## 1. Danh sách file mới (tất cả thuộc khu vực sở hữu của Agent 4)

| File | Ghi chú |
|---|---|
| `db/migration/V4__face_attendance.sql` | Bảng `face_profiles` (UNIQUE `user_id`), `staff_attendances` (UNIQUE `staff_id, date`) |
| `entity/FaceProfile.java` | `descriptor` NVARCHAR(MAX) JSON 128 số; `reference_image` là path/URL |
| `entity/StaffAttendance.java` | Trạng thái KHÔNG lưu cột, tính động lúc đọc |
| `repository/FaceProfileRepository.java`, `StaffAttendanceRepository.java` | |
| `integration/hardware/FaceMatchProperties.java` | Đọc `gms.face-attendance.match-threshold` (đã có sẵn trong yml, 0.45) |
| `integration/hardware/FaceMatchService.java` + `FaceMatchServiceImpl.java` | Euclidean 1:N và 1:1, server tự quyết định khớp |
| `dto/request/FaceDescriptorRequest.java` | Validate đúng 128 chiều |
| `dto/response/FaceProfileResponse.java`, `FaceAttendanceResponse.java`, `StaffAttendanceResponse.java` | |
| `service/FaceProfileService.java` + `impl/FaceProfileServiceImpl.java` | Đăng ký lại = ghi đè |
| `service/FaceAttendanceService.java` + `impl/FaceAttendanceServiceImpl.java` | Route theo role người vừa nhận diện: MEMBER → `BookingService.recordFaceScan`, TRAINER/RECEPTIONIST → `StaffAttendanceService` |
| `service/StaffAttendanceService.java` + `impl/StaffAttendanceServiceImpl.java` | Lần 1 = check-in, lần 2 = check-out, lần 3 = lỗi; trạng thái ON_TIME/LATE/LEFT_EARLY/LATE_AND_LEFT_EARLY/ABSENT tính động |
| `controller/FaceProfileController.java`, `FaceAttendanceController.java`, `StaffAttendanceController.java` | Phân quyền bằng `@PreAuthorize`, lấy user bằng `CurrentUserId.get()` |

## 2. Điểm tích hợp với agent khác

- **Agent 3:** chỉ gọi `BookingService.recordFaceScan(memberId, scanTime)`, không đụng
  `BookingRepository`/entity `Booking`. `Optional.empty()` được coi là trường hợp hợp lệ
  (`bookingLinked=false`), không phải lỗi.
- **Agent 1:** dùng `BusinessException`, `ApiResponse`, `ErrorCode.FACE_*` có sẵn (không cần PR `[shared]`).
- **Agent 2:** không gọi `MembershipService` (chờ xác nhận, xem `AGENT2_AND_3_TO_CONFIRM.md`).

## 3. Quyết định thiết kế (đã chốt cùng Phi/Agent 1/Agent 2/Agent 3)

1. **Không tin client:** server tự tính khoảng cách Euclidean; endpoint chỉ nhận descriptor.
2. **Chống quét đúp bằng Redis** (A.3): `StringRedisTemplate.setIfAbsent("face-scan:"+userId, ...,
   min-scan-interval-seconds=120)` — thay hoàn toàn `userLocks` in-memory cũ. Đúng cả khi race
   đồng thời lẫn khi 2 request cách nhau vài giây. Không cần Agent 1 cấu hình gì thêm (Redis đã
   có sẵn cho JWT blacklist).
3. **Ca chấm công mặc định toàn hệ thống** (07:00–21:00) — giữ nguyên cho v1 (A.2). Cải tiến sau
   (không bắt buộc): đọc ca thật từ `trainer_schedules` qua 1 port do Agent 3 cung cấp.
4. **Đăng ký hộ bằng ảnh tĩnh** (A.1): KHÔNG có endpoint multipart, KHÔNG xử lý ảnh phía server.
   `face-api.js` trích descriptor được từ cả `<img>` lẫn webcam — Lễ tân chọn file, JS tự trích,
   backend chỉ nhận JSON descriptor giống hệt luồng tự đăng ký. `registerForWithImage()` đã bị
   xoá khỏi interface/impl.
5. **`reference_image` luôn `null`** (A.4): v1 không lưu ảnh, chỉ lưu descriptor — server chưa
   từng thấy ảnh gốc. Cột giữ nullable trong entity/migration, không dùng đến.
6. **Không gọi `MembershipService` ở đâu cả** — Agent 2 xác nhận: trừ buổi (`consumeSession`)
   xảy ra ở `BookingServiceImpl.completeBooking()` (Agent 3), không phải lúc face check-in.

## 4. Cấu hình bổ sung

```yaml
gms:
  face-attendance:
    default-shift-start: "07:00"           # chưa có trong yml, code có giá trị mặc định
    default-shift-end: "21:00"
    min-scan-interval-seconds: 120          # Agent 1 đã thêm vào application.yml.example
```

## 5. Chưa làm / để dành

- Bảng `staff_attendances` chưa có API chấm công thủ công (`method = MANUAL`).
- Pagination chuẩn chưa áp dụng (giống Agent 2/3).
- Cải tiến ca chấm công theo `trainer_schedules` thật (A.2) — không bắt buộc trong 10 tuần.
- Test cho `FaceProfileServiceImpl` — chưa viết (ưu tiên thấp hơn 3 bộ đã có vì logic đơn giản
  hơn, chủ yếu CRUD + serialize JSON).
- `mvn clean verify` mới chạy **local, 1 lần**, chưa qua CI — nên chạy lại sau khi rebase lên
  `main` mới nhất trước khi merge thật, phòng trường hợp Agent 2/3 đổi code sau các file
  `AGENT*_RESPONSE_TO_AGENT4.md` đã gửi.

## 8. Ghi chú về test

- `FaceAttendanceServiceImplTest` dùng 1 `TransactionTemplate` giả (`PassThroughTransactionTemplate`)
  chạy thẳng callback, không cần `DataSource` thật — chỉ kiểm tra logic routing (MEMBER →
  Booking, TRAINER/RECEPTIONIST → StaffAttendance) và logic dedup Redis (mock
  `StringRedisTemplate`/`ValueOperations`), không phải integration test thật.
- Test bao phủ: không nhận diện được mặt, không khớp mặt tự đăng nhập, quét đúp trong
  `min-scan-interval-seconds` (trả `duplicateScan=true`, không gọi Booking/StaffAttendance),
  check-in/check-out cho Member lẫn Trainer/Receptionist, không có booking hôm nay (không phải
  lỗi), role không hỗ trợ.
- `StaffAttendanceServiceImplTest` bao phủ đủ 5 trạng thái động (`ON_TIME`, `LATE`,
  `LEFT_EARLY`, `LATE_AND_LEFT_EARLY`, `ABSENT`) và lỗi khi quét lần 3 trong ngày.

## 6. Giả định đã được xác nhận (không còn là giả định)

- `BookingResponse.getCheckOutTime()` — ĐÚNG (Agent 3 xác nhận), dùng để phân biệt CHECK_IN/CHECK_OUT.
- `roles.name` lưu `"MEMBER"`/`"TRAINER"`/`"RECEPTIONIST"` in hoa, không tiền tố `ROLE_` — ĐÚNG (Agent 1 xác nhận).
- `UserRepository` có sẵn `findById`/`getReferenceById` chuẩn Spring Data — ĐÚNG (Agent 1 xác nhận).
- `pom.xml` không có thư viện ảnh/ML — ĐÚNG (Agent 1 xác nhận), khớp hướng A.1 đã chọn.

## 7. Lịch sử sửa lỗi

- `@Transactional` trên `doProcessScan()` bị self-invocation nên vô hiệu → thay bằng
  `TransactionTemplate`.
- `userLocks` (`ConcurrentHashMap` + `synchronized`, chỉ đúng 1 instance, không dọn dẹp) →
  thay bằng Redis dedup theo quyết định A.3 của Phi/Agent 1.
- `registerForWithImage()` (từng ném `UnsupportedOperationException`/`FACE_IMAGE_INVALID`) →
  xoá hẳn theo quyết định A.1.
