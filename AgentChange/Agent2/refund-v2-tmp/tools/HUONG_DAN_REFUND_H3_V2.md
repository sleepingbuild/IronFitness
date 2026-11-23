# Refund và H3 (bản sửa tên method) — Agent 2

## Thay đổi so với gói cũ
- `refundPaymentByPosOrder(UUID)` đổi thành **`refundByRelated(String relatedType, UUID relatedId)`** (Agent 1 đã chốt tên):
  - `POS`: tìm giao dịch mới nhất của đơn (cột `pos_order_id`) rồi hoàn; gọi lần hai khi đã REFUNDED thì không lỗi, không hoàn thêm;
    đơn không có giao dịch hoặc giao dịch chưa SUCCESS -> `PAYMENT_NOT_REFUNDABLE`.
  - `MEMBERSHIP`: `PAYMENT_NOT_REFUNDABLE`.
  - Loại khác hoặc `relatedId` null: `VALIDATION_ERROR`.
- Giữ `refundPayment(UUID transactionId)`.
- Test: 3 test `refundPaymentByPosOrder_*` được thay bằng 6 test `refundByRelated_*`.
- H3 (`GET /payments` có `status`, `userId`, `userFullName`) giữ nguyên như gói cũ.

## 8 file (thư mục `repo\src`), chồng lên V7
PaymentStatus, PaymentController, PaymentTransactionResponse, PaymentTransactionRepository, PaymentService, PaymentServiceImpl,
PaymentControllerTest, PaymentServiceImplTest.

## Giới hạn
Chưa chạy được Maven ở phía mình; chỉ kiểm tra tĩnh (không còn tên cũ, ngoặc cân bằng). Cần `mvn clean verify` trên máy bạn.
Gói này phụ thuộc V7 (`setRelated`, `getPosOrderId`, `findFirstByPosOrderId...`) nên phải chép lên nhánh đã có V7.
