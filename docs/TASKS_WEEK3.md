# TASKS – Tuần 3: Hoàn thiện tính năng, nội dung thật, tính năng khác biệt và đóng băng tính năng

## TV1 – Phi: Tech Lead / PM

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W3-TV1-01 | Rà tồn đọng tuần 2, merge các PR đã xong, cập nhật `TASKS_WEEK2.md`; kiểm tra `main` build được (`gradlew assembleDebug`, `mvnw verify`) | Tuần 2 chốt sổ | T2 | [ ] |
| W3-TV1-02 | Đọc báo cáo thị trường, chọn tối đa 2 tính năng (Phụ lục A), cập nhật phạm vi, `docs/API.md`, `docs/DATABASE.md`, ghi ADR | Quyết định + tài liệu hợp đồng | T2 | [ ] |
| W3-TV1-03 | Review PR của các agent (đúng task, đúng phạm vi, khớp API/DB, không secret, có ảnh chụp màn hình) | PR được duyệt | T2–T6 | [ ] |
| W3-TV1-04 | Chốt cách demo và nơi chạy máy chủ: (a) máy cá nhân qua LAN hoặc (b) triển khai miễn phí (kiểm tra gói MySQL miễn phí hiện có, giới hạn, độ ổn định); ghi `docs/DEPLOY.md` kèm phương án dự phòng | `docs/DEPLOY.md` | T3 | [ ] |
| W3-TV1-05 | Bật bảo vệ nhánh `main` trên GitHub (W1-TV1-07) và thêm `.gitattributes` (hết cảnh báo CRLF) | Cấu hình GitHub + PR nhỏ | T2 | [ ] |
| W3-TV1-06 | Viết lại `docs/SETUP.md` (chạy dự án từ đầu cho thành viên mới, dựa trên `docs/BACKEND.md`) | `docs/SETUP.md` | T4 | [ ] |
| W3-TV1-07 | Rà bảo mật và chất lượng: không secret trong Git (kiểm tra lịch sử commit), phạm vi quyền Android, ID AdMob đúng (thử nghiệm), gỡ `AdsDemoActivity` và mã demo không còn dùng, cập nhật `ARCHITECTURE.md`/`AGENT.md` theo thực tế | Checklist + PR dọn dẹp | T5 | [ ] |
| W3-TV1-08 | Báo cáo đợt 3: cập nhật `REPORT_CONTENT.md` (khảo sát, ảnh app, kết quả test và hiệu năng, trang admin, đồng bộ offline), tích hợp `contrib`, đổi nhãn trạng thái, gửi agent báo cáo | `REPORT_CONTENT.md` đợt 3 | T6 | [ ] |
| W3-TV1-09 | Tuyên bố **đóng băng tính năng** (T6): liệt kê tính năng vào bản nộp, tính năng bị cắt, hạn chế đã biết | Danh sách đóng băng trong `BOARD.md` và `docs/SUBMISSION.md` bản nháp | T6 | [ ] |

## TV4 – Quang Anh(kiêm backend): Backend và Admin Web

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W3-TV4-01 | **Nội dung thật:** nhận `docs/seed/exercises.csv` (30–40 bài, mỗi bài có link GIF hợp lệ và ghi nguồn, giấy phép), viết `V4__seed_exercises.sql`: ẩn 12 bài thử (`is_active = FALSE`, cập nhật `updated_at`) rồi thêm bài thật; nếu quá lớn thì chia `V5`. Agent 1 có thể soạn trước phần chữ (tên, mô tả, giây hoặc số lần, nghỉ) để TV5 chỉ thêm link | Bài tập thật trong DB | T3 | [ ] |
| W3-TV4-02 | Admin: **nhập bài tập từ CSV** (`/admin/exercises/import`: tải file, xem trước, báo lỗi từng dòng, thêm hàng loạt trong một giao dịch) và nút **kiểm tra link** (gửi HEAD tới `mediaUrl`, báo còn sống hay không) | Công cụ nhập liệu và kiểm tra link | T4 | [ ] |
| W3-TV4-03 | Tính năng khác biệt #1 phía máy chủ theo Phụ lục A (mặc định A: `GET /me/stats` trả tổng buổi, tổng phút, chuỗi ngày liên tiếp, số buổi 7 và 30 ngày gần nhất; cập nhật `API.md`) | API + test | T4 | [ ] |
| W3-TV4-04 | Tính năng khác biệt #2 phía máy chủ nếu cần (mặc định B không cần API mới, chỉ ghi luật gợi ý vào `API.md` hoặc tài liệu thiết kế) | API hoặc tài liệu | T5 | [ ] |
| W3-TV4-05 | Hiệu năng CSDL: migration chỉ mục kết hợp `workout_sessions (user_id, completed_at)`, kiểm tra `EXPLAIN` truy vấn lịch sử, chạy `scripts/perf-api.ps1` **trước và sau**, ghi bảng so sánh | Migration + bảng số đo | T4 | [ ] |
| W3-TV4-06 | **`/auth/google`** theo ADR 11: xác thực ID token bằng Nimbus và JWKS của Google (kiểm tra `iss`, `aud`, `exp`, `email_verified`), tạo hoặc liên kết user theo email, trả `AuthResponse`. **Chỉ làm khi đã có Web client ID** | `POST /auth/google` | T5 (nếu kịp) | [ ] |
| W3-TV4-07 | **Môi trường demo không cần Internet:** tải Bootstrap về `static/` thay CDN (người điều phối tải file, Agent 1 sửa mẫu trang); `scripts/start-demo.ps1` (kiểm tra MySQL chạy, tạo database nếu thiếu, build, chạy cổng 8090, in địa chỉ LAN); `scripts/seed-demo.sql` tạo tài khoản demo và lịch sử mẫu để Calendar có ngày đánh dấu | Script và dữ liệu demo | T5 | [ ] |
| W3-TV4-08 | Chống dò mật khẩu: khóa tạm đăng nhập sau 5 lần sai trong 15 phút theo email và địa chỉ IP, có test; cập nhật `API.md` | Cơ chế khóa + test | T5 (nếu kịp) | [ ] |
| W3-TV4-09 | ERD cho báo cáo: người điều phối xuất bằng MySQL Workbench (Database → Reverse Engineer), đặt `docs/report/images/diagrams/erd.png`; Agent 1 hướng dẫn và đối chiếu với `DATABASE.md` | Hình ERD | T5 | [ ] |
| W3-TV4-10 | Mở rộng test tích hợp cho mọi API mới và trang admin mới, chạy `mvnw verify` xanh trước khi đóng băng | Test | T6 | [ ] |

Thứ tự: 01 trước (dữ liệu thật cho cả Android và báo cáo), rồi 03 → 04, 02 và 05 song song, 07 trước ngày đóng băng.

## TV2 – Kiệt: Android A (Tài khoản, Lịch sử, Nhắc tập)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W3-TV2-01 | Hoàn thiện tồn đọng tuần 2: Lịch sử (Calendar tự làm, ngày đánh dấu đúng múi giờ), Thống kê, Nhắc tập (AlarmManager, đăng ký lại sau khởi động, quyền `POST_NOTIFICATIONS` Android 13+) | Lịch sử, thống kê, nhắc tập chạy thật | T3 | [ ] |
| W3-TV2-02 | Tính năng khác biệt #1 phía app theo Phụ lục A (mặc định A: chuỗi ngày tập, huy hiệu mốc, tổng 7 và 30 ngày) trên màn Thống kê; dữ liệu từ Room, đồng bộ lại sau khi đăng nhập máy mới (`restoreHistory`) | Logic ViewModel | T4 | [ ] |
| W3-TV2-03 | **Đăng nhập Google** theo ADR 11 (Credential Manager): nút ở màn Đăng nhập và Đăng ký, Web client ID đọc từ `local.properties` (ngoài Git), ẩn nút khi chưa cấu hình. **Chỉ làm khi W3-TV4-06 xong và có client ID** | Đăng nhập Google | T5 (nếu kịp) | [ ] |
| W3-TV2-04 | Xử lý trường hợp biên: hết phiên giữa chừng, mất mạng khi đồng bộ, đăng xuất rồi đăng nhập tài khoản khác trên cùng máy (lịch sử không lẫn), đổi múi giờ máy, xoay màn hình, hủy quyền thông báo | Danh sách lỗi đã xử lý | T5 | [ ] |
| W3-TV2-05 | Mã hóa token bằng Android Keystore (nếu còn thời gian; hiện lưu SharedPreferences riêng, chưa mã hóa) | `TokenStore` mã hóa | T5 (tùy chọn) | [ ] |
| W3-TV2-06 | Unit test: logic Calendar, chuỗi ngày liên tiếp (ranh giới tháng, nửa đêm, múi giờ), tổng thời gian | Test | T5 | [ ] |

## TV3 – Quang: Android B (Thư viện, Workout Player)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W3-TV3-01 | Hoàn thiện tồn đọng tuần 2: Workout Player đầy đủ (state machine, bài theo số lần, nghỉ, Pause/Resume/Skip, `SoundPool`, giữ màn sáng, giữ thời gian khi xoay), lưu buổi tập bằng `WorkoutLogRepository.saveWorkout`, màn tổng kết, interstitial sau FINISHED | Player hoàn chỉnh | T3 | [ ] |
| W3-TV3-02 | Độ tin cậy Player: xử lý cuộc gọi đến và mất audio focus, thoát giữa chừng không ghi buổi tập, bài có link GIF hỏng hiển thị ảnh thay thế | Danh sách xử lý | T4 | [ ] |
| W3-TV3-03 | Tính năng khác biệt #2 phía app theo Phụ lục A (mặc định B: thẻ gợi ý theo BMI ở tab Tập luyện, ghi rõ "chỉ là gợi ý"; luật do Agent 1 chốt trong W3-TV4-04). Nếu chọn D hoặc E thì thực hiện tương ứng (yêu thích, ExoPlayer cho bài video) | Logic ViewModel | T4 | [ ] |
| W3-TV3-04 | **Hiệu năng app:** `DiffUtil` cho danh sách, cache ảnh Glide hợp lý, giảm thời gian khởi động nguội, đo bằng Android Studio Profiler (kịch bản P01, P02, P06 trong `REPORT_CONTENT.md`), ghi số đo | Số đo + cải tiến | T5 | [ ] |
| W3-TV3-05 | Unit test Player: chuyển trạng thái, pause giữ thời gian, skip, bài theo số lần, hết bài cuối chuyển FINISHED | Test | T4 | [ ] |

## TV5 – Huân: UI/UX, QA, Content

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W3-TV5-01 | **Nội dung thật:** hoàn thiện `docs/seed/exercises.csv` 30–40 bài đủ 4 nhóm cơ, mỗi bài có link GIF hợp lệ, ghi **nguồn và giấy phép** từng link, kiểm tra link còn sống; giao cho Agent 1 viết migration | `exercises.csv` + bảng nguồn | T3 | [ ] |
| W3-TV5-02 | Test case tuần 3 cho mọi tính năng mới, đăng nhập Google (nếu có), kịch bản offline rồi online, nhiều tài khoản trên một máy | `docs/test/week3.md` | T3 | [ ] |
| W3-TV5-03 | **Kiểm thử hồi quy** toàn bộ test case tuần 1 đến 3 trên **hai cấu hình** (ví dụ emulator API 26 và API 34 trở lên), ghi đạt/không đạt, lỗi theo mức độ | Báo cáo hồi quy + danh sách lỗi | T5 | [ ] |
| W3-TV5-04 | **Kiểm thử hiệu năng:** chạy kịch bản P01 đến P06, dùng `scripts/perf-api.ps1` cho API, ghi bảng số đo, thiết bị và cấu hình máy chủ | `docs/test/performance.md` | T5 | [ ] |
| W3-TV5-05 | **Thử nghiệm người dùng:** 5 người ngoài nhóm dùng app khoảng 10 phút theo kịch bản (đăng ký, chọn bài, tập, xem lịch sử), ghi thời gian hoàn thành, chỗ vướng, ý kiến; tổng hợp | `docs/research/usability.md` | T5 | [ ] |
| W3-TV5-06 | Thiết kế bổ sung: giao diện cho tính năng mới, trạng thái rỗng và lỗi còn thiếu, biểu tượng ứng dụng, ảnh minh họa cho slide | `docs/design/` bản cập nhật | T4 | [ ] |
| W3-TV5-07 | Đóng góp báo cáo đợt 3: kết quả khảo sát đã chốt, kiểm thử chức năng, hiệu năng, thử nghiệm người dùng, biểu đồ đạt/không đạt | `docs/report/contrib/agent5-dot3-*.md` và ảnh | T6 | [ ] |
| W3-TV5-08 | Kịch bản demo nháp (10–12 phút) và **video quay màn hình dự phòng** cho từng chức năng | `docs/DEMO.md` + video | T6 | [ ] |

## Phi - Quang Anh - Huân: Frontend 

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W3-TV6-01 | Hoàn thiện tồn đọng tuần 2: layout Player và tổng kết, Lịch sử, Thống kê, Cài đặt nhắc tập, nối ViewModel của TV2 và TV3 | Màn hình hoàn chỉnh | T3 | [ ] |
| W3-TV6-02 | Giao diện cho tính năng khác biệt #1 và #2 (mặc định: huy hiệu và chuỗi ngày trên Thống kê, thẻ gợi ý theo BMI) | Màn hình tính năng mới | T4 | [ ] |
| W3-TV6-03 | Admin Web: giao diện nhập CSV và nút kiểm tra link, dùng Bootstrap cục bộ khi W3-TV4-07 xong | Template admin | T5 | [ ] |
| W3-TV6-04 | Hoàn thiện trải nghiệm: chế độ tối đúng ở mọi màn, kiểm tra TalkBack (`contentDescription`, vùng chạm 48dp), màn hình lớn và xoay ngang, chuyển cảnh nhẹ | Danh sách đã rà | T5 | [ ] |
| W3-TV6-05 | Đóng góp báo cáo đợt 3 theo `AGENT6_REPORT_TASK.md`: mô tả và ảnh từng màn bản gần cuối, sơ đồ điều hướng, khó khăn và giải pháp | `docs/report/contrib/agent6-dot3-*.md` và ảnh | T6 | [ ] |

## W3-ALL – Việc chung cho cả nhóm

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W3-ALL-01 | **Dùng thử chéo:** mỗi thành viên dùng app thật ít nhất 3 ngày trên máy của mình, ghi lỗi và góp ý vào Google Sheet chung (mức độ, các bước tái hiện, ảnh) | Bảng lỗi chung | T2–T5 | [ ] |
| W3-ALL-02 | Điền bảng phân công đầy đủ vào Google Sheet và gửi Agent 1 link, họ tên, MSSV để đưa vào báo cáo | Link Google Sheet, danh sách thành viên | T3 | [ ] |
| W3-ALL-03 | Mỗi thành viên đọc phần báo cáo liên quan đến việc mình làm và sửa chỗ sai hoặc thiếu | Góp ý ghi vào `BOARD.md` | T6 | [ ] |


