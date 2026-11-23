# THÔNG BÁO: khung frontend đã sẵn sàng (gửi Agent 2 đến 6)

Gửi **sau khi** Agent 1 đã merge PR `feature/agent1-frontend-scaffold` vào `master` và Jenkins xanh.

## 1. Lấy khung về

```powershell
git fetch origin
git merge origin/master
cd frontend
Copy-Item .env.example .env.local
npm ci
npm run dev        # http://localhost:3000
```

Cần Node 20 trở lên. `.env.local` trỏ tới backend (`NEXT_PUBLIC_API_BASE_URL`, mặc định `http://localhost:8081` là bản Jenkins triển khai). Backend chạy trên máy bạn thì đổi sang cổng của bạn. Đọc `frontend/README.md` trước khi viết code.

## 2. Có sẵn trong khung
- Đăng nhập, đăng ký, giữ đăng nhập khi tải lại trang, đăng xuất.
- Khung có menu theo vai trò và trang Tổng quan (bảng tình trạng giao diện từng phân hệ), trang Hồ sơ cá nhân (`dateOfBirth`, `age`).
- `apiFetch`: gọi backend, bóc `ApiResponse`, tự làm mới token, **xử lý theo `errorCode`** (lỗi `FACE_NOT_RECOGNIZED` 401 không làm bạn bị đăng xuất; `skipAuthRedirect: true` cho kiosk).
- Thành phần dùng chung (`Button`, `Field`, `Alert`, `PageHeader`, `EmptyState`), thông báo nhanh (`useToast`), `RequireRole`.
- **Trang giữ chỗ cho mọi route của module bạn** (menu và phân quyền đã chạy). Bạn thay nội dung file `page.tsx` đó.
- Kiểm thử đơn vị cho bộ gọi API (10 ca về quy tắc phiên và lỗi).

## 3. Việc cần làm
Mỗi agent làm màn hình trong kế hoạch của mình (đợt 1: Agent 2, 3, 4; đợt 2: Agent 5, 6 sau khi các PR phụ thuộc vào `master`):
1. Thay trang giữ chỗ trong `src/app/(app)/<route>/page.tsx`; code riêng đặt ở `src/features/<module>/`.
2. Gọi API chỉ qua `apiFetch`; không ghi cứng URL; xử lý theo `errorCode`.
3. Mỗi màn hình đủ bốn trạng thái: đang tải, rỗng, lỗi API, không có quyền.
4. Xong màn hình: đổi `status` thành `"ready"` ở dòng của bạn trong `src/config/navigation.ts`.
5. Trước khi mở PR: `npm run lint`, `npm run typecheck`, `npm test`, `npm run build` đều xanh; kèm ảnh chụp màn hình trong PR.

Chỉ sửa `src/app/(app)/<route của bạn>/`, `src/features/<module của bạn>/` và dòng của bạn trong `navigation.ts`. Các file ở `lib/` và `components/` thuộc Agent 1: cần đổi hoặc thêm thành phần dùng chung thì nhờ Agent 1.

## 4. Chưa có và sắp có (Agent 1)
- Endpoint cho giao diện: `GET /api/v1/trainers`, `GET /api/v1/users?role=MEMBER&q=`, `GET /api/v1/branches` (và 401 JSON cho người chưa đăng nhập).
- Tài khoản thử và dữ liệu mẫu: `docs/TEST_ACCOUNTS.md`, `scripts/dev-seed/00_users.sql`. Mỗi agent viết file seed của module mình theo quy tắc trong `REPLIES_FRONTEND_PLANS.md`.
- Tải ảnh đại diện (`POST /profile/me/avatar`) và stage build frontend trong Jenkins.

Trong lúc chờ, đăng ký tài khoản hội viên ở `/register` để thử. Tài khoản nhân viên cần nạp bằng seed.

## 5. Đã đối chiếu với backend
Khung đã được đối chiếu với `AuthController` và `PageResponse` thật (cách làm mới token, đăng xuất, hình dạng phân trang). Điểm duy nhất chưa đối chiếu trực tiếp là tên các trường của `AuthResponse`. Nếu bạn thấy khung sai so với Swagger, báo Agent 1 kèm đường dẫn endpoint, đừng tự sửa file `lib/`.

## 6. Báo lỗi
Dùng mẫu ở `NOTICE_FRONTEND_PHASE.md` mục 6. Lỗi khung dùng chung gửi Agent 1; lỗi giao diện gửi chủ module; lỗi API sai so với `API_DESIGN.md` gửi chủ backend của module đó.
