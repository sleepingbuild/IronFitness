# Nội dung slide: Phân tích yêu cầu và thiết kế CSDL hệ thống quản lý phòng gym (GMS)

Nguồn: `USER_STORIES.md`, `DB_DESIGN_REVIEW.md`, `REQUIREMENTS.md`, `CHANGE_PT_ATTENDANCE_AND_PAY.md`, `DECISIONS_DB_V2.md`.

## Hướng dẫn cho người tạo slide

- Mỗi mục "Slide N" là một slide. Giữ chữ ngắn gọn, bảng giữ nguyên cột và hàng.
- Chỗ đánh dấu **🖼️ [ẢNH DB]** là ảnh xuất từ dbdiagram.io, người trình bày tự chèn. **🖼️ [ẢNH LUỒNG]** là sơ đồ luồng nghiệp vụ (Activity Diagram) do Agent 1 vẽ, cũng tự chèn. Chỉ chừa chỗ trống khoảng 60% diện tích slide, không tự vẽ thay.
- Chỗ ghi **[điền]** là thông tin người trình bày điền (tên nhóm, tên giảng viên, ngày).
- Kịch bản nói nằm ở file riêng `SPEAKER_SCRIPT.md`, không đưa vào slide.

## Danh sách ảnh cần chèn

| Slide | Loại | Ảnh | Nguồn |
|---|---|---|---|
| 7 | 🖼️ ẢNH LUỒNG | Activity Diagram: Mua và thanh toán gói tập | Agent 1 vẽ |
| 8 | 🖼️ ẢNH LUỒNG | Activity Diagram: Vòng đời buổi tập với PT | Agent 1 vẽ |
| 10 | 🖼️ ẢNH DB | ERD tổng quan 19 bảng | dbdiagram: `gms_v2_0_overview` |
| 11 | 🖼️ ẢNH DB | ERD phân hệ Người dùng và Gói tập | dbdiagram: `gms_v2_1_nguoidung_goitap` |
| 12 | 🖼️ ẢNH DB | ERD phân hệ Lịch tập, Điểm danh và Lương PT | dbdiagram: `gms_v2_2_lichtap_diemdanh_luong` |
| 13 | 🖼️ ẢNH DB | ERD phân hệ Thanh toán và Bán lẻ | dbdiagram: `gms_v2_3_thanhtoan_banle` |
| 15 | 🖼️ ẢNH DB (tuỳ chọn) | Cắt vùng `pos_orders`, `pos_order_items`, `products` từ sơ đồ phân hệ 3 | cắt từ ảnh slide 13 |
| 17 | 🖼️ ẢNH DB (tuỳ chọn) | Bảng `payment_transactions` bản cũ (v1) và bản mới (v2) | v1: sơ đồ `gms_1_core_membership`; v2: sơ đồ `gms_v2_3_thanhtoan_banle` |

---

# PHẦN 1: PHÂN TÍCH YÊU CẦU

## Slide 1: Tiêu đề

- **Hệ thống quản lý phòng gym (GMS)**
- Phân tích yêu cầu và thiết kế cơ sở dữ liệu
- [điền: tên nhóm, thành viên, giảng viên, ngày]
- Công nghệ: Spring Boot 3, SQL Server 2022, Redis, JWT

## Slide 2: Bài toán và người dùng

- **Bài toán:** phòng gym cần quản lý hội viên, gói tập, lịch tập với PT, điểm danh, bán lẻ tại quầy và lương PT thay cho sổ sách thủ công.
- **Ứng dụng làm cho ai:** Chủ phòng gym (Quản trị viên), Lễ tân, Huấn luyện viên (PT), Nhân viên Sales, Hội viên, và Khách chưa có tài khoản.
- **Quy trình thực hiện:** Xác định yêu cầu → Thiết kế cơ sở dữ liệu → Code Spring Boot. Thiết kế DB được đối chiếu lại với user story để tìm thiếu sót.

## Slide 3: Tác nhân và giới hạn quyền hạn

| Tác nhân | Được làm | Không được làm |
|---|---|---|
| Khách | Đăng ký, đăng nhập, xem gói tập | Truy cập dữ liệu cá nhân hay nghiệp vụ |
| Hội viên | Quản lý hồ sơ, mua và đổi gói, đặt và huỷ lịch PT, điểm danh, xem lịch sử | Xem dữ liệu hội viên khác; quản lý gói tập hay sản phẩm |
| Huấn luyện viên (PT) | Quản lý ca làm việc, xác nhận và hoàn thành buổi dạy, điểm danh buổi dạy, xem lương | Bán gói; xem báo cáo hệ thống |
| Lễ tân | Bán và gia hạn gói, xác nhận thanh toán, đặt lịch hộ, điểm danh kiosk, đăng ký khuôn mặt hộ, bán lẻ, chấm công | Cấu hình hệ thống; xem báo cáo tài chính toàn hệ thống |
| Nhân viên Sales | Quản lý khách tiềm năng, chuyển thành hội viên, xem hội viên sắp hết hạn | Bán gói trực tiếp; xem thanh toán và báo cáo |
| Quản trị viên | Quản lý gói tập, lương PT, nhân sự, chi nhánh; xem mọi báo cáo | (toàn quyền cấu hình) |

Ghi chú nhỏ: việc hệ thống tự chuyển trạng thái theo thời gian (hết hạn gói, huỷ lịch quá hạn) không phải một tác nhân.

## Slide 4: Chức năng cốt lõi (MVP)

**Hai luồng chính:**
1. Xem gói tập → Mua gói → Thanh toán tại quầy → Gói được kích hoạt
2. Đặt lịch với PT → PT xác nhận → Điểm danh khuôn mặt → Hoàn thành buổi → Trừ buổi của hội viên và tính công cho PT

**Chức năng hỗ trợ:** quản lý gói tập, bán lẻ tại quầy (POS), quản lý khách tiềm năng, báo cáo, chấm công nhân viên.

**Cố ý bỏ qua ở phiên bản 1.0:**
- Lớp tập nhóm (Group X)
- Nhắc gia hạn qua Zalo hoặc SMS
- Chốt ca thu ngân
- Báo cáo nâng cao theo chi nhánh
- Hoa hồng cho Sales (chỉ PT được trả lương theo buổi)

## Slide 5: Danh sách user story

**83 user story, 6 tác nhân và 1 story dùng chung.** Tên use case theo dạng Động từ + Danh từ.

| Tác nhân | Số story | Ví dụ |
|---|---|---|
| Khách | 3 | Đăng ký tài khoản; Đăng nhập hệ thống; Xem danh sách gói tập |
| Hội viên | 17 | Mua gói tập; Nâng cấp gói tập; Đặt lịch tập với PT; Điểm danh vào phòng tập bằng khuôn mặt |
| Huấn luyện viên | 14 | Tạo ca làm việc; Xác nhận lịch tập; Hoàn thành buổi tập; Xem lương theo buổi |
| Lễ tân | 22 | Bán gói tập cho hội viên; Xác nhận thanh toán tại quầy; Tạo đơn bán lẻ; Chấm công bằng khuôn mặt |
| Nhân viên Sales | 5 | Thêm khách tiềm năng; Chuyển đổi khách tiềm năng thành hội viên; Xem hội viên sắp hết hạn |
| Quản trị viên | 21 | Thêm gói tập; Xem báo cáo doanh thu; Cấu hình mức lương theo buổi cho PT; Quản lý chi nhánh |
| Dùng chung | 1 | Đăng xuất hệ thống |

Danh sách đầy đủ nằm ở phụ lục (`USER_STORIES.md`).

## Slide 6: Mẫu user story

Định dạng: **Là [tác nhân], tôi muốn [việc cần làm] để [lợi ích].**

- **M11 Đặt lịch tập với PT:** Là Hội viên, tôi muốn đặt một buổi tập với PT vào khung giờ trống để có buổi tập đúng giờ (hệ thống chặn trùng giờ, quá giới hạn buổi mỗi tuần hoặc hết buổi).
- **T11 Hoàn thành buổi tập:** Là Huấn luyện viên, tôi muốn đánh dấu hoàn thành buổi đã dạy (sau khi hai bên đã điểm danh) để hệ thống trừ buổi của hội viên và tính công cho tôi.
- **R20 Tạo đơn bán lẻ:** Là Lễ tân, tôi muốn lập đơn bán đồ uống hoặc thực phẩm bổ sung cho hội viên hoặc khách vãng lai để hệ thống tự trừ tồn kho và ghi nhận thanh toán.

## Slide 7: Luồng nghiệp vụ 1: Mua và thanh toán gói tập

**🖼️ [ẢNH LUỒNG] Activity Diagram: Mua và thanh toán gói tập** (Agent 1 vẽ)

Các nhánh cần có trong sơ đồ:
- Đang có gói chờ thanh toán → từ chối đăng ký thêm
- Chưa có gói đang hoạt động → tạo đăng ký ở trạng thái chờ thanh toán (PENDING)
- Có gói đang hoạt động, đổi sang gói giá cao hơn hoặc bằng (nâng cấp) → huỷ gói cũ ngay, tạo gói mới PENDING
- Có gói đang hoạt động, đổi sang gói giá thấp hơn (hạ cấp) → giữ gói cũ, tạo gói mới SCHEDULED bắt đầu ngày sau khi gói cũ hết hạn, thu tiền ngay
- Lễ tân xác nhận thanh toán → gói ACTIVE, ngày bắt đầu là ngày thanh toán
- Theo thời gian: hết hạn → EXPIRED; SCHEDULED tới ngày → ACTIVE

**Trường hợp ngoại lệ:** gói đã ngừng bán không mua được; xác nhận thanh toán hai lần không kích hoạt lặp.

## Slide 8: Luồng nghiệp vụ 2: Vòng đời buổi tập với PT

**🖼️ [ẢNH LUỒNG] Activity Diagram: Vòng đời buổi tập với PT** (Agent 1 vẽ)

Mốc thời gian: buổi bắt đầu lúc S, kết thúc lúc E, ân hạn 15 phút.

| Tình huống | Trạng thái | Hội viên | PT |
|---|---|---|---|
| Hội viên hoặc lễ tân huỷ buổi chưa được PT xác nhận | CANCELLED | Không mất buổi | Không có công |
| Huỷ buổi đã xác nhận, còn từ 2 giờ trở lên | CANCELLED | Không mất buổi | Không có công |
| Huỷ buổi đã xác nhận, còn dưới 2 giờ | LATE_CANCELLED | Mất 1 buổi | Vẫn có công |
| PT chưa quét mặt vào sau S + 15 phút (kể cả khi cả hai cùng vắng) | PT_NO_SHOW | Không mất buổi | Không có công |
| PT đã vào, hội viên chưa quét mặt sau S + 15 phút | NO_SHOW | Mất 1 buổi | Vẫn có công |
| Cả hai có mặt, PT hoàn thành buổi | COMPLETED | Trừ 1 buổi | Có công (ra trước giờ kết thúc mà không có lý do thì không tính công) |

Ngoài ra: đặt lịch phải qua kiểm tra (không ở quá khứ, trong ca làm việc của PT, không trùng giờ, còn buổi, trong giới hạn buổi mỗi tuần); buổi chưa được PT xác nhận khi sát giờ sẽ tự huỷ.

---

# PHẦN 2: THIẾT KẾ CƠ SỞ DỮ LIỆU

## Slide 9: Từ yêu cầu đến bảng dữ liệu

1. Mỗi user story được ánh xạ vào các bảng cần để lưu dữ liệu của nó (ma trận 83 story → bảng).
2. Kết quả: 82 story thao tác trên dữ liệu đều ánh xạ vào ít nhất 1 bảng (story Đăng xuất chỉ dùng Redis); cả 19 bảng đều được ít nhất 1 story dùng.
3. Đối chiếu phát hiện thiếu sót → cải tiến thành bản thiết kế v2 (slide 17).

Ví dụ:

| Story | Bảng dữ liệu |
|---|---|
| Đặt lịch tập với PT | `bookings`, `trainer_schedules`, `member_packages`, `packages` |
| Hoàn thành buổi tập | `bookings`, `member_packages`, `commissions`, `trainer_pay_rates` |
| Tạo đơn bán lẻ | `pos_orders`, `pos_order_items`, `products`, `payment_transactions` |

## Slide 10: Tổng quan 19 bảng

**🖼️ [ẢNH DB] ERD tổng quan 19 bảng** (dbdiagram: `gms_v2_0_overview`)

Chia theo phân hệ:

| Phân hệ | Bảng |
|---|---|
| Người dùng và phân quyền | `users`, `roles`, `user_roles`, `user_profiles`, `branches` |
| Gói tập và thanh toán | `packages`, `member_packages`, `payment_transactions` |
| Lịch tập và điểm danh | `trainer_schedules`, `bookings`, `face_profiles`, `staff_attendances`, `gym_check_ins` |
| Bán lẻ | `products`, `pos_orders`, `pos_order_items` |
| Lương PT | `trainer_pay_rates`, `commissions` |
| Khách tiềm năng | `leads` |

Ghi chú dưới ảnh: sơ đồ tổng quan chỉ giữ cột khoá và cột quan hệ; đủ cột ở ba sơ đồ phân hệ (slide 11 đến 13).

## Slide 11: Phân hệ Người dùng và Gói tập

**🖼️ [ẢNH DB] ERD phân hệ 1** (dbdiagram: `gms_v2_1_nguoidung_goitap`, 8 bảng)

- `users` ↔ `roles`: nhiều-nhiều, tách bằng `user_roles`
- `user_profiles`: quan hệ 1-1 với `users`
- `member_packages`: bảng trung gian giữa `users` và `packages`, có thuộc tính riêng (`status`, `start_date`, `end_date`, `remaining_sessions`)
- `leads`: khách tiềm năng, nối `users` hai lần (nhân viên phụ trách, tài khoản sau khi chuyển đổi)

## Slide 12: Phân hệ Lịch tập, Điểm danh và Lương PT

**🖼️ [ẢNH DB] ERD phân hệ 2** (dbdiagram: `gms_v2_2_lichtap_diemdanh_luong`, 9 bảng)

- `bookings`: học viên và PT đều là `users`, nên `bookings` là bảng trung gian của quan hệ nhiều-nhiều giữa hai người
- `trainer_schedules` lưu theo ngày cụ thể, không theo thứ lặp lại
- `face_profiles`: 1-1 với `users` (mỗi người một hồ sơ khuôn mặt)
- `gym_check_ins` (bảng mới): lượt vào phòng không gắn buổi PT
- `commissions`: 1-1 với `bookings` (mỗi buổi tối đa một bản ghi trả công)

## Slide 13: Phân hệ Thanh toán và Bán lẻ

**🖼️ [ẢNH DB] ERD phân hệ 3** (dbdiagram: `gms_v2_3_thanhtoan_banle`, 8 bảng)

- `pos_orders` ↔ `products`: nhiều-nhiều, tách bằng `pos_order_items`
- `payment_transactions` có hai khoá ngoại tuỳ chọn (`member_package_id`, `pos_order_id`), mỗi giao dịch chỉ gắn với đúng một đối tượng
- `user_id` trong `payment_transactions` cho phép trống: khách vãng lai mua lẻ tại quầy

## Slide 14: Khoá chính và khoá ngoại

| Tiêu chí | Kết quả |
|---|---|
| Khoá chính | 19/19 bảng có khoá chính (kiểu UUID); `user_roles` dùng khoá chính kép (`user_id`, `role_id`) |
| Khoá ngoại | 28 khoá ngoại; không còn cột `*_id` nào thiếu khoá ngoại |
| Chống dữ liệu mồ côi | Mặc định `ON DELETE NO ACTION` (không xoá được cha khi còn con); 3 chỗ dùng `CASCADE`: `user_roles`, `user_profiles`, `pos_order_items` |
| Ràng buộc khác | `CHECK` đúng một cột có giá trị ở `payment_transactions`; `UNIQUE` trên khoá ngoại để tạo quan hệ 1-1 |

Gói tập không xoá cứng, chỉ ngừng bán (`is_active = 0`), vì `member_packages` đang tham chiếu.

## Slide 15: Mối quan hệ 1-1, 1-N, N-N

**🖼️ [ẢNH DB] (tuỳ chọn)** cắt vùng `pos_orders`, `pos_order_items`, `products`

| Loại | Số lượng | Ví dụ |
|---|---|---|
| 1-1 | 4 | `user_profiles` – `users`; `face_profiles` – `users`; `trainer_pay_rates` – `users`; `commissions` – `bookings` |
| 1-N | 24 | `branches` → `member_packages`; `users` → `payment_transactions` |
| N-N | 4, tách bằng bảng trung gian | xem bảng dưới |

| Quan hệ N-N | Bảng trung gian |
|---|---|
| Người dùng ↔ Vai trò | `user_roles` |
| Người dùng ↔ Gói tập | `member_packages` |
| Đơn bán lẻ ↔ Sản phẩm | `pos_order_items` |
| Hội viên ↔ PT | `bookings` |

Ví dụ điển hình: một đơn hàng có nhiều sản phẩm, một sản phẩm nằm trong nhiều đơn hàng, nên cần `pos_order_items` (tương đương `order_details`).

## Slide 16: Chuẩn hoá 2NF và 3NF

- **2NF:** 18 bảng có khoá chính đơn nên không có phụ thuộc hàm riêng phần; `user_roles` không có thuộc tính ngoài khoá.
- **3NF:** loại bỏ cột suy ra được và cột trùng lặp: `age` đổi thành `date_of_birth`; bỏ `users.avatar_url` (trùng `user_profiles`); bỏ `subtotal` và `total_amount` trong bán lẻ.

**Các cột suy ra được nhưng cố ý giữ:**

| Cột | Vì sao có vẻ suy ra được | Vì sao vẫn giữ |
|---|---|---|
| `member_packages.end_date` | = ngày bắt đầu + thời hạn gói | Giá trị đã chốt cho gói đã bán; thời hạn gói có thể đổi sau đó |
| `member_packages.remaining_sessions` | = số buổi của gói trừ số buổi đã dùng | Là trạng thái theo thời điểm đăng ký, không suy lại được khi gói đổi |
| `commissions.amount` | = mức lương trong `trainer_pay_rates` | Bản chụp tại lúc chốt buổi; đổi mức lương sau không làm đổi công đã tính |
| `pos_order_items.unit_price` | = giá hiện tại của sản phẩm | Bản chụp giá lúc bán; đổi giá sau không làm đổi hoá đơn cũ |
| `bookings.end_time` | = giờ bắt đầu + độ dài buổi | Cho phép các buổi có độ dài khác nhau |

## Slide 17: Cải tiến thiết kế từ phân tích (v1 → v2)

**🖼️ [ẢNH DB] (tuỳ chọn)** `payment_transactions` bản cũ và bản mới cạnh nhau

| # | Thay đổi | Lý do |
|---|---|---|
| 1 | `payment_transactions`: cặp `related_type` + `related_id` đổi thành hai khoá ngoại; `user_id` cho phép trống | Cột cũ trỏ tới hai bảng nên không có khoá ngoại; khách vãng lai không ghi nhận được thanh toán |
| 2 | Thêm bảng `gym_check_ins` | Lượt vào phòng không có buổi PT không có chỗ lưu |
| 3 | `user_profiles.age` đổi thành `date_of_birth` | Tuổi suy ra được và lỗi thời |
| 4 | Bỏ `users.avatar_url` | Trùng `user_profiles.avatar_url` |
| 5 | Bỏ `subtotal`, `total_amount` | Suy ra được từ số lượng và đơn giá |

Cả 5 thay đổi đã được nhóm duyệt, triển khai theo thứ tự ưu tiên.

## Slide 18: Hiện trạng triển khai và bước tiếp

**[cập nhật trạng thái thật trước khi trình bày]**

| Migration | Nội dung | Trạng thái |
|---|---|---|
| V1, V2, V2_1, V2_2 | Người dùng, vai trò, hồ sơ, gói tập, thanh toán (8 bảng) | Đã chạy trên nhánh chính |
| V3 | Lịch làm việc PT, đặt lịch | [điền: đã merge hoặc chờ merge] |
| V4 | Hồ sơ khuôn mặt, chấm công | [điền: đã merge hoặc chờ merge] |
| V5, V6 | POS, lương PT, khách tiềm năng | [điền: đang làm hoặc đã merge] |
| V7 đến V9 | Cải tiến v2 (5 thay đổi ở slide 17) | Kế hoạch |

## Slide 19: Kết luận

- Yêu cầu được làm rõ trước: 6 tác nhân, 83 user story, hai luồng cốt lõi.
- Cơ sở dữ liệu truy vết được về yêu cầu: 19 bảng, mỗi bảng phục vụ ít nhất một user story.
- Đạt các tiêu chí thiết kế: 19/19 khoá chính, 28 khoá ngoại, 4 quan hệ nhiều-nhiều tách bằng bảng trung gian, chuẩn hoá 3NF.
- Thiết kế đã được cải tiến sau khi đối chiếu với yêu cầu.
- **Cảm ơn thầy cô và các bạn đã lắng nghe. Q&A.**

---

# PHỤ LỤC

## Đối chiếu yêu cầu của giảng viên với slide

| Yêu cầu của giảng viên | Slide |
|---|---|
| Ứng dụng làm ra cho ai dùng | 2, 3 |
| Chức năng cốt lõi (MVP), bỏ qua phần rườm rà | 4 |
| Liệt kê tác nhân và giới hạn quyền hạn | 3 |
| User story, tên theo dạng Động từ + Danh từ | 5, 6 |
| Luồng nghiệp vụ, có nhánh ngoại lệ | 7, 8 |
| Liệt kê các bảng cần có | 9, 10 |
| Thuộc tính và kiểu dữ liệu | 11, 12, 13 |
| Khoá chính, khoá ngoại | 14 |
| Quan hệ 1-1, 1-N, N-N tách bằng bảng trung gian | 15 |
| Chuẩn hoá tối thiểu 2NF/3NF | 16 |
| Vẽ bằng công cụ trực quan (dbdiagram.io) | 10, 11, 12, 13 |

## Cần xác nhận trước khi trình bày

1. Trạng thái merge của V3, V4, V5, V6 để điền slide 18.
2. Activity Diagram do Agent 1 vẽ có đủ nhánh ngoại lệ ở slide 7 và 8 chưa.
3. Đã xuất ảnh 4 sơ đồ v2 từ dbdiagram.io.
4. Số liệu 19 bảng, 28 khoá ngoại, 83 story khớp với `DATABASE_SCHEMA.md` sau khi Agent 1 cập nhật.
5. Các luật chốt buổi mới trong slide 8 (đặc biệt ra sớm không có lý do thì không tính công, và kiểm tra còn buổi khi đặt lịch) đang là giả định hoặc đang triển khai theo `CHANGE_PT_ATTENDANCE_AND_PAY.md`, cần đối chiếu lại với bản chốt cuối của Phi.
6. Điền tên nhóm, thành viên, giảng viên và ngày ở slide 1.
