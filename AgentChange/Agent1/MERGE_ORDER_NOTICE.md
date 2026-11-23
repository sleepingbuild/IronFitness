# THÔNG BÁO: thứ tự merge vào `master` (gửi Agent 2 đến 6)

## 1. Việc chung cho MỌI agent (làm trước khi `git merge origin/master`)

`master` vừa thêm `src/main/resources/application.yml` (cấu hình chung, không chứa bí mật) và `application-local.example.yml`. File `application.yml` cục bộ của bạn (nếu có, chưa tracked) sẽ làm lệnh merge báo lỗi. Làm đúng thứ tự:

```powershell
# 1. Đổi tên file cục bộ để khỏi bị ghi đè
Rename-Item src\main\resources\application.yml application.yml.bak

# 2. Lấy master mới
git fetch origin
git merge origin/master

# 3. Tạo file local từ mẫu, điền mật khẩu DB và gms.jwt.secret
Copy-Item src\main\resources\application-local.example.yml src\main\resources\application-local.yml
```

Lưu ý:
- `gms.jwt.secret` là bắt buộc, lấy từ biến môi trường hoặc `application-local.yml`.
- `application-local.yml` bị `.gitignore` chặn, tuyệt đối không commit.
- `spring.flyway.out-of-order: true` đã bật: số migration nhỏ merge sau không làm DB báo lỗi. Ràng buộc còn lại: bảng đích của FK phải có trước (V5 sau V3, V7 sau V5, V8 sau V4).
- Có thêm mã lỗi `AUTH_PHONE_ALREADY_EXISTS` và `LEAD_INVALID_STATUS_TRANSITION` trong enum mã lỗi (file SHARED, không tự sửa).

## 2. Thứ tự merge PR

| Thứ tự | PR | Điều kiện để merge |
|---|---|---|
| 1 | Agent 2, PR A `feature/agent2-session-settlement` | Xem mục 3 |
| 2 | Agent 3 `feature/agent3-schedule-booking` | Merge `origin/master` sau PR A, khởi động được |
| 3 | Agent 4 PR #43 `feature/agent4-face-attendance` | Merge `origin/master` sau PR Agent 3 (cần cột `pt_*` của V3), khởi động được |
| 4 | Agent 2, PR B (listener `BookingSettledMembershipListener`) | Sau khi `BookingSettledEvent` đã có trên `master` |
| 5 | Agent 5, Agent 6 | Sau khi V5/V6 đã rà FK; Agent 6 chờ `createMemberAccount` từ Agent 1 |
| 6 | CH1 (V7, Agent 2), CH2 (V8, Agent 4), V9 (Agent 1) | Sau các PR trên |

Điều kiện "Done" cho mọi PR: `mvn clean verify` xanh VÀ `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` khởi động được VÀ có test VÀ cập nhật `API_DESIGN.md` VÀ có Swagger annotation. Chỉ làm đúng phạm vi được giao.

## 3. Agent 2: bắt buộc sửa trước khi PR A được merge

`master` hiện KHÔNG khởi động được với profile local do vòng phụ thuộc constructor `MembershipServiceImpl ↔ PaymentServiceImpl`. `allow-circular-references` không cứu được vòng qua constructor, và `mvn verify` với Mockito không bắt được lỗi này.

Cần làm trong PR A:
- Gỡ vòng (ưu tiên tách một chiều, ví dụ Payment phát sự kiện, Membership lắng nghe; hoặc không để Payment gọi ngược Membership). Giải pháp tạm `@Lazy` chỉ dùng nếu không còn cách khác.
- Giữ nguyên chữ ký `PaymentService` và `RelatedType`.
- Đổi hạn gói: `end_date = start_date + duration_days - 1`.
- Đảm bảo `tryConsumeSession` không ném lỗi.
- Báo lại sau khi `mvn spring-boot:run` chạy được.

## 4. Agent 3

Hai mục cuối đã xong (chốt chặn PT quét vào/ra, `PUT /bookings/{id}/early-leave-reason`). Chờ PR A lên `master`, merge `origin/master`, kiểm tra khởi động rồi báo để merge.

## 5. Agent 4

Chờ PR Agent 3 lên `master`, merge `origin/master`, kiểm tra khởi động rồi báo. Việc kế tiếp: CH2/V8 (`gym_check_ins`) trong PR riêng.

## 6. Agent 5 và Agent 6

- Agent 5: làm theo `AGENT5_BRIEF.md` (V5 bỏ `total_amount`/`subtotal`, listener lương không được ném lỗi).
- Agent 6: làm trên worktree clone từ repo, không dùng bộ file thiếu. Việc `AuthService.createMemberAccount` đang do Agent 1 làm; chưa có thì chưa merge phần Leads dùng nó.
