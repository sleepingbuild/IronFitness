# TASKS – Tuần 1: Setup và nền tảng

**Mục tiêu:** chốt kiến trúc và hợp đồng API/DB, dựng khung Android và Backend, chạy được Đăng ký/Đăng nhập end-to-end, hiển thị được danh mục và bài tập mẫu từ server vào Room.

**Công nghệ:** Android Java + Room + Retrofit; Spring Boot + MySQL + Flyway; Admin Thymeleaf.

Trạng thái: `[ ]` chưa làm, `[~]` đang làm, `[x]` xong.

## TV1 – Tech Lead

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV1-01 | Họp khởi động, chốt phạm vi MVP, công nghệ, quy tắc làm việc | Biên bản ngắn | T2 | [ ] |
| W1-TV1-02 | Tạo repo monorepo (`android/`, `backend/`, `docs/`), `.gitignore`, nhánh `main`/`develop` | Repo | T2 | [ ] |
| W1-TV1-03 | Khởi tạo project Android (package `com.homefit.app`), thêm Room, Retrofit, Glide, Navigation, ViewBinding; tạo sẵn cấu trúc package | Android khung chạy được | T3 | [ ] |
| W1-TV1-04 | Chốt `docs/API.md` và `docs/DATABASE.md` cùng TV2/3/4 | Hợp đồng bản v1 | T3 | [ ] |
| W1-TV1-05 | Review PR, hỗ trợ tích hợp | | T2–T6 | [ ] |
| W1-TV1-06 | Backlog tuần 2 | `docs/TASKS_WEEK2.md` | T6 | [ ] |

## TV2 – Android A (Tài khoản, Profile)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV2-01 | Màn Splash (kiểm tra token), Đăng ký, Đăng nhập (Email) theo UI của TV5 | UI + ViewModel | T5 | [ ] |
| W1-TV2-02 | `AuthRepository`, `ApiService` auth, `AuthInterceptor`, `TokenStore` (EncryptedSharedPreferences) | Gọi API auth thật | T5 | [ ] |
| W1-TV2-03 | Đăng nhập Google (lấy ID token, gọi `/auth/google`) | Login Google | T6 | [ ] |
| W1-TV2-04 | Màn Profile: nhập chiều cao, cân nặng, tính BMI (`BmiCalculator`), lưu Room `UserProfileEntity`, gọi `PUT /me/profile` | Profile và BMI | T6 | [ ] |
| W1-TV2-05 | Unit test `BmiCalculator` (các mốc gầy, bình thường, thừa cân, béo phì) | Test | T6 | [ ] |

Phụ thuộc: cần API auth từ W1-TV4-03 (có thể dùng mock server hoặc dữ liệu giả đến T4).

## TV3 – Android B (Thư viện bài tập, Player)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV3-01 | Room: `CategoryEntity`, `ExerciseEntity`, DAO theo `docs/DATABASE.md` | Database local | T4 | [ ] |
| W1-TV3-02 | Màn Danh mục nhóm cơ và Danh sách bài tập (RecyclerView + adapter), đọc từ Room | UI list | T5 | [ ] |
| W1-TV3-03 | `ExerciseRepository`: gọi `/categories`, `/exercises`, lưu Room, UI quan sát LiveData | Đồng bộ dữ liệu | T6 | [ ] |
| W1-TV3-04 | Prototype Workout Player: hiển thị GIF (Glide), đếm ngược `CountDownTimer`, nút Pause/Resume/Skip | Prototype chạy | T6 | [ ] |
| W1-TV3-05 | Nghiên cứu ExoPlayer cho video, ghi kết luận vào PR | Ghi chú | T6 | [ ] |

Phụ thuộc: dữ liệu mẫu và URL GIF từ W1-TV5-02, API từ W1-TV4-04. Trước đó dùng dữ liệu giả chèn tay vào Room.

## TV4 – Backend và Admin Web

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV4-01 | Khởi tạo Spring Boot (Web, Security, JPA, Validation, Thymeleaf, Flyway, MySQL driver), profile `local`, `application-example.yml` | Backend chạy được | T3 | [ ] |
| W1-TV4-02 | Flyway `V1__init.sql` (users, categories, exercises, workout_sessions), `V2__seed.sql` (danh mục, bài mẫu, admin) | Schema và seed | T4 | [ ] |
| W1-TV4-03 | Auth API: register, login, refresh, JWT filter, BCrypt, `GlobalExceptionHandler` | `/auth/*` chạy | T4 | [ ] |
| W1-TV4-04 | API đọc: `GET /categories`, `GET /exercises` (hỗ trợ `updatedAfter`), `GET/PUT /me` | API cho app | T5 | [ ] |
| W1-TV4-05 | Admin Web: đăng nhập form login, layout Thymeleaf, CRUD danh mục (bản đầu) | `/admin` | T6 | [ ] |
| W1-TV4-06 | Google login backend: xác thực ID token | `/auth/google` | T6 (nếu kịp) | [ ] |

Ưu tiên: W1-TV4-02, 03, 04 trước vì TV2 và TV3 phụ thuộc.

## TV5 – UI/UX, QA, Content

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV5-01 | Wireframe và UI Figma: Splash, Đăng nhập, Đăng ký, Profile/BMI, Danh mục, Danh sách bài, Player | Figma | T3 (wireframe), T4 (UI) | [ ] |
| W1-TV5-02 | Chuẩn bị 12–16 bài mẫu (tên, mô tả, nhóm cơ, giây hoặc reps, rest) + upload GIF lên nơi host (Cloudinary/CDN), danh sách URL | `docs/seed/exercises.csv` | T3 (bản nháp), T4 (đủ) | [ ] |
| W1-TV5-03 | Icon nhóm cơ, bảng màu, âm thanh bíp (file nhẹ, được phép dùng) | Tài nguyên `res/` | T4 | [ ] |
| W1-TV5-04 | Test case: Đăng ký/Đăng nhập, BMI (giá trị biên, để trống, số âm) | `docs/test/week1.md` | T5 | [ ] |
| W1-TV5-05 | Test tích hợp cuối tuần và báo lỗi | Danh sách lỗi | T6 | [ ] |

Lưu ý nội dung: GIF/video phải có quyền sử dụng hợp lệ (tự quay hoặc nguồn cho phép dùng thương mại).

## Mốc theo ngày

| Ngày | Việc chính |
|---|---|
| **T2** | Họp khởi động, tạo repo, chốt công nghệ |
| **T3** | Khung Android + Backend chạy; chốt API/DB v1; wireframe xong |
| **T4** | Flyway + Auth API xong; UI Figma xong; Room xong; bàn giao seed |
| **T5** | App gọi API thật; API đọc bài tập xong |
| **T6** | Review chéo, demo nội bộ end-to-end, chốt backlog tuần 2 |

## Tiêu chí hoàn thành tuần 1 (demo T6)

1. Đăng ký rồi đăng nhập bằng Email trên app, token được lưu, mở lại app vẫn đăng nhập.
2. Nhập chiều cao/cân nặng thì hiển thị BMI đúng và lưu lên server.
3. App hiển thị danh mục và danh sách bài tập lấy từ server, tắt mạng vẫn xem được (từ Room).
4. Prototype Player hiển thị GIF và đếm ngược.
5. Admin đăng nhập được và thêm/sửa/xóa danh mục; thay đổi hiện ra trên app sau khi làm mới.

## Rủi ro tuần 1

- API chưa sẵn sàng → TV2/TV3 dùng dữ liệu giả, không chờ.
- URL GIF chết hoặc chậm → TV5 kiểm tra link trước khi đưa vào seed.
- Google login tốn thời gian cấu hình (SHA-1, client ID) → để cuối tuần, có thể lùi sang tuần 2.
