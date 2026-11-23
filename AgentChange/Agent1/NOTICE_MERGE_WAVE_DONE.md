# THÔNG BÁO: các đợt merge đã xong và việc tiếp theo (gửi Agent 2 đến 6)

Gửi **sau khi** Agent 1 đã chạy lệnh đối chiếu (mục 1) và sửa bảng dưới cho khớp thực tế.

## 1. Trạng thái `master`

| Hạng mục | Chủ | Trạng thái |
|---|---|---|
| Khung frontend (Next.js) kèm bản sửa làm mới token | Agent 1 | Đã vào `master` |
| Gói tập công khai (`security-public-packages`) | Agent 2 | Đã vào `master` |
| Mã lỗi `PAYMENT_NOT_REFUNDABLE` | Agent 2 | Đã vào `master` |
| Tra gói của hội viên khác (`membership-lookup`) | Agent 2 | Đã vào `master` |
| `@PreAuthorize` cho `trainer-schedules/available`, seed `20_bookings.sql` | Agent 3 | Đã vào `master` |
| `GET /face-profiles/me` | Agent 4 | Đã vào `master` |
| V8 `gym_check_ins`, `GET /check-ins/me` | Agent 4 | Đã vào `master` |
| V5 POS và lương PT theo buổi | Agent 5 | Đã vào `master` |
| Màn hình frontend của Agent 3 (ca làm việc, buổi dạy, buổi tập của tôi) và Agent 4 | Agent 3, 4 | Đã vào `master` |
| `GET /users`, `/trainers`, `/branches` và 401/403 JSON | Agent 1 | **Chờ merge** |
| `00_users.sql` và `docs/TEST_ACCOUNTS.md` | Agent 1 | **Chờ merge** |
| Ba mã lỗi POS (`POS_ORDER_NOT_FOUND`, `COMMISSION_NOT_FOUND`, `TRAINER_PAY_RATE_NOT_FOUND`) | Agent 1 | **Chờ PR [shared]** |
| V7 (tách `related_id` của giao dịch) | Agent 2 | **Chờ**, xem mục 3 |

## 2. Việc chung cho mọi agent

```powershell
git fetch origin
git merge origin/master
mvn clean verify
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
cd frontend; npm ci; npm run dev
```

- Database riêng của bạn tự áp dụng V5 và V8 khi khởi động. Nếu báo lỗi Flyway, tạo lại **database riêng của bạn** (không dùng `gms_db_ci`, không `flyway repair`).
- Người chưa đăng nhập sẽ nhận **401 JSON** (`AUTH_TOKEN_INVALID`) thay vì 403 rỗng sau khi PR của Agent 1 merge. `apiFetch` đã xử lý cả hai, không phải sửa gì.
- Cả hai đợt frontend đều mở. Mỗi agent cần có **ít nhất một màn hình chạy được** trên khung, kèm dữ liệu mẫu, để chủ dự án thử bằng mắt. Báo lỗi theo mẫu ở `NOTICE_FRONTEND_PHASE.md` mục 6.

### Dữ liệu thử
Sau khi PR seed của Agent 1 merge: `scripts/dev-seed/00_users.sql` và `docs/TEST_ACCOUNTS.md` (mật khẩu chung `Gym@12345`, tám tài khoản cho đủ vai trò, bảng ở file đó). Mỗi agent viết file seed của mình (`10_`, `20_`, `40_`, `50_`, `60_`) theo quy tắc: chạy lại không lỗi, tham chiếu user theo email, ngày giờ tương đối theo giờ Việt Nam.

## 3. Việc của từng agent

### Agent 2
1. **V7:** chưa merge được. Cần trước khi merge: (a) điền phần bằng chứng còn trống trong mô tả PR, đặc biệt kết quả chạy trên database **có dữ liệu cũ**; (b) Agent 6 xem và xác nhận phần sửa trong `ReportRepository`; (c) Agent 1 chạy kiểm tra dữ liệu trên `gms_db_ci`. Chi tiết ở phản hồi riêng cho Agent 2 bên dưới.
2. Sau V7: `PaymentService.refundByRelated(RelatedType, UUID)` (tên đã chốt, thay cho `refundPaymentByPosOrder` trong mô tả V7), bộ lọc giao dịch H3, rồi hoàn tiền.
3. Seed `10_packages_memberships.sql`; màn hình gói tập (công khai, đăng ký gói, gói của tôi) trên khung frontend.
4. H4: xác nhận hành vi khi bỏ trống `branchId` để quyết định giao diện có bắt chọn chi nhánh không.

### Agent 3
1. Đặt lịch của hội viên cần `GET /trainers` (chờ PR của Agent 1); đặt hộ của lễ tân cần thêm `/users` và `/branches`.
2. Chạy lại `20_bookings.sql` với tài khoản chính thức sau khi có `00_users.sql`, báo kết quả.
3. Thử cùng Agent 5 luồng: PT quét vào, quét ra, hoàn thành buổi, kiểm tra bản ghi lương.
4. Chuỗi PR frontend còn lại merge theo thứ tự đã báo.

### Agent 4
1. Màn hình đợt 1: đăng ký khuôn mặt, tự điểm danh, kiosk, chấm công, lịch sử check-in (V8 đã vào).
2. Kiểm tra mã ngưỡng so khớp `0.45` với vài khuôn mặt thật; ghi kết quả.
3. Nếu chưa có: thêm dòng `GET /face-profiles/me` vào `API_DESIGN.md`.

### Agent 5
1. V5 đã vào: làm màn hình POS cho đơn có hội viên (đơn khách vãng lai chỉ chạy sau V7); seed `50_pos.sql`.
2. Thử listener lương PT với booking thật của Agent 3.
3. Đổi sang ba mã lỗi mới sau khi PR [shared] của Agent 1 merge. Huỷ đơn gọi `refundByRelated` sau khi Agent 2 làm xong.

### Agent 6
1. `checkin-traffic` đã có nguồn `gym_check_ins` (V8): cập nhật SQL sang hai nguồn như `NOTICE_AGENT4_CH2_V8`.
2. `commissions-summary` đã được mở khoá (V5 vào): làm endpoint với `CommissionService.summarize`.
3. **V7 sửa hai truy vấn trong `ReportRepository` của bạn** (`revenueByDay`, `topPackages`): xem phần thay đổi trong PR V7 của Agent 2 và xác nhận trước khi merge.
4. Màn hình Leads có thể bắt đầu; báo cáo bắt đầu sau khi V7 vào. Chạy lại bản rà Swagger sau khi controller POS và hoa hồng đã vào `master`. Seed `60_leads_reports.sql`.

### Agent 1 (đang làm)
Merge PR danh mục (`/users`, `/trainers`, `/branches`, 401 JSON), PR seed người dùng, PR ba mã lỗi POS; duyệt V7; sau đó: nâng cấp Next.js, thêm stage frontend vào Jenkins, gom nhóm Swagger theo module.

## 4. Mốc tiếp theo
Mỗi agent có một màn hình chạy được kèm dữ liệu thử; chủ dự án thử từng module và gửi lỗi theo mẫu; các agent sửa theo phản hồi. Điều kiện Done của mọi PR không đổi.
