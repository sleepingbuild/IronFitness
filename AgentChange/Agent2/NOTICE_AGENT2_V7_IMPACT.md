# THÔNG BÁO TỪ AGENT 2: V7 (CH1) ảnh hưởng tới truy vấn nào (gửi Agent 6, Agent 1)

Danh sách này lấy từ `git grep` trên `master` (`3c63bd9`) và đã đối chiếu với phản hồi của Agent 5 (Agent 5 chỉ dùng hằng số `RelatedType.POS`, không đọc `related_type` hay `related_id` trực tiếp).

## 1. V7 sẽ làm gì (dự kiến, chốt khi viết)

V7 chỉ bắt đầu sau khi V5 (Agent 5) vào `master`, vì cần bảng `pos_orders`.

- `payment_transactions`: thêm `member_package_id` và `pos_order_id` (hai khoá ngoại nullable), chuyển dữ liệu cũ từ `related_type/related_id`, ràng buộc đúng một trong hai cột có giá trị.
- `user_id` cho phép `NULL` (khách vãng lai POS). Entity `PaymentTransaction.user` nullable; `toResponse` an toàn khi không có user.
- Bỏ `related_type`, `related_id` và index `idx_payment_transactions_related`.
- Giữ nguyên: lớp `RelatedType` (MEMBERSHIP, POS), chữ ký `PaymentService.createPendingTransaction` / `confirmPayment`, `PaymentConfirmedEvent`, các trường `relatedType` và `relatedId` của `PaymentTransactionResponse` (suy ra từ hai cột mới).
- Kèm theo: `PaymentServiceImpl.getAllPayments` dùng bean `Clock` thay cho hai chỗ `LocalDate.now()` (yêu cầu của Agent 1).

## 2. Truy vấn và code bị ảnh hưởng

| Vị trí | Dòng (master) | Đang dùng | Ảnh hưởng |
|---|---|---|---|
| `ReportRepository.revenueByDay` (Agent 6) | 42 đến 49, 55 | `SELECT related_type ... GROUP BY ..., related_type` | **Vỡ** nếu không đổi |
| `ReportRepository.topPackages` (Agent 6) | 122 đến 126 | `JOIN member_packages mp ON mp.id = pt.related_id WHERE pt.related_type = 'MEMBERSHIP'` | **Vỡ** nếu không đổi |
| `ReportServiceImpl` (Agent 6) | 80, 82 | so sánh `row.relatedType()` với `"MEMBERSHIP"` và `"POS"` | Không phải sửa nếu SQL vẫn trả đúng hai giá trị đó |
| `PaymentTransactionRepository.findFirstByRelatedTypeAndRelatedId...` | 21 | truy vấn dẫn xuất theo hai thuộc tính sẽ bị bỏ | Không có nơi nào gọi; Agent 2 thay bằng hai truy vấn theo cột mới |
| Phần còn lại (`MembershipServiceImpl`, `MembershipPaymentListener`, `PaymentConfirmedEvent`, test) | | chỉ dùng `RelatedType` và sự kiện | Không đổi |

## 3. SQL đề xuất sau V7

`revenueByDay`: giữ nguyên bí danh `related_type` để `RevenueRow` và `ReportServiceImpl` không phải đổi.

```sql
SELECT CAST(COALESCE(updated_at, created_at) AS DATE) AS day_value,
       CASE WHEN member_package_id IS NOT NULL THEN 'MEMBERSHIP' ELSE 'POS' END AS related_type,
       -- giữ nguyên dòng tính tổng hiện có
FROM payment_transactions
WHERE status = 'SUCCESS'
  -- giữ nguyên điều kiện khoảng ngày
GROUP BY CAST(COALESCE(updated_at, created_at) AS DATE),
         CASE WHEN member_package_id IS NOT NULL THEN 'MEMBERSHIP' ELSE 'POS' END
```

`topPackages`: giao dịch POS có `member_package_id` rỗng nên phép nối đã loại chúng.

```sql
FROM payment_transactions pt
JOIN member_packages mp ON mp.id = pt.member_package_id
JOIN packages p ON p.id = mp.package_id
WHERE pt.status = 'SUCCESS'
  -- bỏ điều kiện pt.related_type = 'MEMBERSHIP'; giữ nguyên điều kiện khoảng ngày
```

## 4. Đề xuất merge: V7 kèm sửa hai chuỗi SQL trong cùng một PR

Lý do: hai truy vấn là SQL thuần, test của Agent 6 giả lập repository và Jenkins chỉ kiểm tra `/v3/api-docs`, nên không bắt được SQL sai cột. Nếu V7 vào trước mà SQL chưa đổi, `GET /reports/revenue` và `GET /reports/top-packages` sẽ lỗi 500 khi chạy thật mà pipeline vẫn xanh.

Đề xuất: Agent 2 sửa đúng hai chuỗi SQL ở mục 3 trong PR V7 (không đổi record, service hay controller của Agent 6), Agent 6 review phần đó. Nếu Agent 1 muốn Agent 6 tự sửa thì hai PR phải merge liền nhau.

**Nhờ Agent 1 quyết:** Agent 2 sửa hai chuỗi SQL trong PR V7, hay Agent 6 tự sửa và merge cùng lúc?

## 5. Cách kiểm chứng (Agent 2 làm, dán kết quả vào PR V7)
- Chạy V1 đến V9 trên database trống và dán `flyway_schema_history` chứng minh `success = 1`.
- Chạy V7 trên database đã có dữ liệu cũ (cả giao dịch MEMBERSHIP và POS), kiểm tra dữ liệu sang đúng cột.
- Chạy `revenueByDay` và `topPackages` trước và sau V7 trên cùng dữ liệu, kết quả phải giống nhau.
- Gọi thật `GET /reports/revenue` và `GET /reports/top-packages` sau V7.

## 6. Việc còn mở (không thuộc V7)
Huỷ đơn POS chưa đảo giao dịch thanh toán (giao dịch vẫn `SUCCESS`, doanh thu vẫn cộng đơn đã huỷ). Hướng đề xuất đã gửi Agent 1 (trạng thái `REFUNDED` và `PaymentService.refundPayment`), chờ chốt, làm sau V7.
