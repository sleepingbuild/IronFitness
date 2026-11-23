# feat(seed): 41_pt_live_session.sql (buổi PT đang diễn ra) + docs: giờ PT quét trong BookingResponse

Nhánh: `feature/agent4-live-seed` → `master`. Không đổi mã Java.

Liên quan: mục "Agent 4" điểm 1 và 2 trong `NHIEM_VU_MOI_TAT_CA_AGENT.md`.

## Nội dung

**1. Tài liệu (`API_DESIGN.md`).** Mô tả các trường giờ PT quét trong `BookingResponse` (`ptCheckInTime`, `ptCheckInMethod`, `ptCheckOutTime`, `ptCheckOutMethod`, `ptEarlyLeaveReason`, `fullDuration`). Không thêm trường mới: các trường này đã có sẵn trong `BookingResponse`, và do `BookingService.recordTrainerFaceScan` (Agent 3) ghi khi PT quét mặt. Phần Agent 4 chỉ chuyển lời gọi quét vào service đó.

**2. Seed (`scripts/dev-seed/41_pt_live_session.sql`).** Chỉ dùng cho database dev, chạy tay trong SSMS, chạy lại nhiều lần không lỗi. Tạo một booking `CONFIRMED` hôm nay ở khung giờ chẵn hiện tại (giờ Việt Nam): PT `trainer2@gym.test`, hội viên `member04@gym.test`, PT và hội viên đã quét vào, chưa ai quét ra. Dùng để thử màn hình PT (giờ vào có, giờ ra để trống) và thử quét ra sớm.

## Điều cần biết

- Chạy sau `00_users.sql` và `40_attendance.sql` (cần `member04@gym.test`).
- Ghi chú dòng seed là `[dev-seed-a4]` (khác `[dev-seed]` của `20_bookings.sql`, nên hai file không xóa nhầm nhau). Chạy lại chỉ xóa dòng seed **chưa có hoa hồng**, nên không gây lỗi `fk_commissions_booking` như đã gặp ở Docker.
- Không dùng `member.active` và `trainer1`: `20_bookings.sql` đã có buổi "đã học xong" của họ, thêm buổi nữa sẽ chồng giờ và làm kiosk chọn nhầm buổi.
- `member04` không có gói tập, nên nút "Hoàn thành" của buổi này có thể lỗi. Muốn thử "Hoàn thành" thì dùng buổi "đã học xong" của `member.active`.
- Chỉ chạy trong khung 06:00 đến 21:59 giờ Việt Nam; ngoài khung thì in thông báo và bỏ qua.
- Gói Docker của Agent 1 chỉ nạp các file seed họ liệt kê, nên file này chưa có trong Docker nếu Agent 1 chưa thêm.

## Đã thử

Trên database `gms_db_agent4`:

- [x] Chạy trong SSMS: có đúng một dòng, `CONFIRMED`, 17:00 đến 18:00, `trainer2@gym.test` và `member04@gym.test`, `pt_check_in_time` 17:03, `check_in_time` 17:04, `pt_check_out_time` và `check_out_time` là `NULL`
- [ ] Chạy lại lần hai vẫn đúng một dòng
- [x] `GET /api/v1/bookings/me` bằng tài khoản `trainer2` trả 200 và có booking của `member04`
- [ ] Response có `ptCheckInTime` khác null và `ptCheckOutTime` là null

Chưa chạy `mvn verify`: PR này chỉ thêm file SQL và sửa tài liệu.
