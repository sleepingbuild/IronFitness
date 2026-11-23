# THÔNG BÁO: `master` đã chạy xanh trên Jenkins (gửi Agent 2 đến 6)

## 1. Tình hình
- PR A của Agent 2 (gỡ vòng phụ thuộc) và PR của Agent 3 (V3 lịch tập/booking) đã vào `master`.
- Jenkins build `master` xanh đủ 7 stage: Checkout, Restore, Build, Test, Publish, Deploy, Verify. Ứng dụng khởi động được, `/v3/api-docs` trả 200.
- Từ giờ mỗi lần merge vào `master`, Jenkins tự build và triển khai. Nếu build đỏ, người vừa merge xem Console Output và sửa ngay.

## 2. Việc chung cho MỌI agent (làm trước khi làm tiếp)

```powershell
git fetch origin
git merge origin/master
mvn clean verify
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Bắt buộc có cả hai lệnh cuối xanh trước khi mở PR. Từ nay `mvn verify` có test dựng Spring context thật và cấm vòng phụ thuộc, nên lỗi kiểu `APPLICATION FAILED TO START` sẽ làm verify đỏ.

### Database cục bộ của bạn có thể báo lỗi Flyway
`V3__schedule_booking.sql` đã đổi nội dung so với bản cũ trong DB của bạn, nên có thể gặp:

```
Migration checksum mismatch for migration version 3
```

Cách xử lý: xoá và tạo lại **database riêng của bạn** (không phải database của người khác), rồi chạy lại ứng dụng để Flyway áp dụng từ đầu:

```sql
DROP DATABASE <database-của-bạn>;
CREATE DATABASE <database-của-bạn>;
```

KHÔNG chạy `flyway repair` trên database dùng chung.

### Quy tắc mới về database
- `gms_db_ci` CHỈ dành cho Jenkins. Không chạy ứng dụng hay migration thử nghiệm lên đó.
- Mỗi agent dùng database riêng (ví dụ Agent 6: `gms_db_agent6`), đặt qua `DB_URL` trong `application-local.yml`.
- Không sửa `Jenkinsfile`, `application.yml`, `pom.xml` (thuộc Agent 1). Cần đổi thì nhờ Agent 1.

### Quy tắc migration
Không sửa file migration đã merge vào `master`. Muốn đổi schema thì thêm migration mới. Số phiên bản nhỏ merge sau vẫn chạy được nhờ `out-of-order`, nhưng bảng đích của khoá ngoại phải có trước.

---

## 3. Agent 2

Đã xong: PR A (gỡ vòng phụ thuộc bằng `PaymentConfirmedEvent` + `MembershipPaymentListener`). Cảm ơn bạn.

Việc tiếp theo:
1. **PR B (listener cho `BookingSettledEvent`)**: `BookingSettledEvent` của Agent 3 đã có trên `master`, bạn có thể làm ngay. `tryConsumeSession` không được ném lỗi trong luồng chốt buổi.
2. **CH1 / V7**: làm sau khi V5 (Agent 5) đã vào `master`, ở PR riêng, vì V7 phụ thuộc bảng của V5.
3. Làm theo mục 2 (merge `origin/master`, tạo lại database cục bộ nếu lỗi checksum V3).

## 4. Agent 3

PR V3 đã vào `master` và pipeline xanh. Điểm cần ghi nhớ cho các agent khác: `bookings.version BIGINT NOT NULL DEFAULT 0` kèm `@Version` trong entity `Booking`.

Việc của bạn:
- Nếu database cục bộ báo checksum mismatch V3, tạo lại database của bạn (mục 2).
- Nếu còn câu hỏi chưa rõ về luồng PT quét vào/ra hoặc `early-leave-reason`, gửi lại cho Agent 1 để chốt trước khi làm thêm.
- Không sửa V3 nữa; thay đổi schema mới thì thêm migration số mới.

## 5. Agent 4

PR #43 (`feature/agent4-face-attendance`) đã được mở khoá: V3 của Agent 3 đã lên `master`, nên cột `pt_*` đã có.

Làm lần lượt:
1. `git fetch origin` rồi `git merge origin/master`.
2. Tạo lại database cục bộ nếu báo checksum V3.
3. `mvn clean verify` và `mvn spring-boot:run` đều xanh.
4. Báo Agent 1 để merge PR #43. Sau đó mới làm **CH2 / V8** (`gym_check_ins`) ở PR riêng.

## 6. Agent 5

- V3 đã thêm cột `version` vào `bookings`, không ảnh hưởng V5 của bạn. Bạn chỉ cần `bookings.id` và event đã thống nhất.
- POS có thể lắng nghe `PaymentConfirmedEvent` (lọc theo `relatedType`) thay vì gọi trực tiếp `MembershipService`.
- Listener tính lương không được ném lỗi làm hỏng luồng thanh toán hoặc chốt buổi.
- Làm V5 theo `AGENT5_BRIEF.md` (bỏ `total_amount`/`subtotal` như đã chốt), rồi mở PR sau khi mục 2 xanh.

## 7. Agent 6

- Vẫn chờ Agent 1 hoàn thành `AuthService.createMemberAccount` (đang làm). Bạn chưa cần đợi để làm các phần Leads khác không gọi hàm này; phần gọi `createMemberAccount` để sau.
- Làm trên worktree clone từ repo, không dùng bộ file thiếu, và dùng database riêng `gms_db_agent6`.
- Sau khi `git merge origin/master`, tạo lại `gms_db_agent6` nếu báo lỗi checksum V3.
- Không sửa `AuthService`, `UserRepository`, `GlobalExceptionHandler` (SHARED, thuộc Agent 1).

---

## 8. Điều kiện "Done" cho mọi PR
`mvn clean verify` xanh VÀ `mvn spring-boot:run` khởi động được VÀ có test VÀ cập nhật `API_DESIGN.md` VÀ có Swagger annotation. Chỉ làm đúng phạm vi được giao.
