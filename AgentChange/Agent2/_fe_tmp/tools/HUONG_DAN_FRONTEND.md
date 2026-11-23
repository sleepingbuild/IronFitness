# Frontend đợt 1 của Agent 2: cách áp dụng, kiểm tra và mở PR

Gói này chỉ chứa file thuộc phạm vi Agent 2: `frontend/src/features/membership/`, năm trang dưới `frontend/src/app/(app)/`
(`packages`, `packages/[id]`, `my-membership`, `payments`, `admin/packages`) và script đổi trạng thái menu.
Không có file nào của `lib/`, `components/` hay `config/` (ngoài bốn dòng của Agent 2 trong `navigation.ts`).

## 1. Áp dụng
```powershell
cd D:\SrpingBoot-GYM\gms-agent2
git fetch origin --prune
git status -sb                                   # phai sach
git switch feature/agent2-frontend
git merge --ff-only origin/master

$tf = "D:\AgentChange\Agent2\_fe_tmp"
if (Test-Path $tf) { Remove-Item $tf -Recurse -Force }
Expand-Archive "D:\AgentChange\Agent2\agent2-frontend-batch1.zip" -DestinationPath $tf
Copy-Item "$tf\repo\*" . -Recurse -Force
powershell -ExecutionPolicy Bypass -File "$tf\tools\update-navigation.ps1"     # phai in: Da doi 4 dong
git status -sb
```
`git status` chỉ nên có: thư mục `frontend/src/features/membership/` mới, năm trang trong `frontend/src/app/(app)/`
(bốn trang sửa, một trang `[id]` mới) và `frontend/src/config/navigation.ts` (4 dòng). Có file nào khác thì dừng lại.

## 2. Bốn lệnh kiểm tra (đều phải xanh trước khi mở PR)
```powershell
cd frontend
Copy-Item .env.example .env.local        # lan dau; sua NEXT_PUBLIC_API_BASE_URL thanh http://localhost:8082
npm ci
npm run lint
npm run typecheck
npm test
npm run build
```
Kết quả mong đợi khi mình thử trên bản dựng lại khung: lint không cảnh báo, typecheck sạch, 42 test xanh (31 của module + 11 của khung),
build biên dịch được cả năm route. Nếu lệnh nào đỏ, dán lại phần lỗi.

## 3. Thử bằng tay (cần backend chạy, cổng 8082, database có dữ liệu mẫu)
Chạy backend như các lần trước (`SERVER_PORT=8082`), nạp `10_packages_memberships.sql`, rồi `npm run dev` và mở http://localhost:3000.
Đăng ký một hội viên ở `/register` và đăng nhập.

| # | Màn hình | Cách thử | Kết quả đúng |
|---|---|---|---|
| 1 | `/packages` | Mở với tài khoản hội viên | Thấy 4 gói đang bán, không thấy gói đã ngừng bán |
| 2 | `/packages/<id>` | Bấm Xem chi tiết, rồi Đăng ký gói này, rồi Xác nhận đăng ký | Hiện mã giao dịch và số tiền, nói rõ đang chờ lễ tân |
| 3 | Đăng ký lần hai | Đăng ký thêm một gói | Báo đang có gói chờ thanh toán, có liên kết tới Gói của tôi |
| 4 | `/my-membership` | Ba tab | Tab hiện tại báo có gói chờ thanh toán; tab lịch sử có dòng Chờ thanh toán; tab thanh toán có giao dịch Chờ xác nhận |
| 5 | `/payments` | Cần tài khoản lễ tân (chờ `00_users.sql` của Agent 1) | Giao dịch chờ hiện ra; bấm Xác nhận đã thu thì gói của hội viên chuyển sang Đang dùng |
| 6 | Đổi gói | Với hội viên đang có gói ACTIVE (seed `member.active`) | Có danh sách nâng cấp và hạ cấp, có cảnh báo trước khi đổi; nâng cấp nói rõ gói cũ bị huỷ ngay |
| 7 | `/admin/packages` | Cần tài khoản quản trị | Tạo, sửa, ngừng bán; gói ngừng bán biến mất khỏi `/packages` và `/packages/<id>` báo không tìm thấy |
| 8 | Bốn trạng thái | Tắt backend; mở bằng tài khoản không đủ quyền | Lỗi có nút Thử lại; hội viên mở `/admin/packages` thấy "không có quyền" |

## 4. Mô tả PR gợi ý
```text
feat(frontend): man hinh goi tap va thanh toan dot 1 (Agent 2)
- /packages, /packages/[id]: danh sach va chi tiet goi, hoi vien dang ky goi
- /my-membership: goi hien tai, nang cap/ha cap (co canh bao nang cap huy goi cu ngay), lich su goi, lich su thanh toan
- /payments (Le tan, Admin): xac nhan da thu tien, ban goi ho hoi vien
- /admin/packages (Super Admin): tao, sua, ngung ban goi
- Chi sua src/features/membership/, 5 trang trong (app)/, 4 dong cua Agent 2 trong navigation.ts
- lint, typecheck, test (42), build xanh. Kem anh chup man hinh: (dinh kem)
Gioi han da biet: xem muc 5 cua huong dan.
```
Kèm ảnh chụp: danh sách gói, chi tiết sau khi đăng ký, Gói của tôi (có cảnh báo chờ thanh toán), hộp xác nhận nâng cấp, trang Thanh toán (nếu có tài khoản lễ tân), Quản trị gói, và một màn lỗi hoặc không có quyền.

## 5. Giới hạn đã biết (ghi vào PR)
- **Tìm hội viên:** màn Bán gói hộ gọi `GET /api/v1/users?role=MEMBER&q=` (Agent 1 làm). Khi API chưa có hoặc lỗi, màn hiện cảnh báo và cho nhập thẳng mã hội viên (UUID).
- **Lọc giao dịch theo trạng thái:** đầy đủ sau khi H3 vào `master`. Trước đó backend bỏ qua `status` và `userId` (chỉ lấy hôm nay), giao diện vẫn lọc thêm ở trình duyệt trên danh sách đó. Cột Người mua hiện tên sau H3, trước đó hiện mã rút gọn.
- **Lễ tân nâng, hạ cấp hộ hội viên:** chưa có, cần H2 (`GET /member-packages/current?userId=`) vào `master` rồi làm ở PR sau.
- **Chi nhánh:** chưa gửi `branchId` (chờ Agent 2 xác nhận hành vi khi bỏ trống và `GET /branches` của Agent 1).
- **Xem gói khi chưa đăng nhập:** chưa làm, vì các trang nằm trong khung yêu cầu đăng nhập (thuộc Agent 1). Backend đã mở sẵn sau khi PR `[shared]` của G3 merge.
- **Kiểm thử:** chỉ có test cho phần logic thuần (định dạng, thông báo lỗi, kiểm tra form gói, phân loại nâng hạ cấp). Khung chưa cấu hình môi trường để test render component.
- **Quy tắc form gói theo loại** (gói theo thời hạn cần số ngày, gói theo buổi và gói PT cần số buổi) do giao diện áp, chặt hơn backend hiện tại (backend cho phép bỏ trống).

## 6. Một nhận xét gửi Agent 1 (không chặn PR)
Backend trả `message` đúng bằng tên mã lỗi khi không có lời giải thích riêng (ví dụ `"message":"PACKAGE_NOT_FOUND"`), và `errorMessage()` dùng nguyên `message` đó nên người dùng có thể thấy tên mã. Module này tự xử lý bằng `membershipErrorMessage`. Đề xuất sửa gốc trong `lib/api/errors.ts`: coi `message === errorCode` là chưa có message để rơi xuống bảng `DEFAULT_MESSAGES`.
