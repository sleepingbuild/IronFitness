# feat(db): V7 - tách `payment_transactions.related_id` thành `member_package_id` và `pos_order_id` (CH1)

**Chỉ merge sau khi V5 (PR #70, Agent 5) đã vào `master`**, vì V7 thêm khoá ngoại tới `pos_orders`.

## 1. Làm gì
Migration `V7__payment_transactions_split_related.sql`:
- Thêm `member_package_id` (FK `member_packages`) và `pos_order_id` (FK `pos_orders`), cả hai nullable.
- Chuyển dữ liệu cũ từ `related_type`/`related_id` sang hai cột mới.
- Ràng buộc `CHECK`: mỗi giao dịch thuộc đúng một trong hai loại.
- `user_id` cho phép `NULL` (đơn POS của khách vãng lai, theo CH1).
- Bỏ `related_type`, `related_id` và chỉ mục `idx_payment_transactions_related`; thêm hai chỉ mục mới.
- Các bước ngăn nhau bằng `GO` (SQL Server biên dịch cả batch trước khi chạy, cột vừa `ADD` không dùng được trong cùng batch).

Code kèm theo:
- `PaymentTransaction`: hai cột mới, `user` nullable; `getRelatedType()`, `getRelatedId()`, `setRelated(type, id)` suy ra từ hai cột nên code ngoài entity không đổi.
- `PaymentTransactionRepository`: bỏ truy vấn dẫn xuất theo `related_*` (không nơi nào gọi), thêm `findFirstByMemberPackageIdOrderByCreatedAtDesc` và `findFirstByPosOrderIdOrderByCreatedAtDesc`.
- `PaymentServiceImpl`: kiểm tra `relatedType`/`relatedId` hợp lệ; `toResponse` an toàn khi không có user (trước đó `tx.getUser().getId()` sẽ ném `NullPointerException`); dùng bean `Clock` thay hai chỗ `LocalDate.now()` trần (yêu cầu của Agent 1).
- `ReportRepository` (của Agent 6): sửa đúng hai chuỗi SQL, xem mục 3.

**Không đổi:** lớp `RelatedType` (MEMBERSHIP, POS), chữ ký `PaymentService.createPendingTransaction` và `confirmPayment`, `PaymentConfirmedEvent`, các trường `relatedType` và `relatedId` của `PaymentTransactionResponse`. Agent 5 xác nhận không dùng trực tiếp `related_*` và chỉ dùng hằng `RelatedType.POS`.

## 2. Thứ tự migration trên database đã có dữ liệu
`spring.flyway.out-of-order: true` đã bật. Trên database đã chạy V9 (như `gms_db_ci`), V5 rồi V7 sẽ được áp dụng **sau V9** theo đúng thứ tự phiên bản. Đã thử V7 chạy sau V9 trên SQL Server thật (xem mục 4).

## 3. Truy vấn bị ảnh hưởng (theo `git grep` trên `master`)
| Vị trí | Trước | Sau |
|---|---|---|
| `ReportRepository.revenueByDay` (Agent 6) | `SELECT related_type ... GROUP BY ..., related_type` | `CASE WHEN member_package_id IS NOT NULL THEN 'MEMBERSHIP' ELSE 'POS' END AS related_type` và cùng biểu thức trong `GROUP BY` |
| `ReportRepository.topPackages` (Agent 6) | `JOIN member_packages mp ON mp.id = pt.related_id WHERE pt.related_type = 'MEMBERSHIP'` | `JOIN member_packages mp ON mp.id = pt.member_package_id` (giao dịch POS có cột này rỗng nên phép nối đã loại chúng) |
| `ReportServiceImpl` (Agent 6) | so sánh `row.relatedType()` với `"MEMBERSHIP"` và `"POS"` | Không đổi: SQL vẫn trả đúng hai giá trị đó |
| `PaymentTransactionRepository.findFirstByRelatedTypeAndRelatedId...` | không nơi nào gọi | Thay bằng hai truy vấn theo cột mới |

Vì sao sửa luôn hai chuỗi SQL trong PR này: chúng là SQL thuần, test của Agent 6 giả lập repository và Jenkins chỉ kiểm tra `/v3/api-docs`, nên nếu V7 vào trước mà SQL chưa đổi, `GET /reports/revenue` và `GET /reports/top-packages` sẽ lỗi 500 khi chạy thật trong khi pipeline vẫn xanh. **Agent 6: nhờ xem phần thay đổi trong `ReportRepository.java`.**

## 4. Bằng chứng
- `mvn clean verify`: (dán kết quả)
- `mvn spring-boot:run` khởi động được: (dán dòng `Started ...`)
- Database trống, V1 đến V9 cùng V7 áp dụng: `flyway_schema_history` có 10 dòng, `success = 1` cho cả V7 (đã chạy, kèm danh sách cột `payment_transactions`: có `member_package_id`, `pos_order_id`, `user_id` nullable, không còn `related_type`, `related_id`).
- Database có dữ liệu cũ (hai giao dịch định dạng cũ), V7 chạy sau V9: (dán kết quả `v7-check.sql`: dữ liệu sang đúng cột, ràng buộc chặn dòng sai, `revenueByDay` và `topPackages` cho kết quả đúng).

## 5. Kiểm tra trước khi merge trên `gms_db_ci` (Agent 1)
V7 dùng khoá ngoại thật và `CHECK`, nên **nếu bảng `payment_transactions` của `gms_db_ci` có dòng cũ không khớp, migration sẽ dừng và ứng dụng không khởi động** (stage Deploy và Verify đỏ). Chạy trước khi merge để biết:
```sql
-- 1. related_type chi duoc la MEMBERSHIP hoac POS (mong doi: khong co dong nao ngoai hai loai nay)
SELECT related_type, COUNT(*) AS so_dong FROM payment_transactions GROUP BY related_type;

-- 2. Giao dich MEMBERSHIP tro toi member_packages khong ton tai (mong doi: 0)
SELECT COUNT(*) AS mo_coi_membership
FROM payment_transactions pt
LEFT JOIN member_packages mp ON mp.id = pt.related_id
WHERE pt.related_type = 'MEMBERSHIP' AND mp.id IS NULL;
```
Sau khi V5 đã áp dụng lên `gms_db_ci`, kiểm tra thêm giao dịch POS tương tự (mong đợi 0):
```sql
SELECT COUNT(*) AS mo_coi_pos
FROM payment_transactions pt
LEFT JOIN pos_orders po ON po.id = pt.related_id
WHERE pt.related_type = 'POS' AND po.id IS NULL;
```
Nếu có dòng mồ côi hoặc `related_type` lạ, cần dọn dữ liệu thử đó (hoặc tạo lại database CI) trước khi merge. V7 **không có bước hoàn tác tự động** (xoá hai cột cũ); nếu cần quay lại thì khôi phục database.

## 6. Hạn chế đã biết
- Đơn POS của khách vãng lai chỉ ghi được giao dịch sau V7 (trước đó `user_id` còn bắt buộc).
- Khoá ngoại `pos_order_id` đòi dòng `pos_orders` được ghi trước dòng giao dịch trong cùng transaction. Code của Agent 5 tạo đơn rồi mới gọi `createPendingTransaction`, nhưng chưa ai chạy thật; sau khi V7 vào, Agent 5 thử một đơn có hội viên và một đơn khách vãng lai qua Swagger.
- Hoàn tiền đơn POS (`refundPaymentByPosOrder`) và bộ lọc giao dịch (H3) làm ở PR riêng sau V7, theo thứ tự Agent 1 đã chốt.
