# Quyết định của Agent 1 về đề xuất DB v2 (phản hồi `AGENT2_REQUEST_APPROVAL_DB_V2.md`)

**Trạng thái:** đề xuất của Agent 1, chờ Phi xác nhận trước khi giao việc.
**Nguyên tắc:** thiết kế (`DATABASE_SCHEMA.md`) đi trước, migration theo sau. Phần chấm điểm nặng nhất là Bước 2 (60%), nên v2 được chốt là **thiết kế chính thức** ngay, rồi triển khai dần theo thứ tự ưu tiên dưới đây.

## 1. Quyết định 5 thay đổi

| # | Quyết định | Điều kiện / cách làm |
|---|---|---|
| CH1 | ✅ **Duyệt** | Giữ nguyên chữ ký `PaymentService` (`createPendingTransaction(...)` và `RelatedType` ở tầng service), chỉ đổi cách lưu: tầng entity ánh xạ `RelatedType.MEMBERSHIP` sang cột `member_package_id`, `RelatedType.POS` sang `pos_order_id`. Nhờ vậy **Agent 5 không phải sửa code** đang viết. Làm bằng **một migration duy nhất sau khi V5 merge** (cần `pos_orders` để tạo FK): thêm hai cột, chuyển dữ liệu cũ, thêm `CHECK` đúng một cột có giá trị, bỏ `related_type`/`related_id`, cho `user_id` nullable. Agent 2 thực hiện trong một PR riêng cùng sửa entity, service, test. |
| CH2 | ✅ **Duyệt, làm sau PR #43** | Bảng `gym_check_ins` chỉ ghi lượt quét **không có buổi PT phù hợp**; lượt quét có booking vẫn chỉ cập nhật `bookings` (tránh hai nơi cùng đếm một lượt). Kiosk kiểm tra hội viên có gói `ACTIVE`, không có thì từ chối (`MEMBERSHIP_EXPIRED`). Báo cáo lưu lượng (Agent 6) lấy hợp của hai nguồn. Agent 4 làm trong PR riêng sau khi PR #43 merge, migration riêng. |
| CH3 | ✅ **Duyệt** (Agent 1 làm) | `user_profiles.age` thành `date_of_birth`; luật tuổi: ngày sinh phải cách ngày đăng ký (`users.created_at`) ít nhất 18 năm. Làm ngay bây giờ vì chưa có frontend, đổi hợp đồng API lúc này gần như không tốn gì. Dữ liệu `age` cũ không suy ngược ra ngày sinh được, đặt `NULL` (DB dev chưa có dữ liệu thật). |
| CH4 | ✅ **Duyệt** (Agent 1 làm) | Bỏ `users.avatar_url`; nguồn duy nhất là `user_profiles.avatar_url`. Agent 1 kiểm tra code đang đọc/ghi `User.avatarUrl` trước khi bỏ cột. Gộp với CH3 và sửa `users.phone` (filtered unique index) thành một migration của Agent 1. |
| CH5 | ✅ **Duyệt** | Bỏ `pos_order_items.subtotal` và `pos_orders.total_amount`; tổng tính từ `quantity × unit_price` (`unit_price` là bản chụp giá lúc bán nên ổn định). Agent 5 chưa viết V5 nên chuyển ngay cho họ, không tốn chi phí. Số tiền đã thu vẫn nằm ở `payment_transactions.amount`. |

## 2. Quyết định thêm (mục 4 của Agent 2)

**Tám story chưa có endpoint: đưa hết vào phạm vi.** Giảng viên chấm "phủ kín chức năng CRUD cơ bản", và `REQUIREMENTS.md` đã giao Super Admin "quản lý nhân sự".

| Story | Quyết định |
|---|---|
| G3 Khách xem gói tập | Cho phép `GET /api/v1/packages` không cần đăng nhập (chỉ gói đang bán). Agent 2 làm; cần sửa quy tắc mở trong `SecurityConfig` (Agent 1). |
| M17 Hội viên xem lịch sử check-in | Làm cùng CH2, Agent 4 (`GET /api/v1/check-ins/me`, hợp của `gym_check_ins` và booking). |
| R22 Lễ tân tạo tài khoản hội viên | Agent 1, dùng `createMemberAccount` đang làm cho Agent 6. |
| A10 Cấu hình lương PT | Đã có: `GET/PUT /api/v1/trainer-pay-rates/{trainerId}` trong `API_DESIGN.md` bản mới (Agent 5). |
| A18–A21 Tài khoản nhân viên, phân quyền, khoá tài khoản, quản lý chi nhánh | Agent 1: `POST /users` (tạo nhân viên), `PUT /users/{id}/roles`, `PATCH /users/{id}/status`, CRUD `/branches`. Hiện không có cách nào tạo tài khoản PT, Sales, Lễ tân qua API. |

## 3. Thứ tự migration và merge

`spring.flyway.out-of-order: true` đã nằm trong `application.yml` (PR `config/application-yml`), nên số nhỏ merge sau không làm DB báo lỗi. Ràng buộc còn lại là **khoá ngoại phải có bảng đích trước**.

| Version | Nội dung | Agent | Điều kiện |
|---|---|---|---|
| V3, V4 | Booking, Face Attendance | 3, 4 | đang chờ merge |
| V5 | POS, lương PT (không còn `subtotal`, `total_amount`) | 5 | sau V3 (FK `commissions` → `bookings`) |
| V6 | Leads | 6 | không phụ thuộc |
| V7 | `payment_transactions` tách FK (CH1) | 2 | sau V5 |
| V8 | `gym_check_ins` (CH2) | 4 | sau V4 |
| V9 | `user_profiles.date_of_birth`, bỏ `users.avatar_url`, filtered unique index `users.phone` (CH3, CH4) | 1 | độc lập |

Thứ tự ưu tiên khi thiếu thời gian (cắt từ dưới lên): CH1 → CH5 → CH2 và M17 → CH3, CH4 và sửa `phone` → tám story còn lại. Phần nào chưa kịp triển khai thì ghi rõ trong bài là thiết kế mục tiêu, kèm giải trình.

## 4. Việc cần Agent 2 làm tiếp

1. Gửi lại cho Agent 1: `USER_STORIES.md`, `DB_DESIGN_REVIEW.md` và các file `gms_v2_*.dbml` (Agent 1 mới nhận được file yêu cầu này, chưa nhận các file đính kèm).
2. Sửa số liệu không khớp: mục 2 ghi **83** story, mục kiểm tra ghi **82/82**; thống nhất một con số.
3. Thêm vào bài thuyết trình một slide giải trình **các cột suy ra được nhưng cố ý giữ** (để ghi trước tiêu chí chuẩn hoá): `member_packages.end_date` và `remaining_sessions` (trạng thái theo thời điểm đăng ký, không suy lại được khi gói đổi), `commissions.amount` và `pos_order_items.unit_price` (bản chụp giá), `bookings.end_time` (cho phép độ dài buổi khác nhau).
4. Thực hiện CH1 (sau V5) và phần G3 theo mục 1 và 2. Giữ chữ ký `PaymentService`.

## 5. Việc của Agent 1
- Cập nhật `DATABASE_SCHEMA.md`, `docs/ERD_TONG_QUAT.md` thành v2 sau khi nhận file DBML để đối chiếu.
- Làm V9, `createMemberAccount`, sửa `register`, handler lỗi, module quản lý nhân sự và chi nhánh.
- **Vẽ Activity Diagram cho Bước 1 (15%) còn thiếu.** Hai chức năng phức tạp nhất đề xuất: (1) Đặt lịch PT và vòng đời booking (nhiều nhánh: chồng giờ, giới hạn tuần, hết buổi, hết hạn gói, huỷ muộn, no-show), (2) PT điểm danh, chốt buổi, trừ buổi và tính lương (nhánh ra sớm có/không lý do, PT vắng, học viên vắng).

## 6. Thông báo cho các agent bị ảnh hưởng
- **Agent 5:** bỏ `subtotal` và `total_amount` khỏi V5; vẫn gọi `PaymentService` như cũ (chữ ký không đổi). Cập nhật `AGENT5_BRIEF.md` mục 3 cho khớp.
- **Agent 4:** sau PR #43, làm CH2 (`gym_check_ins`, kiosk kiểm tra gói `ACTIVE`, M17).
- **Agent 6:** báo cáo lưu lượng sẽ lấy hợp `gym_check_ins` và `bookings.check_in_time` (làm khi CH2 xong).
