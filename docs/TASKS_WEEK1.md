# TASKS – Tuần 1: Setup và nền tảng

**Mục tiêu:** chốt hợp đồng API/DB, dựng khung Android và Backend, chạy được Đăng ký/Đăng nhập end-to-end, hiển thị danh mục và bài tập mẫu từ server vào Room.

**Công nghệ:** Android Java + Room + Retrofit; Spring Boot 4.x + MySQL + Flyway; Admin Thymeleaf.

Trạng thái: `[ ]` chưa làm, `[~]` đang làm / chờ duyệt, `[x]` xong và đã merge vào `main`.

## Quy trình mỗi task

1. Agent soạn file vào `D:\IronFitnessChanges\<thư-mục-agent>\files\...` (đường dẫn giống repo), ghi `CHANGELOG.md`, báo agent khác qua `BOARD.md` nếu cần.
2. Chạy `apply-agent.bat <thư-mục-agent> agentN/<tên-task>`: tạo nhánh từ `main` và chép file vào repo.
3. Build, chạy thử, commit, push nhánh, mở Pull Request vào `main`.
4. **Agent 1 (PM) duyệt**, rồi Squash and merge. Sau đó cập nhật cột TT trong file này.

## Tiến độ khung dự án

- [x] Repo GitHub, `main`, tài liệu (README, ARCHITECTURE, AGENT, CONTRIBUTING, docs/*)
- [x] Khung Android (Empty Views Activity, Java, package `com.homefit.app`, Room/Retrofit/Glide/Navigation, quyền INTERNET, các package rỗng)
- [x] Khung Backend (Spring Boot 4.x, MySQL, Flyway + `flyway-mysql`, profile `local`)
- [x] Khu vực agent `D:\IronFitnessChanges` và script `apply-agent.bat`
- [ ] Bật bảo vệ nhánh `main` trên GitHub (yêu cầu Pull Request)

## TV1 – Phạm Hoàng Phi: Tech Lead / PM

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV1-01 | Chốt phạm vi MVP, công nghệ, quy tắc làm việc | Quyết định trong README/AGENT | T2 | [x] |
| W1-TV1-02 | Tạo repo, `.gitignore`, nhánh `main`, quy tắc nhánh riêng từng agent | Repo | T2 | [x] |
| W1-TV1-03 | Khởi tạo project Android và Backend, thêm thư viện, tạo cấu trúc package | Khung chạy được | T3 | [x] |
| W1-TV1-04 | Chốt `docs/API.md` và `docs/DATABASE.md` bản v1 | Hợp đồng v1 | T3 | [x] |
| W1-TV1-05 | Review PR của các agent, giữ hợp đồng API/DB nhất quán | PR được duyệt | T2–T6 | [~] |
| W1-TV1-06 | Backlog tuần 2 | `docs/TASKS_WEEK2.md` | T6 | [ ] |
| W1-TV1-07 | Bật bảo vệ nhánh `main` | Cấu hình GitHub | T3 | [ ] |

## TV4 – Giang Tuấn Kiệt: Backend và Admin Web (ưu tiên đầu tiên vì TV2/TV3 phụ thuộc)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV4-01 | Khởi tạo Spring Boot, profile `local`, `application-example.yml` | Backend chạy được | T3 | [x] |
| W1-TV4-02 | Flyway `V1__init.sql` (users, categories, exercises, workout_sessions, refresh_tokens) và `V2__seed_categories.sql` (4 nhóm cơ) | Schema và dữ liệu danh mục | T4 | [~] |
| W1-TV4-03 | Auth API: register, login, refresh, JWT filter, BCrypt, `GlobalExceptionHandler`; tạo tài khoản admin đầu tiên qua cấu hình (không lưu mật khẩu trong migration) | `/auth/*` chạy | T4 | [ ] |
| W1-TV4-04 | API đọc: `GET /categories`, `GET /exercises` (hỗ trợ `updatedAfter`), `GET/PUT /me` | API cho app | T5 | [ ] |
| W1-TV4-05 | Admin Web: đăng nhập form, layout Thymeleaf, CRUD danh mục (bản đầu) | `/admin` | T6 | [ ] |
| W1-TV4-06 | Google login backend: xác thực ID token | `/auth/google` | T6 (nếu kịp) | [ ] |
| W1-TV4-07 | Nhận `docs/seed/exercises.csv` của TV5, viết `V3__seed_exercises.sql` | Bài tập mẫu trong DB | T5 | [ ] |

Thứ tự: 02 → 03 → 04 trước vì TV2 và TV3 phụ thuộc.

## TV2 – : Vũ Quang Anh (Tài khoản, Profile)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV2-01 | Màn Splash (kiểm tra token), Đăng ký, Đăng nhập (Email) theo UI của TV5 | UI + ViewModel | T5 | [ ] |
| W1-TV2-02 | `AuthRepository`, `ApiService` auth, `AuthInterceptor`, `TokenStore` | Gọi API auth thật | T5 | [ ] |
| W1-TV2-03 | Đăng nhập Google (lấy ID token, gọi `/auth/google`) | Login Google | T6 | [ ] |
| W1-TV2-04 | Màn Profile: chiều cao, cân nặng, BMI (`BmiCalculator`), Room `UserProfileEntity`, `PUT /me/profile` | Profile và BMI | T6 | [ ] |
| W1-TV2-05 | Unit test `BmiCalculator` (gầy, bình thường, thừa cân, béo phì) | Test | T6 | [ ] |

Phụ thuộc: API auth từ W1-TV4-03. Trước đó dùng dữ liệu giả.

## TV3 – Giang Văn Quang: Android B (Thư viện bài tập, Player)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV3-01 | Room: `CategoryEntity`, `ExerciseEntity`, DAO theo `docs/DATABASE.md` | Database local | T4 | [ ] |
| W1-TV3-02 | Màn Danh mục và Danh sách bài tập (RecyclerView + adapter), đọc từ Room | UI list | T5 | [ ] |
| W1-TV3-03 | `ExerciseRepository`: gọi `/categories`, `/exercises`, lưu Room, UI quan sát LiveData | Đồng bộ dữ liệu | T6 | [ ] |
| W1-TV3-04 | Prototype Workout Player: GIF (Glide), đếm ngược, Pause/Resume/Skip | Prototype chạy | T6 | [ ] |
| W1-TV3-05 | Nghiên cứu ExoPlayer cho video, ghi kết luận vào PR | Ghi chú | T6 | [ ] |

Phụ thuộc: dữ liệu mẫu từ TV5 và API từ W1-TV4-04. Trước đó chèn dữ liệu giả vào Room.

## TV5 – Phạm Văn Huân: UI/UX, QA, Content

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W1-TV5-01 | Wireframe và UI: Splash, Đăng nhập, Đăng ký, Profile/BMI, Danh mục, Danh sách bài, Player | `docs/design/` | T3 (wireframe), T4 (UI) | [ ] |
| W1-TV5-02 | 12–16 bài mẫu (tên, mô tả, nhóm cơ, giây hoặc reps, rest) + GIF host ngoài, danh sách URL | `docs/seed/exercises.csv` | T3 (nháp), T4 (đủ) | [ ] |
| W1-TV5-03 | Icon nhóm cơ, bảng màu, âm thanh bíp (file nhẹ, có quyền dùng) | Tài nguyên `res/` | T4 | [ ] |
| W1-TV5-04 | Test case: Đăng ký/Đăng nhập, BMI (giá trị biên, để trống, số âm) | `docs/test/week1.md` | T5 | [ ] |
| W1-TV5-05 | Test tích hợp cuối tuần, báo lỗi | Danh sách lỗi | T6 | [ ] |

Lưu ý nội dung: GIF/video phải có quyền sử dụng hợp lệ.

## Mốc theo ngày

| Ngày | Việc chính |
|---|---|
| **T2–T3** | Khung repo, Android, Backend, tài liệu (**đã xong**) |
| **T4** | Flyway V1/V2 và Auth API; UI Figma; Room; bàn giao seed |
| **T5** | App gọi API thật; API đọc bài tập xong |
| **T6** | Review chéo, demo nội bộ end-to-end, chốt backlog tuần 2 |

## Tiêu chí hoàn thành tuần 1 (demo T6)

1. Đăng ký rồi đăng nhập bằng Email trên app, token được lưu, mở lại app vẫn đăng nhập.
2. Nhập chiều cao/cân nặng thì hiển thị BMI đúng và lưu lên server.
3. App hiển thị danh mục và danh sách bài tập lấy từ server; tắt mạng vẫn xem được (từ Room).
4. Prototype Player hiển thị GIF và đếm ngược.
5. Admin đăng nhập được và thêm/sửa/xóa danh mục; thay đổi hiện ra trên app sau khi làm mới.

## Rủi ro tuần 1

- API chưa sẵn sàng → TV2/TV3 dùng dữ liệu giả, không chờ.
- URL GIF chết hoặc chậm → TV5 kiểm tra link trước khi đưa vào seed.
- Google login tốn thời gian cấu hình (SHA-1, client ID) → để cuối tuần, có thể lùi sang tuần 2.
