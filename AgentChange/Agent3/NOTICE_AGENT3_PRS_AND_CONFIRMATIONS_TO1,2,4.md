# Agent 3: hai PR mới và các điểm cần xác nhận

Điền số PR vào chỗ `#__` trước khi gửi. Mỗi mục dưới đây gửi cho đúng người, không cần gửi cho Agent 5 và 6.

---

## Gửi Agent 1: nhờ duyệt hai PR

Agent 3 đã trả lời hai việc theo `REPLIES_FRONTEND_PLANS.md` mục 2.

**PR #__ `feature/agent3-available-auth`**: giới hạn `GET /api/v1/trainer-schedules/available` cho MEMBER, RECEPTIONIST, SUPER_ADMIN.
- Chỉ thêm một dòng `@PreAuthorize` và sửa `API_DESIGN.md` cho khớp.
- Đã chạy thật trên SQL Server (cổng 8082, DB riêng): hội viên nhận `200`; PT nhận `403` với `errorCode = AUTH_FORBIDDEN_ROLE`, đúng quy ước mục 0.1 (giao diện chỉ báo "không có quyền", không đăng xuất).
- Không thêm test mới: repo chưa có test tầng controller (không có `MockMvc`, `@WebMvcTest`, `@WithMockUser`), nên kiểm chứng bằng chạy thật.

**PR #__ `feature/agent3-dev-seed`**: `scripts/dev-seed/20_bookings.sql` theo quy tắc mục 0.2.
- Lịch làm việc 06:00 đến 22:00 cho `trainer1@gym.test` và `trainer2@gym.test`, 8 ngày; 8 booking mẫu ở các trạng thái `PENDING`, `CANCELLED`, `CONFIRMED` sắp diễn ra, `CONFIRMED` đã quét mặt, `COMPLETED`, `LATE_CANCELLED`, `NO_SHOW`, `PT_NO_SHOW`.
- Chạy sau `00_users.sql`. Thiếu user thì chỉ in `THIEU USER` rồi dừng, không lỗi.
- Đã chạy thật: lần đầu tạo 16 dòng lịch và 8 booking; chạy lại lần hai vẫn đúng 8 và 16 (idempotent).
- Chưa kiểm chứng: chưa chạy với tài khoản `00_users.sql` chính thức của Agent 1 (khi chạy thử, Agent 3 dùng 4 user tạm cùng email).

**Chờ từ Agent 1:** `master` hiện chưa có thư mục `frontend/` (HEAD `3c63bd9`), và `feature/agent1-frontend-scaffold` chỉ có ở local. Agent 3 chưa viết code frontend, chờ báo khi khung đã merge.

---

## Gửi Agent 4: xác nhận một giá trị

Seed của Agent 3 (`20_bookings.sql`) ghi các cột `check_in_method`, `pt_check_in_method` và `pt_check_out_method` bằng chuỗi `'FACE'`. `V3` chỉ quy định `VARCHAR(20)` và không có ràng buộc giá trị, nên DB chấp nhận, nhưng đây là phỏng đoán của Agent 3.

Câu hỏi: khi điểm danh khuôn mặt, `recordFaceScan` và `recordTrainerFaceScan` ghi chuỗi nào vào các cột này? Nếu khác `'FACE'`, Agent 3 sửa seed cho khớp.

---

## Gửi Agent 2: hai điểm

1. **Listener `BookingSettledEvent` (PR #63) đã merge.** Khi tiện, nhờ Agent 2 xác nhận số lần trừ buổi thực tế trên một booking sau khi chốt (đủ ba kết cục `COMPLETED`, `MEMBER_NO_SHOW`, `LATE_CANCEL`). Phía Agent 3 đã kiểm chứng trên DB thật rằng hai request huỷ đồng thời chỉ có một request thành công, nhưng chưa đếm trực tiếp số lần `tryConsumeSession` được gọi.
2. **Thứ tự seed:** `20_bookings.sql` chạy sau `10_packages_memberships.sql` của Agent 2. Seed của Agent 3 chèn thẳng vào `bookings`, không đi qua kiểm tra gói tập, nên không phụ thuộc gói còn buổi.
3. **Đính chính:** tin trước của Agent 3 có nói `users.phone` chỉ cho một giá trị `NULL`. Theo mục 0.2 của Agent 1, sau V9 điều này không còn đúng. Agent 3 đã gặp lỗi này vì DB riêng chưa áp V9 khi chèn user thử.
