# Nhiệm vụ mới cho các agent (đợt 12/11/2026)

Gửi: Agent 2, 3, 4, 5, 6. Người giao: Agent 1 (Backend Lead), theo yêu cầu của nhóm trưởng Phi.
Đọc hết mục 0 và mục của mình. Làm việc theo quy ước cũ: nhánh từ `origin/master`, PR vào `master`, merge bằng "Create a merge commit", Jenkins xanh mới merge PR tiếp theo.

---

## 0. Quyết định chung (đã chốt, không bàn lại)

**Bỏ / giữ theo vai trò**

| Hạng mục | SUPER_ADMIN | RECEPTIONIST | SALES | TRAINER | MEMBER |
|---|---|---|---|---|---|
| Trang Tổng quan | bỏ | bỏ | bỏ | bỏ | bỏ |
| Hồ sơ: chiều cao, cân nặng, mục tiêu | bỏ | bỏ | bỏ | bỏ | **giữ** |
| Lịch theo ngày | bỏ | bỏ | bỏ | bỏ | **giữ** |
| Thanh toán, Gói tập (menu riêng) | bỏ (đã có trong quản trị gói) | giữ như cũ | giữ như cũ | - | - |
| Đặt lịch | bỏ "lịch theo ngày", thay bằng **Quản lý PT** | chỉ giữ **đặt hộ hội viên** | - | - | tự đặt |

**Quy tắc nghiệp vụ mới**

1. **Đặt lịch tự động (member).** Đặt xong là `CONFIRMED` ngay, vẫn tính vào số buổi còn lại. **Ngoại lệ:** nếu ngày đó PT chưa khai ca làm việc (không có lịch dạy) thì booking chuyển sang `PENDING` và chờ PT xác nhận như cũ. *Đây là cách Agent 1 hiểu câu của nhóm trưởng; Agent 3 xác nhận lại với nhóm trưởng nếu thấy mơ hồ.*
2. **Số buổi còn lại theo gói đã đăng ký.** Mỗi lần đặt phải kiểm tra `remaining_sessions` trừ các booking đang mở (PENDING/CONFIRMED) còn lớn hơn 0. Member phải thấy số buổi còn lại ở màn hình đặt lịch và ở hồ sơ.
3. **Thanh toán tự động (mô phỏng).** Member chọn gói, bấm thanh toán, hệ thống ghi nhận thành công ngay (không cổng thật). Vẫn đi qua `PaymentService` và sự kiện `PaymentConfirmedEvent` để kích hoạt membership.
4. **POS cho member.** Member tự chọn hàng và mua, thanh toán mô phỏng nhận luôn. Dùng lại luồng POS hiện có và `refundByRelated` khi huỷ.
5. **PT hoàn thành buổi:**
   - Thời lượng thực tế **>= 45 phút**: chuyển `COMPLETED` luôn.
   - **< 45 phút**: bật **popup bắt buộc nhập lý do giải trình**, chưa nhập thì không cho lưu.
   - Thời lượng thực tế tính từ giờ PT quét mặt vào (nếu có), nếu chưa có thì từ giờ bắt đầu theo lịch.
   - Thêm nút thao tác **Client_No_Show** (PT đánh dấu hội viên vắng; dùng trạng thái `NO_SHOW` hiện có, không thêm trạng thái trùng nghĩa).
   - Hiển thị giờ **vào** và **ra** của PT ngay cả khi buổi chưa/đang kết thúc (hiện giờ vào, giờ ra để trống tới khi quét ra).
6. **Phân quyền = gán vai trò cho tài khoản** (admin chọn vai trò cho từng user). Không làm ma trận quyền chi tiết.

**Thứ tự để khỏi xung đột file**

- Agent 1 sửa `frontend/src/lib/navigation.ts` và khung `AppShell` **trước** (PR A). Các agent khác **chưa đụng `navigation.ts`** cho tới khi PR A merge; sau đó `git fetch` và rebase rồi mới thêm dòng của mình.
- Nhóm trưởng sẽ merge trước 4 nhánh đang chờ: `shared/agent5-error-codes`, `feature/agent5-error-codes`, `feature/agent3-schedule-ownership`, `feature/agent4-seed-attendance`.
- **Không merge** `feature/agent3-admin-schedule` và `feature/agent3-bookings-branch-filter` ở dạng hiện tại (trái yêu cầu mới). Agent 3 tái sử dụng phần mã hữu ích (xem mục Agent 3).

**Luật tự động (vi phạm là `mvn verify` đỏ):** controller có `@Tag` nằm trong `OpenApiConfig.TAG_ORDER`; endpoint có `@Operation(summary)`; không trả `ApiResponse<?>`; "bây giờ" lấy từ bean `Clock`; đổi API thì cập nhật `API_DESIGN.md`; thêm `ErrorCode` phải qua Agent 1; migration mới phải chạy thử trên DB trống.

---

## Agent 1 (Backend Lead)

- **PR A (frontend):** bỏ Tổng quan mọi vai trò; hồ sơ chỉ hiện chiều cao/cân nặng/mục tiêu cho MEMBER; bỏ lịch theo ngày ở mọi vai trò trừ MEMBER; admin bỏ Thanh toán và Gói tập khỏi menu; lễ tân chỉ giữ đặt hộ; trang mặc định sau đăng nhập đổi theo vai trò.
- **PR D1 (backend + frontend):** quản lý **khách hàng** và **nhân viên** (danh sách, tạo, sửa, khoá/mở tài khoản) và **gán vai trò cho tài khoản** (chỉ SUPER_ADMIN). Cần nêu rõ: không cho admin tự gỡ vai trò SUPER_ADMIN của chính mình; thay đổi vai trò phải làm mất hiệu lực token cũ hoặc chấp nhận hiệu lực tối đa 15 phút (ghi rõ trong `API_DESIGN.md`).
- **PR E (CI/CD):** Jenkins tự cập nhật Docker (xem mục 7).
- Ghép thống kê cá nhân vào trang hồ sơ (frontend) sau khi Agent 6 có endpoint.

## Agent 2 (Gói tập, Membership, Thanh toán)

1. **Thanh toán tự động cho member:** endpoint để member tự mua gói, thanh toán mô phỏng thành công ngay, membership kích hoạt ngay qua `PaymentConfirmedEvent`. Quyền: MEMBER chỉ mua cho chính mình.
2. **Số buổi còn lại:** đảm bảo `GET` thông tin membership của chính member trả đủ `remainingSessions`, ngày hết hạn, gói đang `ACTIVE`; bổ sung số buổi đang giữ chỗ (booking mở) nếu tính được ở phía bạn.
3. **Frontend member:** màn hình xem gói, mua gói, thấy số buổi còn lại.
4. Admin không còn menu Thanh toán/Gói tập riêng; **giữ API** (không xoá backend), chỉ phần quản trị gói còn trong trang quản trị.
5. Còn nợ từ trước: `Clock` thay `LocalDate.now()` trong `PaymentServiceImpl`.

## Agent 3 (Lịch PT, Booking)

1. **Đặt lịch tự động:** theo quy tắc 1 mục 0 (có ca của PT thì `CONFIRMED` ngay; không có ca thì `PENDING`, chờ PT xác nhận). Kiểm tra số buổi còn lại (quy tắc 2). Viết test cho cả hai nhánh.
2. **Lễ tân đặt hộ:** giữ luồng đặt hộ; bỏ màn hình lịch theo ngày của lễ tân và admin.
3. **Quản lý PT (thay lịch theo ngày của admin):** danh sách PT, xem và khai/sửa ca làm việc của từng PT. Tái sử dụng `AdminScheduleWorkspace` từ nhánh `agent3-admin-schedule` nếu hợp, nhưng đổi tên/route thành quản lý PT. Mở PR mới, không merge nhánh cũ.
4. **PT hoàn thành buổi:** quy tắc 5 mục 0. Cần lưu lý do giải trình (cột mới, kèm migration có bằng chứng chạy trên DB trống) và trả lý do trong response. Popup phía frontend bắt buộc nhập, backend cũng phải chặn khi thiếu lý do.
5. **Nút Client_No_Show** cho PT trên màn hình buổi tập (kiểm tra thời điểm hợp lệ: chỉ sau giờ bắt đầu + thời gian chờ đã chốt trước đây).
6. **Hiển thị giờ vào/ra của PT** (lấy từ dữ liệu quét mặt của Agent 4).
7. Báo lại: ảnh hưởng lương PT khi buổi dưới 45 phút (Agent 5 đang trả lương theo buổi). Chưa đổi quy tắc lương nếu nhóm trưởng chưa chốt.

## Agent 4 (Điểm danh khuôn mặt, chấm công)

1. Cung cấp (hoặc xác nhận đã có) **giờ PT quét vào / quét ra theo từng booking** trong response mà Agent 3 dùng; thống nhất tên trường với Agent 3 qua `API_DESIGN.md`.
2. Seed: sau khi nhánh `agent4-seed-attendance` merge, bổ sung dữ liệu có buổi đang diễn ra (đã vào, chưa ra) để thử màn hình PT.
3. Hiệu chỉnh ngưỡng `0.45` với khuôn mặt thật khi có thể.

## Agent 5 (POS, sản phẩm, lương PT)

1. **POS cho member:** MEMBER tự xem sản phẩm, giỏ hàng, mua; thanh toán mô phỏng nhận luôn; quyền `@PreAuthorize` cho MEMBER chỉ thấy và tạo đơn của chính mình.
2. **Frontend POS member** (mục "POS" trong menu member). Menu POS của nhân viên giữ nguyên.
3. Sau khi `agent5-error-codes` merge: dùng ba mã lỗi mới, rà `refundByRelated` khi huỷ đơn.
4. Chuẩn bị tính lương PT khi buổi hoàn thành dưới 45 phút có lý do: chỉ ghi nhận dữ liệu, chưa đổi cách trả lương.

## Agent 6 (Leads, Báo cáo)

1. **Endpoint thống kê cá nhân của member** (ví dụ `GET /members/me/stats`): số buổi đã tập, số buổi còn lại, số lần vào phòng tập, số booking theo trạng thái, trong 30 ngày gần nhất. Lấy dữ liệu qua service/interface của agent khác, không đọc thẳng bảng của họ.
2. Báo cáo của admin không còn dựa vào trang Tổng quan; giữ các endpoint báo cáo hiện có.
3. Còn nợ: `checkin-traffic` hai nguồn, `commissions-summary`, seed `60_`, Leads (không bị ảnh hưởng đợt này).

---

## 7. CI/CD (chỉ Agent 1 làm, các agent khác biết để khỏi lo)

Mục tiêu của nhóm trưởng: **merge hoặc sửa trực tiếp trong repo thì Jenkins chạy; thành công thì Docker tự cập nhật lên web.**

- Thêm stage vào `Jenkinsfile`, chỉ chạy trên `master` và chỉ sau khi build, test, deploy hiện có thành công: `docker compose -f docker-compose.demo.yml up -d --build` cho frontend (và backend demo nếu có đổi).
- Docker build lấy mã **từ workspace của Jenkins**, nên đúng bản vừa merge.
- Tài khoản chạy Jenkins phải dùng được Docker; sẽ có bước kiểm tra sau stage (gọi trang web cổng 3000 và API cổng 8082).
- Các agent khác **không sửa** `Jenkinsfile`, `docker-compose*.yml`, `Dockerfile`.

## Báo cáo tiến độ

Mỗi agent: khi mở PR, ghi trong mô tả (1) đã chạy `mvn clean verify` hay chưa, (2) đã chạy ứng dụng thật hay chưa, (3) bằng chứng migration trên DB trống nếu có. Phần nào chưa chạy thật thì ghi "chưa chạy", không ghi là đã xong.
