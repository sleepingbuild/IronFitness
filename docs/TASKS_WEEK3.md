# TASKS – Tuần 3: Hoàn thiện tính năng, nội dung thật, tính năng khác biệt và đóng băng tính năng

**Mục tiêu:** (1) hoàn chỉnh mọi tính năng lõi còn dở từ tuần 2; (2) làm **tối đa 2 tính năng khác biệt** rút ra từ khảo sát thị trường; (3) thay dữ liệu thử bằng **30–40 bài tập thật** có link GIF hợp lệ; (4) có môi trường demo chạy ổn định, không phụ thuộc Internet; (5) đo hiệu năng và chạy kiểm thử hồi quy; (6) cập nhật báo cáo đợt 3; (7) **đóng băng tính năng vào cuối tuần** (T6): từ tuần 4 chỉ sửa lỗi và hoàn thiện tài liệu.

**Công nghệ:** Android Java + Room + Retrofit + AdMob; Spring Boot 4.x + MySQL + Flyway; Admin Thymeleaf. Không thêm thư viện ngoài danh sách khi chưa hỏi Agent 1 (ngoại lệ đã duyệt: Credential Manager cho đăng nhập Google, Media3 ExoPlayer; xem `docs/DECISIONS.md`).

Trạng thái: `[ ]` chưa làm, `[~]` đang làm / chờ duyệt, `[x]` xong và đã merge vào `main`.

## Giả định về lịch

Đồ án chạy **4 tuần**: tuần 3 là hoàn thiện và đóng băng, tuần 4 là sửa lỗi, báo cáo và nộp (`docs/TASKS_WEEK4.md`). Nếu hạn nộp thực tế khác, báo Agent 1 để chỉnh lịch (gộp hoặc thêm tuần đệm).

## Phân công thực tế

- **Backend (TV4) do Phi phụ trách toàn bộ.** Chỉ chuyển cho agent khác khi người điều phối yêu cầu.
- Android: TV2 (tài khoản, lịch sử, nhắc tập), TV3 (thư viện, Player), TV6 (giao diện, nếu có). Agent nào chưa hoạt động thì người điều phối báo, Agent 1 nhận phần logic dùng chung; giao diện thuộc TV6.
- TV5 làm nội dung, kiểm thử, khảo sát. Báo cáo do agent viết báo cáo (không viết mã) dựng từ `docs/report/REPORT_CONTENT.md`.

## Việc tồn đọng từ tuần 2 (làm trước)

Mọi task tuần 2 chưa đạt `[x]` trong `docs/TASKS_WEEK2.md` **chuyển sang tuần 3 và được ưu tiên**, giữ nguyên task ID cũ. Agent 1 rà lại ngày T2 và ghi vào `BOARD.md`. Hai nhóm quan trọng nhất: **Workout Player + lưu buổi tập** (W2-TV3-03/04) và **Lịch sử + Thống kê + Nhắc tập** (W2-TV2-03/04/05). Không bắt đầu tính năng mới phụ thuộc vào các phần này khi chúng còn dở.

## Quy trình mỗi task

1. Agent soạn file vào `D:\IronFitnessChanges\<thư-mục-agent>\files\...` (đường dẫn giống repo), ghi `CHANGELOG.md`, báo agent khác qua `BOARD.md` nếu cần.
2. Chạy `apply-agent.bat <thư-mục-agent> agentN/<tên-task>` khi `main` sạch (`git status --short` trống).
3. Build, chạy thử, commit **ngay khi build được**, push nhánh, mở Pull Request vào `main`.
4. **Agent 1 duyệt**, rồi Squash and merge. Sau đó cập nhật cột TT trong file này.

Quy ước thêm cho tuần 3:
- Gói phụ thuộc nhau thì **áp dụng theo thứ tự**, gói sau chỉ áp dụng khi PR gói trước đã merge.
- Tính năng nào làm xong phải có: kịch bản kiểm thử (TV5), ảnh chụp hoặc số đo thật để báo cáo, và dòng ghi vào `docs/report/contrib/`.
- Tính năng chưa xong vào **T5** bị cắt khỏi bản nộp (xem "Quy tắc đóng băng" bên dưới). Không kéo dài.

## Phụ lục A – Chọn tính năng khác biệt (tối đa 2)

Nguồn quyết định: `docs/research/market-report.md` (W2-TV5-05). Agent 1 chọn trong **W3-TV1-02**. Bảng dưới là các ứng viên đã đánh giá công sức, để chọn nhanh khi báo cáo khảo sát về.

| Mã | Tính năng | Giá trị với người dùng | Backend | Android | Admin | Công sức | Rủi ro |
|---|---|---|---|---|---|---|---|
| A | **Thành tích và chuỗi ngày tập** (số ngày tập liên tiếp, huy hiệu mốc 5/10/30 buổi, tổng 7 và 30 ngày) | Giữ động lực, dễ thấy trong demo | `GET /me/stats` (tùy chọn, có thể tính từ Room) | Màn Thống kê mở rộng, huy hiệu | Không | Thấp | Thấp |
| B | **Gợi ý buổi tập theo BMI và mục tiêu** (luật đơn giản: phân loại BMI cho nhóm cơ và mức độ gợi ý) | Cá nhân hóa, khác biệt rõ | Không bắt buộc | Thẻ gợi ý ở tab Tập luyện | Không | Trung bình | Trung bình: cần chọn luật và ghi rõ là gợi ý, không phải tư vấn y tế |
| C | **Chương trình tập theo tuần** (lịch 7 ngày cố định) | Có kế hoạch rõ ràng | Bảng `plans`, `plan_items`, API đọc | Màn chương trình, đánh dấu hoàn thành | CRUD chương trình | Cao | Cao (chạm schema, API, 3 màn) |
| D | **Bài tập yêu thích** | Truy cập nhanh | Bảng `favorites` hoặc chỉ lưu Room | Nút tim, danh sách yêu thích | Không | Thấp | Thấp |
| E | **Video hướng dẫn** (ExoPlayer, bài `mediaType = VIDEO`) | Hướng dẫn động tác rõ hơn | Không (đã có `mediaType`) | Trình phát video | Đã có trường | Trung bình | Trung bình: cần video có quyền sử dụng |

**Mặc định nếu khảo sát chưa kịp để chọn: làm A và B.** C chỉ chọn nếu khảo sát cho thấy nhu cầu rất rõ và còn dư công sức; khi đó bỏ B.

Quy tắc chọn: tối đa 2 tính năng; mỗi tính năng phải trả lời được câu hỏi "khảo sát nói gì ủng hộ điều này" (ghi vào báo cáo mục 6.3); không chọn tính năng cần nội dung hoặc quyền sử dụng mà nhóm chưa có.

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

## Quy tắc đóng băng tính năng (T6)

- Từ T6 đến hết đồ án **không thêm tính năng mới**. Mọi PR chỉ là sửa lỗi, hiệu năng, giao diện nhỏ không đổi logic, hoặc tài liệu.
- Tính năng đánh dấu "nếu kịp" mà chưa `[x]` vào cuối **T5 thì bị cắt** khỏi bản nộp: gỡ nút, gỡ mã chưa hoàn thiện khỏi giao diện, ghi vào "hạn chế đã biết" và "hướng phát triển" của báo cáo.
- Agent 1 công bố danh sách tính năng trong bản nộp ở W3-TV1-09.

## Mốc theo ngày

| Ngày | Việc chính |
|---|---|
| **T2** | Chốt sổ tuần 2; chọn tính năng khác biệt; bật bảo vệ `main`; bắt đầu dùng thử chéo |
| **T3** | Player hoàn chỉnh, Lịch sử và Nhắc tập chạy; nội dung thật vào DB (`V4`); chốt cách demo; bảng phân công đầy đủ |
| **T4** | Tính năng khác biệt #1 chạy (backend và app); công cụ nhập CSV; chỉ mục CSDL; Player đáng tin cậy |
| **T5** | Tính năng #2; đăng nhập Google (nếu kịp); môi trường demo không cần Internet; hồi quy, hiệu năng, thử nghiệm người dùng; rà bảo mật |
| **T6** | Đóng băng tính năng; test xanh; báo cáo đợt 3; kịch bản demo nháp và video dự phòng |

## Tiêu chí hoàn thành tuần 3 (demo T6)

1. Mọi tiêu chí demo tuần 2 vẫn đạt (đăng nhập, thư viện, Player, lịch sử, thống kê, nhắc tập, admin, AdMob).
2. Thư viện có **30–40 bài thật** với GIF hiển thị được; bài thử đã bị ẩn.
3. **Tính năng khác biệt** đã chọn chạy được từ đầu đến cuối và có minh chứng trong khảo sát (hoặc ghi rõ vì sao chọn).
4. Admin nhập được bài tập từ CSV và kiểm tra được link media.
5. Có cách chạy demo **không cần Internet** (Bootstrap cục bộ, script khởi động, tài khoản và lịch sử mẫu).
6. Bảng kết quả hiệu năng (đăng nhập, đọc danh sách, ghi buổi tập, lô 100 buổi, tải song song, khởi động ứng dụng) và bảng số test đạt/không đạt.
7. Kết quả thử nghiệm với 5 người dùng ngoài nhóm.
8. `main` build được (`gradlew assembleDebug`, `mvnw verify`), không có secret, không còn mã demo AdMob; danh sách đóng băng đã công bố.

## Rủi ro tuần 3

- Tồn đọng tuần 2 chiếm hết tuần 3 → rà ngày T2, cắt tính năng khác biệt xuống còn một (A) nếu Player hoặc Lịch sử vẫn dở sau T3.
- Không có link GIF có giấy phép cho 30–40 bài → bắt đầu từ T2; chấp nhận 20 bài thật nếu ghi rõ trong báo cáo; không dùng ảnh không rõ quyền sử dụng.
- Khảo sát chưa xong nên chưa chọn được tính năng → dùng mặc định A và B ở Phụ lục A.
- Đăng nhập Google mất thời gian cấu hình (SHA-1, client ID) → tách thành "nếu kịp", cắt vào T5 theo quy tắc đóng băng.
- Hai tính năng khác biệt kéo theo thay đổi schema muộn → mọi thay đổi `API.md`/`DATABASE.md` phải xong trước T4.
- Demo lỗi vì mất mạng hoặc cổng bị chiếm → script `start-demo.ps1`, video dự phòng, và thử demo trên đúng máy sẽ dùng trước ngày nộp.
