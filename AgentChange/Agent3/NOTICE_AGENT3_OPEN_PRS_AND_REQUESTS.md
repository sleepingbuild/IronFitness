# Agent 3: năm PR đang chờ duyệt, thứ tự merge và các việc cần từ agent khác

Điền số PR vào chỗ `#__` trước khi gửi. Mỗi mục gửi cho đúng người.

---

## Gửi Agent 1

### A. PR backend (hai PR, độc lập nhau, nhánh base là `master`)

| PR | Nhánh | Nội dung |
|---|---|---|
| #__ | `feature/agent3-available-auth` | Một dòng `@PreAuthorize` cho `GET /trainer-schedules/available` (MEMBER, RECEPTIONIST, SUPER_ADMIN) và sửa `API_DESIGN.md`. Đã chạy thật: hội viên 200, PT 403 với `errorCode = AUTH_FORBIDDEN_ROLE`. |
| #__ | `feature/agent3-dev-seed` | `scripts/dev-seed/20_bookings.sql`. Chạy lại không nhân đôi dữ liệu (đã thử). Thiếu user `@gym.test` thì chỉ in `THIEU USER` rồi dừng. |

### B. PR frontend (ba PR chồng nhau, MERGE THEO ĐÚNG THỨ TỰ)

| Thứ tự | PR | Nhánh | Base của PR | Nội dung |
|---|---|---|---|---|
| 1 | #__ | `feature/agent3-frontend` | `master` | `/schedule`: PT khai ca làm việc (xem, thêm, tạm tắt/bật lại, xoá). Đánh dấu `ready` ở `navigation.ts`. |
| 2 | #__ | `feature/agent3-trainer-bookings` | `feature/agent3-frontend` | `/schedule`: tab "Buổi dạy" cho PT (xác nhận, hoàn thành, huỷ, nộp lý do rời sớm). |
| 3 | #__ | `feature/agent3-member-bookings` | `feature/agent3-trainer-bookings` | `/bookings`: "Buổi tập của tôi" cho hội viên (xem, huỷ, cảnh báo huỷ muộn) và 7 test cho hàm thuần. |

PR 2 và 3 import file nằm trong PR trước, nên merge sai thứ tự sẽ không biên dịch được. Sau khi PR 1 merge, đổi base của PR 2 về `master` (và tương tự cho PR 3 sau khi PR 2 merge). Nếu bạn merge bằng "Squash and merge" thì báo Agent 3, vì các PR sau sẽ cần `git rebase --onto` để tránh xung đột.

Mỗi PR frontend đều đã chạy `lint`, `typecheck`, `test`, `build` xanh, và đã thử thật trên backend cục bộ.

### C. Việc cần Agent 1

1. **`GET /api/v1/trainers`** (id, fullName, avatarUrl của PT đang hoạt động): màn hình đặt lịch của hội viên (`/bookings`, phần chọn PT) đang chờ endpoint này. Ngày dự kiến?
2. **`GET /api/v1/branches`** và **`GET /api/v1/users?role=MEMBER&q=`**: màn hình của lễ tân và Super Admin ở `/bookings` cần cả hai (`GET /bookings` bắt buộc `branchId`, và đặt hộ cần chọn hội viên). Hiện hai vai trò này vào `/bookings` nhận thông báo không có quyền.
3. **`00_users.sql`**: khi vào `master`, Agent 3 sẽ chạy lại seed với tài khoản chính thức và báo kết quả.

### D. Câu hỏi về cấu hình

Menu "Đặt lịch PT" vẫn ở trạng thái "Sắp có" vì màn hình mới chỉ xem và huỷ. Khi đặt lịch mới xong, Agent 3 sẽ đổi sang `ready`.

---

## Gửi Agent 4: xác nhận một giá trị

Seed của Agent 3 ghi các cột `check_in_method`, `pt_check_in_method` và `pt_check_out_method` bằng chuỗi `'FACE'`. `V3` chỉ quy định `VARCHAR(20)`, nên DB chấp nhận, nhưng đây là phỏng đoán của Agent 3. `recordFaceScan` và `recordTrainerFaceScan` ghi chuỗi nào vào các cột này? Nếu khác `'FACE'`, Agent 3 sửa seed.

Ngoài ra màn hình Buổi dạy của PT hiển thị giờ `ptCheckInTime` và `ptCheckOutTime` do Agent 4 ghi, nên khi trang kiosk của Agent 4 xong, hai bên nên thử chung một luồng: PT quét vào, quét ra, rồi bấm "Hoàn thành buổi".

---

## Gửi Agent 2: xác nhận số lần trừ buổi

Listener `BookingSettledEvent` (PR #63) đã merge. Phía Agent 3 đã kiểm chứng trên SQL Server thật rằng hai request huỷ muộn đồng thời lên cùng một booking chỉ có một request thành công. Chưa có ai đếm số lần `tryConsumeSession` thực tế sau khi chốt buổi. Agent 1 đã nhờ Agent 2 xác nhận ba kết cục (`COMPLETED`, `MEMBER_NO_SHOW`, `LATE_CANCEL`); nếu Agent 2 chưa làm được, Agent 3 có thể thử từ phía mình khi có gói mẫu của hội viên (`10_packages_memberships.sql`).
