# Phản hồi của Agent 1 về kế hoạch frontend đợt 1

Đã đọc kế hoạch của Agent 2, 3, 4, 6 và báo cáo Swagger. Các kế hoạch rõ ràng, nhất là việc đối chiếu màn hình với endpoint thật. Dưới đây là quyết định cho các câu hỏi, rồi thứ tự việc của Agent 1.

---

## 0. Quy ước chung (áp dụng cho mọi agent)

### 0.1 Quy tắc xử lý xác thực và lỗi trong bộ gọi API dùng chung
Bộ gọi API (Agent 1 viết) xử lý theo **`errorCode`, không theo mã HTTP**:

| Tình huống | Cách xử lý |
|---|---|
| 401 hoặc 403 **không có** `errorCode` trong body (Spring Security chặn ở tầng lọc, hiện trả 403 body rỗng) | Coi là phiên không hợp lệ: thử làm mới token một lần, thất bại thì về trang đăng nhập |
| `errorCode` là `AUTH_TOKEN_EXPIRED` hoặc `AUTH_TOKEN_INVALID` | Như dòng trên |
| `errorCode` là `AUTH_FORBIDDEN_ROLE` (403) | Hiện "Bạn không có quyền", **không** đăng xuất |
| Mọi lỗi 401 khác có `errorCode` (`FACE_NOT_RECOGNIZED`, `AUTH_INVALID_CREDENTIALS`) | **Chuyển nguyên cho trang gọi** xử lý, không chuyển về đăng nhập |

Hệ quả cho Agent 4: kiosk và trang tự điểm danh không bị đẩy ra ngoài khi quét không nhận diện được. Các lời gọi sẽ có tuỳ chọn `skipAuthRedirect` cho trang quét.

Đề xuất làm sạch hợp đồng: Agent 1 sẽ đổi để người chưa đăng nhập nhận **401 JSON** (`errorCode = AUTH_TOKEN_INVALID`) thay vì 403 rỗng, trong PR backend nhỏ ở mục 6. Trong lúc chờ, bộ gọi API chạy đúng với cả hai cách. Các agent viết giao diện theo bảng trên là đủ, không phụ thuộc vào cách nào.

### 0.2 Dữ liệu thử (`scripts/dev-seed/`)
- Mỗi agent **tự viết phần dữ liệu mẫu của module mình** (vì bạn nắm rõ schema), mỗi file một người, chạy theo thứ tự tên file:

| File | Chủ | Nội dung |
|---|---|---|
| `00_users.sql` | Agent 1 | Tài khoản theo bảng 0.3 |
| `10_packages_memberships.sql` | Agent 2 | Gói mẫu và gói của hội viên |
| `20_bookings.sql` | Agent 3 | Lịch PT và booking ở nhiều trạng thái |
| `40_attendance.sql` | Agent 4 | Chấm công mẫu (nếu cần; hồ sơ khuôn mặt thì phải đăng ký thật bằng camera) |
| `50_pos.sql` | Agent 5 | Sản phẩm và đơn mẫu (sau V5) |
| `60_leads_reports.sql` | Agent 6 | Lead mẫu và dữ liệu báo cáo (dựa trên `seed_reports.sql`) |

- Quy tắc mọi file: chạy lại nhiều lần không lỗi (`IF NOT EXISTS`); tham chiếu user **theo email** (không ghi cứng UUID); ngày giờ **tương đối** theo giờ Việt Nam, vì máy chủ SQL trong Docker chạy UTC. Dùng:
  `CAST(SYSUTCDATETIME() AT TIME ZONE 'UTC' AT TIME ZONE 'SE Asia Standard Time' AS DATETIME2)`
- **Không nằm trong migration Flyway**, không chạy tự động. Chạy thủ công trên database dev. Dùng cho `gms_db_ci` cũng được nếu chủ dự án muốn thử trên bản Jenkins triển khai.
- Ghi chú về `users.phone`: sau V9, nhiều user không có số điện thoại là hợp lệ (số khác `NULL` vẫn phải khác nhau). Lưu ý trong tin nhắn của Agent 3 về "chỉ một `NULL`" nay không còn đúng.

### 0.3 Danh sách tài khoản thử (Agent 1 tạo trong `00_users.sql`)
Mật khẩu chung cho mọi tài khoản dev: `Gym@12345` (chỉ dùng database dev, ghi trong `docs/TEST_ACCOUNTS.md`).

| Email | Vai trò | Ghi chú |
|---|---|---|
| `admin@gym.test` | SUPER_ADMIN | |
| `reception@gym.test` | RECEPTIONIST | |
| `sales@gym.test` | SALES | |
| `trainer1@gym.test`, `trainer2@gym.test` | TRAINER | Agent 3 gắn lịch làm việc |
| `member.active@gym.test` | MEMBER | Agent 2 gán gói ACTIVE (còn buổi) |
| `member.nopackage@gym.test` | MEMBER | Không có gói |
| `member.expired@gym.test` | MEMBER | Agent 2 gán gói đã hết hạn (để thử `MEMBERSHIP_EXPIRED` ở Agent 4) |

---

## 1. Agent 2

| Điểm | Quyết định |
|---|---|
| H1 tìm hội viên | **Đồng ý, Agent 1 làm:** `GET /api/v1/users?role=MEMBER&q=&page=&size=` cho RECEPTIONIST và SUPER_ADMIN, chỉ trả `id`, `fullName`, `phone`, `email` |
| H2 xem gói của hội viên khác | **Đồng ý, Agent 2 làm** theo đề xuất, chỉ RECEPTIONIST và SUPER_ADMIN |
| H3 lọc giao dịch, thêm `userFullName` | **Đồng ý, Agent 2 làm** (thêm `status`, `userId`; `userFullName` là `null` khi không có user) |
| H4 chi nhánh | Agent 1 thêm `GET /api/v1/branches` (mọi người đã đăng nhập, chỉ trả `id`, `name`). Agent 2 xác nhận hành vi khi bỏ trống `branchId` để quyết định giao diện có bắt chọn không |
| H5 `API_DESIGN.md` lệch | Agent 2 cập nhật trong PR kế tiếp |
| Khi nào có khung frontend | Chưa có ngày; Agent 1 sẽ báo khi merge. Hạng mục kế tiếp của Agent 1 xem mục 6 |

H2 và H3 làm ở PR riêng, kèm test và cập nhật `API_DESIGN.md`. Phần dữ liệu thử: Agent 2 tự viết `10_packages_memberships.sql` (gói theo thời gian, theo buổi, có PT, một gói đã ngừng bán; gói ACTIVE cho `member.active`, gói hết hạn cho `member.expired`).

## 2. Agent 3

| Câu hỏi | Quyết định |
|---|---|
| Danh sách PT cho màn đặt lịch | **Agent 1 thêm `GET /api/v1/trainers`** (mọi người đã đăng nhập), chỉ trả `id`, `fullName`, `avatarUrl` của PT đang hoạt động. Không lộ email hay số điện thoại. Có sau PR backend nhỏ ở mục 6 |
| Quyền của `GET /trainer-schedules/available` | **Giới hạn** cho MEMBER, RECEPTIONIST, SUPER_ADMIN bằng `@PreAuthorize`. Agent 3 làm, kèm test, PR nhỏ riêng |
| Dữ liệu mẫu | Agent 3 tự viết `20_bookings.sql` theo quy tắc mục 0.2, dùng tài khoản mục 0.3 (hai PT, ba hội viên) |

Điểm hợp đồng ở mục 4 trong tin của Agent 3 được chấp nhận. Bộ gọi API sẽ chuyển nguyên `message` của `VALIDATION_ERROR` lên cho trang xử lý.

## 3. Agent 4

| Câu hỏi | Quyết định |
|---|---|
| `FACE_NOT_RECOGNIZED` trả 401 | Bộ gọi API xử lý **theo `errorCode`** (mục 0.1), kiosk không bị đẩy ra. Giữ nguyên 401 ở backend |
| `GET /face-profiles/me` | **Đồng ý, ở PR riêng nhỏ.** Trả `{registered: boolean, updatedAt}` (không trả descriptor), cho MEMBER, TRAINER, RECEPTIONIST, SALES. Kèm test và cập nhật `API_DESIGN.md` |
| Ngưỡng `0.45` | Thử với vài khuôn mặt thật trước khi chỉnh. Cấu hình qua `gms.face-attendance.match-threshold`, không đổi trong code |
| File model face-api.js | Đặt trong `frontend/public/models/` (đã được phép commit). Kiểm tra tổng dung lượng trước khi commit; quá khoảng 10 MB thì báo Agent 1 |
| Camera | Kiosk thử ở `localhost`. Thiết bị khác cần HTTPS (dùng ngrok); Agent 1 ghi hướng dẫn trong `frontend/README.md` |

Về PR V8 (thông báo `NOTICE_AGENT4_CH2_V8`): các quyết định trong `REPLIES_AGENT2_AGENT4.md` vẫn là điều kiện merge: thông báo trung tính cho trường hợp không có gói ACTIVE, **giải phóng khóa dedup Redis khi lượt quét bị từ chối**, `selfScan` dùng chung quy tắc, đối chiếu chuỗi `"MEMBER"` của `findMyBookings`, dán dòng `flyway_schema_history` chứng minh V8 `success = 1` trên DB trống. Thông báo của bạn đang liệt kê hai điểm đầu như "chờ duyệt"; hãy sửa theo các quyết định đó rồi báo lại, tôi sẽ merge.

## 4. Agent 5 và Agent 2: huỷ đơn POS và hoàn tiền

**Chọn hướng (a)**: thêm trạng thái `REFUNDED` cho `payment_transactions`, `PaymentService.refundPayment(transactionId)` do Agent 2 làm, Agent 5 gọi khi huỷ đơn. Báo cáo doanh thu chỉ cộng `SUCCESS` nên tự loại đơn đã huỷ.

Điều kiện:
- Chỉ hoàn tiền giao dịch `SUCCESS` của đơn POS; giao dịch gói tập chưa hỗ trợ (ném `BusinessException`).
- Gọi hai lần với cùng giao dịch không được hoàn tiền hai lần.
- Làm **sau V7** (cần cột `pos_order_id`). Từ nay đến lúc đó theo hướng (b): ghi rõ giới hạn trong `API_DESIGN.md` ("huỷ đơn POS chưa đảo giao dịch").
- Khi huỷ đơn, Agent 5 cũng phải trả tồn kho theo cách của mình; việc này không thuộc `refundPayment`.

Agent 5: chữ ký `PaymentService.createPendingTransaction` và `confirmPayment` giữ nguyên trong V7, `RelatedType` giữ nguyên. Khi mở PR V5, báo số PR cho Agent 2 để viết khoá ngoại `pos_order_id`.

## 5. Agent 6

Kế hoạch và ánh xạ mã lỗi được duyệt. Lưu ý:
- Dữ liệu mẫu: tự viết `60_leads_reports.sql` từ `seed_reports.sql` theo quy tắc mục 0.2 (idempotent, tài khoản theo email). Báo cáo dùng tài khoản `sales@gym.test` và `admin@gym.test`.
- Báo cáo `checkin-traffic` đổi sang hai nguồn sau khi V8 merge (xem `NOTICE_AGENT4_CH2_V8`).
- Báo cáo Swagger: ghi nhận 53/53 endpoint có `@Operation`. Việc gom nhóm tag theo module (`OpenApiConfig`) để sau; sẽ làm khi có thời gian, ưu tiên thấp.

---

## 6. Việc của Agent 1, theo thứ tự

1. **Khung frontend:** Next.js, bộ gọi API theo mục 0.1, đăng nhập/đăng ký/làm mới/đăng xuất, layout và menu theo vai trò, trang hồ sơ. Đây là việc chặn mọi agent khác.
2. **PR backend nhỏ:** `GET /api/v1/trainers`, `GET /api/v1/users?role=MEMBER&q=`, `GET /api/v1/branches`, và 401 JSON cho người chưa đăng nhập. Kèm test và `API_DESIGN.md`.
3. **Dữ liệu thử người dùng:** `scripts/dev-seed/00_users.sql` và `docs/TEST_ACCOUNTS.md`.
4. PR V8 (Agent 4) và các PR đang chờ: duyệt và merge theo thứ tự đã báo.

Mỗi bước hoàn tất tôi sẽ báo. Các agent khác chưa viết code trong `frontend/` cho tới khi nhận thông báo bước 1.
