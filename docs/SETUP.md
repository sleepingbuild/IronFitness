# SETUP – Khởi tạo dự án từng bước (Windows, cmd)

Hai thư mục độc lập trên ổ D:

```
D:\IronFitness            ← REPO (có Git, chỉ code đã duyệt)
D:\IronFitnessChanges     ← KHU VỰC AGENT (ngoài repo, không push)
```

Lệnh trong tài liệu này chạy trong **cmd**. Nếu thấy `'#' is not recognized`, bạn đã dán cả dòng ghi chú; chỉ chạy dòng lệnh.

## Bước 0 – Cài công cụ

Git, JDK 17+, Android Studio (stable), MySQL 8, IntelliJ IDEA (hoặc mở backend bằng IDE bạn quen). Kiểm tra: `git --version`, `java -version`, `mysql --version`.

## Bước 1 – Repo và đẩy tài liệu lên GitHub

Đặt file: `README.md`, `ARCHITECTURE.md`, `AGENT.md`, `CONTRIBUTING.md`, `.gitignore` ở `D:\IronFitness`; `API.md`, `DATABASE.md`, `TASKS_WEEK1.md`, `SETUP.md` trong `D:\IronFitness\docs`.

```bat
cd /d D:\IronFitness
git init -b main
git add .gitignore README.md ARCHITECTURE.md AGENT.md CONTRIBUTING.md docs
git status
git commit -m "docs: them tai lieu du an [W1-TV1-02]"
git remote add origin https://github.com/<user>/<repo>.git
git push -u origin main
```

`git status` phải liệt kê đủ các file ở mục "Changes to be committed" trước khi commit. Nếu Git hỏi tên/email: `git config --global user.name "..."` và `git config --global user.email "..."`.

Trên GitHub, bật bảo vệ `main` (xem `CONTRIBUTING.md`).

## Bước 2 – Android (Android Studio)

1. **File → New → New Project → Phone and Tablet → Empty Views Activity**.
2. Name `IronFitness`; Package `com.homefit.app`; **Save location `D:\IronFitness\android`**; Language **Java**; Minimum SDK **API 26**; Build config **Groovy DSL**.
3. Chờ Gradle sync, chạy thử trên emulator.
4. Trong `app/build.gradle` bật ViewBinding, Java 17 và thêm thư viện:

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    buildFeatures { viewBinding true }
}

dependencies {
    implementation "androidx.room:room-runtime:2.6.1"
    annotationProcessor "androidx.room:room-compiler:2.6.1"
    implementation "com.squareup.retrofit2:retrofit:2.11.0"
    implementation "com.squareup.retrofit2:converter-gson:2.11.0"
    implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"
    implementation "com.github.bumptech.glide:glide:4.16.0"
    annotationProcessor "com.github.bumptech.glide:compiler:4.16.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel:2.8.7"
    implementation "androidx.lifecycle:lifecycle-livedata:2.8.7"
    implementation "androidx.navigation:navigation-fragment:2.7.7"
    implementation "androidx.navigation:navigation-ui:2.7.7"
}
```

5. Thêm vào `AndroidManifest.xml` (trên thẻ `<application>`): `<uses-permission android:name="android.permission.INTERNET" />`
6. Tạo các package theo `ARCHITECTURE.md` mục 2.2, sync lại Gradle và chạy lại app.

## Bước 3 – Backend (Spring Boot, IntelliJ)

1. Vào **start.spring.io**: Maven, Java; **Spring Boot = bản ổn định mới nhất (4.x), không chọn SNAPSHOT/M/RC**; Group `com.homefit`, Artifact `server`, Package `com.homefit.server`, Jar, Java 17+.
2. Dependencies: Spring Web, Spring Security, Spring Data JPA, Validation, Thymeleaf, Flyway Migration, MySQL Driver, Spring Boot DevTools.
3. Generate, giải nén, chép **nội dung bên trong** vào `D:\IronFitness\backend` sao cho `pom.xml` nằm ngay trong `backend\`.
4. Thêm vào `pom.xml`: `org.flywaydb:flyway-mysql` (module Flyway cho MySQL 8).
5. IntelliJ: **File → Open** → chọn `D:\IronFitness\backend\pom.xml` → Open as Project; chọn JDK 17+.
6. Tạo database: `mysql -u root -p -e "CREATE DATABASE homefit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"`
7. Tạo `backend\src\main\resources\application-local.yml` (đã nằm trong `.gitignore`):

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/homefit?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: root
    password: MAT_KHAU_CUA_BAN
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
server:
  port: 8080
```

8. Chạy: `cd /d D:\IronFitness\backend` rồi `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local`. Mở `http://localhost:8080`; thấy trang đăng nhập mặc định của Spring Security là backend đã chạy.

## Bước 4 – Commit hai project lên nhánh riêng, merge vào main

Khung ban đầu do Agent 1 (PM) phụ trách:

```bat
cd /d D:\IronFitness
git checkout -b agent1/w1-project-skeleton
git add android backend
git commit -m "chore: khoi tao project Android va Spring Boot [W1-TV1-03]"
git push -u origin agent1/w1-project-skeleton
```

Mở Pull Request vào `main`, duyệt, Squash and merge. Rồi `git checkout main` và `git pull`.

## Bước 5 – Khu vực agent (ngoài repo)

1. Copy thư mục `IronFitnessChanges` (kèm `apply-agent.bat`, `README.md`, `PROMPTS.md`, `BOARD.md`) vào `D:\IronFitnessChanges`.
2. Mở 5 phiên làm việc riêng, mỗi phiên là một agent: dán prompt trong `PROMPTS.md` (Phần chung + Phần riêng) cùng task từ `docs/TASKS_WEEK1.md`.
3. Agent giao file vào `D:\IronFitnessChanges\<thư-mục-agent>\files\...` (đường dẫn trong `files\` giống hệt repo).
4. Đưa vào repo bằng nhánh riêng:

```bat
D:\IronFitnessChanges\apply-agent.bat agent3-android-workout agent3/w1-exercise-list
```

5. Build, chạy thử, commit, push nhánh, mở PR. Agent 1 duyệt rồi mới merge vào `main`.

## Checklist hoàn thành khởi tạo

- [ ] Repo trên GitHub có nhánh `main` với tài liệu
- [ ] `android\` build được và chạy trên emulator
- [ ] `backend\` chạy ở `http://localhost:8080`
- [ ] `D:\IronFitnessChanges` có 5 thư mục agent, `BOARD.md`, `apply-agent.bat`
- [ ] Bảo vệ nhánh `main` đã bật
