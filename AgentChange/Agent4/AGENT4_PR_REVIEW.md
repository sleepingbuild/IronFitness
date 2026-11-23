# PR Review Request — Agent 4 (Face Attendance & Staff Attendance)

**Gửi:** Agent 1 (+ Phi đồng review, theo thoả thuận gộp)
**Nhánh đề xuất:** `feature/agent4-face-attendance`
**Build:** ✅ `mvn clean verify` — BUILD SUCCESS (10.071s, 2026-09-30)

## Mô tả ngắn

Implement module Face Attendance & Staff Attendance (Issue 4.x): đăng ký hồ sơ khuôn mặt bằng
descriptor 128 chiều, 2 luồng nhận diện (Kiosk 1:N, Tự điểm danh 1:1) dùng Euclidean distance
server-side, chống quét đúp qua Redis, chấm công PT/Lễ tân với trạng thái tính động. Không
đụng file SHARED. Toàn bộ quyết định thiết kế đã được Phi/Agent 1/Agent 2/Agent 3 xác nhận qua
`Agent4-Respone.md`, `AGENT2_RESPONSE_TO_AGENT4.md`, `AGENT3_RESPONSE_TO_AGENT4.md` — chi tiết
xem `AGENT4_README.md`.

## Acceptance Criteria (theo `ISSUES.md` Milestone 4)

- [x] Kiến trúc descriptor + Euclidean distance, server tự verify, không tin client
- [x] Ngưỡng khớp 0.45 đọc từ config (`gms.face-attendance.match-threshold`)
- [x] 3 luồng: Kiosk (1:N), Tự điểm danh (1:1), Đăng ký hộ (descriptor JSON, không ảnh — theo A.1)
- [x] Check-in/check-out thông minh: lần 1 = in, lần 2 = out — cho cả Booking (Member) và
      StaffAttendance (Trainer/Receptionist)
- [x] Chấm công động: ON_TIME/LATE/LEFT_EARLY/LATE_AND_LEFT_EARLY/ABSENT
- [x] Chống quét đúp/race-condition — Redis SETNX theo A.3, không phải in-memory lock
- [x] Migration `V4__face_attendance.sql`
- [x] Test cơ bản: `FaceMatchServiceImplTest`, `FaceAttendanceServiceImplTest`,
      `StaffAttendanceServiceImplTest`

## Migration mới

`db/migration/V4__face_attendance.sql` — bảng `face_profiles`, `staff_attendances`. Số thứ tự
đúng dải được giao (V4, Agent 4), không đụng `V2_2`/`V3` đã có.

## Xác nhận trước khi merge (theo checklist mục 7 CONTRIBUTING.md)

- [x] Build pass (`mvn clean verify`), không sửa file SHARED
- [x] Không có secret/API key commit
- [x] Migration mới, đặt tên đúng convention, chưa đụng migration cũ
- [x] Mọi response qua `ApiResponse<T>` — không trả entity/DTO trần
- [ ] Swagger annotation trên endpoint mới — **CHƯA LÀM**, cần bổ sung trước merge thật
      (`FaceProfileController`, `FaceAttendanceController`, `StaffAttendanceController` hiện
      chưa có `@Operation`/`@ApiResponses`)
- [x] Không sửa khu vực agent khác chưa xin phép

## Cấu hình cần Agent 1 xác nhận đã có trong `application.yml` thật (không phải chỉ `-local`)

```yaml
gms:
  face-attendance:
    match-threshold: 0.45                 # đã có sẵn theo bạn xác nhận trước đó
    min-scan-interval-seconds: 120         # bạn nói đã thêm vào application.yml.example — xác nhận đã có ở application.yml thật/CI chưa
    default-shift-start: "07:00"           # CHƯA có trong yml, code tự có giá trị mặc định nếu thiếu key — không bắt buộc thêm
    default-shift-end: "21:00"
```

## Việc còn nợ (không chặn merge, ghi nhận để không quên)

Xem đầy đủ ở `AGENT4_README.md` mục 5 — tóm tắt: chưa có API chấm công thủ công (MANUAL), chưa
pagination, chưa test `FaceProfileServiceImpl`, Swagger annotation (xem trên).

## File đính kèm

- `AGENT4_README.md` — changelog đầy đủ, quyết định thiết kế, lịch sử sửa lỗi
- Toàn bộ source trong `agent4-face-attendance.zip` đã gửi trước đó (18 file Java + 1 migration
  + 3 file test)
