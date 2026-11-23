# Agent 4 gửi Agent 3: giờ PT quét vào/ra theo booking

Liên quan: mục "Agent 4" điểm 1 và mục "Agent 3" điểm 6 trong `NHIEM_VU_MOI_TAT_CA_AGENT.md`. Đối chiếu trên `origin/master` tại `a0eaeae`.

## Kết luận

**Không cần thêm trường mới.** `BookingResponse` đã có đủ dữ liệu để hiển thị giờ vào/ra của PT:

| Trường (`BookingResponse`) | Ý nghĩa |
|---|---|
| `ptCheckInTime` | Giờ PT quét vào buổi. `null` nếu PT chưa quét vào |
| `ptCheckInMethod` | Cách ghi nhận giờ vào của PT |
| `ptCheckOutTime` | Giờ PT quét ra. `null` cho tới khi PT quét ra (nghĩa là buổi đang diễn ra hoặc PT chưa quét ra) |
| `ptCheckOutMethod` | Cách ghi nhận giờ ra của PT |
| `ptEarlyLeaveReason` | Lý do PT rời sớm, nếu có |
| `fullDuration` | Tính động, không lưu DB. `null` = PT chưa quét ra; `true` = ra từ giờ kết thúc trở đi, hoặc ra sớm nhưng có lý do; `false` = ra sớm không lý do |

Cùng một DTO còn có `checkInTime`, `checkOutTime` (giờ hội viên quét). Hai nhóm này độc lập.

## Luồng ghi dữ liệu (để Agent 3 biết nguồn)

- PT quét mặt (kiosk hoặc tự quét) đi qua `FaceAttendanceServiceImpl.processTrainerScan`, và hàm này gọi `bookingService.recordTrainerFaceScan(trainerId, now)` của Agent 3. **Chính service của Agent 3 ghi** `ptCheckInTime` và `ptCheckOutTime`; Agent 4 chỉ gọi vào.
- Phản hồi quét của PT (`FaceAttendanceResponse`) trả `bookingId`, `action` (`CHECK_IN` hoặc `CHECK_OUT`), `earlyLeave` và `reasonRequired` (cả hai chỉ có nghĩa khi `CHECK_OUT` trước giờ kết thúc buổi).
- PT quét mà không có buổi phù hợp: không phải lỗi, trả `action = NONE`, `bookingLinked = false` và `message = "Không có buổi nào cần điểm danh"`.
- "Bây giờ" của lượt quét lấy từ bean `Clock` (`LocalDateTime.now(clock)`), đã đúng luật.

## Việc Agent 4 không làm

- Quy tắc 45 phút (hoàn thành trực tiếp hoặc popup lý do) và nút `Client_No_Show` là logic booking của Agent 3, Agent 4 không đụng.
- Thời lượng thực tế nên tính từ `ptCheckInTime` nếu có, nếu không thì từ giờ bắt đầu theo lịch, đúng như mục 5 của nhiệm vụ.

## Điều chưa kiểm chứng (nhờ Agent 3 xác nhận)

- Các trường `pt*` đã được xác nhận trong response của `GET /api/v1/bookings/me` (tài khoản PT, 14/11/2026): buổi đang diễn ra có `ptCheckInTime` khác null, còn `ptCheckOutTime` và `fullDuration` để trống. Chưa kiểm tra `GET /api/v1/bookings/{id}`, `GET /api/v1/bookings?date=...` và `/bookings/me` của hội viên: nhờ Agent 3 xác nhận các endpoint này cũng điền các trường `pt*`.
- Danh sách giá trị hợp lệ của `ptCheckInMethod` và `ptCheckOutMethod` (Agent 4 chỉ biết rằng quét mặt thì ghi nhận giờ, chưa xem giá trị chuỗi cụ thể).

## Đoạn bổ sung cho `API_DESIGN.md` (mục Bookings)

Dán ngay dưới bảng endpoint của booking, hoặc phần mô tả `BookingResponse` nếu đã có:

```
**Giờ PT quét vào/ra (BookingResponse).** `ptCheckInTime` là giờ PT quét vào buổi (null nếu chưa quét). `ptCheckOutTime` là giờ PT quét ra, null cho tới khi PT quét ra, nên buổi đang diễn ra có `ptCheckInTime` khác null và `ptCheckOutTime` null. `ptCheckInMethod`, `ptCheckOutMethod` là cách ghi nhận. `ptEarlyLeaveReason` là lý do PT rời sớm (nếu có). `fullDuration` tính động, không lưu DB: null khi PT chưa quét ra; true khi PT ra từ giờ kết thúc trở đi hoặc ra sớm có lý do; false khi ra sớm không lý do. Giờ là LocalDateTime giờ Việt Nam, không kèm múi giờ. Các trường này do `BookingService.recordTrainerFaceScan` ghi khi PT quét mặt ở `/face-attendance/kiosk` hoặc `/face-attendance/self`.
```
