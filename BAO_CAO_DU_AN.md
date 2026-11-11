# BÁO CÁO DỰ ÁN: ỨNG DỤNG IRON FITNESS (HOME WORKOUT)

## 1. Giới thiệu tổng quan
**IronFitness** (hay HomeFit) là một ứng dụng di động trên nền tảng Android được thiết kế nhằm mục đích hỗ trợ người dùng tập luyện thể dục thể thao ngay tại nhà mà không cần đến phòng gym hay thiết bị chuyên dụng. 
Dự án được xây dựng với giao diện trực quan, cung cấp lộ trình tập luyện cá nhân hóa theo từng nhóm cơ, độ khó, và mục tiêu cụ thể nhằm nâng cao sức khỏe cộng đồng.

## 2. Mục tiêu dự án
* Xây dựng một ứng dụng tập luyện thân thiện, dễ sử dụng cho mọi đối tượng (từ người mới bắt đầu đến nâng cao).
* Cung cấp thư viện bài tập phong phú, chuẩn xác (đọc từ dữ liệu JSON cấu trúc hóa).
* Theo dõi và báo cáo tiến trình tập luyện (Calories, thời gian, lịch sử tập).
* Đảm bảo trải nghiệm liền mạch với thiết kế Material Design hiện đại, hoạt động tốt kể cả khi ngoại tuyến (offline).

## 3. Các tính năng cốt lõi (Core Features)
Ứng dụng được chia thành 4 phân hệ chính thông qua thanh điều hướng (Bottom Navigation) và 1 phân hệ thực thi:

### 3.1. Tab "Tập luyện" (Home)
* **Banner Gợi ý**: Đề xuất các bài tập trong ngày hoặc lộ trình tập luyện (Ví dụ: "Ngày bắt đầu 1").
* **Lọc theo nhóm cơ**: Chọn nhanh các nhóm cơ tập trung như Bụng, Tay, Ngực, Chân, Toàn thân.
* **Danh sách bài tập tự động cập nhật**: Trình bày danh sách lộ trình theo độ khó (Dễ, Trung bình, Khó) tương ứng với nhóm cơ được chọn.
* **Bộ lọc nhanh (Quick Filters)**: Phân loại bài tập theo thời lượng (`<7 phút`, `7-15 phút`, `>15 phút`), cấp độ (`Người mới`), hoặc loại hình (`Khởi động`, `Giãn cơ`).
* **Khuyến nghị cá nhân hóa**: Gợi ý các giáo trình mục tiêu như "Tăng chiều cao", "Đốt cháy calo", v.v.

### 3.2. Tab "Khám phá" (Discover)
* Cung cấp giao diện trực quan bằng các lưới (Grid) và thanh cuộn ngang (Horizontal Scroll).
* Tích hợp các bộ sưu tập bài tập theo dạng Thử thách (Plank, HIIT), Bài tập nhanh (Tabata), và Bài tập giãn cơ.
* Hỗ trợ công cụ tìm kiếm và tra cứu lịch sử khám phá.

### 3.3. Tab "Báo cáo" (Report)
* Thống kê tổng số bài tập đã hoàn thành, lượng Calo tiêu thụ và Tổng thời gian.
* Lịch sử tập luyện (Calendar) và chuỗi ngày tập liên tiếp (Streaks).
* Theo dõi chỉ số cơ thể, cân nặng và tính toán BMI.

### 3.4. Tab "Cài đặt" (Settings)
* Quản lý hồ sơ cá nhân và đồng bộ dữ liệu.
* Tùy chỉnh giọng nói hướng dẫn (TTS), ngôn ngữ và giao diện (Sáng/Tối).

### 3.5. Trình phát bài tập (Workout Player)
* Màn hình chuẩn bị (Countdown) và màn hình tập luyện chính thức.
* Hỗ trợ hiển thị ảnh động (GIF) và Video (thông qua ExoPlayer).
* Xử lý linh hoạt 2 loại bài tập: Theo thời gian (Duration) và theo số lần (Reps).
* Theo dõi tiến độ và chuyển bài tập tự động (Auto-next) kèm thời gian nghỉ (Resting).

## 4. Kiến trúc và Công nghệ Kỹ thuật
* **Ngôn ngữ phát triển**: Java.
* **Môi trường**: Android Studio, Minimum SDK 24+.
* **Kiến trúc phần mềm**: MVVM (Model - View - ViewModel) kết hợp với LiveData giúp tách biệt logic xử lý dữ liệu và giao diện, chống rò rỉ bộ nhớ (Memory Leaks).
* **Quản lý điều hướng**: Jetpack Navigation Component (Single-Activity Architecture). Dùng `nav_graph` để quản lý luồng di chuyển và truyền tham số an toàn (Safe Args).
* **Quản lý dữ liệu**:
  * **Cục bộ (Offline)**: Đọc, phân tích cú pháp dữ liệu cấu trúc (JSON/Markdown) từ thư mục `assets`. Dữ liệu cá nhân, lịch sử được lưu vào cơ sở dữ liệu SQLite thông qua thư viện `Room Database`.
  * **Mạng (Network)**: Sử dụng `Retrofit2` và `OkHttp` để kết nối API (Sẵn sàng cho việc đồng bộ cloud).
* **Đa phương tiện**: 
  * `Glide` dùng để tải và hiển thị hình ảnh/GIF tốc độ cao.
  * `Media3 (ExoPlayer)` xử lý luồng Video bài tập.
* **Giao diện (UI/UX)**: Sử dụng Material Design 3, CoordinatorLayout, NestedScrollView.

## 5. Các kết quả đạt được trong giai đoạn gần nhất
1. **Hoàn thiện UI/UX**: Chuyển đổi thành công kiến trúc giao diện sang bố cục 4 Tabs tiêu chuẩn. Xây dựng các trang tĩnh và thành phần (Chips, MaterialCardView, Grid) giống 100% bản vẽ thiết kế mới.
2. **Tích hợp Dữ liệu Offline linh hoạt**: Thiết kế thành công `AssetExerciseRepository` để load hệ thống bài tập từ định dạng file cục bộ, giúp ứng dụng không bị phụ thuộc vào API mạng và tăng tốc độ khởi tạo.
3. **Thuật toán Lọc thông minh**: Triển khai view model lọc bài tập nhanh chóng dựa trên thuộc tính thời gian và số lượng động tác.
4. **Bảo trì và Fix lỗi**: Xử lý triệt để các lỗi đụng độ kiểu dữ liệu (Cast Exception) trong Navigation, đảm bảo ứng dụng chạy mượt mà không crash.

## 6. Hướng phát triển trong tương lai
* **Mở rộng kho dữ liệu**: Tích hợp các bài tập chuẩn khoa học (30+ bài cho mỗi nhóm cơ/độ khó). Chuyển toàn bộ dữ liệu Asset vào Database Room trong lần khởi chạy đầu tiên để hỗ trợ truy vấn (Query) tối ưu hơn.
* **Phát triển AI/Tính năng nâng cao**: Đề xuất bài tập thông minh dựa trên lịch sử cân nặng và thói quen người dùng.
* **Kết nối Backend**: Đồng bộ hóa dữ liệu lên máy chủ (Cloud Sync) để sao lưu tài khoản, tích hợp Google Fit/Health Connect.
