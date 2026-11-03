# Hướng dẫn và Kế hoạch Triển khai (Deployment Guide)

Tài liệu này đánh giá các phương án hosting miễn phí (Free Tier) để triển khai Backend (Spring Boot) và Cơ sở dữ liệu (MySQL) cho dự án HomeFit, đồng thời đưa ra đề xuất chính thức.

## 1. Đánh giá các nền tảng Hosting Miễn phí

### 1.1 Backend (Spring Boot Java 17/25)
Do Spring Boot khá tốn RAM khi khởi động (khoảng 300MB - 500MB), các gói miễn phí có RAM < 512MB thường bị crash (Out Of Memory).

*   **Render (Web Service Free Tier)**
    *   **Giới hạn:** 512MB RAM, ngủ đông (spin down) sau 15 phút không có request. Khởi động lại mất khoảng 1-2 phút. 750 giờ/tháng (đủ dùng 24/7 cho 1 app).
    *   **Ưu điểm:** Hỗ trợ Docker (rất tốt cho Spring Boot), dễ tích hợp GitHub CI/CD, có sẵn HTTPS.
    *   **Nhược điểm:** Delay lúc khởi động lại khá lâu, có thể gây timeout cho request đầu tiên từ Android app.

*   **Railway (Hobby Plan / Free Tier cũ)**
    *   **Giới hạn:** 512MB RAM. Dùng chung credit hàng tháng (khoảng $5/tháng).
    *   **Ưu điểm:** Không tự động ngủ đông, deploy cực nhanh từ GitHub, giao diện trực quan.
    *   **Nhược điểm:** Nếu hết $5 credit sẽ bị ngắt ứng dụng cho đến tháng sau.

*   **Koyeb (Free Tier)**
    *   **Giới hạn:** 1 Eco Dyno, 512MB RAM, 50GB băng thông.
    *   **Ưu điểm:** Không ngủ đông (always-on), hỗ trợ Docker, hiệu năng khá ổn định.
    *   **Nhược điểm:** Đôi khi khóa đăng ký mới hoặc yêu cầu xác minh thẻ tín dụng.

### 1.2 Cơ sở dữ liệu (MySQL 8)
Nhiều nền tảng PaaS hiện nay chuyển sang ưu tiên PostgreSQL. Cần tìm nơi hỗ trợ MySQL 8 miễn phí.

*   **Aiven (Free MySQL)**
    *   **Giới hạn:** 1 CPU, 1GB RAM, 5GB Storage.
    *   **Ưu điểm:** Hiệu năng rất tốt, ổn định, hỗ trợ MySQL 8 đầy đủ.
    *   **Nhược điểm:** Yêu cầu SSL khi kết nối (cần cấu hình thêm ở Spring Boot).

*   **Clever-Cloud (Free MySQL)**
    *   **Giới hạn:** 10MB Storage (Rất ít, chỉ đủ để demo hoặc test nhẹ). Hỗ trợ tối đa 5 kết nối đồng thời.
    *   **Ưu điểm:** Đăng ký nhanh, không cần thẻ.
    *   **Nhược điểm:** Dung lượng quá thấp cho môi trường Production thật.

*   **Railway (MySQL Plugin)**
    *   Dùng chung credit $5/tháng với Backend. Dễ quản lý cùng một nơi.

## 2. Đề xuất Phương án Triển khai

### 2.1 Phương án Chính (Primary)
*   **Backend:** **Render (Web Service)** bằng Dockerfile.
    *   *Lý do:* Miễn phí hoàn toàn, hỗ trợ Docker tốt, RAM 512MB đủ cho Spring Boot nếu cấu hình `-Xmx256m`. Vấn đề spin-down có thể khắc phục bằng cách cấu hình Android App có màn hình Splash chờ hoặc Retry logic.
*   **Database:** **Aiven (Free MySQL)**.
    *   *Lý do:* 5GB Storage là quá đủ cho dự án môn học, ổn định và không chia sẻ credit.

### 2.2 Phương án Dự phòng (Fallback)
*   **Backend & Database:** **Railway**.
    *   *Lý do:* Cài đặt cả Spring Boot và MySQL trên cùng Railway. Nếu Render lỗi, chuyển sang Railway cực nhanh. Nhược điểm là phải theo dõi sát credit để tránh bị sập cuối tháng.

## 3. Các bước triển khai lên Render (Backend)

**Bước 1: Chuẩn bị Dockerfile**
Tạo tệp `Dockerfile` ở thư mục `backend/`:
```dockerfile
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8090
# Giới hạn RAM để tránh bị kill trên Render
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
```

**Bước 2: Cấu hình trên Render**
1. Đăng nhập Render.com, chọn New -> Web Service.
2. Kết nối repo GitHub `IronFitness`.
3. Cài đặt các tham số:
   *   **Root Directory:** `backend`
   *   **Environment:** `Docker`
   *   **Plan:** Free
4. Thêm Environment Variables:
   *   `SPRING_PROFILES_ACTIVE=prod`
   *   `SPRING_DATASOURCE_URL=jdbc:mysql://<aiven-host>:<port>/homefit?useSSL=true`
   *   `SPRING_DATASOURCE_USERNAME=<user>`
   *   `SPRING_DATASOURCE_PASSWORD=<pass>`
   *   `JWT_SECRET=<chuỗi-bí-mật-dài-ít-nhất-256-bit>`

## 4. Các bước triển khai lên Aiven (Database)

1. Đăng nhập console.aiven.io.
2. Tạo dịch vụ MySQL (Free Plan), chọn Region gần Việt Nam (vd: Singapore).
3. Copy Connection URI (chứa Host, Port, Username, Password).
4. Do Flyway tự động chạy, chỉ cần cung cấp URL cho Spring Boot, Flyway sẽ tự tạo các bảng `users`, `categories`, `exercises` trong lần boot đầu tiên.
