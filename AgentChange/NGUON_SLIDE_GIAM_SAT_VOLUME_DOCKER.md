# Nguồn tạo slide: Công cụ giám sát, theo dõi và volume trên Docker (Nhóm 6-9)

Tài liệu này dành cho agent làm slide báo cáo. Mọi thông tin lấy từ mã nguồn thật của repo `sleepingbuild/SpringBoot-GYM-MANAGEMENT-SYSTEM` (nhánh `master`, sau PR #145). Chi tiết lệnh: `docs/DOCKER_TOOLS_VOLUMES.md`, `docs/MONITORING.md`, `docs/CICD_SSH_DEPLOY.md`.

**Yêu cầu của giảng viên (nhóm 6-9):** cài đặt và trình bày các công cụ hỗ trợ giám sát, theo dõi, tạo volume trên môi trường Docker.

**Quy ước cho agent làm slide:** chỉ dùng số liệu, tên, cổng ghi trong file này. Ảnh chụp màn hình phải do nhóm chụp từ máy chạy thật (danh sách ở mục 8), không tự vẽ giả giao diện.

---

## 1. Tóm tắt một câu

Hệ thống GMS chạy bằng Docker Compose; **Prometheus** thu thập số liệu từ backend (qua Spring Boot Actuator) và từ **cAdvisor** (đo từng container), **Grafana** vẽ dashboard và Prometheus có 5 luật cảnh báo; **Docker Desktop, lệnh `docker`, Portainer** dùng để theo dõi và quản lý container, **volume** giữ dữ liệu không mất khi container bị xóa hay cập nhật.

## 2. Sơ đồ tổng thể (dùng làm một slide)

```
Trình duyệt ──► Web (Next.js) :3000 ──► API (Spring Boot) :8082 ──► SQL Server :11433
                                              │   │                  (volume sqlserver-data)
                                              │   └──► Redis
                                              │   └──► volume uploads (tệp tải lên)
                       /actuator/prometheus   │
                                              ▼
cAdvisor :8083 ──(CPU, RAM, mạng từng container)──► Prometheus :9090 ──► Grafana :3001
                                                    (volume prometheus-data)  (volume grafana-data)

Docker Desktop / lệnh docker / Portainer :9443 ──► xem, theo dõi, quản lý container, image, volume
```

Các dịch vụ trong `docker-compose.demo.yml` (project `gms-demo`): `sqlserver`, `db-init`, `redis`, `backend`, `seed`, `frontend`. Dịch vụ giám sát thêm từ `docker-compose.monitoring.yml`: `prometheus`, `cadvisor`, `grafana`.

## 3. Các công cụ: vai trò và cách hoạt động

| Công cụ | Phân loại | Vai trò | Cách hoạt động (một ý cho slide) | Địa chỉ |
|---|---|---|---|---|
| **Docker Desktop** | Theo dõi / quản lý | Giao diện xem container, image, volume, log | Hiển thị trạng thái và log lấy trực tiếp từ Docker Engine | cửa sổ ứng dụng |
| **Lệnh `docker`** | Theo dõi | Theo dõi nhanh bằng dòng lệnh | `ps`, `stats`, `logs`, `top`, `inspect`, `events`, `system df` | PowerShell |
| **Spring Boot Actuator + Micrometer** | Nguồn số liệu | Backend tự xuất số liệu: yêu cầu HTTP, JVM, kết nối DB (Hikari) | Mở endpoint `/actuator/prometheus` ở định dạng Prometheus đọc được | `:8082/actuator/prometheus` |
| **Prometheus** v2.53.0 | Giám sát | Thu thập, lưu số liệu theo thời gian, đánh giá luật cảnh báo | Chủ động "kéo" (scrape) số liệu mỗi **10 giây** từ 3 job: `gms-backend`, `cadvisor`, `prometheus`; giữ **7 ngày** | `:9090` |
| **cAdvisor** v0.49.1 | Giám sát container | Đo CPU, RAM, mạng, ổ đĩa của **từng container** | Đọc thông tin cgroup của máy chủ Docker, xuất số liệu cho Prometheus ở job `cadvisor` | `:8083` |
| **Grafana** 11.1.0 | Hiển thị | Vẽ biểu đồ từ số liệu Prometheus | Datasource và 2 dashboard được nạp sẵn (provisioning) từ thư mục `monitoring/grafana` | `:3001` |
| **Portainer CE** (tùy chọn) | Quản lý | Giao diện web quản lý container, image, volume, network | Đọc socket Docker của máy (quyền cao, chỉ bật khi học và demo) | `https://localhost:9443` |

Tài khoản Grafana: `admin` / `admin12345` (mặc định, đổi bằng biến `GMS_GRAFANA_PASSWORD`). Ai mở Grafana cũng xem được dashboard (chế độ Viewer ẩn danh), muốn sửa phải đăng nhập admin.

### 3.1 Prometheus thu thập gì (nên có 1 slide)

| Job | Mục tiêu | Số liệu chính |
|---|---|---|
| `gms-backend` | `backend:8080/actuator/prometheus` | số yêu cầu HTTP theo mã trạng thái, độ trễ, bộ nhớ JVM, CPU, luồng, GC, kết nối DB Hikari |
| `cadvisor` | `cadvisor:8080` | CPU, RAM, mạng, ổ đĩa của từng container |
| `prometheus` | `localhost:9090` | số liệu của chính Prometheus |

### 3.2 Hai dashboard Grafana

- **GMS Tổng quan backend** (12 ô): Backend UP/DOWN, yêu cầu mỗi giây, lỗi 5xx mỗi giây, độ trễ p95, heap đang dùng, yêu cầu theo mã trạng thái, độ trễ p50 và p95, bộ nhớ JVM, CPU, kết nối cơ sở dữ liệu (Hikari), luồng JVM và GC, top 5 đường dẫn nhiều yêu cầu nhất.
- **GMS Docker (container và tài nguyên)** (10 ô): container đang chạy, tổng RAM các container, tổng CPU, trạng thái cAdvisor, CPU theo container, RAM theo container, mạng nhận vào, mạng gửi ra, dung lượng ghi của container, thời gian chạy của container.

Giải thích p50/p95 cho người nghe: 50% hoặc 95% yêu cầu được xử lý trong thời gian đó hoặc nhanh hơn.

### 3.3 Năm luật cảnh báo (`monitoring/prometheus/alerts.yml`)

| Cảnh báo | Điều kiện | Mức |
|---|---|---|
| `BackendDown` | không lấy được số liệu backend quá 30 giây | critical |
| `HighServerErrorRate` | lỗi 5xx trên 0,05 lần/giây trong 1 phút | warning |
| `HighLatencyP95` | 95% yêu cầu chậm hơn 1 giây trong 2 phút | warning |
| `HeapAlmostFull` | heap JVM trên 90% trong 5 phút | warning |
| `DatabasePoolExhausted` | trên 90% kết nối của pool đang dùng trong 2 phút | warning |

Giới hạn cần nói thật: **chưa nối Alertmanager**, nên cảnh báo chỉ hiện ở trang Alerts của Prometheus, không gửi mail hay tin nhắn. Trạng thái cảnh báo: Inactive, Pending (đang đếm thời gian `for`), Firing.

## 4. Volume

**Ý chính:** dữ liệu trong container mất khi container bị xóa; **volume** nằm ngoài vòng đời container nên xóa và tạo lại container hay cập nhật phiên bản, dữ liệu vẫn còn.

| Loại | Ví dụ trong dự án | Dùng cho |
|---|---|---|
| Named volume | `sqlserver-data:/var/opt/mssql` | dữ liệu cần giữ lâu dài, Docker quản lý |
| Bind mount | `./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro` | đưa tệp cấu hình từ thư mục dự án vào container (chỉ đọc) |
| tmpfs | (dự án không dùng) | dữ liệu tạm trong RAM |

Volume của dự án (Compose thêm tiền tố `gms-demo_`):

| Volume | Gắn vào | Chứa gì |
|---|---|---|
| `gms-demo_sqlserver-data` | SQL Server `/var/opt/mssql` | **toàn bộ cơ sở dữ liệu** (quan trọng nhất) |
| `gms-demo_uploads` | Backend `/app/uploads` | tệp người dùng tải lên |
| `gms-demo_prometheus-data` | Prometheus `/prometheus` | số liệu giám sát, giữ 7 ngày |
| `gms-demo_grafana-data` | Grafana `/var/lib/grafana` | cài đặt, tài khoản Grafana |
| `gms-portainer_portainer-data` | Portainer `/data` | tài khoản, cấu hình Portainer |
| `gms-sonar_*` (4 volume) | SonarQube và PostgreSQL | kết quả phân tích mã |

Lệnh tạo và quản lý: `docker volume create`, `ls`, `inspect`, `rm`, `prune`; xem dung lượng bằng `docker system df -v`.

**Thực hành chứng minh (làm sống khi trình bày, khoảng 1 phút):**
1. `docker volume create gms-demo-thuchanh`
2. Container tạm ghi tệp vào volume rồi tự xóa (`--rm`).
3. Container tạm **mới** gắn cùng volume và đọc lại: tệp vẫn còn.
4. `docker volume rm gms-demo-thuchanh`.

**Cảnh báo an toàn (nên có 1 slide hoặc 1 dòng):** `docker compose down` giữ volume; **`down -v` và `docker volume prune` xóa dữ liệu**. Sao lưu: tệp tải lên bằng container phụ + `tar`; cơ sở dữ liệu bằng lệnh `BACKUP DATABASE` của SQL Server rồi `docker cp` (an toàn hơn chép tệp khi đang chạy).

## 5. Liên hệ với CI/CD (nối với phần Jenkins của nhóm)

Khi Jenkins xanh trên `master`, stage **Docker web** (máy cục bộ) hoặc **Deploy SSH** (qua SSH, hỗ trợ Linux và Windows) dựng lại hệ thống **cùng cả dịch vụ giám sát** (Prometheus, Grafana, cAdvisor), nên không cần làm tay. Vì dữ liệu nằm ở volume, mỗi lần cập nhật phiên bản qua Jenkins **không mất cơ sở dữ liệu**. Sau khi dựng, Jenkins nạp lại luật cảnh báo của Prometheus (SIGHUP, chỉ gửi khi Prometheus đã sẵn sàng) và chờ kiểm tra sức khỏe (Verify).

## 6. Giới hạn và bảo mật (nói thật khi bị hỏi)

- Endpoint `/actuator/prometheus` **mặc định không mở** (trả 404); chỉ mở khi chạy kèm `docker-compose.monitoring.yml`. Bản chạy Jenkins cổng 8081 và chạy tay không lộ số liệu. Số liệu không chứa dữ liệu người dùng hay mật khẩu nhưng nếu đưa lên môi trường thật cần chặn cổng hoặc thêm xác thực.
- Mật khẩu Grafana, JWT hiện là **mặc định** (chưa dùng file `~\.gms.env`); đổi trước khi demo chính thức nếu cần.
- Portainer cần quyền đọc socket Docker: chỉ bật khi học và demo.
- Chưa có Alertmanager.
- cAdvisor có thể không hiện tên container nếu Docker Desktop dùng kho image containerd (dashboard Docker sẽ trống hoặc chỉ có mã container). Cách xử lý ở `docs/DOCKER_TOOLS_VOLUMES.md` mục 5. Nhóm cần kiểm tra trên máy thật trước khi chụp ảnh.

## 7. Dàn ý slide gợi ý (khoảng 12 slide, 10 phút)

| # | Tiêu đề | Nội dung chính |
|---|---|---|
| 1 | Mục tiêu | Yêu cầu nhóm 6-9, hệ thống GMS chạy trên Docker |
| 2 | Kiến trúc Docker của GMS | Sơ đồ mục 2 |
| 3 | Vì sao cần giám sát và theo dõi | phát hiện sự cố sớm, biết tài nguyên, có bằng chứng (ý ngắn) |
| 4 | Bộ công cụ | Bảng mục 3 |
| 5 | Theo dõi bằng Docker Desktop, lệnh docker, Portainer | ảnh chụp + 5 lệnh chính |
| 6 | Prometheus và Actuator | job, chu kỳ 10 giây, giữ 7 ngày |
| 7 | cAdvisor | đo từng container, vì sao cần thêm ngoài Actuator |
| 8 | Grafana: 2 dashboard | ảnh chụp 2 dashboard |
| 9 | Cảnh báo | bảng 5 luật, demo dừng backend, `BackendDown` Firing |
| 10 | Volume | khái niệm, 3 loại, bảng volume của dự án |
| 11 | Demo volume + sao lưu | thực hành 4 bước, cảnh báo `down -v` |
| 12 | Tích hợp CI/CD, giới hạn, kết luận | mục 5 và 6 |

**Kịch bản demo trực tiếp (10 bước)** có sẵn ở `docs/DOCKER_TOOLS_VOLUMES.md` mục 4; có thể chép gọn vào ghi chú người nói.

## 8. Ảnh chụp màn hình cần nhóm chụp từ máy thật

1. `docker compose -f docker-compose.demo.yml -f docker-compose.monitoring.yml ps` (các dịch vụ Up, healthy)
2. `docker stats --no-stream`
3. Docker Desktop, tab Containers và tab Volumes
4. Portainer (nếu bật), danh sách volume
5. Prometheus `/targets`: `gms-backend`, `cadvisor`, `prometheus` đều UP
6. Prometheus `/alerts`: 5 luật; thêm 1 ảnh `BackendDown` ở trạng thái Firing sau khi dừng backend
7. Grafana dashboard **GMS Tổng quan backend** (đã tạo lưu lượng bằng `scripts\demo-smoke.ps1` hoặc vòng lặp trong `docs/MONITORING.md`)
8. Grafana dashboard **GMS Docker** (kiểm tra có tên container, không chỉ mã)
9. Kết quả `docker volume ls` và `docker volume inspect gms-demo_sqlserver-data`
10. Kết quả thực hành volume (bước 3 của mục 4)

Nếu chưa có ảnh nào trong danh sách, slide ghi rõ chỗ để ảnh thay vì chèn ảnh minh họa không phải của dự án.
