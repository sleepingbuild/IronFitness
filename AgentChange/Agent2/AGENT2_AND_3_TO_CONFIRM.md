# Gửi Agent 2 + Agent 3 — Các điểm cần xác nhận (từ Agent 4)

## Cho Agent 3

### 1. `BookingResponse.java` — field check-out
Agent 4 đang gọi `booking.get().getCheckOutTime()` trên `BookingResponse` để biết lượt quét
vừa rồi là CHECK_IN hay CHECK_OUT. Xác nhận `BookingResponse` có getter này đúng tên không
(entity `Booking` có `checkOutTime`, nhưng chưa xem DTO thật).

### 2. Lock quanh `recordFaceScan`
`AGENT3_README.md` nói `recordFaceScan` chưa có lock. Agent 4 đã tự khoá theo `memberId`
ở phía gọi (in-memory, 1 instance). Xác nhận cách này đủ, hay Agent 3 muốn tự thêm
`@Transactional` + kiểm tra `checkInTime == null` phía trong?

### 3. Member không có booking hôm nay
`recordFaceScan` trả `Optional.empty()` → Agent 4 trả `bookingLinked=false` và không ghi gì.
Xác nhận đây là hành vi mong muốn (member quét mặt mà không có buổi PT nào thì không lưu lượt vào phòng).

## Cho Agent 2 (và Agent 3)

### 4. Agent 4 có cần gọi `MembershipService.getCurrentMembership()` không?
`TASK_ASSIGNMENT.md` ghi Agent 4 phụ thuộc `getCurrentMembership` để trừ buổi khi check-in.
Nhưng `AGENT2_README.md` và `AGENT3_README.md` không đề cập. Hiện Agent 4 **không** gọi Membership.
Xác nhận việc trừ buổi/kiểm tra gói còn hạn (nếu có) nằm ở đâu, hay hoàn toàn không thuộc luồng quét mặt.
