# Danh sách file và phân công: CI, CD, Docker

Tài liệu liệt kê **mọi file liên quan đến CI, CD, Docker** trong kho `master` (commit `f5a692c`), kèm mục đích và trạng thái từng file, rồi đề xuất cách chia thành các gói việc để phân công cho nhóm. Số dòng đo trực tiếp từ kho.

**Quy ước trạng thái:** **[XONG]** đã chạy thật và nhóm xác nhận; **[CẦN LÀM]** còn việc dở; **[CHƯA CÓ]** chưa tồn tại. Cột "Mức sửa": **SHARED** nghĩa là file dùng chung, ai sửa cũng phải qua Agent 1 duyệt (xem mục 5).

---

## 1. Danh sách file theo nhóm

### 1.1 CI/CD (Jenkins)

| File | Dòng | Làm gì | Trạng thái | Mức sửa |
|---|---:|---|---|---|
| `Jenkinsfile` | 131 | Pipeline Jenkins `GMS-CICD`: trigger `githubPush`, các stage Checkout, Restore, Build, Test (đăng kết quả JUnit), Publish (đóng gói jar), Deploy (dừng dịch vụ Windows, chép jar, khởi động lại), Verify (gọi `/v3/api-docs`). Chỉ build backend | **[XONG]** cho backend; **[CẦN LÀM]** thêm frontend, SonarQube | SHARED |
| `.github/workflows/*` | 0 | GitHub Actions | **[CHƯA CÓ]**, dự án dùng Jenkins thay thế | |

Thiết lập **ngoài kho** (nằm trên máy Jenkins, không có trong Git): job `GMS-CICD`, webhook GitHub qua ngrok, thư mục `C:\deploy\gms`, dịch vụ Windows `GymManagementSystem` (cổng 8081), cơ sở dữ liệu `gms_db_ci`. Người phụ trách CD cần ghi lại các cấu hình này vào tài liệu vì không có file nào lưu chúng.

### 1.2 Docker

| File | Dòng | Làm gì | Trạng thái | Mức sửa |
|---|---:|---|---|---|
| `Dockerfile` | 24 | Image backend, 2 giai đoạn (Maven + JDK 17 → JRE 17), chạy bằng người dùng không root, có `curl` để kiểm tra sức khoẻ, múi giờ Việt Nam | **[XONG]** | SHARED |
| `.dockerignore` | 13 | Loại `target`, `frontend`, `docs`, `scripts`, cấu hình bí mật cục bộ khỏi bối cảnh build backend | **[XONG]** | SHARED |
| `frontend/Dockerfile` | 29 | Image frontend Next.js (standalone), 3 giai đoạn, nhận `NEXT_PUBLIC_API_BASE_URL` lúc build | **[XONG]** | SHARED |
| `frontend/.dockerignore` | 6 | Loại `node_modules`, `.next`, `.env*.local` | **[XONG]** | SHARED |
| `docker-compose.demo.yml` | 118 | Chạy cả hệ thống: SQL Server, tạo DB, Redis, backend, nạp dữ liệu mẫu, frontend; có kiểm tra sức khoẻ; cổng web 3000, API 8082, SQL 11433 | **[XONG]** | SHARED |
| `docker-compose.monitoring.yml` | 46 | Ghép thêm Prometheus (9090) và Grafana (3001), bật số liệu cho backend | **[XONG]** | SHARED |
| `docker-compose.sonar.yml` | 48 | SonarQube 10.6 Community + PostgreSQL (cổng 9000), chạy riêng | **[XONG]** | SHARED |
| `docker-compose.yml` | 42 | Bản cũ cho phát triển: chỉ SQL Server + Redis (container `gms-sqlserver`, `gms-redis`, cổng 1433, 6379). **Jenkins phụ thuộc vào container này** vì cơ sở dữ liệu `gms_db_ci` nằm trong đó | **[XONG]** | SHARED |
| `frontend/next.config.mjs` | 7 | Dòng `output: "standalone"` bắt buộc cho `frontend/Dockerfile` | **[XONG]** | SHARED |

Chưa có: đẩy image lên registry (Docker Hub hoặc kho riêng) và gắn tag phiên bản **[CHƯA CÓ]**.

### 1.3 Giám sát (Prometheus, Grafana)

| File | Dòng | Làm gì | Trạng thái |
|---|---:|---|---|
| `monitoring/prometheus/prometheus.yml` | 16 | Thu số liệu backend mỗi 10 giây | **[XONG]** |
| `monitoring/grafana/provisioning/datasources/prometheus.yml` | 9 | Khai báo nguồn dữ liệu Prometheus cho Grafana | **[XONG]** |
| `monitoring/grafana/provisioning/dashboards/dashboards.yml` | 9 | Nạp dashboard tự động | **[XONG]** |
| `monitoring/grafana/dashboards/gms-overview.json` | 705 | Dashboard "GMS Tổng quan backend", 12 ô | **[XONG]** |
| `docs/MONITORING.md` | 47 | Hướng dẫn bật, dùng, xử lý sự cố | **[XONG]** |

Chưa có: quy tắc cảnh báo (alert rules) **[CHƯA CÓ]**.

### 1.4 Chất lượng mã (SonarQube, JaCoCo)

| File | Dòng | Làm gì | Trạng thái | Mức sửa |
|---|---:|---|---|---|
| `scripts/sonar-analyze.ps1` | 44 | Chạy test, đo độ phủ, gửi kết quả lên SonarQube | **[XONG]** | |
| `docs/SONARQUBE.md` | 46 | Hướng dẫn chạy SonarQube, tạo token, đọc kết quả | **[XONG]** | |
| `lombok.config` | 4 | Đánh dấu mã Lombok sinh ra là `@Generated` để không tính vào độ phủ | **[XONG]** | SHARED |
| `pom.xml` (một phần) | | Plugin `jacoco-maven-plugin`, các thuộc tính `sonar.*`, loại `dto`/`entity`/`config` khỏi độ phủ | **[XONG]** | SHARED |

### 1.5 Dữ liệu mẫu và kiểm thử khói (dùng cho Docker và demo)

| File | Dòng | Làm gì | Trạng thái |
|---|---:|---|---|
| `scripts/dev-seed/00_users.sql` | | Tài khoản mẫu cho 5 vai trò (mật khẩu chung `Gym@12345`) | **[XONG]** |
| `scripts/dev-seed/10_packages_memberships.sql` | | Gói tập và gói hội viên mẫu | **[XONG]** |
| `scripts/dev-seed/20_bookings.sql` | | Lịch PT và booking mẫu | **[XONG]** |
| `scripts/dev-seed/50_pos.sql` | | Sản phẩm và đơn POS mẫu | **[XONG]** |
| `scripts/dev-seed/60_leads_reports.sql`, `61_expiring_members.sql` | | Khách tiềm năng, dữ liệu báo cáo, hội viên sắp hết hạn | **[XONG]** |
| `scripts/dev-seed/run-all.ps1`, `run-all.sh`, `README.md` | | Chạy toàn bộ seed theo thứ tự tên | **[XONG]** |
| `scripts/demo-smoke.ps1` | 168 | Kiểm thử khói theo vai trò: đăng nhập 6 tài khoản, 56 kiểm tra 200, 401, 403 | **[XONG]**; kết quả 56/56 cuối cùng **[CHƯA XÁC NHẬN]** |
| `.gitattributes` | | Ép file `.sh` giữ kết thúc dòng LF để chạy được trong container | **[XONG]** |

### 1.6 Cấu hình CI/CD nằm trong file khác

| File | Phần liên quan | Mức sửa |
|---|---|---|
| `src/main/java/com/gym/management/config/SecurityConfig.java` | Cho phép `/actuator/health` và `/actuator/prometheus` không cần đăng nhập; tắt CSRF (API không dùng cookie); CORS đang cho mọi nguồn (`*`) | SHARED |
| `src/main/resources/application.yml` | Cấu hình actuator, cấp độ log, biến môi trường `DB_URL`, `GMS_JWT_SECRET`, `REDIS_HOST` | SHARED |

### 1.7 Tài liệu

`README.md` (84 dòng), `docs/DOCKER_DEMO.md` (54), `docs/MONITORING.md` (47), `docs/SONARQUBE.md` (46), `docs/TEST_ACCOUNTS.md` (34).

### 1.8 Công cụ nằm ngoài kho (trên máy Agent 1, `D:\AgentChange\Agent1\`)

| File | Dùng để | Đề xuất |
|---|---|---|
| `check_prs.ps1` | Kiểm tra PR trước khi merge: mô phỏng gộp nhánh, `mvn verify`, kiểm tra frontend, chạy thử migration trên DB trống | **Nên đưa vào `scripts/` trong kho** để mọi thành viên dùng chung. Hiện Agent 3 đã phải xin file này |
| `apply_docker.ps1`, `apply_monitoring.ps1`, `apply_sonar.ps1`, `fix_sonar_scm.ps1`, `apply_missing_param_fix.ps1` | Script cài đặt một lần, nội dung đã nằm trong kho | Không cần đưa vào kho |

---

## 2. Việc còn dở cần phân công (từ kết quả thực tế)

### 2.1 SonarQube lần quét thứ hai: Quality Gate **Failed**

Lần quét thứ hai (dashboard, tab New Code, "Since November 8, 2026") cho kết quả:

| Điều kiện | Thực tế | Yêu cầu |
|---|---|---|
| Vấn đề mới (New issues) | **4** | 0 |
| Độ phủ trên mã mới (Coverage) | **63,9%** | từ 80% |
| Trùng lặp | 0,0% | đạt |

Lần quét đầu "Passed" vì chưa có mốc so sánh mã mới; lần này đã có mốc nên cổng áp dụng thật. Mã mới giữa hai lần quét (so sánh `master` với thời điểm quét đầu) gồm: `LeadAssignmentController` (55 dòng, **chưa có test**), `SalesUserRepository` (40 dòng SQL thuần, **chưa có test**), `LeadAssignmentServiceImpl` (đã có test 141 dòng), `PosOrderServiceImpl` (có test), `SecurityConfig` (đã loại khỏi độ phủ). Nhiều khả năng hai file chưa test là nguyên nhân kéo độ phủ xuống 63,9%. Đây là suy luận từ danh sách file thay đổi, chưa phải số đo trên từng file. **Cần lấy danh sách 4 vấn đề mới** bằng lệnh ở mục 4.

### 2.2 Ba cảnh báo Security Hotspots (0,0% đã xem xét)

| Cảnh báo | Mức | Vị trí | Đánh giá của Agent 1 |
|---|---|---|---|
| Authentication: `'PASSWORD' detected ... hard-coded password` | Cao | `LeadServiceImpl.java` dòng 47, hằng `PASSWORD_ALPHABET` | **Báo nhầm.** Đó là bảng chữ cái để sinh mật khẩu tạm bằng `SecureRandom`, không phải mật khẩu. Đánh dấu **Safe** kèm lý do |
| Cross-Site Request Forgery (CSRF) | Cao | Suy ra là `SecurityConfig.java` dòng 60 (`csrf.disable()`) | **Chấp nhận được**: API không dùng cookie phiên mà dùng JWT trong header, nên CSRF không áp dụng. Đánh dấu **Safe** kèm lý do. *(Suy ra từ mã, cần mở từng cảnh báo để xác nhận)* |
| Insecure Configuration | Thấp | Suy ra là `SecurityConfig.java` dòng 87 (CORS cho mọi nguồn `*`, kèm chú thích TODO) | **Việc thật nên làm** trước khi triển khai thật: giới hạn nguồn được phép theo cấu hình. *(Suy ra từ mã)* |

Trạng thái xem xét trên SonarQube phải làm bằng tay trong giao diện (đăng nhập, mở từng hotspot, chọn "Safe" hoặc "Fixed", ghi lý do).

### 2.3 Việc triển khai lên server (CD thật)

**[CHƯA CÓ]**, đang chờ quyết định: server là máy nào, hệ điều hành gì, truy cập bằng cách nào. Chi tiết cần cung cấp ở mục 6.

---

## 3. Đề xuất chia gói việc

Chia thành 5 gói độc lập. Nếu nhóm 3 người thì gộp như bảng ở cuối mục. **Mức công:** S = dưới 1 giờ, M = nửa ngày, L = một ngày trở lên.

### Gói A: CI (Jenkins)
**Mục tiêu:** pipeline kiểm tra đủ cả backend và frontend, ghi lại bằng chứng.
- Thêm stage frontend (`npm ci`, `lint`, `typecheck`, `test`, `build`) vào `Jenkinsfile`. **(M)** Hiện Jenkins không build frontend nên frontend không được CI kiểm tra.
- Thêm stage SonarQube (cần token trong Jenkins và SonarQube chạy sẵn). **(M)**
- Đưa `check_prs.ps1` vào `scripts/`, viết hướng dẫn dùng. **(S)**
- Chụp bằng chứng: Stage View xanh, nhật ký từng stage, cấu hình webhook.
- **File:** `Jenkinsfile`, `scripts/check_prs.ps1` (mới). **Bàn giao:** PR đã merge, Jenkins chạy xanh với stage mới.

### Gói B: CD (triển khai)
**Mục tiêu:** triển khai lên server thật, có kiểm tra và khả năng quay lui.
- Triển khai lên server (cần thông tin ở mục 6). **(L)**
- Thêm **rollback tự động** khi Verify thất bại (giữ bản jar cũ, khôi phục khi lỗi). Hiện lỗi sau Deploy làm dịch vụ chết cho tới khi xử lý tay. **(M)**
- Ghi lại toàn bộ cấu hình nằm ngoài kho (mục 1.1) vào tài liệu. **(S)**
- Tuỳ yêu cầu của mẫu: triển khai qua SSH hoặc IIS.
- **File:** `Jenkinsfile` (stage Deploy, Verify), script triển khai mới, tài liệu. **Bàn giao:** ứng dụng chạy trên server, bằng chứng, hướng dẫn quay lui.

### Gói C: Docker
**Mục tiêu:** đóng gói chắc chắn, chạy được trên máy sạch, sẵn sàng đưa lên server.
- Thử `docker compose up` trên **một máy sạch** (chưa từng chạy dự án) và ghi lại lỗi gặp phải. **(M)**
- Chuyển mật khẩu và khoá bí mật sang biến môi trường (`.env`), bỏ giá trị mặc định cho môi trường thật. **(M)**
- Gắn tag phiên bản cho image, (tuỳ chọn) đẩy lên Docker Hub. **(M)**
- Gắn bước build image vào Jenkins (phối hợp Gói A). **(M)**
- **File:** `Dockerfile`, `frontend/Dockerfile`, `docker-compose.demo.yml`, `docs/DOCKER_DEMO.md`. **Bàn giao:** hướng dẫn chạy trên máy sạch đã thử, ảnh chụp.

### Gói D: Giám sát
**Mục tiêu:** dashboard có giá trị thực tế và có cảnh báo.
- Thêm quy tắc cảnh báo cho Prometheus (backend ngừng chạy, lỗi 5xx tăng, độ trễ cao). **(M)**
- Thêm ô dashboard nghiệp vụ (số đơn POS, số booking theo trạng thái) nếu cần. **(M)**
- Chụp ảnh dashboard khi có lưu lượng thật.
- **File:** `monitoring/**`, `docker-compose.monitoring.yml`, `docs/MONITORING.md`.

### Gói E: Chất lượng mã và kiểm thử
**Mục tiêu:** Quality Gate đạt, không còn lỗi độ tin cậy mức cao.
- Lấy danh sách 4 vấn đề mới và 3 lỗi độ tin cậy (hạng E) và xử lý (mục 4). **(M)**
- Viết test cho `LeadAssignmentController` và `SalesUserRepository` để đưa độ phủ mã mới lên từ 80%. **(M)**
- Xem xét 3 Security Hotspots: đánh dấu Safe có lý do hai cái đầu, sửa CORS cái thứ ba (giới hạn nguồn theo biến cấu hình, kiểm tra lại trên Docker với frontend cổng 3000). **(M)**
- Chạy lại `demo-smoke.ps1`, ghi kết quả 56/56.
- Bổ sung test cho các lớp yếu theo `PHAN_CONG_BACKEND.md` mục 11.
- **File:** `src/test/**`, `SecurityConfig.java` (SHARED, qua Agent 1), `scripts/demo-smoke.ps1`.

### Nếu nhóm chỉ có 3 người

| Người | Gói | Trọng tâm |
|---|---|---|
| Người 1 | A + B | Jenkins và triển khai |
| Người 2 | C + D | Docker và giám sát |
| Người 3 | E | Chất lượng mã và kiểm thử |

---

## 4. Lệnh lấy danh sách vấn đề trên SonarQube

Dùng cho Gói E. Dùng `$u`, `$k`, `$auth` như đã có trong `TONG_KET_DU_AN_GMS.md` mục 11:

```powershell
# 4 vấn đề mới (làm Quality Gate rớt)
$n = Invoke-RestMethod "$u/issues/search?componentKeys=$k&resolved=false&inNewCodePeriod=true&ps=20" -Headers $auth
$n.issues | Select-Object rule, severity, @{n='file';e={$_.component -replace '.*:',''}}, line, message | Format-List

# Lỗi độ tin cậy (làm hạng Reliability bị E)
$b = Invoke-RestMethod "$u/issues/search?componentKeys=$k&resolved=false&types=BUG&ps=20" -Headers $auth
$b.issues | Select-Object rule, severity, @{n='file';e={$_.component -replace '.*:',''}}, line, message | Format-List

# Quy tắc vi phạm nhiều nhất (để biết sửa ít quy tắc là xử lý được phần lớn)
$f = Invoke-RestMethod "$u/issues/search?componentKeys=$k&resolved=false&facets=rules&ps=1" -Headers $auth
$f.facets[0].values | Select-Object -First 10 | Format-Table val, count -AutoSize
```

## 5. Quy tắc khi sửa file SHARED

Các file đánh dấu **SHARED** ở mục 1 (`Jenkinsfile`, mọi `Dockerfile` và `docker-compose*.yml`, `pom.xml`, `SecurityConfig.java`, `application.yml`, `lombok.config`, `frontend/next.config.mjs`) ảnh hưởng tới mọi người. Quy tắc:
1. Mọi sửa đổi đi qua PR và được Agent 1 duyệt trước khi merge.
2. Trước khi mở PR chạy `check_prs.ps1 -Simulate -Verify`; PR đụng `Jenkinsfile` thì ghi rõ cách thử.
3. Sau khi merge, **chờ Jenkins xanh** rồi mới merge PR kế tiếp. Không sửa `Jenkinsfile` song song ở hai nhánh.
4. Docker build lấy mã **từ thư mục đang đứng**: trước khi build phải `git switch --detach origin/master` (đã từng làm bản Docker chạy mã cũ vì quên bước này).

## 6. Thông tin cần có để triển khai lên server (Gói B)

Trả lời được các câu sau thì mới chọn được cách triển khai:
1. Server là máy nào: **máy ảo/VPS Linux**, máy Windows khác trong mạng, hay dịch vụ đám mây (AWS, Azure, Google Cloud)? Hay chưa có server?
2. Hệ điều hành và phiên bản; địa chỉ IP hoặc tên miền; có cài sẵn Docker không.
3. Cách truy cập: SSH (tên người dùng, khoá hay mật khẩu) hoặc Remote Desktop. **Không gửi mật khẩu hay khoá riêng vào cuộc trò chuyện**; chỉ cho biết loại truy cập, phần bí mật nhập thẳng trên máy.
4. Cổng nào được mở ra ngoài (ít nhất cổng web và cổng API).
5. Yêu cầu của mẫu: bắt buộc qua SSH, IIS, hay Docker đều được?
