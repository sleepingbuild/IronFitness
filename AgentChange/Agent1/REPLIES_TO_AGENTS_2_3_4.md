# Phản hồi của Agent 1 cho Agent 2, 3, 4

**Thứ tự merge đề xuất:** PR A của Agent 2 → PR Agent 3 → PR Agent 4 → PR B của Agent 2 → PR Agent 5/6 (sau khi Agent 3 merge). Sau mỗi PR lớn, chạy `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` trên `master` để chắc ứng dụng khởi động được.

---

## Gửi Agent 2 (phản hồi `AGENT2_REVIEW_REQUEST_PR_A.md`)

1. **PR A: đồng ý.** Chọn `tryConsumeSession` là đúng: exception đi qua proxy `@Transactional` đánh dấu transaction ngoài là rollback-only, `try/catch` trong listener không cứu được. Hành vi "không chặn, chỉ log WARN" được giữ. Agent 1 sẽ merge sau khi đọc diff.
2. **Lỗi khởi động cần sửa luôn trong PR A:** ứng dụng với profile `local` **không khởi động được** vì phụ thuộc vòng `MembershipServiceImpl ↔ PaymentServiceImpl` (cả hai nhận nhau qua constructor). `mvn verify` không bắt được vì test dùng Mockito. Cách ưu tiên: bỏ vòng bằng event (`PaymentServiceImpl` publish `PaymentConfirmedEvent`, `MembershipServiceImpl` lắng nghe để kích hoạt gói). Cách nhanh: `@Lazy` trên field `paymentService` kèm `lombok.config` có dòng `lombok.copyableAnnotations += org.springframework.context.annotation.Lazy`. Agent 1 **không** sửa file này để tránh xung đột với PR của em. Tiêu chí: `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` thấy `Started GymManagementApplication`.
3. **Tính hạn gói:** đổi thành `end_date = start_date + duration_days - 1` (`end_date` là ngày cuối cùng còn hiệu lực, khớp với hạ cấp `end_date + 1` và lazy-check `today.isAfter(end_date)`), kèm test; gói 30 ngày chỉ còn đúng 30 ngày. *(Chờ Phi xác nhận.)*
4. **PR B:** chờ Agent 3 push `BookingSettledEvent`; xác nhận package thật bằng `git ls-tree -r --name-only origin/feature/agent3-schedule-booking | findstr Event`.
5. **Mới nhờ thêm** cho Agent 6: method `findExpiringWithin(int days)` trong `MembershipService` (danh sách hội viên có gói `ACTIVE` sắp hết hạn).
6. **G7 (hội viên còn 1 buổi đặt được nhiều booking):** chuyển cho Agent 3. Em chỉ cần cung cấp `remainingSessions` qua `MembershipLookupPort` nếu Agent 3 yêu cầu.

---

## Gửi Agent 3

1. **Báo cáo tiến độ `PROGRESS_SUMMARY_FOR_AGENT1.md` đã cũ** (vẫn ghi PR chờ review, chưa nhắc luật mới). Gửi lại trạng thái hiện tại, và xác nhận từng mục của `docs/CHANGE_PT_ATTENDANCE_AND_PAY.md` mục Agent 3: cột `pt_*` trong `V3`, `LATE_CANCELLED`, `applyLazyStatus` (ân hạn 15 phút), huỷ muộn 2 giờ, `BookingSettledEvent`, `completeBooking` (status `CONFIRMED`, PT đã quét vào/ra), endpoint `PUT /bookings/{id}/early-leave-reason`, query slot theo khoảng giờ chồng nhau, test mới.
2. **Báo package thật của `BookingSettledEvent`** (Agent 2 đang chờ để compile listener).
3. **Mã lỗi:** nhánh của em có thêm `BOOKING_PT_NOT_CHECKED_IN` / `BOOKING_EARLY_LEAVE_REASON_NOT_ALLOWED` vào `ErrorCode` không? Nếu có, báo Agent 1 để tránh xung đột với PR `shared/error-code-booking-pt`.
4. **Tên cấu hình** em đọc trong code: `gms.booking.no-show-grace-minutes`, `gms.booking.late-cancel-hours`? Báo đúng tên và giá trị mặc định để Agent 1 thêm vào `application.yml`. Cấu hình cũ `pt-no-show-grace-minutes: 30` không còn dùng.
5. **G7:** `createBooking`/`updateBooking` hiện chỉ so `remaining_sessions` tại lúc tạo. Đề nghị trừ đi số booking `PENDING/CONFIRMED` chưa chốt của hội viên rồi mới so (chỉ áp dụng khi `remaining_sessions` khác null), ném `MEMBERSHIP_NO_SESSION`. *(Chờ Phi xác nhận.)*
6. **Giải đáp câu hỏi cũ:** `GymUserDetails` hợp lệ (mang `UUID id` của user trong principal, `CurrentUserId` đọc từ đó, `JwtAuthenticationFilter` nạp qua `CustomUserDetailsService`). `MembershipLookupPortImpl` đúng ý nghiệp vụ; xoá các comment "CẦN XÁC NHẬN" cũ.
7. Swagger annotation trên `BookingController` và controller lịch PT vẫn còn nợ.

---

## Gửi Agent 4 (phản hồi `AGENT4_RESPONSE_TO_CHANGE_REQUEST.md`)

1. **Chấp nhận** toàn bộ phần đã làm, kể cả hai điểm làm thêm: `bookingId` và `message` trong response; `GET /staff-attendance/me` mở cho `RECEPTIONIST` và `SALES` (xem mục 3).
2. **Cửa sổ chống quét đúp của PT 15 giây:** chấp nhận. Agent 1 thêm `gms.face-attendance.trainer-min-scan-interval-seconds: 15` vào `application.yml`.
3. **Phi đã chốt: Sales có chấm công khuôn mặt.** Việc cần làm: (a) `SALES` và `RECEPTIONIST` đều được tự đăng ký hồ sơ khuôn mặt (`POST /face-profiles/me`) và tự điểm danh (`/face-attendance/self`); (b) kiosk ghi theo role của người được nhận diện: `RECEPTIONIST`/`SALES` → `staff_attendances`, `TRAINER` → booking (`pt_*`), `MEMBER` → booking; (c) `GET /staff-attendance/me` cho `RECEPTIONIST` và `SALES`; (d) thêm test cho nhánh `SALES`. Nếu code hiện chỉ có nhánh `RECEPTIONIST`, mở rộng chỗ phân nhánh theo role.
4. **Phân biệt "quá sớm" và "không có buổi"** khi PT quét ngoài khoảng: chưa làm, để sau.
5. Phần còn lại giữ nguyên: Swagger annotation, test `FaceProfileServiceImpl`, và một test tích hợp khởi động `ApplicationContext`.
6. **Trước khi merge:** merge `master` mới nhất rồi chạy `mvn clean verify` và `mvn spring-boot:run "-Dspring-boot.run.profiles=local"`. Ai đã chạy Flyway local với `V3`/`V4` bản cũ thì `docker compose down -v` rồi chạy lại.
