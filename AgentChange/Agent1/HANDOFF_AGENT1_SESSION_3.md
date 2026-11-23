# HANDOFF Agent 1, phiên 3 (13 đến 15/11/2026)

Đọc hết file này trước khi làm. Phiên mới: tóm tắt lại bằng lời của mình cho Phi, rồi hỏi **đúng một câu**: làm việc nào trước (gợi ý ở mục 7).

## 1. Bối cảnh và cách làm việc

- Người dùng: **Phi** (Phạm Hoàng Phi), Backend Lead và Fullstack, nhóm làm dự án môn Công nghệ phần mềm. Giao tiếp **bằng tiếng Việt**.
- Mình là **Agent 1 (Backend Lead)** của dự án **GMS (Gym Management System)**: Spring Boot 3.3 (Java 17) + SQL Server, frontend Next.js 14. Repo: `https://github.com/sleepingbuild/SpringBoot-GYM-MANAGEMENT-SYSTEM`. Có 6 agent (1 đến 6), các agent khác làm module riêng, Phi gửi nhiệm vụ cho họ qua file md.
- **Mình không có quyền ghi lên GitHub, không merge, không chạy Maven** (sandbox không tải được Maven Central). Mình đọc repo công khai bằng `git clone`, chạy được frontend (npm: lint, typecheck, test, build) và `javac` kiểm tra cú pháp. Mọi code Java mình viết **chưa được biên dịch ở sandbox**, Jenkins mới là nơi kiểm tra thật. Luôn nói thẳng điều này.
- Quy trình bàn giao: mình tạo **file patch** (`git diff`) trong `/mnt/user-data/outputs/`, Phi áp dụng trên nhánh mới từ `origin/master`, push, mở PR, chờ Jenkins xanh, merge:
  ```powershell
  git fetch origin --prune
  git switch -c <nhanh-moi> --no-track origin/master
  git apply --index --ignore-whitespace D:\AgentChange\Agent1\<file>.patch
  git commit -m "..."
  git push -u origin <nhanh-moi>
  ```
  Luôn kiểm tra `git apply --check` lên `origin/master` mới nhất trước khi gửi patch. Đặt **tên nhánh mới** (nhánh cũ trùng tên đã gây lỗi nhiều lần).
- Khuôn mặt trả lời: dẫn bằng kết luận, ngắn gọn (Phi dùng điện thoại), không bịa kết quả chưa chạy, dán lệnh PowerShell đầy đủ.

## 2. Môi trường của Phi (Windows, mọi thứ chạy trên một máy)

- Thư mục repo: `D:\SrpingBoot-GYM\gms-docs` (có nhiều worktree: `master` đang mở ở `D:\SrpingBoot-GYM\gym-management-system`, nên **không `git switch master` ở gms-docs**, dùng `git switch --detach origin/master`). `core.fileMode=false` đã đặt (file `.sh` mình tạo có quyền thực thi gây lệch). Patch lưu ở `D:\AgentChange\Agent1\`.
- **Jenkins** chạy bằng lệnh `java -jar jenkins.war --httpPort=8080` dưới tài khoản **ADMIN** (không phải dịch vụ Windows; tắt cửa sổ hoặc tắt máy thì dừng). Job **GMS-CICD**, webhook GitHub qua **ngrok** (địa chỉ đổi khi khởi động lại bản miễn phí, phải cập nhật Payload URL).
- Backend dịch vụ Windows `GymManagementSystem` (cổng 8081, `C:\deploy\gms\gms.jar`) do stage Deploy cũ cập nhật.
- **Docker Desktop**: hệ thống chạy bằng `docker-compose.demo.yml` (project `gms-demo`): web **3000**, API **8082**, SQL Server 11433, + `docker-compose.monitoring.yml` (Prometheus **9090**, Grafana **3001**, tài khoản `admin`/`admin12345`). Tài khoản mẫu: xem `docs/DOCKER_DEMO.md`. Mật khẩu/JWT đang là **mặc định** (không có `~\.gms.env`).
- Đã sửa `C:\Users\ADMIN\.docker\config.json` (bỏ `credsStore`, thêm `auths` giả) để Docker chạy được qua SSH. Nếu lỗi `error getting credentials ... logon session` quay lại: làm lại bước đó.
- **OpenSSH Server** bật trên chính máy Windows, khóa `C:\Users\ADMIN\.ssh\gms_deploy_ed25519`, khóa công khai ở `C:\ProgramData\ssh\administrators_authorized_keys`. Biến Jenkins (Global properties): `GMS_SSH_OS=windows`, `GMS_SSH_HOST=127.0.0.1`, `GMS_SSH_USER=ADMIN`.
- Máy **không đủ ổ để tạo máy ảo**. Đã tạm **ngưng deploy Vercel/Render**. Giảng viên chỉ yêu cầu: (3) thiết lập CI/CD, (4) tự động deploy lên server qua SSH, (5) triển khai và giám sát bằng Prometheus + Grafana; thêm phần mới: công cụ giám sát/theo dõi và volume trên Docker.

## 3. Quyết định nghiệp vụ đã chốt (Phi)

- Bỏ trang Tổng quan ở mọi vai trò; hồ sơ chỉ ghi chiều cao, cân nặng, mục tiêu cho **member**; chỉ giữ trang lịch cho member; admin bỏ Thanh toán và Gói tập (đã có trong quản trị gói), thêm quản lý khách hàng, nhân viên, **gán vai trò cho tài khoản**; lễ tân chỉ giữ đặt hộ hội viên.
- Member: thanh toán **mô phỏng nhận luôn**, đặt lịch tự động `CONFIRMED` (riêng ngày PT không có lịch dạy thì `PENDING` chờ PT xác nhận; Agent 3 triển khai theo khung ngoài ca 21:00 đến 23:59, Phi đã merge), mục POS, số buổi còn lại theo gói, thống kê cá nhân trong hồ sơ.
- PT: hoàn thành buổi, **dưới 45 phút bắt buộc nhập lý do** (cấu hình `gms.booking.min-complete-minutes`); hiển thị giờ vào/ra; cần nút **Client_No_Show** (xem mục 6).
- Jenkins xanh trên `master` thì web Docker (hoặc máy chủ SSH) tự cập nhật.

## 4. Đã làm và đã merge vào master (theo ghi nhận trong chat)

- PR A: bỏ Tổng quan, lọc hồ sơ và đặt lịch theo vai trò; trang đầu sau đăng nhập theo vai trò (`lib/home.ts`). PR D1 (#118): backend `/api/v1/admin/users` (danh sách, tạo nhân viên, sửa, gán vai trò, khoá/mở, không tự đổi vai trò hay tự khoá mình, mã lỗi `USER_NOT_FOUND`, `USER_SELF_CHANGE_FORBIDDEN`), trang `/admin/users`, và `JwtAuthenticationFilter` từ chối tài khoản bị khoá ngay.
- CI/CD: stage `Docker web` (dùng cả file giám sát), stage `Deploy SSH` (Linux và Windows: `scripts/deploy/remote-deploy.sh` và `.ps1`, `setup-server.sh`), Verify chờ ~6 phút, sửa dấu nháy Windows (#134), chỉ gửi SIGHUP cho Prometheus sau khi sẵn sàng (#137, nguyên nhân thật của lần Jenkins đỏ do Prometheus: SIGHUP gửi quá sớm làm nó thoát mã 2 và `docker kill` không cho tự bật lại), in nhật ký đúng dịch vụ lỗi. `Deploy SSH` đã chạy **xanh** (vào 127.0.0.1).
- Giám sát: `monitoring/prometheus/alerts.yml` (5 luật: BackendDown, HighServerErrorRate, HighLatencyP95, HeapAlmostFull, DatabasePoolExhausted), `docs/MONITORING.md`, `docs/CICD_SSH_DEPLOY.md`.
- Phi đã merge hầu hết nhánh của các agent (3 đợt): sql-time-param, face-match-log, live-seed, seed-month, e2e-demo, member-stats-api, open-booking-port, agent2-frontend-member, agent5-member-pos, agent3-admin-schedule, auto-confirm, offhours-ui.

## 5. Patch đã giao nhưng CHƯA xác nhận đã áp dụng (hỏi Phi trước)

| File patch | Nội dung |
|---|---|
| `pr_d2_member_stats.patch` | Thống kê cá nhân của member trong Hồ sơ (`features/profile/MemberStatsPanel.tsx`, tính từ gói và danh sách buổi của mình) |
| `pr_sonar_fix.patch` | 4 issue Sonar (hằng số `RECEPTIONIST`/`SUPER_ADMIN` trong `MembershipController`, comment giống code và TODO trong `SecurityConfig`), CORS cấu hình được (`GMS_CORS_ALLOWED_ORIGINS`), test cho `LeadAssignmentController`, `SalesUserRepository`, `AdminUserController`, `SecurityConfig` |
| `pr_docker_tools_volumes.patch` | cAdvisor trong compose giám sát + job Prometheus, dashboard `gms-docker.json`, `docker-compose.portainer.yml` (tùy chọn), `docs/DOCKER_TOOLS_VOLUMES.md`, Jenkins và script SSH dựng lại cả cAdvisor |

Việc kiểm tra cần làm sau khi merge `pr_docker_tools_volumes.patch`: container `gms-demo-cadvisor-1` tạo tay trước đó sẽ bị tạo lại; dashboard Docker có thể trống nếu Docker Desktop dùng kho image containerd (kiểm tra `container_memory_working_set_bytes` có nhãn `name="gms-demo..."` ở `http://localhost:8083/metrics`).

## 6. Việc còn dở

1. **5 nhánh mới của các agent, chưa biết Phi đã merge chưa.** Mình đã gộp thử: không xung đột, frontend 192/192 test và build đạt. Thứ tự: `agent5-pos-menu-member`, `agent4-seed-runner-entries`, `agent2-my-membership-overview`, `agent6-e2e-stats-sync` (merge cùng lúc), chờ Jenkins xanh, rồi `agent3-complete-rules` (luật hoàn thành 45 phút, nhánh lớn nhất). **Không merge** `agent3-bookings-branch-filter` (lỗi thời, xung đột) và `shared/agent5-error-codes` (rỗng).
2. **Nút Client_No_Show cho PT: chưa ai làm.** Chỉ có chữ hiển thị `NO_SHOW`, chưa có nút bấm hay endpoint cho PT. Hỏi Phi có cần soạn yêu cầu cho Agent 3 không.
3. **SonarQube:** Quality Gate đang đỏ (New Code: 4 issue, coverage 63,9% trên 22 dòng mới, ngưỡng ≥ 80%). Sau khi merge `pr_sonar_fix.patch` và các nhánh mới, Phi quét lại và chụp trang Overview (New Code). Có thể phát sinh issue mới từ code của các agent.
4. **Cập nhật `TONG_KET_DU_AN_GMS.md`** (vẫn ghi "Quality Gate Passed" của lần quét đầu) và file nhiệm vụ cho khớp trạng thái cuối, để chuẩn bị báo cáo.
5. Khuyến nghị tăng cường: nên có `~\.gms.env` (mật khẩu DB, JWT, Grafana) thay mặc định trước buổi demo; nếu cần máy chủ riêng cho giảng viên thì dùng laptop của bạn trong nhóm (Docker Desktop + làm mục 2B của `docs/CICD_SSH_DEPLOY.md`, đổi `GMS_SSH_HOST`).
6. Ghi chú nhỏ: Agent 6 đã làm endpoint `/members/me/stats` nhưng giao diện thống kê của mình tính trực tiếp từ API sẵn có (không phụ thuộc). Agent 2 chưa nối cổng `OpenBookingCountPort` vào giao diện đặt lịch ngoài phần `my-membership-overview` (xem nhánh số 3 ở trên).

## 7. Gợi ý câu hỏi mở đầu phiên mới

"Bạn đã áp dụng và merge chưa: `pr_d2_member_stats`, `pr_sonar_fix`, `pr_docker_tools_volumes`, và 5 nhánh mới? Jenkins đang xanh hay đỏ? Mình nên làm việc nào trước: (a) quét lại Sonar và sửa issue, (b) cập nhật `TONG_KET_DU_AN_GMS.md`, (c) soạn yêu cầu nút Client_No_Show cho Agent 3?"

## 8. Cạm bẫy đã gặp (đừng lặp lại)

- Lệnh `git switch -c` trùng tên nhánh cũ làm áp dụng nhầm nhánh; luôn đặt tên mới và `git status --short` phải sạch trước khi `git apply`.
- PowerShell 5.1: lệnh native ghi stderr bị tính là lỗi nếu dùng `$ErrorActionPreference="Stop"`; dùng `Continue` và kiểm tra `$LASTEXITCODE`. `powershell.exe -File` giữ nguyên dấu nháy đơn trong giá trị.
- `docker kill` (kể cả `compose kill`) khiến Docker coi là "tắt chủ động", `restart: unless-stopped` không bật lại.
- Flyway của dự án có `out-of-order: true` (migration `V5_1` hợp lệ).
- Không thêm `[stated]` vào bộ nhớ những thứ chỉ nằm trong log công cụ; chỉ ghi điều Phi nói.
