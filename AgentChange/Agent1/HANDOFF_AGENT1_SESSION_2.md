# Bàn giao phiên làm việc của Agent 1 (phiên 2)

Tài liệu này để một phiên chat mới của **Agent 1 (Backend Lead)** đọc xong là tiếp tục được công việc, không phải hỏi lại. Tiếp nối `HANDOFF_AGENT1_SESSION_SUMMARY_1.md` (phiên 1). Trạng thái kho được đo ngày **10/11/2026**; mọi thứ có thể đã đổi, nên việc đầu tiên của phiên sau là chạy `git fetch` và xem lại mục 5.

**Cách dùng:** tải file này lên đầu phiên mới cùng `HANDOFF_AGENT1_SESSION_SUMMARY_1.md`, rồi nói "đọc kỹ và xác nhận lại để tiếp tục". Phiên mới nên tóm tắt lại bằng lời của mình và hỏi người dùng đúng một câu: việc nào làm trước.

---

## 1. Vai trò và giới hạn

- Vai trò: **Agent 1, Backend Lead** của dự án GMS (Spring Boot). Phụ trách kiến trúc, các file SHARED, duyệt PR của agent khác, hướng dẫn thứ tự merge, CI/CD, Docker, chất lượng mã.
- Người dùng là nhóm trưởng (tên trong hồ sơ: Phi). Nhóm có các agent làm song song (Agent 2 đến 6), mỗi agent một mảng (xem `PHAN_CONG_BACKEND.md`).
- **Mình KHÔNG làm được:** merge hay ghi lên GitHub (chỉ đọc kho công khai bằng `git clone`), chạy PowerShell, Maven (Maven Central bị chặn trong sandbox), Docker, SQL Server, trình duyệt. **Làm được:** `git` đọc kho, Python, Node 22 và npm (chạy được lint, typecheck, test, build frontend), tìm kiếm web. Do đó mọi script, mã Java, HQL, T-SQL do mình viết đều **chưa chạy thật** cho tới khi người dùng chạy và báo kết quả. Phải nói rõ điều này khi giao.
- Khi nói về kết quả, phân biệt rõ: **đã kiểm chứng** (người dùng chạy và dán kết quả), **suy luận từ mã** (ghi "chưa xác minh"), **chưa làm**.

## 2. Quy tắc làm việc (giữ nguyên từ phiên 1 và bổ sung)

- Trả lời bằng **tiếng Việt, ngắn gọn, nêu kết luận trước**. Lệnh PowerShell đưa từng bước, người dùng dán kết quả lại để mình đọc.
- Không đoán mò: có kho mã thì **đọc mã** trước khi nói. Đối chiếu số liệu bằng lệnh, không bằng trí nhớ.
- Làm việc ở thư mục `D:\SrpingBoot-GYM\gms-docs`, là một **git worktree** (kho chính ở thư mục khác). Nhánh tạo từ `origin/master`: `git switch -c <nhánh> --no-track origin/master`. Muốn đứng đúng `master` mà không vướng việc nhánh đó đã bị worktree khác giữ: `git switch --detach origin/master`.
- Merge luôn bằng **"Create a merge commit"** (không squash). **Chờ Jenkins xanh rồi mới merge PR kế tiếp.**
- Migration Flyway: thêm file mới, **không sửa file đã merge**, phải chạy thử trên **DB trống** và kiểm tra dữ liệu cũ nếu thêm khoá ngoại.
- **Cách giao mã cho người dùng:** viết thành **script `apply_*.ps1`** (UTF-8 có BOM, các file nhúng toàn ASCII hoặc UTF-8 đúng) rồi người dùng chạy. Tiếng Việt dán thẳng vào terminal PowerShell 5.1 dễ hỏng mã hoá, nên mã Java có tiếng Việt phải đi qua script. Script phải: kiểm tra cây sạch, không ghi đè file đã có, kiểm tra neo (`Contains`) **trước khi** ghi bất kỳ file nào, giữ kiểu xuống dòng của file gốc (`CRLF` hay `LF`).
- Mỗi lần đóng gói script: mô phỏng bản vá trên bản clone mã thật bằng Python, kiểm tra nội dung nhúng khớp file gốc, kiểm tra UTF-8 sạch. Lỗi đã gặp: một file nhúng chứa dòng bắt đầu bằng `'@` làm hỏng chuỗi bao ngoài (đã viết lại bằng mảng chuỗi nối).
- **Trước khi build Docker:** Docker build lấy mã **từ thư mục đang đứng**, không lấy từ GitHub. Luôn `git status`, `git switch --detach origin/master` rồi mới `up -d --build`. Đã từng làm bản Docker chạy mã cũ vì quên bước này.
- Không đưa mật khẩu hay khoá riêng vào cuộc trò chuyện.
- Bẫy PowerShell 5.1: hiển thị tiếng Việt từ API sai là lỗi hiển thị (giải mã Latin-1), không phải lỗi dữ liệu; lệnh trong tin nhắn phụ thuộc biến của phiên cũ (`$b`, `$h`) sẽ lỗi khi người dùng mở cửa sổ mới, nên lệnh phải tự đủ.

## 3. Môi trường (đường dẫn, cổng, tài khoản)

| Mục | Giá trị |
|---|---|
| Kho GitHub | `sleepingbuild/SpringBoot-GYM-MANAGEMENT-SYSTEM` (công khai) |
| JDK, Maven trên máy người dùng | `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`, `C:\tools\apache-maven-3.9.9` |
| Công cụ của Agent 1 trên máy người dùng | `D:\AgentChange\Agent1\` (các `apply_*.ps1`, `check_prs.ps1`) |
| Jenkins | job `GMS-CICD`, webhook GitHub qua ngrok, triển khai thành Windows service `GymManagementSystem`, cổng **8081**, DB `gms_db_ci` |
| SQL Server và Redis cho dev/Jenkins | `docker-compose.yml` cũ: container `gms-sqlserver` (cổng 1433), `gms-redis` (6379). **Jenkins phụ thuộc container này** |
| Bản Docker demo | `docker-compose.demo.yml`, project `gms-demo`: web **3000**, API **8082**, SQL Server **11433**, DB `gms_demo` |
| Giám sát | ghép thêm `-f docker-compose.monitoring.yml`: Grafana **3001** (xem không cần đăng nhập; quản trị `admin` / `admin12345`), Prometheus **9090** |
| SonarQube | `docker-compose.sonar.yml`, cổng **9000**, dự án `gym-management-system` |
| Tài khoản mẫu (mật khẩu chung `Gym@12345`) | `admin@gym.test`, `reception@gym.test`, `sales@gym.test`, `trainer1@gym.test`, `trainer2@gym.test`, `member.active@gym.test`, `member.nopackage@gym.test`, `member.expired@gym.test` |

Lệnh hay dùng (kèm hai file compose khi cần giám sát):
```powershell
git fetch origin --prune; git switch --detach origin/master
docker compose -f docker-compose.demo.yml up -d --build
docker compose -f docker-compose.demo.yml down -v      # xoá dữ liệu demo, làm lại từ đầu
.\scripts\demo-smoke.ps1                               # 56 kiểm tra quyền theo vai trò
& D:\AgentChange\Agent1\check_prs.ps1 -Simulate -Verify [-Migrate] -Order <nhánh1>,<nhánh2>
```

## 4. Việc đã làm trong phiên 2 (tóm tắt)

| Hạng mục | Kết quả |
|---|---|
| Gộp lô PR lớn lúc đầu phiên | Hoàn tất. Xử lý các sự cố chéo giữa PR (bảng ở mục 7) |
| PR danh mục `/users`, `/trainers`, `/branches` + 401/403 dạng JSON, seed người dùng | Đã merge |
| Bản sửa 400 cho thiếu tham số bắt buộc (`GlobalExceptionHandler`) | Đã merge (#102) |
| Bộ Docker (Dockerfile backend/frontend, `docker-compose.demo.yml`, `scripts/demo-smoke.ps1`) | Đã merge (#101). Người dùng đã chạy được cả hệ thống |
| Giám sát Prometheus + Grafana | Đã merge (#108). Người dùng xác nhận ba trang hiển thị đúng |
| SonarQube + JaCoCo + `sonar-analyze.ps1` (+ bản sửa `sonar.scm.disabled` cho git worktree) | Đã merge (#110, #111). Đã quét hai lần (mục 5.3) |
| `check_prs.ps1` (mô phỏng gộp nhánh, `mvn verify`, kiểm tra frontend, chạy thử migration trên DB trống) | Đang dùng; **chỉ nằm ở `D:\AgentChange\Agent1\`, chưa có trong kho** |
| Kiểm tra các PR của agent khác | Nhiều đợt: các PR đến #104 đã merge. Hoàn tiền POS (PR #109) **đã kiểm chứng trên Docker**: giao dịch `SUCCESS` chuyển `REFUNDED` |
| Tài liệu phân công | `PHAN_CONG_BACKEND.md` (chia 5 thành viên, **đã gửi nhóm**), `PHAN_CONG_CICD_DOCKER.md` (danh sách file CI/CD/Docker và 5 gói việc), `TONG_KET_DU_AN_GMS.md` (cho agent viết báo cáo, slide) |
| Chuẩn bị triển khai đám mây | Đã soạn gói `apply_cloud_deploy.ps1` (**chưa áp dụng**, xem mục 6). Người dùng yêu cầu **tạm dừng** |

Quyết định đã chốt: **story T15 (PT tạo buổi cho hội viên) không làm**. PT chỉ tạo ca làm việc, xác nhận và hoàn thành buổi; hội viên và lễ tân đặt lịch.

## 5. Trạng thái hiện tại và việc đang dở

### 5.1 Kho (`master`)

- Đầu `master` lúc kết thúc phiên: commit `01b0bea` ("Update AppShell.tsx"), trước đó `7b149d6` ("Update layout.tsx"), trước đó `f5a692c` (PR #104). **Hai commit trực tiếp này (không qua PR) mình chưa xem.**
- Jenkins: người dùng báo xanh sau khi merge hết PR đến #104.
- **Có 6 nhánh mới chưa merge, mình CHƯA xem:** `feature/agent3-admin-schedule`, `feature/agent3-bookings-branch-filter`, `feature/agent3-schedule-ownership`, `feature/agent4-seed-attendance`, `feature/agent5-error-codes`, `shared/agent5-error-codes`. Hai nhánh `error-codes` có thể đụng file SHARED (`ErrorCode`), cần xem kỹ nhất.

### 5.2 Việc ưu tiên cho phiên sau (đề xuất thứ tự)

1. **Kiểm tra 6 nhánh mới và 2 commit trực tiếp** (mục 5.1): `git diff --stat`, xung đột, luật hợp đồng (mục 7), file SHARED, migration. Chạy `check_prs.ps1 -Simulate -Verify -Order ...`. Frontend có thể kiểm tra ngay trong sandbox bằng `npm ci`, lint, typecheck, test, build.
2. **SonarQube** (mục 5.3): lấy danh sách 3 bug (hạng E) và 4 vấn đề mới, xử lý hotspot, viết test cho `LeadAssignmentController` và `SalesUserRepository`, quét lại.
3. **Cập nhật `TONG_KET_DU_AN_GMS.md`**: file vẫn ghi kết quả lần quét đầu ("Quality Gate Passed"); cần sửa theo lần quét thứ hai (Failed, mục 5.3) và các số liệu mới. `PHAN_CONG_BACKEND.md` đã gửi nhóm nên không sửa để khỏi lệch bản (mục tồn đọng số 1 hoàn tiền POS và số 3 T15 coi như đã đóng).
4. **Triển khai đám mây** (mục 6), khi người dùng muốn tiếp tục.
5. Đưa `check_prs.ps1` vào `scripts/` trong kho; thêm stage frontend và SonarQube vào `Jenkinsfile`, rollback tự động sau Deploy (nếu mẫu của khoa yêu cầu; **chưa biết**, đây là câu hỏi mở).
6. Báo cáo, slide: cần **ba file mẫu .docx của khoa** (không có trong phiên này) để viết hai hướng dẫn riêng cho môn Triển khai phần mềm và Bài tập lớn.
7. Kiểm chứng lỗi 404, 405, 415 có bị biến thành 500 không (người dùng chưa chạy lệnh thử). Bổ sung `GET /face-profiles/me` vào `API_DESIGN.md` (việc của Agent 4).

### 5.3 SonarQube (kết quả hai lần quét)

| | Lần 1 (dashboard "1 ngày trước") | Lần 2 ("4 phút trước") |
|---|---|---|
| Quality Gate | Passed (không có mốc mã mới) | **Failed**: 2 điều kiện rớt |
| Mã mới | không có | "Since November 8, 2026": **4 vấn đề mới** (yêu cầu 0), **độ phủ mã mới 63,9%** (yêu cầu 80%), trùng lặp 0,0% |
| Số dòng mã | 7,5 nghìn | 7,7 nghìn |
| Toàn bộ (Overall) | Security A (0), **Reliability E (3 vấn đề mức cao)**, Maintainability A (94: 16 cao, 64 trung bình, 14 thấp), Coverage **72,6%**, Duplications 0,3% | |
| Security Hotspots (lần 2) | | 3, 0% đã xem xét: Authentication (Cao), CSRF (Cao), Insecure Configuration (Thấp) |

- **Độ phủ JaCoCo toàn dự án 70%** (đo 08/11/2026): service 80%, controller 12%, security 9%. Chi tiết ở `PHAN_CONG_BACKEND.md` mục 11.
- **Hotspot Authentication:** `LeadServiceImpl` dòng 47, hằng `PASSWORD_ALPHABET`. **Báo nhầm** (bảng chữ cái sinh mật khẩu tạm bằng `SecureRandom`). Đánh dấu **Safe** trong giao diện SonarQube (làm tay).
- **Hai hotspot kia suy ra từ mã, chưa thấy nội dung:** CSRF nhiều khả năng là `csrf.disable()` trong `SecurityConfig` (API dùng JWT trong header, không cookie, nên Safe); Insecure Configuration nhiều khả năng là CORS cho mọi nguồn (`*`). Cần mở từng cái để xác nhận.
- **Nguyên nhân độ phủ mã mới thấp (suy luận, chưa đo từng file):** `LeadAssignmentController` (55 dòng) và `SalesUserRepository` (40 dòng SQL thuần) đều chưa có test.
- Khi tắt đọc Git (`sonar.scm.disabled=true`, bắt buộc vì `gms-docs` là worktree), SonarQube tính "mã mới" theo file thay đổi giữa hai lần quét, không theo từng dòng.
- Lệnh lấy danh sách vấn đề qua API: nằm trong `PHAN_CONG_CICD_DOCKER.md` mục 4 và `TONG_KET_DU_AN_GMS.md` mục 11.

### 5.4 Việc chưa có kết quả xác nhận

- Kiểm thử khói `demo-smoke.ps1`: lần ghi lại gần nhất **54/55**; sau đó đã sửa lỗi thiếu tham số (nay có 56 kiểm tra) nhưng người dùng chưa báo kết quả cuối. Chưa được viết là 56/56.
- Các lệnh kiểm tra giao lead (`GET /leads/sales-users`) chưa được người dùng báo kết quả sau khi dựng lại đúng `master`.

## 6. Triển khai đám mây (ĐÃ TẠM DỪNG)

**Yêu cầu của người dùng:** frontend lên **Vercel**, backend lên **Render**, đều gói miễn phí. Người dùng sau đó bảo dừng việc deploy.

**Phát hiện then chốt:** Render **không có SQL Server** (chỉ PostgreSQL), mà dự án dùng cú pháp riêng của SQL Server (10 trong 11 file migration; 3 repository dùng SQL thuần). Chuyển sang PostgreSQL là việc lớn, nhiều rủi ro nên **không chọn**. Đã đề xuất **Azure SQL Database (gói miễn phí)** làm cơ sở dữ liệu; Redis dùng **Render Key Value (gói Free)**. **Chưa biết người dùng có tài khoản Azure** (câu hỏi mở số 1).

**Đã tra cứu web (ngày 10/11/2026; chính sách có thể đổi, kiểm tra lại trước khi dựa vào):**
- Render Free: 512 MB RAM, CPU rất nhỏ, **ngủ sau 15 phút** không có yêu cầu, khởi động lại mất khoảng một phút trở lên; Key Value Free 25 MB, **mất dữ liệu khi khởi động lại**; PostgreSQL Free hết hạn sau 30 ngày; `PORT` mặc định 10000; Render dùng BuildKit nên `RUN --mount=type=cache` trong `Dockerfile` chạy được; Java không chạy trực tiếp nên dùng Docker.
- Azure SQL free offer: có hiệu lực chung, 100 000 vCore-giây mỗi tháng, 32 GB, có tuỳ chọn **"Auto-pause the database until next month"** (khuyên chọn để khỏi tính tiền). **Kết nối mở làm cơ sở dữ liệu không tự tạm dừng** nên không ping giữ backend thức liên tục (sẽ dùng hết hạn mức trong vài ngày).
- Vercel Hobby: miễn phí, chỉ dùng cá nhân, phi thương mại.

**Gói đã soạn, CHƯA áp dụng và CHƯA biên dịch: `apply_cloud_deploy.ps1`** (trong thư mục outputs; neo đã kiểm tra khớp trên `master` tại commit `f5a692c`, cần kiểm tra lại vì `master` đã đổi):
- Sửa `SecurityConfig.java`: CORS đọc từ cấu hình `gms.cors.allowed-origin-patterns` (biến môi trường `GMS_CORS_ALLOWED_ORIGIN_PATTERNS`, mặc định `*` nên bản Docker và Jenkins không đổi hành vi). Dùng **trường `@Value`** (không đổi chữ ký hàm `corsConfigurationSource()` vì nó được gọi trực tiếp ở chỗ khác). Việc này cũng xử lý hotspot "Insecure Configuration".
- Sửa `frontend/next.config.mjs`: `output: process.env.VERCEL ? undefined : "standalone"` (Docker vẫn dùng standalone).
- File mới: `docs/DEPLOY_CLOUD.md` (từng bước Azure SQL, Render, Vercel, bảng biến môi trường, kiểm tra RAM 512 MB bằng `docker run -m 512m`, xử lý sự cố), `scripts/wake-backend.ps1` (đánh thức backend trước buổi demo), `scripts/seed-remote.ps1` (nạp dữ liệu mẫu vào Azure SQL qua Docker), và test `SecurityConfigCorsTest.java` (3 test).
- Bảng biến môi trường cho Render (rút gọn): `SERVER_PORT=10000`, `DB_URL` (JDBC Azure SQL có `encrypt=true`, `loginTimeout=60`, `connectRetryCount=3`), `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT=6379`, `GMS_JWT_SECRET` (từ 32 ký tự), `GMS_CORS_ALLOWED_ORIGIN_PATTERNS`, `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1`, `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5`, `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=1`. Trên Vercel: Root Directory `frontend`, biến `NEXT_PUBLIC_API_BASE_URL=https://<dịch-vụ>.onrender.com` (nhúng lúc build, đổi thì phải Redeploy).

**Chưa kiểm chứng (phải nói rõ khi tiếp tục):** (1) bộ migration V1 đến V9 chạy trên Azure SQL (đã chạy trên SQL Server 2022 Docker); (2) ứng dụng chạy nổi trong 512 MB và thời gian khởi động nguội trên Render; (3) mã Java và test CORS mới chưa được biên dịch; (4) việc Render dựng image thành công bằng Maven trong giới hạn gói miễn phí.

**Cảnh báo cho người dùng (đã nêu):** bản demo công khai kèm tài khoản mẫu có mật khẩu chung đã biết; Render tự triển khai mỗi commit lên `master` bất kể Jenkins đỏ hay xanh; ảnh đại diện tải lên mất khi khởi động lại; token đã đăng xuất dùng lại được tối đa 15 phút sau khi Redis khởi động lại.

## 7. Bài học và luật hợp đồng (rất quan trọng khi duyệt PR)

**Luật tự động kiểm tra (làm `mvn verify` đỏ nếu vi phạm):**
1. Mọi controller có `@Tag` và **tên tag phải nằm trong `OpenApiConfig.TAG_ORDER`**; mọi endpoint có `@Operation(summary = ...)`.
2. Không endpoint nào trả `ApiResponse<?>` (phải kiểu cụ thể) — `ApiResponseContractTest`.
3. "Bây giờ" lấy từ bean `Clock`, không dùng `LocalDateTime.now()` trần.
4. Đổi API thì cập nhật `API_DESIGN.md`; thêm `ErrorCode` phải qua Agent 1.

**Sự cố đã xảy ra thật (không lặp lại):**

| Sự cố | Nguyên nhân gốc | Cách xử lý |
|---|---|---|
| Dịch vụ Jenkins chết sau khi merge V7 | `gms_db_ci` có 3 giao dịch POS mồ côi, khoá ngoại mới thất bại | Backup, xoá 3 dòng có chốt chặn số dòng, khởi động lại |
| Build đỏ sau khi gộp hai PR đều xanh (Swagger) | Test yêu cầu tag mới nằm trong `TAG_ORDER` | Thêm 3 tag |
| Build đỏ sau khi gộp hai PR (`ApiResponse<?>`) | Test mới cấm kiểu wildcard | Đổi sang kiểu cụ thể |
| Seed Docker lỗi `QUOTED_IDENTIFIER` | `sqlcmd` Linux tắt mặc định, `users` có chỉ mục lọc | Cờ `-I` và `SET QUOTED_IDENTIFIER ON` |
| Seed lỗi trùng `uq_users_phone` | Hai seed dùng chung dải số điện thoại | `00_users` dùng `0911000xxx`, seed 60 dùng `09000000xx` |
| Thiếu tham số bắt buộc trả 500 | Handler cuối `Exception.class` nuốt lỗi của Spring | Handler trả 400 `VALIDATION_ERROR` |
| Bản Docker chạy mã cũ | Build lấy mã từ thư mục đang đứng | `git switch --detach origin/master` trước khi build |
| Sonar không đọc được kho Git | `gms-docs` là git worktree | `-Dsonar.scm.disabled=true` |
| Cổng 14330 bị chiếm khi dựng Docker | Tiến trình khác | Đổi sang 11433 |

Điều còn nghi ngờ: handler cuối `Exception.class` có thể cũng biến 404, 405, 415 thành 500 (**chưa kiểm chứng**).

## 8. File Agent 1 đã tạo (thư mục outputs) và trạng thái

| File | Là gì | Trạng thái |
|---|---|---|
| `check_prs.ps1` | Kiểm tra PR: tầng 1 kiểm tra tĩnh, tầng 2 mô phỏng gộp và build, tầng 3 chạy ứng dụng trên DB trống | Đang dùng, **chưa vào kho** |
| `apply_docker.ps1`, `apply_missing_param_fix.ps1`, `apply_monitoring.ps1`, `apply_sonar.ps1`, `fix_sonar_scm.ps1` | Script cài đặt một lần | **Đã áp dụng**, nội dung đã nằm trong kho (PR #101, #102, #108, #110, #111) |
| `apply_cloud_deploy.ps1` | Gói triển khai đám mây (mục 6) | **Chưa áp dụng** |
| `PHAN_CONG_BACKEND.md` | Chia backend cho 5 thành viên, luồng từng phần, bảng bổ sung test (mục 11) | Đã gửi nhóm |
| `PHAN_CONG_CICD_DOCKER.md` | Danh sách file CI, CD, Docker và 5 gói việc | Đã tạo, dùng để phân công |
| `TONG_KET_DU_AN_GMS.md` | Tổng kết cho agent viết báo cáo, slide (nhãn [ĐÃ XONG] / [CHƯA XÁC NHẬN] / [CHƯA LÀM]) | **Cần cập nhật** theo lần quét SonarQube thứ hai |

Trong kho do Agent 1 thêm: `docs/DOCKER_DEMO.md`, `docs/MONITORING.md`, `docs/SONARQUBE.md`, `docker-compose.demo.yml`, `docker-compose.monitoring.yml`, `docker-compose.sonar.yml`, `monitoring/**`, `scripts/demo-smoke.ps1`, `scripts/sonar-analyze.ps1`, `lombok.config`, các `Dockerfile`.

## 9. Câu hỏi mở cần hỏi người dùng

1. Có tài khoản **Azure** (email sinh viên) chưa, và có muốn tiếp tục triển khai đám mây không?
2. Mẫu của môn Triển khai phần mềm **có yêu cầu** SonarQube hoặc Docker nằm trong pipeline Jenkins, rollback, IIS, SSH không? (quyết định có làm thêm `Jenkinsfile` hay không)
3. Có thể gửi lại **ba file mẫu .docx** của khoa để viết hai hướng dẫn báo cáo?
4. Kết quả `demo-smoke.ps1` mới nhất, và ảnh SonarQube sau khi xử lý lỗi.

## 10. Ghi chú về bộ nhớ

Đã ghi vào bộ nhớ: trạng thái Docker demo, giám sát và Jenkins xanh; quyết định T15 không làm; việc đã gửi bản phân công backend cho 5 thành viên; và ý định chọn Vercel, Render nhưng đã tạm dừng việc deploy.
