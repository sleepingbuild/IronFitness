# NỘI DUNG SLIDE THUYẾT TRÌNH – CI/CD VỚI JENKINS

> Mục có dòng **[Ảnh: ...]** là chỗ bạn tự chèn ảnh. Phần còn lại là nội dung chữ trên slide.
> Các chỗ trong dấu `<...>` cần điền thông tin thật.

---

## Slide 1 – Trang bìa

**TRIỂN KHAI CI/CD CHO DỰ ÁN GYM MANAGEMENT SYSTEM**
GitHub – Jenkins – Maven – Spring Boot

- Môn học: `<tên môn học>`
- Giảng viên hướng dẫn: `<tên giảng viên>`
- Sinh viên thực hiện: Phạm Hoàng Phi – MSSV `<...>`
- Thành viên nhóm: `<tên – MSSV>`, `<tên – MSSV>`
- Trường / Lớp: `<...>` – Năm học `<...>`

---

## Slide 2 – Mục tiêu bài thực hành

- Tự động hoá quy trình: **push code → build → kiểm thử → đóng gói → triển khai → kiểm tra**.
- Áp dụng trực tiếp cho dự án đang phát triển (Spring Boot 3.3, Java 17, Maven, SQL Server).
- Ba sản phẩm nộp:
  1. URL GitHub chứa mã nguồn, các nhánh và `Jenkinsfile`.
  2. `Jenkinsfile` hoàn chỉnh.
  3. Ảnh cấu hình job Jenkins và lịch sử các lần chạy pipeline.

---

## Slide 3 – Kiến trúc và luồng CI/CD

Luồng hoạt động:

1. Lập trình viên push / merge Pull Request vào GitHub.
2. GitHub gửi **webhook** (push event) qua **ngrok** tới Jenkins.
3. Jenkins kéo mã nguồn và đọc `Jenkinsfile`.
4. Pipeline chạy: Checkout → Restore → Build → Test → Publish → Deploy → Verify.
5. Kết quả (xanh / đỏ) hiển thị trong Stage View.

Công cụ: Git/GitHub, Jenkins, JDK 17, Maven 3.9.9, ngrok, NSSM (Windows service), Docker (SQL Server).

**[Ảnh: sơ đồ luồng CI/CD (tự vẽ hoặc chụp)]**

---

## Slide 4 – Yêu cầu 1: Repository GitHub

- Repo: `https://github.com/sleepingbuild/SpringBoot-GYM-MANAGEMENT-SYSTEM`
- Có đầy đủ: mã nguồn, `pom.xml`, `Jenkinsfile` ở thư mục gốc.
- Quy trình làm việc theo **nhánh + Pull Request**; nhánh mặc định `master`.
- Nhánh minh hoạ pipeline: `ci/green-demo`.

**[Ảnh: trang repo GitHub thấy `Jenkinsfile`]**
**[Ảnh: danh sách nhánh (Branches)]**

---

## Slide 5 – Yêu cầu 2: Cấu hình Jenkins job

- Job kiểu **Pipeline**, tên `GMS-CICD`.
- Definition: *Pipeline script from SCM* (Git), Script Path: `Jenkinsfile`.
- Branch: `*/master`.
- Build Trigger: **GitHub hook trigger for GITScm polling**.
- Công cụ khai báo trong Jenkins: **JDK17**.

**[Ảnh: trang Configure của job]**

---

## Slide 6 – Yêu cầu 3: Jenkinsfile hoàn chỉnh

| Stage | Công việc |
|---|---|
| Checkout | Lấy mã nguồn từ GitHub |
| Restore | `mvn dependency:resolve` – tải thư viện |
| Build | `mvn compile` – biên dịch |
| Test | `mvn test` – chạy kiểm thử, xuất báo cáo JUnit |
| Publish | `mvn package` – tạo file jar, lưu artifact |
| Deploy | Chép jar, khởi động lại Windows service (chỉ nhánh `master`) |
| Verify | Gọi API kiểm tra ứng dụng trả mã 200 (chỉ nhánh `master`) |

Điểm đáng chú ý: `githubPush()` trigger, `timeout`, `disableConcurrentBuilds`, chỉ triển khai từ `master`.

**[Ảnh: nội dung `Jenkinsfile` (chụp hoặc cắt phần chính)]**

---

## Slide 7 – Kết nối GitHub Webhook

- Payload URL: `https://<ngrok>/github-webhook/`, kiểu `application/json`, sự kiện **push**.
- Bảo mật bằng **secret** (chữ ký SHA-256); Jenkins lưu secret dạng credential.
- Kết quả: mã phản hồi **200** trong *Recent Deliveries*.
- Kiểm chứng: build #8 ghi rõ **"Started by GitHub push by sleepingbuild"** (sau khi merge PR #56).

**[Ảnh: GitHub Webhook – Recent Deliveries mã 200]**
**[Ảnh: Console Output build #8, dòng "Started by GitHub push"]**

---

## Slide 8 – Lịch sử các lần chạy pipeline

- Tổng cộng các lần chạy #3 → #9, thể hiện đầy đủ tiến trình xử lý lỗi.
- **#9 xanh** trên nhánh `ci/green-demo`: Checkout → Publish thành công, Deploy/Verify bỏ qua theo thiết kế (chỉ chạy trên `master`).
- Các lần đỏ cho thấy pipeline **phát hiện lỗi thật** và dừng đúng stage.

**[Ảnh: Stage View / Stages của job `GMS-CICD`]**
**[Ảnh: Console Output build #9 kết thúc SUCCESS]**

---

## Slide 9 – Phân tích kết quả từng nhóm build

| Build | Kết quả | Nguyên nhân | Cách xử lý |
|---|---|---|---|
| #3, #4, #5 | Đỏ (Build) | Máy dùng Java 25, không tương thích Lombok của dự án | Khai báo `JDK17`, thêm khối `tools` vào Jenkinsfile |
| #6, #7, #8 | Đỏ (Deploy) | Ứng dụng không khởi động do **vòng phụ thuộc bean** trong mã nguồn | Chuyển cho người phụ trách module sửa; lỗi thuộc ứng dụng chứ không thuộc pipeline |
| #9 | Xanh | Chạy trên nhánh khác `master` | Chứng minh các stage Checkout → Publish hoạt động |

Ý nghĩa: CI phát hiện sớm lỗi mà `mvn test` (dùng Mockito) không bắt được.

---

## Slide 10 – Khó khăn gặp phải và cách giải quyết

1. **Sai phiên bản JDK** → cấu hình JDK17 riêng cho Jenkins, kiểm tra bằng `mvn -version`.
2. **`JAVA_HOME` không đúng** trong Jenkins → trỏ tới thư mục JDK, không trỏ `bin`.
3. **Trùng cổng 8080** giữa Jenkins và ứng dụng → ứng dụng chạy cổng **8081**.
4. **SQL Server chạy trong Docker** (cổng 1433) → bật Docker trước khi triển khai.
5. **Webhook báo `400 No valid signature`** → tạo lại webhook với secret đúng, được `200`.
6. **Chạy ứng dụng như service** → dùng NSSM, chạy bằng JDK 17 đầy đủ đường dẫn.

---

## Slide 11 – Kết luận và hướng phát triển

Kết quả đạt được:
- Pipeline tự động chạy khi push/merge vào `master`.
- Có đủ ba sản phẩm: repo GitHub, Jenkinsfile, ảnh cấu hình và lịch sử chạy.
- Phát hiện và truy vết lỗi nhanh qua từng stage.

Hướng phát triển:
- Dùng **database riêng cho CI** (ví dụ `gms_db_ci`) để các lần chạy không ảnh hưởng nhau.
- Chạy kiểm thử tích hợp bằng Testcontainers.
- Triển khai qua **IIS làm reverse proxy** (phần thực hành tiếp theo).
- Chuyển sang Multibranch Pipeline để mỗi nhánh tự chạy kiểm thử.

---

## Slide 12 – Hỏi đáp

**Cảm ơn thầy/cô và các bạn đã lắng nghe.**
Repo: `https://github.com/sleepingbuild/SpringBoot-GYM-MANAGEMENT-SYSTEM`
