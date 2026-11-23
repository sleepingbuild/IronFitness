# Agent 2 — Membership, Package & Payment — Ghi chú bàn giao (v2, sau phản hồi Agent 1/Phi)

## 1. Đã xử lý theo `Agent2-Respone.md`

- ✅ Xoá `security/CurrentUserId.java`, `repository/UserRepository.java`, `repository/BranchRepository.java`
  (dùng bản thật từ `master`). Đã rà lại toàn bộ code Agent 2 — **không có chỗ nào khác** tự
  `UUID.fromString(authentication.getName())`, chỉ `CurrentUserId.java` (đã xoá) từng làm việc này,
  nên không cần sửa gì thêm ở controller khi JWT subject là email thay vì UUID.
- ✅ Đối chiếu lại: Agent 2 **chưa từng** tạo `service/MembershipServiceImpl.java` (hay Package/Payment
  tương ứng) ở gốc `service/` — toàn bộ impl chỉ nằm đúng trong `service/impl/` (đúng cấu trúc
  `ARCHITECTURE.md`). Nếu khi merge vẫn thấy trùng, file trùng đó không phải từ Agent 2 — kiểm tra lại
  xem có agent/stub nào khác từng đặt nhầm ở `service/` gốc.
- ✅ Thêm pagination (`PageResponse<T>` + `PageableUtils`) cho toàn bộ endpoint list:
  `GET /packages`, `GET /packages/admin`, `GET /member-packages/me/history`, `GET /payments/me`.
- ✅ Quyết định #5 (hoãn `branchId`): đổi `GET /api/v1/payments` thành lọc theo khoảng ngày
  `?from=&to=` (mặc định hôm nay nếu không truyền) thay vì theo chi nhánh.
- ✅ Bổ sung test còn thiếu: `upgradePackage` (huỷ ngay + tạo PENDING, chặn nếu giá thấp hơn, chặn
  nếu không phải chủ sở hữu/không phải staff) và `PaymentServiceImplTest` (confirmPayment kích hoạt
  đúng membership, idempotent khi confirm 2 lần, không đụng membership nếu relatedType = POS,
  ném `PAYMENT_NOT_FOUND` khi sai id).

## 2. Về việc chạy `mvn clean verify`

**Không thể chạy thật trong môi trường tôi đang thao tác** — sandbox này chỉ được phép gọi ra
ngoài tới GitHub/PyPI/npm (không có Maven Central/`repo.maven.apache.org`), nên `mvn` không tải
được dependency (Spring Boot, Lombok, JUnit...). Tôi đã làm bước thay thế:

- Kiểm tra ngoặc `{}` khớp ở mọi file `.java` (không file nào lệch).
- Đối chiếu số lượng method giữa mỗi interface (`MembershipService`, `PackageService`,
  `PaymentService`) và `@Override` trong impl tương ứng — khớp đủ 9/7/6.
- Rà lại toàn bộ import, không còn import chết nào liên quan đến các file đã xoá.

→ Vẫn cần bạn chạy `mvn clean verify` thật trong máy/CI sau khi merge — đây chỉ là kiểm tra tĩnh,
không thay thế được build thật (không bắt được lỗi kiểu sai tên method của `Package`/`User`/`Branch`
thật nếu có sai lệch nhỏ nào đó ngoài những gì tôi được cung cấp).

## 3. Cấu hình `application.yml`

Agent 2 **không đụng** tới `application.yml.example` trong đợt code này (không có config nào
cần thêm ở bước hiện tại) — không có rủi ro tạo khối `gms:` thứ 2 như cảnh báo mục 4 của Phi.

## 4. Trạng thái ISSUES.md sau đợt sửa này

| Issue | Trạng thái |
|---|---|
| 2.1 CRUD Package | Done + pagination |
| 2.2 Đăng ký + confirm thanh toán | Done |
| 2.3 Nâng cấp | Done + test đầy đủ (huỷ ngay, chặn giá thấp hơn, chặn sai chủ sở hữu) |
| 2.4 Hạ cấp + lazy-check | Done (thu tiền ngay theo quyết định Phi mục 3.2 — đã ghi chú, cần Agent 1 đồng bộ vào `REQUIREMENTS.md`) |
| 2.5 `getCurrentMembership` | Done |
| 2.6 Chặn đăng ký khi có PENDING | Done |
| 2.7 API Lễ tân bán hộ + lịch sử (phân trang) | Done |
| 2.8 `confirmPayment` dùng chung | Done + test idempotency/POS |

## 5. Còn lại — cần Agent 1 cập nhật `API_DESIGN.md` (không phải việc của Agent 2 tự sửa file SHARED)

Theo quyết định Phi mục 3, `API_DESIGN.md` cần cập nhật 2 chỗ (đề nghị Agent 1 review PR):

1. Bảng `packages`: tách dòng `GET` (mọi role đã đăng nhập) khỏi `POST/PUT/DELETE` (Super Admin),
   thêm `GET /packages/admin`.
2. Bảng payments: xoá dòng `GET /api/v1/payments?branchId=...`, thay bằng
   `GET /api/v1/payments?from=&to=` (Receptionist/Admin).
3. Cả 2 bảng: ghi chú thêm `?page=&size=&sort=` cho mọi endpoint list (đã làm đúng theo mục 2
   `API_DESIGN.md` gốc, chỉ là tài liệu chưa ghi rõ ràng cho từng endpoint).

## 6. Vẫn còn để dành (chưa ai yêu cầu gấp)

- `remaining_sessions` khi nâng cấp: xác nhận mất, không quy đổi (theo Phi mục 3.4) — đã đúng code,
  không cần sửa.
