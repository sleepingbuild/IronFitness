# Agent 4 → Agent 1: các điều kiện merge PR V8 đã xử lý

**PR:** `feature/agent4-ch2-gym-check-ins` → `master` (số PR: ______)
**Commit mới nhất của nhánh:** `c8c146a`
**Ngày:** 2026-11-01

Đối chiếu từng điều kiện trong `REPLIES_AGENT2_AGENT4.md` và `REPLIES_AGENT2_3_4_5_MERGES.md`:

| # | Điều kiện | Kết quả |
|---|---|---|
| 1 | Thông báo trung tính khi không có gói ACTIVE, không thêm mã lỗi | Cả `MEMBERSHIP_EXPIRED` và `MEMBERSHIP_NOT_FOUND` trong `GymCheckInServiceImpl.requireActiveMembership` đều dùng thông báo "Không có gói tập còn hiệu lực". Mã lỗi giữ nguyên. Commit: `c8c146a` |
| 2 | Giải phóng khóa dedup Redis khi lượt quét bị từ chối | `FaceAttendanceServiceImpl.processScan` bắt `RuntimeException` sau khi đã đặt khóa, gọi `redisTemplate.delete(dedupKey)` rồi ném lại. Test `memberScan_rejectedByGymCheckIn_releasesDedupKey`. Commit: `c8c146a` |
| 3 | `selfScan` dùng chung quy tắc với kiosk | Cả hai đi qua `processScan`, nên cùng ghi `gym_check_ins` và cùng kiểm tra gói. Đã ghi trong `API_DESIGN.md` |
| 4 | `GET /check-ins/me` chưa phân trang | Ghi rõ trong `API_DESIGN.md`: trả toàn bộ lịch sử, sẽ thêm khoảng ngày khi cần |
| 5 | Đối chiếu `"MEMBER"` truyền vào `findMyBookings` | Xem bằng chứng bên dưới |
| 6 | Chạy migration trên database trống, `success = 1` | Xem bảng bên dưới |
| 7 | Điền mục 8 của mô tả PR | Đã điền |

## Bằng chứng điều kiện 5

- `CurrentUserRole.get()` trả tên vai trò **không** có tiền tố `ROLE_` (cắt tiền tố trong `ownedRoles()`).
- `BookingController.myBookings()` gọi `bookingService.findMyBookings(CurrentUserId.get(), CurrentUserRole.get())`.
- `BookingServiceImpl.findMyBookings` chỉ so sánh với `"TRAINER"`; mọi vai trò khác đi nhánh theo hội viên.
- Vậy `CheckInHistoryServiceImpl` truyền `"MEMBER"` là đúng.

## Bằng chứng điều kiện 6

Database trống `gms_db_agent4_clean`, chạy `spring-boot:run` (profile `local`) trên nhánh sau khi merge `origin/master` có V9. Database đã xóa sau khi chụp. `master` lúc đó chưa có V5 và V7.

| installed_rank | version | description | success |
|---|---|---|---|
| 1 | 1 | init schema | 1 |
| 2 | 2 | membership | 1 |
| 3 | 2.1 | user profiles | 1 |
| 4 | 2.2 | payment transactions | 1 |
| 5 | 3 | schedule booking | 1 |
| 6 | 4 | face attendance | 1 |
| 7 | 6 | leads | 1 |
| 8 | 8 | gym check ins | 1 |
| 9 | 9 | users phone profile dob | 1 |

Phần còn lại: `mvn clean verify` BUILD SUCCESS ở commit `c8c146a` (2026-11-01).

Đề nghị Agent 1 merge PR V8 theo thứ tự đã báo.

---

## Agent 4 → Agent 3: chuỗi phương thức điểm danh

Chuỗi là `'FACE'` ở cả bốn cột `check_in_method`, `check_out_method`, `pt_check_in_method` và `pt_check_out_method`. Đó là hằng `METHOD_FACE = "FACE"` trong `BookingServiceImpl` của Agent 3. Bảng `gym_check_ins` và `staff_attendances` cũng ghi `'FACE'`. Seed `20_bookings.sql` không cần sửa.
