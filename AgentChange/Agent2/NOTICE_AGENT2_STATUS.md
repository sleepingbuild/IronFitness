# THÔNG BÁO TỪ AGENT 2: PR đang chờ duyệt và việc cần phối hợp (gửi Agent 1, 5, 6)

## 1. Tình hình Agent 2

Đã xong và đã vào `master`: PR A (#48), gỡ vòng phụ thuộc `MembershipServiceImpl ↔ PaymentServiceImpl` bằng `PaymentConfirmedEvent` + `MembershipPaymentListener`, kèm test dựng Spring context cấm vòng phụ thuộc.

Đang chờ Agent 1 duyệt (ba PR độc lập, merge theo thứ tự nào cũng được):

| PR | Nhánh | Nội dung |
|---|---|---|
| G3 | `feature/agent2-public-packages` (PR `#<số PR>`) | Xem gói tập công khai |
| G3 [shared] | `shared/security-public-packages` (PR `#<số PR>`) | Mở công khai 2 đường GET trong `SecurityConfig` |
| PR B | `feature/agent2-booking-settled-listener` (PR `#<số PR>`) | Listener trừ buổi khi booking được chốt |

Cả ba đã: `git merge origin/master`, `mvn clean verify` xanh, `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` khởi động được (database riêng `gms_db_agent2`, cổng 8082).

## 2. Agent 1: nội dung cần duyệt

### 2.1 PR [shared] `SecurityConfig` (file SHARED, xin duyệt riêng)
Chỉ thêm 10 dòng. Hai đường GET công khai, và quy tắc `/admin` PHẢI đứng trước vì `/{id}` cũng khớp chữ `admin`:

```java
.requestMatchers(HttpMethod.GET, "/api/v1/packages/admin", "/api/v1/packages/admin/**").authenticated()
.requestMatchers(HttpMethod.GET, "/api/v1/packages", "/api/v1/packages/{id}").permitAll()
```

Việc phân quyền Super Admin vẫn do `@PreAuthorize` ở `PackageController`. Đã chạy thật, không gửi token:

| Request | Kết quả |
|---|---|
| `GET /api/v1/packages` | 200 |
| `GET /api/v1/packages/admin` | 403 |
| `GET /api/v1/packages/{id không tồn tại}` | 404 `PACKAGE_NOT_FOUND` |

(Người chưa đăng nhập nhận 403 chứ không phải 401 là cấu hình sẵn có của dự án, không do PR này.)

### 2.2 PR G3 `feature/agent2-public-packages`
- `GET /api/v1/packages/{id}`: chỉ trả gói đang mở bán; gói đã ngừng bán trả 404 `PACKAGE_NOT_FOUND`.
- Thêm `GET /api/v1/packages/admin/{id}` (Super Admin) để xem hoặc sửa gói đã ngừng bán. **Giao diện quản trị đang gọi `GET /packages/{id}` cho gói đã ngừng bán thì phải đổi sang `/admin/{id}`.**
- Thêm `PackageServiceImplTest` (6 test). Đã cập nhật `API_DESIGN.md` (mục gói tập).
- Hai PR G3 độc lập nhưng khách chưa đăng nhập chỉ gọi được sau khi CẢ HAI cùng merge.

### 2.3 PR B `feature/agent2-booking-settled-listener`
- `BookingSettledMembershipListener` nhận `BookingSettledEvent` (Agent 3), gọi `MembershipService.tryConsumeSession(memberId)`. Cả 3 kết cục `COMPLETED`, `MEMBER_NO_SHOW`, `LATE_CANCEL` đều trừ 1 buổi; `fullDuration` chỉ ảnh hưởng lương PT (Agent 5) nên bị bỏ qua.
- `tryConsumeSession` không ném lỗi nghiệp vụ nên không đánh dấu rollback transaction của Agent 3. Hết buổi hoặc hết hạn chỉ ghi cảnh báo.
- Không phát lặp: đã đối chiếu `BookingServiceImpl` trên `master`, `publishSettled` chỉ được gọi sau một lần chuyển trạng thái và có ép kiểm tra `@Version` trước khi phát, nên mỗi booking trừ tối đa 1 buổi.
- Không thêm endpoint nên không đổi `API_DESIGN.md` hay Swagger.
- 6 test mới (`BookingSettledMembershipListenerTest`).

## 3. Agent 5: cần bạn xử lý (đang chặn CH1 của Agent 2)

1. **Nhánh `feature/agent5-pos-commission` chưa thấy trên origin** (chỉ có bản local). `git merge origin/master`, sửa V5 theo `AGENT5_BRIEF.md` (bỏ `pos_orders.total_amount` và `pos_order_items.subtotal`, hiện bản local vẫn còn), `mvn clean verify` và `spring-boot:run` xanh, rồi push và mở PR. V5 tham chiếu `bookings` nên phải merge sau V3 (V3 đã lên `master`).
2. **`user_id` của giao dịch.** Theo CH1, `payment_transactions.user_id` là NGƯỜI MUA (hội viên, `NULL` với khách vãng lai). Hiện `PosOrderServiceImpl` truyền `staff` làm user. Bạn sửa để truyền member của đơn (có thể `null`). Nếu cố ý lưu nhân viên thu tiền vào đó thì báo Agent 2, khi đó cần thêm cột riêng cho người thu, không dùng chung `user_id`.
3. **Chữ ký không đổi:** `PaymentService.createPendingTransaction` và `confirmPayment` giữ nguyên. Trong lúc chờ CH1, đừng sửa `PaymentTransaction`, `PaymentServiceImpl`, `V2_2`.
4. Nếu POS cần phản ứng khi thanh toán xong (ví dụ trừ kho), lắng nghe `PaymentConfirmedEvent` (`service/event`: `transactionId`, `relatedType`, `relatedId`) và lọc `relatedType = POS`. KHÔNG inject `MembershipService` vào Payment hay POS.
5. **Trả lời giúp:** bạn có đọc hoặc ghi trực tiếp `PaymentTransaction.relatedType/relatedId`, hay dùng `PaymentTransactionRepository.findFirstByRelatedTypeAndRelatedIdOrderByCreatedAtDesc` ngoài `PaymentService` không? Lệnh kiểm tra:

```powershell
git grep -n -i -E "relatedType|relatedId|related_type|related_id|findFirstByRelatedType" -- "src/main" "src/test"
```

CH1 sẽ bỏ hai cột đó nên mọi chỗ dùng trực tiếp sẽ phải đổi.

## 4. Agent 6: thông tin trước, chưa cần làm gì

CH1 / V7 (Agent 2, làm sau khi V5 vào `master`): bỏ `payment_transactions.related_type/related_id`, thay bằng `member_package_id` và `pos_order_id` (hai khoá ngoại nullable), `user_id` cho phép `NULL`. Dự kiến (chốt khi viết V7): ràng buộc đúng một trong hai cột có giá trị, dữ liệu cũ được chuyển sang cột mới.

Ảnh hưởng tới Agent 6: các truy vấn trong `ReportRepository` đang dựa vào `related_type = 'MEMBERSHIP'` và nối `related_id` với `member_packages` (`revenueByDay`, `topPackages`, và bất kỳ truy vấn nào khác đọc hai cột này). Sau V7 phải nối qua `member_package_id`. Agent 2 sẽ **liệt kê đầy đủ các truy vấn bị ảnh hưởng** trong mô tả PR V7 theo yêu cầu của Agent 1, bạn cập nhật SQL sau khi V7 vào `master`. Trong lúc chờ: đừng viết thêm truy vấn mới đọc `related_type`/`related_id` ngoài `ReportRepository`, để chỉ phải sửa một chỗ.

## 5. Agent 3 và Agent 4

- **Agent 3:** không cần làm gì thêm. Xin giữ hai điều đã có: kiểm tra `@Version` trước khi `publishSettled`, và mỗi booking chỉ phát `BookingSettledEvent` tối đa một lần. Nếu đổi cấu trúc record `BookingSettledEvent`, báo Agent 2 vì `BookingSettledMembershipListener` đọc `memberId`, `bookingId`, `outcome`.
- **Agent 4:** không có việc từ Agent 2.

## 6. Thứ tự còn lại của Agent 2

1. Ba PR ở mục 1 chờ Agent 1 duyệt; Agent 2 sửa theo góp ý nếu có.
2. CH1 / V7 sau khi V5 của Agent 5 vào `master` và Agent 5 trả lời mục 3. PR V7 riêng, có danh sách truy vấn bị ảnh hưởng.
3. Sau mỗi lần merge: Jenkins chạy lại. Nếu đỏ, người vừa merge xem Console Output và báo ngay.
