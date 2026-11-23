# PR frontend đợt 1 — Agent 4 (Điểm danh khuôn mặt và chấm công)

**Nhánh:** `feature/agent4-frontend` → `master`
**Tác giả:** Agent 4

## 1. Màn hình trong PR

| Route | Vai trò | Nội dung |
|---|---|---|
| `/face-profile` | Member, Trainer, Receptionist, Sales | Đăng ký hoặc đăng ký lại khuôn mặt của chính mình (camera, trích descriptor ở trình duyệt, gửi `POST /api/v1/face-profiles/me`). Hiện trạng thái đăng ký nếu `GET /face-profiles/me` có |
| `/check-in` | Member, Trainer, Receptionist, Sales | Tự điểm danh (`POST /api/v1/face-attendance/self`). PT quét ra sớm thì hiện ô nhập lý do (`PUT /api/v1/bookings/{id}/early-leave-reason`, ít nhất 5 ký tự) |
| `/kiosk` | Receptionist, Super Admin | Kiosk tự quét liên tục (`POST /api/v1/face-attendance/kiosk`), dùng `skipAuthRedirect: true` |
| `/attendance` | Receptionist, Sales, Super Admin | Lễ tân và Sales xem chấm công của mình (`GET /staff-attendance/me`); Super Admin xem báo cáo theo ngày (`GET /staff-attendance?date=`) |
| `/check-ins` | Member | Lịch sử vào phòng tập và buổi PT (`GET /check-ins/me`) |

Mỗi màn có đủ bốn trạng thái (đang tải, rỗng, lỗi API, không có quyền). Năm mục menu của Agent 4 đã đổi `status` thành `"ready"`.

## 2. Thư viện mới (cần Agent 1 xem)

- `@vladmandic/face-api` thêm vào `frontend/package.json` và `package-lock.json`, **ở commit riêng** (`chore(fe): them @vladmandic/face-api`). Là bản fork còn được duy trì của face-api.js (backend ghi nhận face-api.js, descriptor 128 chiều).
- Thư viện chỉ được nạp bằng `import()` trong trình duyệt (không chạy khi dựng phía máy chủ).
- 6 file mô hình trong `frontend/public/models/`: tổng 7.025.512 byte (khoảng 6,7 MiB), dưới ngưỡng 10 MB đã thống nhất.
- `next build` có **2 cảnh báo** `Critical dependency: require function is used in a way in which dependencies cannot be statically extracted` từ `face-api.esm.js` của thư viện. Build vẫn thành công. Nếu cần build không cảnh báo thì cần Agent 1 quyết định (có thể sửa `next.config.mjs`, file thuộc Agent 1).

## 3. Kiểm tra đã chạy

- `npm run lint`, `npm run typecheck`, `npm test`, `npm run build`: đều xanh (điền số test: ______).
- Thử thật trên máy với backend `master` ở cổng 8082 và camera thật:
  - Đăng ký khuôn mặt, tự điểm danh của Lễ tân (chấm công vào), kiosk (chấm công ra), màn chấm công của Lễ tân và báo cáo theo ngày của Admin.
  - Tài khoản thử tạo bằng `/register` rồi đổi vai trò trong database dev; không đưa vào repo (chờ `00_users.sql` của Agent 1).

## 4. Điều chưa kiểm chứng hoặc chưa làm

- Ngưỡng nhận diện `0.45` mới chỉ thử với một người; chưa thử nhiều người cùng lúc, khuôn mặt giống nhau hoặc ánh sáng yếu.
- Lịch sử check-in (`/check-ins`) chưa chụp với dữ liệu thật qua luồng quét, vì hội viên thử chưa có gói ACTIVE (không thể tạo lượt vào phòng). Đã kiểm tra bằng dữ liệu chèn tay vào `gym_check_ins`.
- Màn **Đăng ký hộ** (Lễ tân hoặc Admin đăng ký khuôn mặt cho người khác) **chưa làm**: cần `GET /api/v1/users?role=MEMBER&q=` của Agent 1 để tìm hội viên, hiện chưa có trên `master`.
- Trang `/face-profile` cần `GET /api/v1/face-profiles/me` (PR riêng). Nếu PR đó chưa merge, trang vẫn đăng ký được, chỉ không hiện trạng thái.
- Trạng thái chấm công (đi muộn, về sớm) do backend tính theo ca mặc định toàn hệ thống 07:00 đến 21:00, nên khi thử ban ngày thường ra "đi muộn và về sớm". Đây là hành vi của backend, không phải của giao diện.
- Camera chỉ chạy ở `localhost` hoặc HTTPS; thử trên thiết bị khác cần `next dev --experimental-https`.

## 5. Cách thử

1. Chạy backend `master` (profile `local`, cổng 8082) và `npm run dev` trong `frontend/` với `NEXT_PUBLIC_API_BASE_URL=http://localhost:8082`.
2. Đăng ký tài khoản, vào `/face-profile`, cho phép camera, chụp.
3. Vào `/check-in` và quét. Tài khoản hội viên chưa có gói sẽ nhận "Không có gói tập còn hiệu lực".

## 6. Ảnh chụp màn hình

- [ ] Đăng ký khuôn mặt thành công
- [ ] Tự điểm danh: kết quả thành công
- [ ] Tự điểm danh: lỗi (gói tập hoặc không nhận diện)
- [ ] Kiosk: kết quả
- [ ] Chấm công của Lễ tân
- [ ] Báo cáo chấm công của Admin (có dữ liệu và ngày rỗng)
- [ ] Lịch sử check-in
