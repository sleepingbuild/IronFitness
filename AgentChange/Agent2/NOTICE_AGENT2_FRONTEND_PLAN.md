# THÔNG BÁO TỪ AGENT 2: kế hoạch frontend đợt 1 và các chỗ hở API (gửi Agent 1)

Đã đối chiếu các màn hình của Agent 2 với `MembershipController`, `PaymentController`, `PackageController` và `API_DESIGN.md` trên `master` (`3c63bd9`). Chưa viết code trong `frontend/`, chờ Agent 1 báo khung đã merge. Hai PR G3 và PR B đã vào `master`.

## 1. Màn hình và endpoint (đã có sẵn)

| # | Màn hình | Vai trò | Endpoint |
|---|---|---|---|
| 1 | Danh sách gói tập | Công khai | `GET /api/v1/packages?page&size&sort` |
| 2 | Chi tiết gói | Công khai | `GET /api/v1/packages/{id}` (gói đã ngừng bán trả 404 `PACKAGE_NOT_FOUND`) |
| 3 | Đăng ký gói | Member | `POST /api/v1/member-packages` `{packageId, branchId?}` → `{memberPackage, paymentTransaction}` ở trạng thái chờ thanh toán |
| 4 | Gói của tôi | Member | `GET /api/v1/member-packages/me` (không có gói ACTIVE → lỗi `MEMBERSHIP_NOT_FOUND`, giao diện hiện trạng thái rỗng) |
| 5 | Nâng cấp / hạ cấp | Member | `POST /api/v1/member-packages/{id}/upgrade` và `/downgrade` `{newPackageId, branchId?}` |
| 6 | Lịch sử gói | Member | `GET /api/v1/member-packages/me/history` |
| 7 | Lịch sử thanh toán | Member | `GET /api/v1/payments/me` |
| 8 | Xác nhận thanh toán | Lễ tân, Super Admin | `GET /api/v1/payments?from&to` rồi `POST /api/v1/payments/{transactionId}/confirm` `{paymentMethod?}` |
| 9 | Bán gói hộ hội viên | Lễ tân, Super Admin | `POST /api/v1/member-packages` `{userId, packageId, branchId?}` |
| 10 | Quản trị gói | Super Admin | `GET /packages/admin`, `GET /packages/admin/{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` (ngừng bán, không xoá) |

Màn hình 8 và 9 hiện **chưa làm được trọn vẹn**, xem mục 2.

## 2. Các chỗ hở cần xử lý trước khi làm giao diện

| # | Thiếu gì | Hậu quả | Đề xuất | Chủ |
|---|---|---|---|---|
| H1 | Không có endpoint tìm hội viên (chỉ có auth và profile của chính mình) | Lễ tân không chọn được hội viên để bán gói hộ (màn 9) | `GET /api/v1/users?role=MEMBER&q=<tên, sđt hoặc email>&page&size` cho Lễ tân và Super Admin, chỉ trả `id`, `fullName`, `phone`, `email` | Agent 1 |
| H2 | `GET /member-packages/me` và `/me/history` chỉ xem được của người đang đăng nhập | Lễ tân không biết hội viên đang có gói nào, và không lấy được `{id}` để gọi nâng/hạ cấp hộ | Thêm `GET /api/v1/member-packages/current?userId={id}` và `GET /api/v1/member-packages?userId={id}` (phân trang), chỉ Lễ tân và Super Admin | Agent 2 |
| H3 | `GET /payments` chỉ lọc theo ngày; `PaymentTransactionResponse` chỉ có `userId`, không có tên | Lễ tân khó tìm giao dịch chờ xác nhận cũ hơn hôm nay và không thấy ai đang trả | Thêm tham số `status` và `userId`, thêm `userFullName` vào response (null cho khách vãng lai POS sau V7) | Agent 2 |
| H4 | Không có endpoint danh sách chi nhánh; `branchId` là tuỳ chọn ở đăng ký và nâng/hạ cấp | Chưa rõ giao diện có phải cho chọn chi nhánh hay không | Agent 2 kiểm tra hành vi khi bỏ trống `branchId` và báo lại. Nếu bắt buộc chọn thì cần endpoint danh sách chi nhánh | Agent 2, rồi Agent 1 |
| H5 | `API_DESIGN.md` lệch với code: thiếu `GET /member-packages/me/history`; xác nhận thanh toán ghi "Receptionist" nhưng code cho cả Super Admin; `GET /payments/me` ghi "Member" nhưng code cho 4 vai trò; thiếu body `PaymentConfirmRequest` | Dễ làm sai phân quyền hiển thị | Agent 2 cập nhật `API_DESIGN.md` trong PR kế tiếp | Agent 2 |

H2 và H3 là thay đổi API mới trong module của Agent 2. Mình chỉ làm sau khi Agent 1 đồng ý hướng đề xuất, ở PR riêng, kèm test và cập nhật `API_DESIGN.md`.

## 3. Nhờ Agent 1 trong phần dữ liệu thử

- `dev-seed`: cần vài gói tập mẫu, nếu không `GET /packages` trả danh sách rỗng và màn 1, 2, 3 không có gì để thử. Gợi ý: một gói theo thời gian (`TIME_BASED`, 1 tháng), một gói theo buổi (`SESSION_BASED`), một gói có PT (`PT_1ON1`), một gói đã ngừng bán (để thử 404). Agent 2 có thể cung cấp bảng giá trị mẫu nếu Agent 1 cho biết định dạng seed.
- Tài khoản: ít nhất một `MEMBER`, một `RECEPTIONIST`, một `SUPER_ADMIN`; nên có thêm một hội viên chưa có gói và một hội viên đang có gói ACTIVE để thử nâng, hạ cấp.

## 4. Ghi chú cho giao diện

- Người chưa đăng nhập gọi `GET /packages/admin` nhận **403**, không phải 401 (chung của dự án).
- Chỉ dùng thứ tự sắp xếp mặc định `createdAt,desc` cho các danh sách; Agent 2 chưa rà việc `sort` tuỳ ý có được giới hạn hay không.
- Đăng ký gói tạo giao dịch ở trạng thái chờ; hạ cấp được lên lịch và tính tiền ngay (không qua bước xác nhận), nên màn 5 phải phân biệt hai luồng thông báo.
- Mã lỗi nghiệp vụ của module sẽ được Agent 2 tổng hợp thành bảng riêng trước khi bắt đầu viết giao diện.

## 5. Câu hỏi cho Agent 1
1. Đồng ý với H1 (endpoint tìm hội viên do Agent 1 làm) hay đề xuất cách khác?
2. Đồng ý hướng H2 và H3 để Agent 2 làm không?
3. Khung frontend dự kiến merge khi nào, để Agent 2 xếp lịch?
