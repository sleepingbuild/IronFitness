# Agent 2 → Agent 1: yêu cầu của giảng viên và đề xuất cập nhật thiết kế DB (v2), nhờ duyệt

## 1. Yêu cầu của giảng viên cho buổi thuyết trình sắp tới

**Bước 1, Phân tích yêu cầu (40%)**
- Tác nhân (10%): liệt kê đủ vai trò, mô tả giới hạn quyền hạn từng tác nhân.
- Danh sách use case / user story (15%): phủ kín chức năng bắt buộc (CRUD cơ bản) và nghiệp vụ đặc thù; tên chức
  năng theo dạng Động từ + Danh từ (ví dụ "Duyệt đơn hàng").
- Luồng nghiệp vụ (15%): mô hình hoá (Activity Diagram hoặc flowchart) 1 đến 2 chức năng phức tạp nhất, có xử lý
  nhánh ngoại lệ.

**Bước 2, Thiết kế cơ sở dữ liệu và ERD (60%)** (nhấn mạnh phần này)
- Thực thể và thuộc tính (20%): đủ bảng để đỡ toàn bộ use case ở Bước 1; cột hợp lý, đúng kiểu dữ liệu.
- Khoá PK và FK (15%): 100% bảng có PK; FK khai báo chính xác, không để dữ liệu mồ côi.
- Mối quan hệ (15%): xác định đúng 1-1, 1-N; bắt buộc tách N-N bằng bảng trung gian.
- Chuẩn hoá (10%): tối thiểu 2NF/3NF, không lặp dữ liệu dạng mảng, không gom trường không liên quan vào một bảng.

## 2. Đã làm

| Hạng mục | Kết quả | Tệp |
|---|---|---|
| Tác nhân | 6 tác nhân kèm giới hạn quyền hạn | `USER_STORIES.md` |
| User story | 83 story, lấy từ `API_DESIGN.md`, `REQUIREMENTS.md` và `CHANGE_PT_ATTENDANCE_AND_PAY.md` | `USER_STORIES.md` |
| Đối chiếu story với bảng | Ma trận story → bảng, mỗi bảng phục vụ story nào | `DB_DESIGN_REVIEW.md` |
| Thiết kế DB mục tiêu v2 | 19 bảng, 28 khoá ngoại, 4 sơ đồ theo phân hệ và 1 sơ đồ tổng | `gms_v2_*.dbml` |
| Luồng nghiệp vụ (Bước 1, 15%) | **Chưa làm** | |

Đã kiểm tra: các file `.dbml` đọc không lỗi bằng `@dbml/core`; v2 có 19/19 bảng có PK, không còn cột `*_id`
nào thiếu FK, 4 quan hệ 1-1, 24 quan hệ 1-N, N-N tách bằng `user_roles`, `member_packages`, `pos_order_items`,
`bookings`; 82/82 story có dữ liệu ánh xạ được vào ít nhất 1 bảng.

**DB đang chạy vẫn là v1.** Các thay đổi dưới đây chưa triển khai.

## 3. Cần Agent 1 duyệt: 5 thay đổi từ v1 sang v2

| # | Thay đổi | Lý do | Ai triển khai | Ảnh hưởng code đã có | Quyết định |
|---|---|---|---|---|---|
| CH1 | `payment_transactions`: bỏ `related_type` + `related_id`, thêm `member_package_id` và `pos_order_id` (FK, đúng một cột có giá trị, `CHECK`); `user_id` cho phép `NULL` | `related_id` trỏ tới hai bảng nên không có FK (tiêu chí PK/FK). Bán lẻ cho khách vãng lai không ghi được thanh toán vì `user_id` đang `NOT NULL` (story R20) | Agent 2; FK sang `pos_orders` thêm ở V5 (Agent 5) | Entity `PaymentTransaction`, `PaymentService`, `MembershipServiceImpl` (chỗ tạo giao dịch), test; cần chuyển dữ liệu cũ | - [ ] Duyệt  - [ ] Không |
| CH2 | Bảng mới `gym_check_ins` (lượt vào phòng không gắn buổi PT) | Điểm danh hiện chỉ ghi vào `bookings`, nên lượt vào phòng không có buổi PT không có chỗ lưu; ảnh hưởng story M16, M17, R14, A7 (lịch sử check-in, kiosk, báo cáo lưu lượng) | Agent 4, Agent 3 xác nhận | Luồng quét mặt khi không có booking (hiện trả `bookingLinked=false` và không ghi) | - [ ] Duyệt  - [ ] Không |
| CH3 | `user_profiles.age` đổi thành `date_of_birth` | Tuổi suy ra được từ ngày sinh và lỗi thời (tiêu chí chuẩn hoá) | Agent 1 | Entity `UserProfile`, DTO, quy tắc tuổi tối thiểu 18 (file SHARED, PR gắn `[shared]`) | - [ ] Duyệt  - [ ] Không |
| CH4 | Bỏ `users.avatar_url` | Trùng `user_profiles.avatar_url` (đã ghi DEPRECATED) | Agent 1 | Entity `User` (SHARED) và nơi dùng `avatarUrl` | - [ ] Duyệt  - [ ] Không |
| CH5 | Bỏ `pos_order_items.subtotal` và `pos_orders.total_amount` | Suy ra được từ `quantity` × `unit_price` | Agent 5 | Không có, V5 chưa viết | - [ ] Duyệt  - [ ] Không |

Đề xuất của Agent 2: duyệt CH1, CH2, CH5 (CH1 và CH2 vá thiếu sót chức năng, CH5 không tốn gì). CH3 và CH4 chỉ là
đánh bóng chuẩn hoá nhưng đụng file SHARED đã merge; nếu thiếu thời gian thì giữ nguyên và giải trình khi trình bày.

Nếu không duyệt một thay đổi, sơ đồ trình bày sẽ ghi rõ đó là thiết kế mục tiêu chưa triển khai, kèm giải trình
(ví dụ CH1: giữ khoá đa hình và nêu đây là đánh đổi có chủ ý).

## 4. Cần Agent 1 quyết định thêm

1. **8 story chưa có endpoint** trong `API_DESIGN.md`: G3 (Khách xem gói tập, hiện `GET /packages` cần đăng nhập), M17
   (hội viên xem lịch sử check-in), R22 (lễ tân tạo tài khoản hội viên), A10 (cấu hình lương PT), A18 đến A21 (tạo
   tài khoản nhân viên, phân quyền, khoá tài khoản, quản lý chi nhánh). Đưa vào backlog hay coi là ngoài phạm vi?
2. **Thứ tự và số migration:** V3 và V4 đang chờ merge; migration có số nhỏ hơn merge sau sẽ làm Flyway báo lỗi
   trừ khi bật `outOfOrder`. Cần thống nhất thứ tự merge hoặc cách đánh số.
3. **Tài liệu dùng chung do Agent 1 giữ:** sau khi triển khai, cập nhật `DATABASE_SCHEMA.md` và
   `docs/ERD_TONG_QUAT.md` cho khớp v2.

## 5. Đính kèm

- `USER_STORIES.md`, `DB_DESIGN_REVIEW.md`
- `gms_v2_schema.dbml` (bản đủ cột, một sơ đồ) và `gms_v2_0_overview.dbml`, `gms_v2_1_nguoidung_goitap.dbml`,
  `gms_v2_2_lichtap_diemdanh_luong.dbml`, `gms_v2_3_thanhtoan_banle.dbml` (bốn sơ đồ)
