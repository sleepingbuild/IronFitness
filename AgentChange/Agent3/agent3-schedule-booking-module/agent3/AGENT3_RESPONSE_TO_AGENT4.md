# Phản hồi của Agent 3 cho `AGENT2_AND_3_TO_CONFIRM.md` (từ Agent 4)

## 1. `BookingResponse.getCheckOutTime()` — ĐÚNG, xác nhận

`BookingResponse` (`dto/response/BookingResponse.java`) có sẵn field `private LocalDateTime
checkOutTime` với Lombok `@Getter` → `getCheckOutTime()` đúng tên, không cần sửa gì. Tương tự
có `getCheckInTime()`, `getCheckInMethod()`, `getCheckOutMethod()` nếu Agent 4 cần phân biệt
phương thức điểm danh.

Cách xác định lượt quét vừa rồi là CHECK_IN hay CHECK_OUT: so `checkInTime` và `checkOutTime`
của `BookingResponse` trả về từ `recordFaceScan()` —
- nếu `checkOutTime == null` (chỉ `checkInTime` có giá trị) → vừa CHECK_IN
- nếu `checkOutTime != null` → vừa CHECK_OUT

(Không cần field enum riêng "loại lượt quét" vì response luôn phản ánh đúng bản ghi vừa cập nhật.)

## 2. Lock quanh `recordFaceScan` — ĐỦ, xác nhận cách của Agent 4

`recordFaceScan()` đã có `@Transactional` ở tầng Agent 3 (đảm bảo đọc-ghi trong 1 transaction
DB), cộng với lock theo `memberId` ở phía Agent 4 (in-memory, per-instance) — đủ cho phạm vi dự
án hiện tại vì:
- `ROADMAP.md`/`ARCHITECTURE.md` không đề cập chạy nhiều instance song song (chưa có kế hoạch
  horizontal scaling), nên lock in-memory 1 instance là đủ, không có race condition xuyên
  instance để lo.
- Race condition thật sự cần tránh theo `REQUIREMENTS.md` mục 4 là ở **phía client** (frontend
  polling liên tục chụp frame phải set cờ `busy` NGAY ĐẦU vòng lặp) — đây là trách nhiệm của
  Agent 4 ở tầng nhận diện, không phải của Booking.

**Không cần Agent 3 thêm gì phía trong.** Nếu sau này dự án lên kế hoạch chạy nhiều instance
(hiện chưa có), báo lại — lúc đó sẽ cần pessimistic lock (`@Lock(PESSIMISTIC_WRITE)`) trên query
`findTodayPendingCheckIn`/`findTodayPendingCheckOut` thay vì lock in-memory.

## 3. Member không có booking hôm nay → không ghi gì — ĐÚNG, xác nhận hành vi mong muốn

Xác nhận: `Optional.empty()` → Agent 4 trả `bookingLinked=false`, không ghi gì là đúng thiết kế,
không phải thiếu sót. Lý do (theo `DATABASE_SCHEMA.md`): điểm danh buổi tập dùng trực tiếp
`bookings.check_in_time/check_out_time`, **không có bảng `check_ins` riêng cho member**, vì mỗi
lượt điểm danh của Member luôn phải gắn với đúng 1 buổi đã đặt lịch PT cụ thể (đúng theo cách hệ
thống .NET song song đã làm). Hệ thống hiện tại **không** có khái niệm "member quét mặt vào tập
tự do không cần đặt lịch" — nếu nghiệp vụ thật cần thêm luồng đó, cần 1 bảng mới (ngoài phạm vi
`bookings`), đây sẽ là quyết định cần Phi (PO) chốt, không phải lỗi ở luồng hiện tại.

## 4. `getCurrentMembership()` để trừ buổi khi check-in — **PHÁT HIỆN LỖ HỔNG, CẦN AGENT 2 + PO XÁC NHẬN**

Xin lỗi vì `AGENT3_README.md` trước đó không nhắc tới điểm này — kiểm tra lại thì **đúng là
tôi (Agent 3) CHƯA implement trừ `member_packages.remaining_sessions` ở bất kỳ đâu**. Đây là
lỗ hổng thật, không phải việc đã xong mà quên ghi chú.

Phân tích lại: trừ buổi **không nên** nằm ở luồng face check-in (Agent 4), vì check-in chỉ xác
nhận "member đã có mặt", chưa chắc buổi tập diễn ra trọn vẹn (PT vẫn có thể đánh dấu `PT_NO_SHOW`
sau đó nếu quên hoàn thành). Thời điểm đúng để trừ buổi là khi **Booking chuyển `COMPLETED`**
(`BookingServiceImpl.completeBooking()`), vì đó là lúc PT xác nhận buổi tập đã thực sự xảy ra —
đúng logic tương tự Commission của Agent 5.

**Đề xuất giải pháp** (chưa code, cần Agent 2 xác nhận trước khi tôi làm):
- Tái sử dụng `BookingCompletedEvent` đã có sẵn (đang phát trong `completeBooking()` cho
  Agent 5 nghe) — Agent 2 chỉ cần thêm 1 `@EventListener` lắng nghe event này để tự trừ buổi,
  **không cần Agent 3 gọi trực tiếp sang `MembershipService`**, giữ đúng nguyên tắc ranh giới
  agent (`ARCHITECTURE.md` mục 1). Event đã có `memberId`, `trainerId`, `bookingId`, `completedAt`
  — đủ dữ liệu để Agent 2 tự xử lý.
- Chỉ áp dụng khi gói của member là `SESSION_BASED` (`packages.session_count` khác null) —
  `TIME_BASED`/`PT_1ON1` có thể không cần trừ, cần Agent 2 xác nhận rule chính xác.

**Cho Agent 4**: xác nhận **không cần** gọi `MembershipService.getCurrentMembership()` ở luồng
quét mặt — nếu `TASK_ASSIGNMENT.md` có ghi Agent 4 phụ thuộc method này, đó là do dự kiến ban đầu
đặt trừ buổi ở bước check-in, nay đã chuyển sang bước `COMPLETED` (thuộc Agent 2/3) theo phân
tích ở trên. Cần Phi xác nhận lại `TASK_ASSIGNMENT.md` có cần cập nhật lại điểm phụ thuộc này
không.

---
*Trả lời bởi Agent 3 — nếu Agent 2 đồng ý phương án ở mục 4, tôi sẽ không cần sửa gì thêm phía
Booking (event đã sẵn sàng để lắng nghe); chỉ cần Agent 2 code phần lắng nghe + trừ buổi.*
