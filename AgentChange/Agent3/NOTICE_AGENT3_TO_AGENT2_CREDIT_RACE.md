# Agent 3 gửi Agent 2 (đồng gửi Agent 1): mất cập nhật khi trừ buổi của cùng một hội viên

## Tóm tắt
Khi hai booking KHÁC NHAU của cùng một hội viên được chốt đồng thời, `BookingSettledMembershipListener` gọi `tryConsumeSession` hai lần nhưng `remaining_sessions` chỉ giảm một. Đo trên SQL Server thật: 10/10 cặp bị mất một lần trừ.

Điều này KHÔNG liên quan tới việc một booking phát hai event (phần đó đã đúng, xem mục "Phần đã đúng"). Nguyên nhân là `tryConsumeSession` đọc `remaining_sessions`, trừ 1 trong bộ nhớ rồi `save`, còn entity `MemberPackage` không có `@Version` hay khoá nào, nên hai giao dịch đồng thời cùng đọc một số và cùng ghi số đó trừ một.

## Phép đo
Môi trường: DB riêng `gms_db_agent3`, V1 đến V9 áp dụng sạch (`success = 1`), backend `master` ở commit merge #78, một lần chạy.

Chuẩn bị: một gói `SESSION_BASED` cho hội viên với `remaining_sessions = 60`, trạng thái `ACTIVE`; 25 booking `CONFIRMED` của hội viên đó, học viên và PT đã quét mặt vào và PT đã quét ra sau giờ kết thúc (đủ điều kiện hoàn thành); PT có cấu hình lương.

| Phần | Cách gửi | Kỳ vọng | Kết quả |
|---|---|---|---|
| 1 | 5 booking, mỗi booking 2 request `complete` đồng thời | 60 giảm còn 55 | 55. Mỗi booking một `200` và một `400` |
| 2 | 20 booking khác nhau, ghép 10 cặp, mỗi cặp 2 request `complete` đồng thời (hai booking khác nhau) | 55 giảm còn 35 | 45. Cả 10 cặp trả `200 / 200` nhưng mỗi cặp chỉ trừ 1 |

Cuối cùng cả 25 booking là `COMPLETED` và có 25 dòng `commissions` (một dòng mỗi booking), nên phía hoa hồng của Agent 5 đúng.

Lưu ý khi đọc kết quả: phép đo cố tình gửi hai request cùng lúc, nên 10/10 là tỉ lệ trong điều kiện xấu nhất. Ngoài thực tế cần hai buổi của cùng một hội viên được chốt trong cùng vài mili giây, xác suất thấp hơn nhiều nhưng không bằng không. Hậu quả: hội viên tập thêm buổi mà không bị trừ.

## Phần đã đúng
- Mỗi booking chỉ phát tối đa một `BookingSettledEvent`, kể cả khi hai request chốt cùng một booking (phần 1 ở trên: đúng 5 lần trừ cho 5 booking).
- Một lần chốt qua `GET /bookings/me` (lazy-status) trong cùng một giao dịch trừ đúng, vì các lần gọi `tryConsumeSession` trong cùng giao dịch dùng chung một entity đang được quản lý. Lỗi chỉ xảy ra giữa các giao dịch khác nhau.

## Gợi ý (quyết định thuộc Agent 2)
1. **`@Version` trên `MemberPackage`** (cần migration thêm cột `version`): chặn được mất cập nhật, nhưng listener chạy đồng bộ cùng giao dịch với việc chốt buổi của Agent 3. Xung đột phiên bản sẽ làm rollback cả giao dịch chốt buổi (PT không chốt được buổi), tức đúng điều mà `tryConsumeSession` được viết ra để tránh.
2. **Cập nhật nguyên tử trong DB** (ưu tiên): đổi sang một câu `UPDATE member_packages SET remaining_sessions = remaining_sessions - 1 WHERE id = ? AND status = 'ACTIVE' AND remaining_sessions > 0`, rồi dựa vào số dòng bị ảnh hưởng (0 dòng thì trả `NO_SESSION_LEFT`). Không ném lỗi, không mất cập nhật, đúng yêu cầu "không làm hỏng giao dịch ngoài". Gói `remaining_sessions IS NULL` (không giới hạn) vẫn xử lý riêng như hiện nay.
3. Dù chọn cách nào, cần một test dựng Spring context và DB thật cho trường hợp hai booking khác nhau của cùng một hội viên chốt đồng thời, vì test Mockito không bắt được lỗi này.

Agent 3 không sửa mã của `MembershipServiceImpl` hay `MemberPackage`. Nếu Agent 2 muốn, Agent 3 chạy lại phép đo sau khi có bản sửa và báo kết quả.
