# Đối chiếu thiết kế DB với tiêu chí chấm (Bước 2), bản v2

Cơ sở: 83 user story trong `USER_STORIES.md`, đối chiếu với 18 bảng của DB đang chạy (v1, theo `DATABASE_SCHEMA.md` 2026-10-12). Kết quả là thiết kế mục tiêu **v2 gồm 19 bảng, 28 khoá ngoại**. **DB đang chạy vẫn là v1**, các thay đổi ở mục 2 chưa được triển khai và cần Agent 1 hoặc Phi duyệt.

Hình tương ứng: `gms_v2_0_overview` (tổng quan, chỉ cột khoá), `gms_v2_1_nguoidung_goitap`, `gms_v2_2_lichtap_diemdanh_luong`, `gms_v2_3_thanhtoan_banle` (đủ cột, mỗi sơ đồ một phân hệ), `gms_v2_schema` (một sơ đồ đủ cột).

## 1. Kết quả theo tiêu chí (v2)

| Tiêu chí | Trọng số | Kết quả | Bằng chứng |
|---|---|---|---|
| Thực thể và thuộc tính | 20% | Đạt | 19 bảng; 82/82 user story có dữ liệu đều ánh xạ được vào ít nhất 1 bảng (C1 chỉ dùng Redis); 19/19 bảng được ít nhất 1 user story dùng (mục 5); mọi cột có kiểu phù hợp |
| Khoá PK và FK | 15% | Đạt | PK 19/19 (`user_roles` dùng PK kép); 28 FK; không còn cột `*_id` nào thiếu khoá ngoại; mặc định `ON DELETE NO ACTION` nên không xoá được cha khi còn con |
| Mối quan hệ | 15% | Đạt | 4 quan hệ 1-1, 24 quan hệ 1-N; N-N tách bằng 4 bảng trung gian (mục 3) |
| Chuẩn hoá | 10% | Đạt 2NF và 3NF | Mục 4; còn một nhóm cột lặp ở `bookings` được giải trình |

## 2. Thay đổi từ v1 sang v2

Phát hiện khi đối chiếu từng user story với từng bảng:

| # | Thay đổi | Lý do | Ai triển khai |
|---|---|---|---|
| CH1 | `payment_transactions`: bỏ cặp `related_type` và `related_id`, thay bằng 2 cột FK `member_package_id`, `pos_order_id` (đúng một cột có giá trị, ràng buộc bằng `CHECK`); `user_id` cho phép `NULL` | Tiêu chí FK: `related_id` trỏ tới hai bảng khác nhau nên không có khoá ngoại. Story **R20** (bán lẻ cho khách vãng lai): `pos_orders.member_id` cho phép trống nhưng `payment_transactions.user_id` đang `NOT NULL`, nên không ghi nhận được thanh toán của khách vãng lai | Agent 2: migration mới (thêm cột, chuyển dữ liệu cũ, xoá cột cũ), sửa entity, `PaymentService`, test. Agent 5: thêm FK sang `pos_orders` ở V5 khi bảng đó tồn tại |
| CH2 | Bảng mới `gym_check_ins` (lượt vào phòng tập không gắn buổi PT) | Stories **M16, M17, R14, A7**: v1 chỉ ghi điểm danh vào `bookings`, nên hội viên vào phòng mà không có buổi PT thì không có chỗ lưu, lịch sử check-in và báo cáo lưu lượng chỉ phản ánh các buổi PT | Agent 4 (luồng quét mặt khi không có booking ghi vào bảng mới thay vì bỏ qua), Agent 3 xác nhận; cần migration mới |
| CH3 | `user_profiles.age` đổi thành `date_of_birth` | Tuổi suy ra từ ngày sinh và lỗi thời theo thời gian (tiêu chí chuẩn hoá) | Agent 1: migration, entity, DTO, quy tắc tuổi tối thiểu 18 |
| CH4 | Bỏ `users.avatar_url` | Trùng `user_profiles.avatar_url` (đã ghi DEPRECATED) | Agent 1: migration, sửa entity `User` (file SHARED) |
| CH5 | Bỏ `pos_order_items.subtotal` và `pos_orders.total_amount` | Suy ra được từ `quantity` × `unit_price` | Agent 5: V5 chưa viết nên chỉ cần viết đúng ngay từ đầu |

Lưu ý khi đặt số migration: V3 và V4 đang chờ merge, nếu một migration có số nhỏ hơn được merge sau thì Flyway báo lỗi, cần thống nhất thứ tự merge hoặc bật `outOfOrder`.

## 3. Quan hệ

N-N được tách bằng bảng trung gian:

| N-N | Bảng trung gian | Ghi chú |
|---|---|---|
| `users` ↔ `roles` | `user_roles` | PK kép (`user_id`, `role_id`), không có thuộc tính riêng |
| `users` ↔ `packages` | `member_packages` | Thực thể kết hợp: `status`, `start_date`, `end_date`, `remaining_sessions` |
| `pos_orders` ↔ `products` | `pos_order_items` | Thuộc tính: `quantity`, `unit_price` |
| Học viên ↔ PT (cùng là `users`) | `bookings` | Thực thể kết hợp: ngày giờ, `status`, điểm danh |

Quan hệ 1-1 (cột FK có `UNIQUE`): `user_profiles.user_id`, `face_profiles.user_id`, `trainer_pay_rates.trainer_id` đều trỏ tới `users`; `commissions.booking_id` trỏ tới `bookings` (mỗi buổi tối đa một bản ghi trả công). `payment_transactions` có hai FK tuỳ chọn, mỗi giao dịch chỉ gắn với đúng một đối tượng (gói tập hoặc đơn bán lẻ). Còn lại là quan hệ 1-N (24).

## 4. Chuẩn hoá

**2NF:** 18 bảng có PK đơn nên không có phụ thuộc hàm riêng phần; `user_roles` có PK kép nhưng không có thuộc tính ngoài khoá.

**3NF:** sau CH3 đến CH5 không còn cột suy ra được hoặc trùng lặp. Các điểm còn lại là có chủ ý:
- `bookings` có hai nhóm cột cùng cấu trúc (`check_in_*`, `check_out_*` của học viên; `pt_check_in_*`, `pt_check_out_*` của PT). Mỗi buổi tập luôn có đúng hai người tham gia với vai trò cố định, và hai nhóm này có luật nghiệp vụ khác nhau (PT điểm danh để tính công, học viên điểm danh để tránh bị tính vắng). Phương án thay thế là tách bảng `booking_attendances`, chỉ nên làm nếu giảng viên yêu cầu chặt vì Agent 3 và 4 đã code theo cấu trúc hiện tại.
- `unit_price`, `payment_transactions.amount`, `commissions.amount` là giá trị lịch sử tại thời điểm giao dịch, không phải dữ liệu dư thừa.
- `member_packages.end_date` được chốt lúc thanh toán; đổi thời hạn gói sau đó không ảnh hưởng gói đã bán.
- `face_profiles.descriptor` là một vector đặc trưng khuôn mặt, chỉ so sánh nguyên khối, không truy vấn từng phần tử.

## 5. Mỗi bảng phục vụ những user story nào

| Bảng | Số story | Các story (tối đa 10 ID đầu) |
|---|---|---|
| `roles` | 6 | G1, G2, R22, S4, A18, A19 |
| `users` | 15 | G1, G2, M1, T1, T14, R1, R22, S3, S4, S5, ... |
| `user_roles` | 6 | G1, G2, R22, S4, A18, A19 |
| `branches` | 3 | R10, A13, A21 |
| `user_profiles` | 7 | M1, M2, M3, T1, T2, T14, R22 |
| `packages` | 20 | G3, M4, M5, M6, M7, M8, M11, M12, R1, R2, ... |
| `member_packages` | 20 | M4, M5, M6, M7, M8, M11, M12, M13, T11, R1, ... |
| `payment_transactions` | 13 | M4, M7, M8, M9, R1, R2, R3, R4, R5, R6, ... |
| `trainer_schedules` | 10 | M10, M11, M12, T3, T4, T5, T6, R7, R8, A15 |
| `bookings` | 22 | M10, M11, M12, M13, M14, M16, M17, T6, T7, T8, ... |
| `face_profiles` | 10 | M15, M16, T9, T12, R11, R12, R13, R14, R15, A16 |
| `staff_attendances` | 3 | R15, R16, A12 |
| `gym_check_ins` | 4 | M16, M17, R14, A7 |
| `products` | 6 | R17, R18, R19, R20, R21, A17 |
| `pos_orders` | 3 | R5, R20, R21 |
| `pos_order_items` | 2 | R20, R21 |
| `trainer_pay_rates` | 4 | T11, T13, A10, A11 |
| `commissions` | 4 | T11, T13, A9, A11 |
| `leads` | 4 | S1, S2, S3, S4 |

## 6. Ma trận user story → bảng dữ liệu

(*) là story chưa có endpoint trong `API_DESIGN.md`, xem `USER_STORIES.md`. `gym_check_ins` là bảng mới của v2.

### Mọi người dùng đã đăng nhập

| ID | Use case | Bảng dữ liệu |
|---|---|---|
| C1 | Đăng xuất hệ thống | không dùng bảng (token lưu Redis) |

### Khách

| ID | Use case | Bảng dữ liệu |
|---|---|---|
| G1 | Đăng ký tài khoản | `users`, `roles`, `user_roles` |
| G2 | Đăng nhập hệ thống | `users`, `user_roles`, `roles` |
| G3 | Xem danh sách gói tập (*) | `packages` |

### Hội viên (MEMBER)

| ID | Use case | Bảng dữ liệu |
|---|---|---|
| M1 | Xem hồ sơ cá nhân | `users`, `user_profiles` |
| M2 | Cập nhật hồ sơ cá nhân | `user_profiles` |
| M3 | Tải lên ảnh đại diện | `user_profiles` |
| M4 | Mua gói tập | `packages`, `member_packages`, `payment_transactions` |
| M5 | Xem gói tập hiện tại | `member_packages`, `packages` |
| M6 | Xem lịch sử gói tập | `member_packages`, `packages` |
| M7 | Nâng cấp gói tập | `member_packages`, `packages`, `payment_transactions` |
| M8 | Hạ cấp gói tập | `member_packages`, `packages`, `payment_transactions` |
| M9 | Xem lịch sử thanh toán | `payment_transactions` |
| M10 | Xem lịch trống của PT | `trainer_schedules`, `bookings` |
| M11 | Đặt lịch tập với PT | `bookings`, `trainer_schedules`, `member_packages`, `packages` |
| M12 | Sửa lịch tập | `bookings`, `trainer_schedules`, `member_packages`, `packages` |
| M13 | Hủy lịch tập | `bookings`, `member_packages` |
| M14 | Xem lịch tập của tôi | `bookings` |
| M15 | Đăng ký khuôn mặt | `face_profiles` |
| M16 | Điểm danh vào phòng tập bằng khuôn mặt | `face_profiles`, `gym_check_ins`, `bookings` |
| M17 | Xem lịch sử check-in (*) | `gym_check_ins`, `bookings` |

### Huấn luyện viên (TRAINER)

| ID | Use case | Bảng dữ liệu |
|---|---|---|
| T1 | Xem hồ sơ cá nhân | `users`, `user_profiles` |
| T2 | Cập nhật hồ sơ cá nhân | `user_profiles` |
| T3 | Tạo ca làm việc | `trainer_schedules` |
| T4 | Sửa ca làm việc | `trainer_schedules` |
| T5 | Xóa ca làm việc | `trainer_schedules` |
| T6 | Xem lịch dạy | `bookings`, `trainer_schedules` |
| T7 | Xác nhận lịch tập | `bookings` |
| T8 | Hủy lịch tập | `bookings` |
| T9 | Điểm danh khuôn mặt buổi dạy | `face_profiles`, `bookings` |
| T10 | Nhập lý do rời buổi sớm | `bookings` |
| T11 | Hoàn thành buổi tập | `bookings`, `member_packages`, `commissions`, `trainer_pay_rates` |
| T12 | Đăng ký khuôn mặt | `face_profiles` |
| T13 | Xem lương theo buổi | `commissions`, `trainer_pay_rates`, `bookings` |
| T14 | Xem chỉ số hội viên đang huấn luyện | `bookings`, `users`, `user_profiles` |

### Lễ tân (RECEPTIONIST)

| ID | Use case | Bảng dữ liệu |
|---|---|---|
| R1 | Bán gói tập cho hội viên | `member_packages`, `packages`, `payment_transactions`, `users` |
| R2 | Gia hạn gói tập | `member_packages`, `packages`, `payment_transactions` |
| R3 | Nâng cấp gói tập hộ hội viên | `member_packages`, `packages`, `payment_transactions` |
| R4 | Hạ cấp gói tập hộ hội viên | `member_packages`, `packages`, `payment_transactions` |
| R5 | Xác nhận thanh toán tại quầy | `payment_transactions`, `member_packages`, `pos_orders` |
| R6 | Xem danh sách giao dịch theo ngày | `payment_transactions` |
| R7 | Đặt lịch tập hộ hội viên | `bookings`, `trainer_schedules`, `member_packages`, `packages` |
| R8 | Sửa lịch tập hộ hội viên | `bookings`, `trainer_schedules`, `member_packages`, `packages` |
| R9 | Hủy lịch tập hộ hội viên | `bookings`, `member_packages` |
| R10 | Xem danh sách lịch tập theo ngày | `bookings`, `branches` |
| R11 | Đăng ký khuôn mặt hộ hội viên | `face_profiles` |
| R12 | Xem hồ sơ khuôn mặt | `face_profiles` |
| R13 | Xóa hồ sơ khuôn mặt | `face_profiles` |
| R14 | Điểm danh hội viên tại kiosk | `face_profiles`, `gym_check_ins`, `bookings` |
| R15 | Chấm công bằng khuôn mặt | `face_profiles`, `staff_attendances` |
| R16 | Xem bảng chấm công của tôi | `staff_attendances` |
| R17 | Thêm sản phẩm bán lẻ | `products` |
| R18 | Cập nhật sản phẩm bán lẻ | `products` |
| R19 | Xem danh sách sản phẩm | `products` |
| R20 | Tạo đơn bán lẻ | `pos_orders`, `pos_order_items`, `products`, `payment_transactions` |
| R21 | Xem danh sách đơn bán lẻ | `pos_orders`, `pos_order_items`, `products` |
| R22 | Tạo tài khoản hội viên tại quầy (*) | `users`, `roles`, `user_roles`, `user_profiles` |

### Nhân viên Sales/CSKH (SALES)

| ID | Use case | Bảng dữ liệu |
|---|---|---|
| S1 | Thêm khách tiềm năng | `leads` |
| S2 | Cập nhật thông tin khách tiềm năng | `leads` |
| S3 | Xem danh sách khách tiềm năng | `leads`, `users` |
| S4 | Chuyển đổi khách tiềm năng thành hội viên | `leads`, `users`, `user_roles`, `roles` |
| S5 | Xem hội viên sắp hết hạn | `member_packages`, `users`, `packages` |

### Quản trị viên (SUPER_ADMIN)

| ID | Use case | Bảng dữ liệu |
|---|---|---|
| A1 | Thêm gói tập | `packages` |
| A2 | Sửa gói tập | `packages` |
| A3 | Ngừng bán gói tập | `packages` |
| A4 | Xem toàn bộ gói tập | `packages` |
| A5 | Xem báo cáo doanh thu | `payment_transactions` |
| A6 | Xem báo cáo tỷ lệ gia hạn | `member_packages` |
| A7 | Xem báo cáo lưu lượng check-in | `gym_check_ins`, `bookings` |
| A8 | Xem báo cáo gói bán chạy | `member_packages`, `packages` |
| A9 | Xem báo cáo tổng lương PT | `commissions`, `users` |
| A10 | Cấu hình mức lương theo buổi cho PT (*) | `trainer_pay_rates`, `users` |
| A11 | Xem lương PT theo tháng | `commissions`, `trainer_pay_rates` |
| A12 | Xem báo cáo chấm công nhân viên | `staff_attendances`, `users` |
| A13 | Xem danh sách lịch tập toàn hệ thống | `bookings`, `branches` |
| A14 | Xem danh sách giao dịch toàn hệ thống | `payment_transactions` |
| A15 | Quản lý ca làm việc của PT | `trainer_schedules` |
| A16 | Quản lý hồ sơ khuôn mặt | `face_profiles` |
| A17 | Quản lý sản phẩm bán lẻ | `products` |
| A18 | Tạo tài khoản nhân viên (*) | `users`, `roles`, `user_roles` |
| A19 | Phân quyền tài khoản (*) | `user_roles`, `roles` |
| A20 | Khóa tài khoản (*) | `users` |
| A21 | Quản lý chi nhánh (*) | `branches` |

## 7. Việc cần làm tiếp

1. Agent 1 hoặc Phi duyệt 5 thay đổi ở mục 2 (đặc biệt CH2, vì ảnh hưởng luồng quét mặt đã code).
2. Mỗi agent tự viết migration và sửa code cho phần của mình theo cột "Ai triển khai".
3. Vẽ lại 5 sơ đồ trên dbdiagram từ các file `gms_v2_*.dbml`.
4. Sau khi triển khai xong, cập nhật `DATABASE_SCHEMA.md` và `docs/ERD_TONG_QUAT.md`.
