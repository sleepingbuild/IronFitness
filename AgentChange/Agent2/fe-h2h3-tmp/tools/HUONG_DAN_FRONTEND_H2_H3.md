# Frontend H2 và H3 (Agent 2)

## Thay đổi (6 file trong `frontend/src/features/membership/`)
| File | Thay đổi |
|---|---|
| `api.ts` | Thêm `getMemberCurrentMembership(userId)` (null khi MEMBERSHIP_NOT_FOUND) và `listMemberMemberships(userId, page, size)`; sửa chú thích `listPayments` |
| `MemberLookup.tsx` (mới) | Tab "Tra cứu hội viên": chọn hội viên, xem gói đang dùng, lịch sử gói (phân trang), cảnh báo gói chờ thanh toán, nút "Đổi gói hộ hội viên" |
| `ChangePlan.tsx` | Thêm prop `forStaff`: lời giải thích theo ngôi nhân viên (huỷ gói cũ ngay, xác nhận thu tiền ở tab Chờ xác nhận). Mặc định giữ nguyên cho hội viên |
| `PendingPayments.tsx` | Thêm bộ lọc "Lọc theo người mua" (gửi `userId`) |
| `MemberPicker.tsx` | Thêm prop tuỳ chọn `changeLabel` (bộ lọc dùng "Bỏ lọc người mua") |
| `PaymentsDesk.tsx` | Thêm tab "Tra cứu hội viên" |

## Phụ thuộc backend
- H2 (`GET /member-packages/current?userId=` và `?userId=`) đã có trên `master`.
- Lọc `userId` ở tab Chờ xác nhận: backend cũ bỏ qua tham số này (tab vẫn chạy, chỉ chưa lọc); có tác dụng sau khi H3 (nhánh refund-h3) merge.
- Tìm hội viên dùng `GET /users` của Agent 1; chưa có thì ô nhập mã hội viên (UUID) vẫn dùng được.

## Giới hạn
Mình chưa chạy được `npm`; chỉ kiểm tra cú pháp bằng tsc (không có lỗi cú pháp). Cần `lint`, `typecheck`, `test`, `build` trên máy bạn.
Không thêm test mới vì phần mới không có logic thuần tuý đáng tách.
