# DÀN Ý SLIDE BÁO CÁO DỰ ÁN HOMEFIT (IRONFITNESS)

Dàn ý chi tiết 10 slide nội dung chính (không tính slide mở đầu và kết thúc) phục vụ thuyết trình báo cáo đồ án / bài tập lớn dự án **HomeFit (IronFitness)**. Mỗi slide được thiết kế đầy đủ mục tiêu, nội dung hiển thị, hình ảnh minh họa gợi ý và ghi chú thuyết trình.

---

## 📌 Bố cục Tổng quan Slide

- **Slide Mở đầu**: Tiêu đề dự án & Thành viên nhóm *(Slide 0 - Không tính vào 10 slide)*
- **Slide 1**: Đặt vấn đề & Mục tiêu Dự án (Problem & Project Objectives)
- **Slide 2**: Kiến trúc Tổng thể Hệ thống (System Architecture Overview)
- **Slide 3**: Đột phá Thiết kế & Công nghệ Android Client (Android Tech Stack & MVVM)
- **Slide 4**: Trải nghiệm Người dùng & Các Tính năng Client (Client Core Features)
- **Slide 5**: Tích hợp Quảng cáo AdMob & Mô hình Tài khoản Premium (AdMob & Premium Strategy)
- **Slide 6**: Thiết kế Máy chủ & Bảo mật Kép Spring Boot (Backend Architecture & Security)
- **Slide 7**: Thiết kế CSDL MySQL & Quy trình Flyway Migration (Database Schema & Flyway)
- **Slide 8**: Chiến lược Đồng bộ Dữ liệu Offline-First & Idempotency (Data Sync Strategy)
- **Slide 9**: Giao diện & Chức năng Trang Quản trị Web Admin (Admin Web Dashboard)
- **Slide 10**: Kiểm thử Hiệu năng, Đóng gói Docker & Triển khai Cloud (Testing & Deployment)
- **Slide Kết thúc**: Tổng kết & Q&A *(Slide 11 - Không tính vào 10 slide)*

---

## 🎬 NỘI DUNG CHI TIẾT CÁC SLIDE

### SLIDE 0: SLIDE MỞ ĐẦU (TITLE SLIDE) *(Slide phụ)*
* **Tiêu đề**: **HOMEFIT (IRONFITNESS)** - ỨNG DỤNG LUYỆN TẬP THỂ THAO TẠI NHÀ
* **Nội dung hiển thị**:
  * Đồ án học phần môn Kiến trúc & Phát triển Phần mềm / Lập trình Động.
  * Nhóm sinh viên thực hiện & Giảng viên hướng dẫn.
  * Logo HomeFit / Hình ảnh đại diện dự án.

---

### SLIDE 1: ĐẶT VẤN ĐỀ & MỤC TIÊU DỰ ÁN (PROBLEM & OBJECTIVES)
* **Tiêu đề Slide**: **1. ĐẶT VẤN ĐỀ VÀ MỤC TIÊU DỰ ÁN**
* **Nội dung chính**:
  * **Thực trạng**: Nhu cầu rèn luyện sức khỏe tại nhà tăng cao nhưng nhiều ứng dụng phụ thuộc mạng internet liên tục, quảng cáo gây gián đoạn buổi tập hoặc thiếu công cụ quản trị nội dung linh hoạt.
  * **Giải pháp HomeFit**:
    * **Android Native App**: Đạt chuẩn **Offline-First**, hoạt động mượt mà không cần mạng, giao diện tối ưu cho người tập.
    * **Spring Boot Backend & Admin Web**: Giúp Quản trị viên cập nhật danh mục, bài tập, lộ trình tập luyện tức thì mà **không cần phát hành lại bản build Android**.
  * **Mục tiêu chính**: Trải nghiệm tập luyện không ngắt quãng, cá nhân hóa thể trạng (BMI), đồng bộ lịch sử tập luyện chống trùng lặp, tối ưu chi phí hạ tầng.
* **Minh họa gợi ý**: Sơ đồ so sánh "App phụ thuộc mạng (Online-Only)" vs "HomeFit Offline-First".
* **Ghi chú thuyết trình**: *"HomeFit được ra đời nhằm giải quyết trải nghiệm tập luyện mượt mà nhất cho người dùng. Dù ở nơi không có Wifi/4G, người dùng vẫn mở ứng dụng, chọn bài tập có ảnh GIF minh họa và đếm ngược thời gian tập chính xác."*

---

### SLIDE 2: KIẾN TRÚC TỔNG THỂ HỆ THỐNG (SYSTEM ARCHITECTURE)
* **Tiêu đề Slide**: **2. KIẾN TRÚC TỔNG THỂ HỆ THỐNG**
* **Nội dung chính**:
  * **Mô hình Client-Server**:
    * **Android Client**: Java 17, MVVM, Room DB (Local Source of Truth), Retrofit 2 REST Client.
    * **Spring Boot Backend**: REST API `/api/v1` (JSON, JWT Stateless) và Admin Web `/admin` (Thymeleaf, Session, CSRF).
    * **Database**: MySQL 8.0 với 15 bản SQL Flyway Migration tự động quản lý schema.
  * **Chiến lược Media**: Lưu trữ URL hình ảnh/GIF từ các CDN ngoài (Cloudinary/GitHub), backend không lưu file tĩnh nặng giúp tối ưu bộ nhớ server.
* **Minh họa gợi ý**: Mô hình luồng dữ liệu (Mermaid Flowchart / Architecture Diagram) biểu diễn kết nối giữa Android App, Server Spring Boot, MySQL và Cloud Media Storage.
* **Ghi chú thuyết trình**: *"Dự án của chúng em áp dụng mô hình phân tách vai trò rõ ràng. Server duy nhất phục vụ cả API cho app và giao diện Web cho Admin, giảm chi phí vận hành và tính phức tạp hệ thống."*

---

### SLIDE 3: ĐỘT PHÁ THIẾT KẾ & CÔNG NGHỆ ANDROID CLIENT
* **Tiêu đề Slide**: **3. CÔNG NGHỆ VÀ KIẾN TRÚC ANDROID CLIENT**
* **Nội dung chính**:
  * **Kiến trúc MVVM (Model-View-ViewModel)**:
    * Luồng dữ liệu 1 chiều: `View (Fragment)` ↔ `ViewModel` ↔ `Repository` ↔ `Room DB / Retrofit API`.
    * Tách biệt hoàn toàn xử lý giao diện và logic nghiệp vụ.
  * **Single Activity Architecture**: Dùng 1 `MainActivity` điều hướng bằng `Navigation Component`, tăng hiệu năng chuyển cảnh.
  * **Công nghệ cốt lõi**:
    * **Java 17**: Mã nguồn idiomatic, hiệu năng cao.
    * **Room Database**: Đóng vai trò là nguồn dữ liệu chuẩn (Local Source of Truth).
    * **Glide 4**: Tải và phát ảnh GIF động minh họa bài tập mượt mà.
    * **ViewBinding**: Thay thế `findViewById`, đảm bảo an toàn kiểu dữ liệu compile-time.
* **Minh họa gợi ý**: Sơ đồ khối các tầng (UI Layer, Domain Layer, Data Layer) của Android Client.
* **Ghi chú thuyết trình**: *"Trên Android, chúng em sử dụng thuần Java 17 kết hợp với MVVM và Room DB. UI không bao giờ truy cập mạng hoặc DB trực tiếp mà luôn quan sát LiveData từ ViewModel, đảm bảo ứng dụng không bị lag/crash kể cả khi xoay màn hình."*

---

### SLIDE 4: TRẢI NGHIỆM NGƯỜI DÙNG VÀ CÁC TÍNH NĂNG CLIENT
* **Tiêu đề Slide**: **4. TRẢI NGHIỆM NGƯỜI DÙNG VÀ TÍNH NĂNG CLIENT**
* **Nội dung chính**:
  * **Workout Player (Trình phát bài tập)**:
    * Xử lý chính xác 2 dạng bài: **Theo thời gian đếm ngược** (Duration) và **Theo số lần thực hiện** (Reps).
    * Quản lý trạng thái đếm ngược thời gian nghỉ (`restSec`), tạm dừng (`PAUSE`), tiếp tục (`RESUME`), bỏ qua (`SKIP`).
    * Giữ màn hình luôn sáng (`FLAG_KEEP_SCREEN_ON`) và phát âm thanh đếm nhịp bằng `SoundPool`.
  * **Hồ sơ & BMI**: Tự động tính chỉ số BMI, phân loại thể trạng và gợi ý nhóm cơ.
  * **Thống kê & Lịch sử**: Giao diện Lưới Lịch (Calendar Grid Adapter), tính số ngày tập liên tiếp (Streak), tổng phút và tổng buổi.
  * **Nhắc nhở tự động**: Đặt lịch thông báo cục bộ qua `AlarmManager`, tự khởi tạo lại sau khi mở máy (`BootReceiver`).
* **Minh họa gợi ý**: Hình ảnh Chụp màn hình (Mockup) 3 màn hình chính: Màn hình Workout Player, Màn hình Thống kê Calendar và Màn hình Chỉ số BMI.
* **Ghi chú thuyết trình**: *"Điểm nhấn của ứng dụng nằm ở Workout Player với đếm ngược mượt mà, không làm gián đoạn buổi tập và tính năng Lịch sử ghi nhận Streak giúp tạo động lực cho người dùng rèn luyện hàng ngày."*

---

### SLIDE 5: TÍCH HỢP QUẢNG CÁO ADMOB & MÔ HÌNH PREMIUM
* **Tiêu đề Slide**: **5. TÍCH HỢP ADMOB VÀ TÍNH NĂNG PREMIUM**
* **Nội dung chính**:
  * **Chiến lược Quảng cáo (AdMob Strategy)**:
    * Tuân thủ quy định bài nhóm, tích hợp SDK Google Play Services Ads (`play-services-ads`).
    * **Banner Ad**: Đặt ở cuối các màn hình Danh mục, Bài tập, Thống kê (không che nút bấm).
    * **Interstitial Ad**: Quảng cáo toàn màn hình sau khi **hoàn thành** buổi tập.
  * **Bảo vệ trải nghiệm người dùng**:
    * Tuyệt đối **không** hiện quảng cáo trong lúc đang tập luyện.
    * Đặt cơ chế Cooldown (giãn cách): Tối thiểu **3 phút** giữa 2 lần hiển thị Interstitial Ad.
  * **Tính năng Premium (`isPremium = true`)**:
    * Cờ Premium lưu tại User Profile và CSDL Server.
    * Lớp `AdsManager` tự động **tắt toàn bộ quảng cáo** khi phát hiện người dùng Premium.
* **Minh họa gợi ý**: Ảnh chụp minh họa Banner Test Ad và Interstitial Test Ad trên Emulator + Sơ đồ logic kiểm tra `isPremium` trong `AdsManager`.
* **Ghi chú thuyết trình**: *"Chúng em tích hợp AdMob bằng các Test ID an toàn. Đặc biệt, hệ thống xử lý logic thông minh: người dùng tập luyện không bị quảng cáo làm phiền, đồng thời người dùng nâng cấp Premium sẽ hoàn toàn sạch quảng cáo."*

---

### SLIDE 6: THIẾT KẾ MÁY CHỦ VÀ BẢO MẬT KÉP SPRING BOOT
* **Tiêu đề Slide**: **6. KIẾN TRÚC BACKEND VÀ BẢO MẬT KÉP**
* **Nội dung chính**:
  * **Công nghệ Máy chủ**: Spring Boot (Java 17+), Spring Data JPA, Hibernate, Bean Validation.
  * **Cơ chế Bảo mật Kép (Dual Security Filter Chain)**:
    * **REST API (`/api/v1/**`)**: Bảo mật Stateless bằng Token JWT (AccessToken ngắn hạn + RefreshToken dài hạn với cơ chế xoay vòng - Token Rotation).
    * **Admin Web (`/admin/**`)**: Bảo mật Form Login + Session ID, hỗ trợ kiểm tra quyền `ADMIN` và bảo vệ chống tấn công CSRF.
  * **Xử lý Lỗi chuẩn hóa**: Lớp `GlobalExceptionHandler` bắt toàn bộ ngoại lệ và trả về JSON chuẩn: `{ "code": "...", "message": "..." }`.
* **Minh họa gợi ý**: Sơ đồ 2 chuỗi Filter Chain của Spring Security (REST JWT vs Web Session).
* **Ghi chú thuyết trình**: *"Backend Spring Boot được thiết kế bảo mật chặt chẽ. API cho ứng dụng di động dùng JWT Token hoàn toàn không lưu trạng thái (Stateless), trong khi trang Web Admin dùng Session kết hợp CSRF để đảm bảo an toàn tuyệt đối."*

---

### SLIDE 7: THIẾT KẾ CSDL MYSQL VÀ QUY TRÌNH FLYWAY MIGRATION
* **Tiêu đề Slide**: **7. THIẾT KẾ CSDL VÀ FLYWAY MIGRATION**
* **Nội dung chính**:
  * **Cấu trúc CSDL MySQL 8**: Bảng utf8mb4 lưu trữ mốc thời gian chuẩn UTC.
  * **Quản lý Phiên bản CSDL với Flyway (15 Migrations từ V1 - V15)**:
    * `V1` - `V5`: Tạo các bảng cốt lõi `users`, `categories`, `exercises`, `workout_sessions`, `refresh_tokens` và seed bài tập.
    * `V6` - `V10`: Thêm bảng hỗ trợ Admin Web, các mục Trang chủ `discover_items`, `home_sections`.
    * `V11` - `V15`: Mở rộng cấu hình `app_configs`, bảng `workouts`, `workout_items`, `challenge_programs` và enum media type.
  * **Tối ưu hóa**: Đánh Index ở các cột truy vấn tần suất cao như `user_id`, `completed_at`, `client_uuid`, `category_id`.
* **Minh họa gợi ý**: Sơ đồ ERD (Entity Relationship Diagram) thu gọn thể hiện mối quan hệ giữa các bảng chính.
* **Ghi chú thuyết trình**: *"Toàn bộ cấu trúc CSDL được quản lý nghiêm ngặt qua Flyway Migration. Nhóm tuyệt đối không dùng ddl-auto=update để tránh làm hỏng dữ liệu production, mọi thay đổi đều có lịch sử phiên bản SQL rõ ràng."*

---

### SLIDE 8: CHIẾN LƯỢC ĐỒNG BỘ DỮ LIỆU OFFLINE-FIRST & IDEMPOTENCY
* **Tiêu đề Slide**: **8. ĐỒNG BỘ DỮ LIỆU VÀ CHỐNG TRÙNG LẶP**
* **Nội dung chính**:
  * **Push Sync (Đồng bộ Lịch sử tập)**:
    * Khi tập offline, App tạo `WorkoutLogEntity` gán `clientUuid` (UUID v4) ngẫu nhiên, `synced = false`.
    * Khi có mạng, App gửi lô bài tập lên API `/workout-sessions/batch`.
    * **Chống trùng lặp (Idempotency)**: Server quy định ràng buộc `UNIQUE (user_id, client_uuid)`. Nếu gửi lại cùng 1 UUID, server chỉ xác nhận thành công mà không tạo dòng mới.
  * **Pull Sync (Đồng bộ Bài tập từ Server)**:
    * Truy vấn theo mốc `updatedAfter`. Server trả về các bài tập mới/thay đổi hoặc bài tập bị ẩn (`is_active = false`) để App cập nhật/xóa tương ứng trong Room DB.
* **Minh họa gợi ý**: Sơ đồ Trình tự (Sequence Diagram) mô tả luồng đồng bộ lô (Batch Sync) giữa App và Server khi từ Mất mạng → Có mạng.
* **Ghi chú thuyết trình**: *"Nhờ cơ chế clientUuid độc nhất cho từng buổi tập, người dùng có gửi đồng bộ bao nhiêu lần do mạng chập chờn thì CSDL vẫn đảm bảo tính đúng đắn, không bao giờ bị ghi trùng lặp dữ liệu."*

---

### SLIDE 9: GIAO DIỆN VÀ CHỨC NĂNG TRANG QUẢN TRỊ WEB ADMIN
* **Tiêu đề Slide**: **9. TRANG QUẢN TRỊ NỘI DUNG WEB ADMIN**
* **Nội dung chính**:
  * **Công nghệ**: Server-side rendering với Thymeleaf, Bootstrap HTML5, thiết kế Responsive.
  * **Chức năng Quản trị**:
    * **Dashboard**: Tổng quan chỉ số người dùng, tổng số buổi tập hoàn thành, lượt tập trong ngày.
    * **Quản lý Bài tập & Nhóm cơ (CRUD)**: Thêm/Sửa/Xóa mềm bài tập, cấu hình thời gian/số reps, **xem trước ảnh GIF minh họa** trực tiếp trên Web.
    * **Quản lý Workouts & Challenges**: Xây dựng lộ trình tập luyện 30 ngày.
    * **Quản lý Người dùng**: Phân quyền, bật/tắt cờ **Premium** cho tài khoản.
    * **Audit Logs**: Ghi nhật ký mọi thao tác chỉnh sửa của Admin.
* **Minh họa gợi ý**: Hình ảnh Chụp màn hình trang Dashboard Admin và Trang biểu mẫu thêm/sửa Bài tập có màn hình xem trước (Preview) GIF.
* **Ghi chú thuyết trình**: *"Trang Quản trị Admin giúp ban quản trị cập nhật nội dung bài tập mới ngay lập tức. Ngay khi Admin nhấn lưu bài tập trên Web, người dùng mở app sẽ tự động nhận được bài tập mới mà không cần cập nhật ứng dụng trên Google Play."*

---

### SLIDE 10: KIỂM THỬ HIỆU NĂNG, ĐÓNG GÓI DOCKER & TRIỂN KHAI CLOUD
* **Tiêu đề Slide**: **10. KIỂM THỬ, ĐÓNG GÓI DOCKER VÀ DEPLOY CLOUD**
* **Nội dung chính**:
  * **Kiểm thử Hiệu năng (Performance Testing)**:
    * Viết script PowerShell `scripts/perf-api.ps1` kiểm thử tự động.
    * Đo đạc thời gian phản hồi API đăng nhập, đọc danh mục, đồng bộ lô 100 buổi tập dưới áp lực concurrency.
  * **Đóng gói Docker Multi-Stage**:
    * Stage Build: Compile source code với JDK 25 Alpine.
    * Stage Run: Chạy ứng dụng nhẹ với JRE 25 Alpine, cấu hình RAM tối đa `-Xmx256m`.
  * **Triển khai Hạ tầng Cloud Miễn phí**:
    * **Backend**: Triển khai Web Service trên **Render.com** (Tự động CI/CD từ GitHub).
    * **Database**: **Aiven Cloud MySQL 8** (5GB Storage, kết nối bảo mật SSL).
* **Minh họa gợi ý**: Bảng kết quả đo thời gian phản hồi API từ script PowerShell + Sơ đồ Multi-stage Build Docker.
* **Ghi chú thuyết trình**: *"Cuối cùng, hệ thống được đóng gói tối ưu bằng Docker và triển khai thực tế trên hạ tầng Cloud Render & Aiven. Toàn bộ API đã được kiểm thử tải, đảm bảo hoạt động ổn định và thời gian phản hồi nhanh."*

---

### SLIDE KẾT THÚC: TỔNG KẾT & Q&A *(Slide phụ)*
* **Tiêu đề**: **CẢM ƠN THẦY CÔ VÀ CÁC BẠN ĐÃ LẮNG NGHE!**
* **Nội dung hiển thị**:
  * Tóm tắt 3 giá trị cốt lõi: Offline-First mượt mà, Quản trị linh hoạt, Hạ tầng đám mây tối ưu.
  * Hướng phát triển tương lai: Thêm bài tập tích hợp AI Pose Detection, nâng cấp IAP thật, cộng đồng chia sẻ lộ trình tập.
  * Mở phần Hỏi & Đáp (Q&A).

---
*Dàn ý slide báo cáo được tổng hợp đầy đủ theo toàn bộ cấu trúc dự án HomeFit.*
