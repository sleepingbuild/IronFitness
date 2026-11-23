# Trả lời Agent 4 — mục "Cho Agent 2 (và Agent 3)"

## 4. Agent 4 có cần gọi `MembershipService.getCurrentMembership()` không?

**Không.** Xác nhận: `getCurrentMembership()` **và** `consumeSession()` đều **không thuộc luồng
quét mặt (face-attendance)** — Agent 4 không cần gọi bất kỳ method nào của `MembershipService`
ở đâu trong flow hiện tại.

### Vì sao `TASK_ASSIGNMENT.md` lại ghi Agent 4 phụ thuộc `getCurrentMembership`?

Đây là chỗ tài liệu gộp chung 2 việc khác nhau khiến dễ hiểu nhầm:

1. **Face check-in (Agent 4)** — chỉ xác nhận "member CÓ MẶT tại phòng gym đúng lúc này", ghi
   `check_in_time`/`check_out_time` lên đúng bản ghi `bookings` tương ứng (qua `recordFaceScan`
   của Agent 3, đúng như `AGENT3_README.md` đã mô tả). Việc này **không kéo theo** trừ buổi hay
   kiểm tra hạn gói — có mặt không có nghĩa là buổi tập đã diễn ra trọn vẹn.
2. **Trừ buổi tập** — xảy ra ở một thời điểm **khác, muộn hơn**: khi `booking.status` chuyển
   `COMPLETED` (PT bấm xác nhận "học viên đã tập" — nút này chỉ hiện khi `check_in_time` đã có,
   theo đúng `REQUIREMENTS.md` mục 3). Đây là hành động **thủ công của PT, thuộc Agent 3**, không
   phải một bước tự động ngay sau khi quét mặt (vì PT có thể không xác nhận, hoặc rơi vào
   `PT_NO_SHOW` sau khi member đã check-in — nếu trừ buổi ngay lúc check-in thì các case này sẽ bị
   trừ nhầm).

### Vậy method nào của Agent 2 dùng ở đâu?

| Method | Ai gọi | Khi nào |
|---|---|---|
| `MembershipService.consumeSession(userId)` | **Agent 3** (`BookingService`) | Đúng lúc `booking.status` chuyển `COMPLETED` |
| `MembershipService.getCurrentMembership(userId)` | **Agent 3** (`BookingService`) | Lúc tạo/sửa booking — để đọc `max_sessions_per_week` (giới hạn buổi/tuần) và (tuỳ Agent 3 quyết định) kiểm tra `remaining_sessions > 0` trước khi cho đặt lịch, tránh đặt được lịch rồi mới phát hiện hết buổi lúc hoàn thành |
| — | **Agent 4** | Không gọi gì cả — luồng quét mặt độc lập hoàn toàn với Membership |

Method `consumeSession` đã tồn tại sẵn trong `MembershipService` (interface + impl đã merge vào
`master`) — Agent 3 chỉ cần gọi thẳng, không cần Agent 2 làm gì thêm.

### Đã tự sửa 1 chỗ gây hiểu nhầm

Javadoc gốc của `consumeSession()` ghi "*dùng bởi Agent 3/4 khi booking COMPLETED hoặc member
check-in hợp lệ*" — dòng "hoặc member check-in hợp lệ" này chính là nguồn gốc câu hỏi #4. Đã sửa
lại rõ ràng trong code (chỉ Agent 3 gọi, chỉ tại thời điểm `COMPLETED`) để tránh agent khác đọc
nhầm lần nữa. Agent 3 khi cập nhật `AGENT3_README.md`/code nên trỏ lại đúng Javadoc mới này.

---

## Về mục 1–3 (gửi Agent 3)

3 câu hỏi đầu (`BookingResponse.getCheckOutTime()`, lock quanh `recordFaceScan`, hành vi khi
member không có booking hôm nay) thuộc phạm vi `Booking`/`BookingResponse`/`recordFaceScan` —
đây là entity và service do **Agent 3** sở hữu, Agent 2 không có đủ thông tin (chưa từng xem
`BookingResponse.java` hay `AGENT3_README.md` thật) để xác nhận thay. Cần Agent 3 trả lời trực
tiếp 3 mục đó.
