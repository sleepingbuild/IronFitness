# Phản hồi cho Agent 2 (từ Agent 1 + Phi)

## 1. Việc phải làm ngay — xoá 5 file tạm, dùng bản thật đã push

`master` giờ đã có bản thật của các file bạn tự tạo tạm. **Xoá các file sau trong nhánh của bạn**, `git pull` lại `master`, không giữ bản của mình:

- `security/CurrentUserId.java` — bản thật đã push, cách gọi `CurrentUserId.get()` **giữ nguyên**, không cần sửa chỗ gọi. Chỉ khác: JWT subject thật là **email**, không phải UUID như bạn giả định — nếu bạn có code nào tự parse subject thành UUID ở nơi khác (ngoài file này), kiểm tra lại.
- `repository/UserRepository.java`, `repository/BranchRepository.java` — bản thật đã có sẵn `findByEmail`, `existsByEmail` từ đầu. File tạm của bạn đã ghi đè mất 2 method này, làm `AuthServiceImpl` không compile được. Xoá file tạm.

## 2. Lỗi cần tự sửa — trùng Service Impl

Đang có cả `service/MembershipServiceImpl.java` **và** `service/impl/MembershipServiceImpl.java` (tương tự Package, Payment) — 2 bean cùng tên, app sẽ không khởi động. Diff 2 bản, giữ đúng bản trong `service/impl/`, xoá bản còn lại. Làm với cả 3 cặp (Membership/Package/Payment).

## 3. Trả lời 5 điểm cần Phi xác nhận

1. **Tách quyền `GET /packages`** — Duyệt. Cập nhật lại `API_DESIGN.md` cho khớp (GET mở cho mọi role đã đăng nhập, POST/PUT/DELETE + `/packages/admin` giữ Super Admin).
2. **Hạ cấp thu tiền ngay, không qua `PENDING` riêng** — Chấp nhận cho v1. Ghi chú lại trong `REQUIREMENTS.md` để không ai hiểu nhầm là thiếu tính năng.
3. **2 endpoint `upgrade`/`downgrade` riêng** — Duyệt, đúng `API_DESIGN.md`.
4. **`remaining_sessions` mất khi nâng cấp, không quy đổi** — Chấp nhận cho v1.
5. **`GET /payments?branchId=...`** — Hoãn. Bảng `payment_transactions` không có `branch_id` theo đúng `DATABASE_SCHEMA.md`. Bỏ endpoint này khỏi `API_DESIGN.md` v1, hoặc đổi thành lọc theo khoảng ngày (`?from=&to=`) nếu Lễ tân thực sự cần tra cứu.

## 4. Cấu hình — không tạo khối `gms:` thứ 2

Nếu bạn cần thêm config, sửa **trong khối `gms:` đã có sẵn** ở `application.yml.example`, đừng tạo khối `gms:` mới (lỗi này đã từng xảy ra và làm mất hết config phía trên nó).

## 5. Việc còn thiếu trước khi coi Issue 2.x là "Done"

- Pagination (`PageResponse<T>`) cho các endpoint list — hiện trả `List<T>` thô.
- Test cho luồng nâng cấp và `PaymentServiceImpl` (đã có test cho lazy-check/chặn PENDING/hạ cấp).
- Chưa có bằng chứng đã `mvn clean verify` thành công sau khi ghép — làm việc này **trước**, sau khi xoá xong 5 file ở mục 1-2.