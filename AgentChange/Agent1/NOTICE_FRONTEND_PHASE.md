# THÔNG BÁO: mở giai đoạn Frontend (gửi Agent 2 đến 6)

## 1. Vì sao mở bây giờ
Backend đã đủ ổn định ở các mảng chính (Auth, Profile, Booking, gói tập, điểm danh khuôn mặt). Mục tiêu giai đoạn này là để **chủ dự án nhìn thấy sản phẩm chạy thật**, bấm thử từng chức năng, tìm lỗi bằng mắt, rồi phản hồi để sửa cả backend lẫn giao diện cho phù hợp.

Không đợi backend xong hết: frontend làm theo hai đợt, theo độ ổn định của API.

| Đợt | Modules | Điều kiện bắt đầu |
|---|---|---|
| **Đợt 1 (bắt đầu ngay)** | Auth, Profile, Gói tập (Agent 2), Lịch PT và Booking (Agent 3), Điểm danh khuôn mặt (Agent 4) | Khung frontend (mục 3) đã merge |
| **Đợt 2** | POS và hoa hồng (Agent 5), Leads và báo cáo (Agent 6), phần mới của Agent 2/4 | V5, V7, V8 và PR Leads đã vào `master` |

Trong lúc chờ khung frontend: đọc API, phác giao diện (giấy hoặc Figma), chuẩn bị danh sách màn hình. **Chưa viết code trong `frontend/` cho tới khi Agent 1 báo khung đã merge**, để tránh xung đột.

## 2. Quyết định kỹ thuật (Agent 1 chốt)
- **Next.js (App Router) + TypeScript + Tailwind CSS**, Node 20 trở lên, quản lý gói bằng `npm`.
- Thư mục `frontend/` nằm cùng repo với backend. `Jenkinsfile` hiện chỉ build backend; stage build frontend Agent 1 sẽ thêm sau.
- Hợp đồng API: `/v3/api-docs` (Swagger UI: `/swagger-ui.html`) và `API_DESIGN.md`. Không đoán endpoint; nếu tài liệu thiếu hoặc sai thì báo Agent 1.
- Địa chỉ backend đặt qua biến `NEXT_PUBLIC_API_BASE_URL` (không ghi cứng URL trong code).

## 3. Agent 1 làm trước (khung dùng chung, sẽ báo khi merge)
1. Khung Next.js, cấu trúc thư mục, cấu hình ESLint và định dạng code.
2. Bộ gọi API dùng chung: tự gắn `Authorization: Bearer`, bóc `ApiResponse`, xử lý lỗi theo `errorCode`.
3. Đăng nhập, đăng ký, làm mới token, đăng xuất; khung layout và menu theo vai trò.
4. Trang hồ sơ cá nhân (`dateOfBirth`, `age`, avatar).
5. Tài khoản kiểm thử theo từng vai trò: `docs/TEST_ACCOUNTS.md` và script nạp dữ liệu dev (`scripts/dev-seed.sql`, không phải migration, không chạy trên CI).

## 4. Quy ước chung

**Phân thư mục (mỗi agent chỉ sửa phần của mình):**
- `frontend/src/app/<module>/`: trang của module bạn.
- `frontend/src/features/<module>/`: component và hàm gọi API riêng của module.
- `frontend/src/lib/`, `frontend/src/components/ui/`: dùng chung, thuộc Agent 1. Cần thêm gì thì nhờ Agent 1.

**Hợp đồng API cần nhớ:**
- Mọi response có dạng `{ success, message, data, errorCode, timestamp }`. Thành công đọc `data`; thất bại hiển thị `message` và xử lý theo `errorCode`.
- Lỗi xác thực: người chưa đăng nhập hiện nhận **403** (không phải 401). Không có token thì chuyển về trang đăng nhập; có token mà bị 403 thì hiện "Bạn không có quyền".
- Access token ngắn hạn (15 phút), dùng refresh token để làm mới. Khi đăng xuất, token bị đưa vào danh sách chặn.
- `id` là UUID. Thời gian là `LocalDateTime` theo giờ Việt Nam, **không có múi giờ trong chuỗi**; đừng tự cộng trừ múi giờ ở trình duyệt.
- Lỗi 400 `VALIDATION_ERROR`: hiển thị `message`. Lỗi 409 `DATA_CONFLICT`: dữ liệu trùng.
- Danh sách có phân trang dùng `PageResponse`.

**Điểm danh khuôn mặt (Agent 4):** đặc trưng khuôn mặt được trích ở **trình duyệt** rồi gửi lên dạng JSON (độ dài và định dạng theo `API_DESIGN.md` và `FaceMatchProperties`). Không gửi ảnh gốc ở luồng quét. Kiosk và tự điểm danh là hai trang riêng.

## 5. Phạm vi đợt 1 theo từng agent
- **Agent 2:** danh sách và chi tiết gói tập (công khai), đăng ký gói, xác nhận thanh toán (lễ tân), trạng thái và lịch sử gói của hội viên, quản trị gói (Super Admin). Cần hai PR G3 đã merge.
- **Agent 3:** PT khai lịch làm việc, hội viên xem khung giờ trống và đặt, huỷ, danh sách booking, nộp lý do rời buổi sớm. Nên có dạng xem lịch theo tuần.
- **Agent 4:** đăng ký khuôn mặt, trang kiosk, trang tự quét, bảng chấm công nhân sự, lịch sử check-in của hội viên (sau khi V8 merge).
- **Agent 5 và 6:** đợt 2. Trong lúc chờ, chuẩn bị màn hình và danh sách endpoint cần dùng.

## 6. Cách phản hồi lỗi để sửa nhanh
Khi chủ dự án hoặc bạn thấy lỗi lúc thử, ghi theo mẫu và gửi đúng người (lỗi giao diện gửi chủ module đó, lỗi API gửi chủ backend của module, lỗi khung dùng chung gửi Agent 1):

```
Màn hình / chức năng:
Vai trò đăng nhập:
Các bước tái hiện:
Kết quả thực tế (kèm ảnh chụp, nội dung response nếu có):
Kết quả mong đợi:
```

Mỗi lỗi thuộc về một trong ba loại: giao diện, API (sai so với `API_DESIGN.md`), hoặc dữ liệu. Backend chỉ sửa trong module của chủ nó; đừng vá backend từ phía frontend bằng cách bỏ qua lỗi.

## 7. Điều kiện hoàn thành một PR frontend
- `npm run build` và `npm run lint` không lỗi.
- Chạy được với backend thật (không dữ liệu giả cố định); ảnh chụp màn hình kèm trong PR.
- Xử lý các trạng thái: đang tải, danh sách rỗng, lỗi API (hiện `message`), không có quyền.
- Phân quyền hiển thị theo vai trò (backend vẫn là lớp bảo vệ thật; ẩn nút không thay thế phân quyền).
- Không commit bí mật, token hay file `.env`. Không sửa `Jenkinsfile`, `application.yml`, `pom.xml` hay file SHARED của backend.
- Cập nhật `frontend/README.md` phần module của bạn: cách chạy và tài khoản dùng để thử.

## 8. Thứ tự tiếp theo
1. Agent 1: khung frontend và tài khoản kiểm thử, báo khi merge.
2. Agent 2, 3, 4: màn hình đợt 1 trên khung đó.
3. Chủ dự án thử từng module, gửi lỗi theo mẫu ở mục 6.
4. Agent 5, 6: đợt 2 sau khi V5, V7, V8 và PR Leads đã vào `master`.
