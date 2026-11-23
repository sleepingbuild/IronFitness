# Tóm tắt phiên làm việc của Agent 1 (để tiếp tục ở phiên chat mới)

Cập nhật: **03/11/2026**. Dự án: **Gym Management System (GMS)**. Người dùng: Phạm Hoàng Phi (Backend Lead), làm việc bằng tiếng Việt trên ứng dụng di động.

## 0. Cách dùng file này

Dán file này (và các file được liệt kê ở mục 10 nếu cần) vào phiên mới. Phiên mới nên:

1. Đọc mục 1 đến 4 để nắm vai trò, quy ước và đường dẫn.
2. Chạy lệnh ở mục 12 để biết **trạng thái thật** của `master` (trạng thái PR trong file này có thể đã cũ).
3. Làm tiếp theo backlog mục 8, theo thứ tự ưu tiên.

Các file trong `/mnt/user-data/outputs/` của phiên cũ có thể không còn: nếu cần file nào ở mục 10, nhờ người dùng tải lại.

---

## 1. Vai trò và cách làm việc

**Mình là Agent 1** trong nhóm 6 "agent" làm song song các phân hệ của GMS. Phạm vi của Agent 1: kiến trúc, các file dùng chung (SHARED), tài liệu (`API_DESIGN.md`...), review và hướng dẫn merge PR của các agent khác, hạ tầng CI/CD, khung frontend.

| Agent | Phân hệ |
|---|---|
| 1 | Xác thực, hồ sơ, file SHARED (bảo mật, mã lỗi, phản hồi chuẩn), Jenkins, khung frontend, danh mục dùng chung, dữ liệu thử người dùng |
| 2 | Gói tập, Membership, Thanh toán |
| 3 | Lịch PT, Booking |
| 4 | Điểm danh khuôn mặt, chấm công, `gym_check_ins` |
| 5 | POS, sản phẩm, lương PT theo buổi |
| 6 | Khách tiềm năng (Leads/CRM), Báo cáo |

Các agent khác là phiên chat riêng; người dùng **chuyển nội dung (file .md, câu trả lời) qua lại** giữa các phiên.

**Giới hạn của mình trong sandbox:**
- **Không có quyền merge trên GitHub**: người dùng bấm merge. Mình đưa ra kiểm tra, thứ tự merge và lệnh.
- **Không chạy được PowerShell, Maven, SQL Server, trình duyệt** trong sandbox. Mọi script, truy vấn HQL/T-SQL đều **chưa được chạy thật** cho tới khi người dùng chạy trên máy. Luôn nói rõ điều này và dặn người dùng chạy `mvn clean verify` và thử chạy ứng dụng thật.
- Đã dựng và kiểm chứng trong sandbox: khung frontend (`npm ci`, lint, typecheck, test, build) bằng Node 22.

**Phong cách phản hồi người dùng đã ổn định:**
- Tiếng Việt, ngắn gọn, mở đầu bằng kết luận; lệnh PowerShell từng bước, người dùng dán lại kết quả để mình đọc.
- Người dùng thích nhận **file .md** để chuyển cho các agent khác; thích merge nhanh nhưng theo đợt có điểm dừng kiểm tra.
- Khi người dùng dán nhật ký lỗi, đọc kỹ nguyên nhân thật trước khi kết luận; không đoán.

---

## 2. Môi trường và đường dẫn trên máy người dùng (Windows)

| Mục | Giá trị |
|---|---|
| Repo GitHub | `https://github.com/sleepingbuild/SpringBoot-GYM-MANAGEMENT-SYSTEM` (nhánh chính `master`) |
| Worktree Agent 1 (sạch, dùng cho PR của Agent 1) | `D:\SrpingBoot-GYM\gms-docs` |
| Worktree giữ `master` | `D:\SrpingBoot-GYM\gym-management-system` (**không** `git switch master` ở worktree khác; dùng `origin/master`) |
| Worktree cũ lẫn lộn của Agent 1 | `D:\SrpingBoot-GYM\gms-agent1` (có file chưa tracked của agent khác; **không dùng**, không xoá khi chưa hỏi) |
| Worktree agent khác | `gms-agent2` đến `gms-agent6` cùng thư mục cha |
| Kho file lưu từ chat | `D:\AgentChange\Agent1\` (và `Agent2` đến `Agent6`); thư mục con đã dùng: `shared`, `v9`, `backend-small`, `dev-seed` |
| JDK 17 | `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot` (máy còn JDK 25 ở cùng thư mục cha; **phải dùng JDK 17**) |
| Maven | `C:\tools\apache-maven-3.9.9` |
| NSSM | `C:\Users\ADMIN\AppData\Local\Microsoft\WinGet\Links\nssm.exe` (cửa sổ PowerShell mới không có biến `$nssm`, phải gán lại) |
| Jenkins | Tiến trình Java chạy dưới tài khoản `MYPC\ADMIN` (**không phải** dịch vụ Windows), cổng 8080; job `GMS-CICD` |
| Ứng dụng đã triển khai | Dịch vụ Windows `GymManagementSystem`, cổng **8081**, jar ở `C:\deploy\gms\gms.jar`, cấu hình cục bộ `C:\deploy\gms\application-local.yml`, log `C:\deploy\gms\logs\service.log` |
| Database | `gms_db` (dùng chung, đã lệch checksum V3 cũ), `gms_db_ci` (**riêng cho Jenkins**), `gms_db_agentN` (riêng từng agent) |
| Docker | `gms-sqlserver` (1433), `gms-redis`, container riêng của Agent 5 |
| Webhook | ngrok trỏ tới Jenkins cổng 8080; URL webhook có dấu `/` cuối `/github-webhook/`; secret SHA-256 lưu ở credential `github-webhook-secret` |

Biến môi trường của dịch vụ `GymManagementSystem` (đặt qua NSSM `AppEnvironmentExtra`): `SERVER_PORT=8081`, `DB_URL` trỏ `gms_db_ci`, `SPRING_DATASOURCE_URL` (cùng giá trị). Nếu đổi, phải ghi lại **toàn bộ** các biến cùng lúc vì lệnh `set` ghi đè.

Quyền điều khiển dịch vụ: đã cấp cho `MYPC\ADMIN` bằng `sc sdset`.

---

## 3. Kiến trúc backend (tóm tắt; chi tiết trong `BACKEND_ARCHITECTURE.md`)

- Monolith phân lớp: Controller, Service (interface + `impl`), Repository (Spring Data JPA), Entity; Java 17, Spring Boot 3.3.4, SQL Server 2022, Redis, Flyway, JWT (JJWT 0.12.5), springdoc.
- Giao tiếp giữa phân hệ qua interface và **sự kiện nội bộ đồng bộ, cùng transaction**: `PaymentConfirmedEvent` (Payment → Membership), `BookingSettledEvent` (Booking → trừ buổi, ghi lương). Payment **không** gọi Membership (đã gỡ vòng phụ thuộc).
- Phản hồi chuẩn `ApiResponse` (`success`, `message`, `data`, `errorCode`, `timestamp`); mã lỗi trong `ErrorCode`; `GlobalExceptionHandler` chuẩn hoá lỗi (400 `VALIDATION_ERROR`, 409 `DATA_CONFLICT`, 403 `AUTH_FORBIDDEN_ROLE`...).
- Quyền: tiền tố `ROLE_` trong authorities → dùng `@PreAuthorize("hasRole('X')")` / `hasAnyRole(...)`. Vai trò: `SUPER_ADMIN`, `RECEPTIONIST`, `SALES`, `TRAINER`, `MEMBER`.
- Thời gian: `LocalDateTime` giờ Việt Nam; mọi chỗ lấy "bây giờ" dùng bean `Clock` (không dùng `now()` trần).
- `spring.flyway.out-of-order: true`. Migration đã có: V1, V2, V2.1, V2.2, V3, V4, V6, V9 (và V5, V8 theo thông báo đã merge); **V7 đang chờ**.

---

## 4. Quy ước làm việc và bài học (quan trọng)

**Git / PR**
- Mọi thay đổi vào `master` qua PR. Merge bằng **"Create a merge commit", không squash** (chuỗi PR frontend của Agent 3 phụ thuộc nhau).
- Tạo nhánh từ `origin/master` bằng `git switch -c <nhánh> --no-track origin/master`, làm việc ở `gms-docs` (cây sạch: `git status --short` phải rỗng).
- Kiểm tra xung đột trước merge: `git merge-tree --write-tree origin/master origin/<nhánh>` rồi đọc `$LASTEXITCODE` (0 là sạch). Xem file đổi: `git diff --name-only origin/master...origin/<nhánh>`. Liệt kê nhánh chưa merge: `git branch -r --no-merged origin/master`.
- Điều kiện Done của mọi PR: `mvn clean verify` xanh, `spring-boot:run` khởi động được, có test, cập nhật `API_DESIGN.md`, có Swagger annotation. Không sửa `Jenkinsfile`, `application.yml`, `pom.xml`, file SHARED nếu không phải Agent 1.

**Migration (bài học từ V9)**
- Không sửa migration đã merge và áp dụng thành công; đổi schema bằng migration mới.
- **Mọi migration phải được chạy thật trên database trống** (V1 đến mới nhất) và dán `flyway_schema_history` làm bằng chứng. V9 từng hỏng vì gọi `QUOTENAME` bên trong `EXEC( ... )` của T-SQL (phải gán vào biến rồi `EXEC sp_executesql`).
- Mỗi agent dùng database riêng; `gms_db_ci` chỉ dành cho Jenkins. Không `flyway repair`.

**Script PowerShell do mình đưa**
- Lưu **UTF-8 có BOM** (Windows PowerShell 5.1 đọc file không BOM theo ANSI và làm hỏng chữ Việt, từng làm hỏng một chuỗi trong `JwtTokenProvider`). Sửa file Java/Markdown **tại chỗ** bằng `String.Replace` với kiểm tra neo, không ghi đè cả file (tránh mất sửa của agent khác, ví dụ bản `ErrorCode` người dùng gửi từng cũ).
- Xuất file nhiều phần từ `git show` phải đặt `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8` trước, rồi ghi bằng `[IO.File]::WriteAllLines(..., UTF8 không BOM)`; nếu không sẽ bị lỗi mã hoá. Khi người dùng gửi file `.txt` dạng UTF-8 BOM, giải mã bằng `utf-8-sig`.
- Dặn người dùng **đính kèm file vào chat** (không dán đầu ra lệnh) khi cần mình đọc nội dung file.

**Khác**
- Không sao chép file `.md` hướng dẫn giữa các môn học; hai báo cáo môn học có file riêng.
- Next.js 14.2.35 (khung frontend) có cảnh báo `npm audit`; **không** chạy `npm audit fix --force`; không đưa máy chủ Next ra Internet; nâng cấp ở PR riêng trước khi triển khai công khai.

---

## 5. Những gì đã làm trong phiên này (theo thứ tự)

1. **Bắt đầu**: nhận `SESSION_SUMMARY.md` của phiên trước (vai trò Agent 1). Hướng dẫn merge các nhánh Agent 2 đến 6; xử lý `config/application-yml`, `shared/error-code-booking-pt`; ghép `API_DESIGN.md`.
2. **CI/CD cho bài thực hành Triển khai phần mềm** (ban đầu theo đề mẫu WebBanHang, sau đổi sang chính dự án Spring Boot): `Jenkinsfile` 7 giai đoạn (Checkout, Restore, Build, Test, Publish, Deploy, Verify), `tools { jdk 'JDK17' }`, deploy chỉ từ `master`, kiểm tra `/v3/api-docs`. Xử lý các sự cố thật: JDK 25 với Lombok, `JAVA_HOME` sai, cổng 8080 bị Jenkins chiếm, SQL Server Docker, vòng phụ thuộc bean (Agent 2), quyền điều khiển dịch vụ, Flyway checksum V3 → tách `gms_db_ci`, V9 lỗi cú pháp, webhook `400 No valid signature` (ô secret trống). Có `SLIDES_CICD.md` và sơ đồ `cicd_flow.png/svg` cho bài CI/CD (yêu cầu 1–3 của đề: URL GitHub, Jenkinsfile, ảnh cấu hình và lịch sử).
3. **Phần SHARED**: `AuthService.createMemberAccount`, `UserRepository.existsByPhone`, chuẩn hoá số điện thoại, 4 handler mới trong `GlobalExceptionHandler`, `ErrorCode.DATA_CONFLICT`, `/swagger-ui.html` công khai, kiểm tra độ dài `gms.jwt.secret` (tối thiểu 32 byte), cập nhật `API_DESIGN.md` (Leads, hồ sơ).
4. **V9** (filtered unique index `users.phone`, `age` thành `date_of_birth`, bỏ `users.avatar_url`) kèm code hồ sơ; V9 gặp lỗi cú pháp, đã sửa và kiểm chứng trên DB trống.
5. **Khung frontend** `frontend/` (Next.js 14, TypeScript, Tailwind): đăng nhập, đăng ký, khung có menu theo vai trò, trang Tổng quan, hồ sơ, `apiFetch` xử lý phiên **theo `errorCode`** (18 test). Sửa lỗi giả định: `refresh` nhận tham số `@RequestParam` nên gửi dạng form. Đã merge cùng bản sửa.
6. **Trả lời các kế hoạch frontend** của Agent 2, 3, 4, 6 (`REPLIES_FRONTEND_PLANS.md`): quy tắc xác thực, cấu trúc dữ liệu thử `scripts/dev-seed/`, danh sách tài khoản thử, các endpoint cần làm.
7. **Điều phối merge nhiều đợt**: kiểm tra từng PR (SecurityConfig gói công khai, `@PreAuthorize` của Agent 3, seed Agent 3, V8 của Agent 4, V5 của Agent 5...), giữ V8 cho tới khi Agent 4 sửa hai điều kiện, yêu cầu bằng chứng chạy migration trên DB trống.
8. **Chuẩn bị (chưa chắc đã merge, xem mục 6)**: PR backend nhỏ (endpoint danh mục + 401/403 JSON), seed người dùng, script ba mã lỗi POS.
9. **Tài liệu**: `BACKEND_ARCHITECTURE.md`; ba file cho agent viết báo cáo (mục 9).

---

## 6. Trạng thái PR và nhánh (theo thông tin mới nhất; kiểm tra lại bằng mục 12)

Người dùng báo "tất cả build Jenkins đều xanh" sau các đợt merge. Theo thông báo của các agent:

| Hạng mục | Ghi chú |
|---|---|
| Khung frontend + sửa `refresh` | Đã vào `master` (đã xác nhận bằng log) |
| `shared/security-public-packages`, `shared/error-code-payment-not-refundable`, `agent2-membership-lookup` | Đã merge theo đợt 1 (cần xác nhận) |
| `agent3-available-auth`, `agent3-dev-seed`, chuỗi frontend Agent 3 (`agent3-frontend` rồi `trainer-bookings` rồi `member-bookings`) | Đã merge hoặc đang merge theo thứ tự |
| `agent4-face-profile-me`, `agent4-frontend` | Đã duyệt hướng merge; thiếu dòng `API_DESIGN.md` cho `GET /face-profiles/me` (Agent 4 thêm) |
| `agent4-ch2-gym-check-ins` (V8) | Điều kiện merge: thông báo trung tính, giải phóng khoá dedup Redis (`redisTemplate.delete`), bằng chứng DB trống. Agent 4 báo đã sửa ở commit `c8c146a`; **cần xác nhận bản sửa đã ở `master`** |
| `agent5-pos-commission` (V5, PR #70) | Đã đọc SQL; cần xem `CommissionServiceImpl.recordSessionPay` (kiểm tra trùng `existsByBookingId` trước khi ghi, không ném lỗi). Theo thông báo cuối đã vào `master` |
| **V7 của Agent 2** (tách `related_id`) | **Chưa merge**: thiếu bằng chứng chạy trên DB có dữ liệu cũ, cần Agent 6 xác nhận sửa `ReportRepository`, cần kiểm tra dữ liệu `gms_db_ci` bằng truy vấn trong mô tả PR. Mình **chưa đọc** `V7__payment_transactions_split_related.sql` |
| **PR danh mục của Agent 1** (`/users`, `/trainers`, `/branches`, 401/403 JSON) | Đã chuẩn bị file + script `apply_backend_small.ps1`; **chưa biết đã áp dụng và merge chưa**. HQL chưa được chạy thật |
| **PR seed người dùng** (`00_users.sql`, `docs/TEST_ACCOUNTS.md`) | Đã chuẩn bị; chưa biết đã merge chưa; chưa thử đăng nhập bằng hàm băm |
| **PR ba mã lỗi POS** (`apply_errorcodes_pos.ps1`) | Chưa mở |

---

## 7. Quyết định đã chốt

- **Xác thực và lỗi ở client**: xử lý theo `errorCode`. 401/403 không có `errorCode` hoặc `AUTH_TOKEN_*` là phiên hỏng (thử làm mới một lần); `AUTH_FORBIDDEN_ROLE` là thiếu quyền; 401 có `errorCode` khác (`FACE_NOT_RECOGNIZED`, `AUTH_INVALID_CREDENTIALS`) chuyển nguyên cho trang gọi. Trang kiosk dùng `skipAuthRedirect`. Backend sẽ đổi người chưa đăng nhập thành 401 JSON (`AUTH_TOKEN_INVALID`) khi PR danh mục merge.
- **Dữ liệu thử**: mỗi agent tự viết seed riêng trong `scripts/dev-seed/` (`00_users` Agent 1, `10_` Agent 2, `20_` Agent 3, `40_` Agent 4, `50_` Agent 5, `60_` Agent 6): chạy lại không lỗi, tham chiếu user theo email, ngày giờ tương đối giờ Việt Nam; không phải migration. Tài khoản thử: `admin@`, `reception@`, `sales@`, `trainer1@`, `trainer2@`, `member.active@`, `member.nopackage@`, `member.expired@` với đuôi `gym.test`, mật khẩu chung `Gym@12345` (chỉ DB dev).
- **Endpoint danh mục (Agent 1)**: `GET /users?role=MEMBER&q=` (RECEPTIONIST, SUPER_ADMIN; chỉ `MEMBER`; sắp theo tên cố định; `size` tối đa 50), `GET /trainers` và `GET /branches` (mọi người đã đăng nhập; chỉ trả thông tin công khai).
- **Gói của hội viên khác (H2) và lọc giao dịch (H3)**: Agent 2 làm.
- **Hoàn tiền POS**: thêm trạng thái `REFUNDED`; `PaymentService.refundByRelated(RelatedType, UUID)` (Agent 2, sau V7), không lưu `transactionId` vào `pos_orders`; gọi lại không hoàn thêm; mã `PAYMENT_NOT_REFUNDABLE` (409). Trước đó ghi giới hạn vào `API_DESIGN.md`.
- **Ba mã lỗi POS** (404): `POS_ORDER_NOT_FOUND`, `COMMISSION_NOT_FOUND`, `TRAINER_PAY_RATE_NOT_FOUND`.
- **Leads**: `LOST` là trạng thái cuối; mật khẩu tạm khi chuyển lead chỉ trả một lần, không ghi log; báo cáo doanh thu là doanh thu gói tập gộp (chưa trừ POS); `checkin-traffic` = `gym_check_ins` + `bookings` (loại trừ nhau).
- **`gym_check_ins` (V8)**: chỉ ghi lượt vào không có booking; vào cần gói ACTIVE; ra không kiểm tra gói; dòng mở từ ngày trước để nguyên; `branch_id` NULL.
- **Frontend**: Next.js 14 (App Router), TypeScript, Tailwind; mỗi agent chỉ sửa `frontend/src/app/(app)/<route>/`, `frontend/src/features/<module>/` và dòng của mình trong `navigation.ts`; thư mục `lib/` và `components/` thuộc Agent 1. Camera cần `localhost` hoặc HTTPS; ưu tiên `next dev --experimental-https`, hạn chế ngrok.
- **Báo cáo môn học**: giao cho một agent riêng; hai môn có hai file hướng dẫn riêng, không dùng chung nội dung.

---

## 8. Backlog của Agent 1 (theo thứ tự ưu tiên)

1. **Xác nhận trạng thái thật** bằng lệnh mục 12; đối chiếu bảng mục 6.
2. **PR danh mục** (`feature/agent1-directory-endpoints`): người dùng áp dụng `apply_backend_small.ps1` (merge `shared/security-public-packages` trước), `mvn clean verify`, **chạy ứng dụng thật trên DB trống** (kiểm tra HQL: `exists` trên `u.roles`, nối `UserProfile`, `escape '!'`), thử ba endpoint và 401/403 JSON; sau đó merge.
3. **PR seed người dùng**: chạy `00_users.sql` hai lần (idempotent, 8 dòng), thử đăng nhập `admin@gym.test` / `Gym@12345`; nếu hàm băm không khớp thì tạo lại.
4. **PR ba mã lỗi POS** bằng `apply_errorcodes_pos.ps1` (nhánh `shared/error-codes-pos`).
5. **Duyệt V7**: đọc file SQL và phần sửa `ReportRepository`; xin bằng chứng chạy trên DB có dữ liệu cũ; kiểm tra `gms_db_ci` (hoặc tạo lại); Agent 6 xác nhận.
6. Duyệt PR hoàn tiền `refundByRelated` và `PAYMENT_NOT_REFUNDABLE` của Agent 2; duyệt H3.
7. Duyệt V8 nếu chưa merge (kiểm tra `redisTemplate.delete`); duyệt `CommissionServiceImpl.recordSessionPay` của Agent 5.
8. Cập nhật tài liệu: `NOTICE_FRONTEND_PHASE.md` và `frontend/README.md` còn nhắc người chưa đăng nhập nhận 403 (sau khi 401 JSON vào); bổ sung ba endpoint vào `frontend` nếu cần kiểu dữ liệu.
9. **Việc nền còn nợ**: nâng cấp Next.js (nhánh có bản vá, cần React 19), thêm stage build frontend vào Jenkins, gom nhóm Swagger theo module (`OpenApiConfig`), tải ảnh đại diện trong frontend (trường tải lên tên là `image`, không phải `file`).
10. **Chưa rõ trạng thái từ phiên trước** (trong `SESSION_SUMMARY.md` cũ): các hạng mục A18 đến A21, R22, G3, Testcontainers; đọc lại file đó nếu cần.
11. **Môn Triển khai phần mềm** còn thiếu so với mẫu báo cáo: tích hợp SonarQube, đóng gói Docker cho ứng dụng và docker-compose, deploy qua SSH lên máy chủ công khai, Prometheus và Grafana. Mình có thể triển khai nếu người dùng yêu cầu (xem `02_REPORT_GUIDE_TRIEN_KHAI_PHAN_MEM.md` mục 5: có tiêu chí hoàn thành). Lưu ý: cần thêm `micrometer-registry-prometheus` và mở `/actuator/prometheus` có kiểm soát (hiện chỉ `/actuator/health` công khai).

---

## 9. Việc của các agent khác (để theo dõi)

- **Agent 2**: V7 (sau khi bổ sung bằng chứng), `refundByRelated`, H3, H4 (hành vi khi bỏ trống `branchId`), `Clock` thay `LocalDate.now()` trong `PaymentServiceImpl`, seed `10_`, màn hình gói tập; xác nhận số lần `tryConsumeSession` trên một booking.
- **Agent 3**: màn hình đặt lịch (chờ `/trainers`, `/branches`, `/users`), chạy lại seed `20_` với tài khoản chính thức, thử cùng Agent 5 luồng quét → hoàn thành → lương.
- **Agent 4**: màn hình đăng ký khuôn mặt, tự điểm danh, kiosk, chấm công, lịch sử check-in; hiệu chỉnh ngưỡng `0.45` với khuôn mặt thật; thêm dòng `API_DESIGN.md` cho `GET /face-profiles/me`; chuỗi phương thức điểm danh là `'FACE'`.
- **Agent 5**: màn hình POS (đơn khách vãng lai chỉ chạy sau V7), seed `50_`, đổi sang ba mã lỗi mới sau khi PR [shared] merge, gọi `refundByRelated` khi huỷ đơn; thử listener lương với booking thật.
- **Agent 6**: `checkin-traffic` sang hai nguồn, `commissions-summary`, sửa SQL theo V7, màn hình Leads và báo cáo, seed `60_`, chạy lại rà Swagger (hiện 53/53 endpoint có `@Operation` ở thời điểm rà, chưa có controller POS).

---

## 10. Các file đã tạo trong phiên (nếu cần dùng lại, nhờ người dùng tải lại)

| Nhóm | File |
|---|---|
| Thông báo và phản hồi cho agent | `MERGE_ORDER_NOTICE.md`, `NOTICE_MASTER_GREEN.md`, `NOTICE_AGENT6_CREATE_MEMBER.md`, `NOTICE_SHARED_UPDATED.md`, `NOTICE_V9.md`, `NOTICE_FRONTEND_PHASE.md`, `NOTICE_FRONTEND_SCAFFOLD_READY.md`, `NOTICE_MERGE_WAVE_DONE.md`, `AGENT2_FIX_CIRCULAR_DEPENDENCY.md`, `REPLIES_AGENT4_AGENT6.md`, `REPLY_AGENT4_V8.md`, `REPLIES_AGENT2_AGENT4.md`, `REPLIES_FRONTEND_PLANS.md`, `REPLIES_AGENT2_3_4_5_MERGES.md`, `REPLIES_V8_V5_FRONTEND_PRS.md` |
| Code SHARED đã viết | `agent1-shared/` (AuthService, AuthServiceImpl, UserRepository, ApiResponse, GlobalExceptionHandler, test); `v9/` (migration V9 đã sửa, entity, DTO, `ProfileServiceImpl`, test, `apply_v9.ps1`) |
| PR backend nhỏ (chờ áp dụng) | `backend-small/`: `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `UserDirectoryRepository`, `DirectoryService(Impl)`, 3 controller, 3 DTO, 2 file test, `apply_backend_small.ps1` |
| Script vá tại chỗ | `apply_shared_patch.ps1`, `apply_api_patch.ps1`, `apply_api_profile_note.ps1`, `apply_errorcodes_pos.ps1`, `apply_backend_small.ps1`, `apply_v9.ps1` (tất cả UTF-8 có BOM) |
| Dữ liệu thử | `dev-seed/00_users.sql`, `dev-seed/TEST_ACCOUNTS.md` |
| Frontend | `frontend.zip` (khung đã đóng gói; bản mới nhất có sửa `refresh`) |
| CI/CD | `Jenkinsfile`, `SLIDES_CICD.md`, `cicd_flow.png`, `cicd_flow.svg` |
| Tài liệu | `BACKEND_ARCHITECTURE.md` |
| Gói báo cáo | `report-pack/01_PROJECT_BRIEF_FOR_REPORT_AGENT.md`, `report-pack/02_REPORT_GUIDE_TRIEN_KHAI_PHAN_MEM.md`, `report-pack/03_REPORT_GUIDE_BAI_TAP_LON.md` |

---

## 11. Điều chưa kiểm chứng / rủi ro đã biết

- **HQL** trong `UserDirectoryRepository` (`exists` trên `u.roles`, nối thực thể `UserProfile`, `escape '!'`, constructor expression với `record`) chưa được chạy; sai cú pháp thì ứng dụng **không khởi động**, test Mockito không bắt được.
- Mọi script `.ps1` chưa được chạy bởi mình; kết quả `git diff --stat` và `git status` sau khi áp dụng là cách kiểm tra.
- Tên trường của `AuthResponse` (`accessToken`, `refreshToken`, `email`, `fullName`, `roles`) trong khung frontend là suy ra từ cách backend dựng đối tượng, chưa đối chiếu trực tiếp.
- `00_users.sql`: hàm băm BCrypt đã được kiểm tra bằng thư viện Python (tiền tố `$2a$`), **chưa** thử đăng nhập vào ứng dụng thật.
- `user.getCreatedAt()` được giả định là `LocalDateTime` (trong `ProfileServiceImpl`); build sẽ báo nếu sai.
- Cổng 8080 là Jenkins, ứng dụng là 8081; một số agent từng nhầm.
- Ngưỡng nhận diện khuôn mặt `0.45` chưa hiệu chỉnh với khuôn mặt thật; chưa có kiểm tra người thật (liveness).
- Trong báo cáo môn học, các mục SonarQube, Docker, SSH, Prometheus/Grafana **chưa làm**; không được khai là đã làm.

---

## 12. Lệnh kiểm tra trạng thái (chạy ở `D:\SrpingBoot-GYM\gms-docs`)

```powershell
git fetch origin --prune

# Nhánh còn chưa merge
git branch -r --no-merged origin/master

# Các hạng mục đã vào master hay chưa
$checks = [ordered]@{
  "V5 POS"             = "src/main/resources/db/migration/V5__pos_commission.sql"
  "V7 split related"   = "src/main/resources/db/migration/V7__payment_transactions_split_related.sql"
  "V8 gym_check_ins"   = "src/main/resources/db/migration/V8__gym_check_ins.sql"
  "face-profile-me"    = "src/main/java/com/gym/management/dto/response/FaceProfileStatusResponse.java"
  "membership-lookup"  = "src/test/java/com/gym/management/controller/MembershipControllerTest.java"
  "directory (/users)" = "src/main/java/com/gym/management/controller/UserController.java"
  "dev-seed users"     = "scripts/dev-seed/00_users.sql"
  "TEST_ACCOUNTS"      = "docs/TEST_ACCOUNTS.md"
}
foreach ($k in $checks.Keys) { $e = git ls-tree origin/master -- $checks[$k]; "{0,-22} {1}" -f $k, $(if ($e) { "CO" } else { "CHUA" }) }
"dedup release (V8)      " + $(if (git grep -q "redisTemplate.delete" origin/master -- src/main/java/com/gym/management/service/impl/FaceAttendanceServiceImpl.java) { "CO" } else { "CHUA" })

# Frontend đã có bản sửa refresh
git show origin/master:frontend/src/lib/auth/session.ts | Select-String "urlencoded"
```

Kiểm tra ứng dụng đã triển khai (cổng 8081) sau mỗi đợt merge: `GET /api/v1/packages` phải trả `200` (công khai) và `GET /api/v1/packages/admin` phải trả `403`.

---

## 13. Việc đầu tiên nên làm ở phiên mới

1. Hỏi người dùng đã merge PR nào kể từ lần cập nhật này; chạy mục 12 và dán kết quả.
2. Làm tiếp mục 8 theo thứ tự: PR danh mục (kèm chạy ứng dụng thật), seed người dùng, ba mã lỗi POS, duyệt V7.
3. Nếu người dùng chuyển sang yêu cầu môn học: gói báo cáo đã sẵn (mục 10); phần Triển khai phần mềm còn thiếu SonarQube, Docker, SSH, Prometheus/Grafana.
