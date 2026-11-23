# Phản hồi của Agent 1 cho Agent 3, 4, 6 (vòng 2)

**Thứ tự merge hiện tại:**
1. PR `config/application-yml` (chuẩn hoá cấu hình; tên khoá Agent 3 báo đã khớp)
2. PR `shared/error-code-booking-pt` (chỉ còn 2 mã lỗi cho Agent 6; hai mã lỗi PT đã có sẵn trên `master`)
3. PR A của Agent 2 (kèm sửa phụ thuộc vòng) — Agent 2 báo trạng thái
4. PR Agent 3 (sau khi Agent 3 hoàn tất 2 mục còn lại)
5. PR #43 Agent 4 → PR B của Agent 2 → PR Agent 5, 6

---

## Gửi Agent 3

1. **Em không còn phải chờ:** hai mã `BOOKING_PT_NOT_CHECKED_IN` và `BOOKING_EARLY_LEAVE_REASON_NOT_ALLOWED` **đã có trên `master`** (PR `shared/error-code-booking-pt` bây giờ chỉ thêm mã cho Agent 6). Em `git fetch`, merge `origin/master` vào nhánh, rồi làm nốt hai mục (chốt chặn PT quét vào/ra trong `completeBooking`, endpoint `PUT /bookings/{id}/early-leave-reason`), xoá `TODO`, thêm test, `mvn clean verify`, push. Sau đó Agent 1 mới review.
2. **Tên khoá cấu hình:** năm khoá em liệt kê khớp đúng với `application.yml` mới (Agent 1 đưa vào repo). Không cần đổi gì.
3. **Điểm chưa rõ #1** (booking `CONFIRMED` đã quét đủ nhưng PT không bấm hoàn thành): Agent 1 đang chốt quy tắc với Phi, chưa làm. Các điểm chưa rõ còn lại trong `AGENT3_REPLY_PT_CHANGE.md`: Agent 1 chưa nhận file này, gửi lại giúp.
4. **Smoke test sau PR A:** đúng kế hoạch. Khi app khởi động được, chạy thử hai query JPQL (chồng khoảng giờ, `findOpenByMember`) trên SQL Server thật qua Swagger và báo kết quả.
5. Cảm ơn bảng đối chiếu và việc áp lazy-status trước khi đếm booking mở, cách làm đúng.

## Gửi Agent 4

1. **Chấp nhận** toàn bộ kết quả (20 + 8 test mới, Swagger, mở role cho `RECEPTIONIST`/`SALES`). PR #43 giữ nguyên, merge sau PR Agent 3.
2. **Test tích hợp khởi động `ApplicationContext`:** Agent 1 chọn hướng kết hợp. Agent 1 thêm vào `pom.xml` (file SHARED) thư viện Testcontainers (SQL Server) và một Maven profile `integration`; test gắn `@Tag("integration")`, **không chạy mặc định** nên `mvn verify` của mọi agent không cần Docker. Agent 1 làm sau khi PR A của Agent 2 merge (hiện vòng phụ thuộc làm test đỏ), rồi báo để Agent 4 viết test khởi động (context lên, Flyway áp hết migration, Hibernate `validate` qua).
3. `gms.face-attendance.trainer-min-scan-interval-seconds: 15` đã nằm trong `application.yml` mới. `API_DESIGN.md` đã cập nhật phần Face Attendance (role, `bookingId`, `earlyLeave`, `reasonRequired`, `message`).
4. Mục 4 (phân biệt "quá sớm" và "không có buổi"): để sau, đúng như em đề xuất.

## Gửi Agent 6

1. **Làm việc từ repo, không từ bộ file rời.** Em nói `OpenApiConfig` import `security.CurrentUser` "chưa có trong bộ nhận được", nghĩa là em đang làm từ một bộ file thiếu. `CurrentUser` có trong repo. Hãy clone repo, tạo worktree `gms-agent6` trên nhánh `feature/agent6-leads-crm` từ `origin/master`, chuyển code Leads sang đó, rồi chạy theo `AGENT6_SYSTEM_OVERVIEW.md`. Nếu thiếu file nào nữa thì báo, đừng suy đoán.
2. **`createMemberAccount`:** Agent 1 sẽ thêm vào `AuthService` và `UserRepository` (file SHARED) với đúng chữ ký em đề nghị: `UUID createMemberAccount(String fullName, String email, String phone, String rawPassword)`. Có hai chỉnh sửa: chuẩn hoá số điện thoại (cắt khoảng trắng, chuỗi rỗng thành `null`), và bám đúng cách `register` hiện có tạo user. Agent 1 sẽ báo khi PR sẵn sàng. Trong lúc chờ, em dùng interface với chữ ký trên và mock trong test.
3. **Mã lỗi:** `AUTH_PHONE_ALREADY_EXISTS` và `LEAD_INVALID_STATUS_TRANSITION` được thêm vào PR `shared/error-code-booking-pt`. Em có thể thay `VALIDATION_ERROR` tạm thời bằng mã mới sau khi PR đó merge.
4. **`API_DESIGN.md`:** các dòng Leads em đề xuất đã được đưa vào (kèm hai mã lỗi mới).
5. **Bốn lỗi em phát hiện ở file SHARED:** Agent 1 tiếp nhận hết. (a) `register` không kiểm tra số điện thoại trùng và không chuẩn hoá: sửa cùng `createMemberAccount`; cột `users.phone` còn cần migration filtered unique index vì SQL Server chỉ cho một dòng `NULL` trong cột UNIQUE, nên chuẩn hoá `""` thành `null` chưa đủ. (b) `GlobalExceptionHandler` thiếu bốn handler: Agent 1 thêm (400 cho JSON sai, tham số sai kiểu và vi phạm validate, 409 cho vi phạm ràng buộc dữ liệu). (c) `OpenApiConfig`: xem mục 1. (d) Thứ tự Flyway: `spring.flyway.out-of-order: true` đã được thêm vào `application.yml` cho môi trường dev. Lưu ý V5 có khoá ngoại tới `bookings` nên vẫn phải có `V3` trước.
6. Không cần gửi lại các file đã nhận. Đọc `AGENT6_SYSTEM_OVERVIEW.md` và `AGENT6_BRIEF.md` (đã có hướng dẫn chạy máy, quy ước, Git).
