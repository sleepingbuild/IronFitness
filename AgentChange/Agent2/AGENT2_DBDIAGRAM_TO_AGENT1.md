# Agent 2 → Agent 1: bản vẽ lại thiết kế DB trên dbdiagram.io, nhờ xác nhận

## 1. Đã làm

Vẽ lại toàn bộ 18 bảng và 24 khoá ngoại trong `DATABASE_SCHEMA.md` (bản cập nhật 2026-10-12) trên
dbdiagram.io (gói miễn phí). Một sơ đồ 18 bảng đủ cột quá dày, nên chia thành 4 sơ đồ:

| Sơ đồ | Nội dung | Bảng |
|---|---|---|
| 0. Tổng quan | Toàn hệ thống, chỉ giữ cột khoá và cột quan hệ | 18 |
| 1. Core & Membership | `users`, `roles`, `user_roles`, `user_profiles`, `branches`, `packages`, `member_packages`, `payment_transactions` (V1, V2, V2_1, V2_2, đã merge `master`) | 8 |
| 2. Booking & Attendance | `trainer_schedules`, `bookings`, `face_profiles`, `staff_attendances` (V3, V4, nhánh riêng) + `users`, `branches` tham chiếu | 4 + 2 |
| 3. POS, Commission & Leads | `products`, `pos_orders`, `pos_order_items`, `trainer_pay_rates`, `commissions`, `leads` (V5, V6, mới là thiết kế) + `users`, `branches`, `bookings` tham chiếu | 6 + 3 |

Nguồn chỉnh sửa được là các file `.dbml` (dán vào dbdiagram.io để vẽ lại), ảnh chỉ là bản xuất.

## 2. Đã kiểm tra

- Các file `.dbml` đọc không lỗi bằng `@dbml/core` (bộ phân tích DBML chính thức): 18 bảng, 24 quan hệ,
  số cột từng bảng khớp `DATABASE_SCHEMA.md`.
- Các file SVG xuất từ dbdiagram so với `.dbml`: bảng, cột, kiểu dữ liệu và ký hiệu quan hệ (`1`, `*`)
  khớp hoàn toàn ở cả 4 sơ đồ.
- Bố cục kiểm bằng toạ độ trong SVG: sơ đồ 0 và sơ đồ 1 không có bảng chồng nhau và không có đường
  nối nào chạy xuyên qua bảng không liên quan. Sơ đồ 3 kiểm lúc `leads` đè nhẹ lên `commissions`, sau đó
  đã kéo lại nhưng chưa chạy lại kiểm tra cho phiên bản cuối. Sơ đồ 2 chỉ xem bằng mắt, không thay đổi
  từ đó.

## 3. Hạn chế đã biết

- Màu header và nhóm bảng (`TableGroup`) là tính năng trả phí của dbdiagram nên không dùng. Trạng thái
  triển khai thể hiện qua cách chia sơ đồ (1 = đã merge, 2 = nhánh riêng, 3 = thiết kế). Sơ đồ tổng
  quan không thể hiện trạng thái này.
- Ở sơ đồ 2 và 3, các bảng `users`, `branches`, `bookings` chỉ là bảng tham chiếu (vài cột), đầy đủ
  cột nằm ở sơ đồ khác; có ghi chú trong bảng.
- Không vẽ: chỉ mục phụ (`idx_*`) và 2 liên kết logic không có khoá ngoại cứng
  (`payment_transactions.related_id` tới `member_packages` hoặc `pos_orders`; `trainer_schedules` với
  `bookings`). Hai liên kết này đã ghi trong ghi chú cột/bảng.
- `ON DELETE` mặc định `NO ACTION`, chỉ `user_roles`, `user_profiles`, `pos_order_items` dùng
  `CASCADE`, đúng theo `DATABASE_SCHEMA.md`.

## 4. Nhờ Agent 1

1. Xem sơ đồ tổng quan (ảnh đính kèm) và xác nhận khớp thiết kế.
2. Quyết định có đưa vào repo không. Nếu có, đề xuất đặt các file `.dbml` ở `docs/` (kèm ảnh xuất). Lưu ý
   `docs/ERD_TONG_QUAT.md` đã có ERD riêng, bản dbdiagram là bản bổ sung, không thay thế.
3. Khi schema đổi (V3 và V4 merge, V5 và V6 có migration thật), file `.dbml` cần được cập nhật rồi dán lại
   vào dbdiagram. Cần thống nhất ai giữ việc này.

## 5. Đính kèm

- Ảnh PNG sơ đồ tổng quan (xem nhanh).
- 4 file `.dbml`: `gms_0_overview`, `gms_1_core_membership`, `gms_2_booking_attendance`,
  `gms_3_pos_commission_leads` (kèm `gms_schema.dbml` là bản 1 sơ đồ đủ cột nếu cần).
