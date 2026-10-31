# TASKS – Tuần 2: Luồng tập hoàn chỉnh, khảo sát thị trường và tính ứng dụng thực tế

**Mục tiêu:** biến khung tuần 1 thành một app **dùng được thật**: người dùng chọn nhóm cơ, tập theo Workout Player đầy đủ, lịch sử và thống kê được lưu và đồng bộ lên server, có nhắc tập hằng ngày, Admin quản lý được bài tập, quảng cáo AdMob hiển thị đúng chỗ. Song song, cả nhóm làm **khảo sát thị trường** để chứng minh app giải quyết nhu cầu thật và để chọn tính năng cho tuần 3–4.

**Công nghệ:** Android Java + Room + Retrofit + AdMob; Spring Boot 4.x + MySQL + Flyway; Admin Thymeleaf. Không thêm thư viện ngoài danh sách khi chưa hỏi Agent 1.

Trạng thái: `[ ]` chưa làm, `[~]` đang làm / chờ duyệt, `[x]` xong và đã merge vào `main`.

## Việc tồn đọng từ tuần 1 (làm trước)

Mọi task tuần 1 chưa đạt `[x]` trong `docs/TASKS_WEEK1.md` **chuyển sang tuần 2 và được ưu tiên trước**, giữ nguyên task ID cũ. Agent 1 rà lại danh sách này ngày T2 và ghi vào `BOARD.md`. Không bắt đầu task tuần 2 phụ thuộc vào một task tuần 1 còn dở nếu chưa báo Agent 1.

## Quy trình mỗi task

1. Agent soạn file vào `D:\IronFitnessChanges\<thư-mục-agent>\files\...` (đường dẫn giống repo), ghi `CHANGELOG.md`, báo agent khác qua `BOARD.md` nếu cần.
2. Chạy `apply-agent.bat <thư-mục-agent> agentN/<tên-task>`: tạo nhánh từ `main` và chép file vào repo.
3. Build, chạy thử, commit **ngay khi build được**, push nhánh, mở Pull Request vào `main`.
4. **Agent 1 (PM) duyệt**, rồi Squash and merge. Sau đó cập nhật cột TT trong file này.

Quy ước thêm cho tuần 2:
- Mỗi tính năng Android dùng **file chuỗi riêng** (`strings_auth.xml`, `strings_library.xml`, `strings_player.xml`, `strings_history.xml`, `strings_settings.xml`) để tránh xung đột khi nhiều agent cùng sửa `strings.xml`.
- Giao diện kiểm tra đủ ba trạng thái: đang tải, rỗng, lỗi (dùng `Resource<T>` và `ErrorMessages`).
- Mỗi PR có ảnh chụp màn hình khi thay đổi giao diện.

## Ghi chú về Agent 6 (Frontend, nếu được thêm vào nhóm)

Nếu nhóm dùng thêm **Agent 6 – Frontend** (xem `AGENT6_FRONTEND.md`), phần **giao diện** của TV2 và TV3 (layout XML, Fragment hiển thị, adapter) chuyển sang TV6; TV2/TV3 giữ **ViewModel, logic, thông báo, timer** và phải giao ViewModel (danh sách LiveData và hàm công khai) **trước** để TV6 nối vào. Các dòng đánh dấu `[UI]` bên dưới là phần chuyển cho TV6. Nếu không có Agent 6, TV2/TV3 làm cả hai phần như tuần 1.

## TV1 – Agent 1: Tech Lead / PM

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W2-TV1-01 | Rà tồn đọng tuần 1, merge các PR đã xong, cập nhật `TASKS_WEEK1.md`, bật bảo vệ nhánh `main` (W1-TV1-07) | Tuần 1 chốt sổ | T2 | [ ] |
| W2-TV1-02 | Review PR của các agent (đúng task, đúng phạm vi, khớp API/DB, không secret, có ảnh chụp màn hình) | PR được duyệt | T2–T6 | [ ] |
| W2-TV1-03 | Duyệt bảng hỏi khảo sát của TV5 trước khi phát; duyệt báo cáo thị trường | Bảng hỏi và báo cáo được duyệt | T2, T5 | [ ] |
| W2-TV1-04 | Chốt nơi deploy backend + MySQL: so sánh các gói miễn phí hiện có (giới hạn, độ ổn định, cách kết nối), đề xuất một phương án và phương án dự phòng | `docs/DEPLOY.md` | T5 | [ ] |
| W2-TV1-05 | Chuyển launcher từ `AdsDemoActivity` sang Splash khi TV2/TV6 giao Splash; gỡ màn demo AdMob; cập nhật Manifest | Launcher chính thức | T5 | [ ] |
| W2-TV1-06 | Quyết định dùng Media3 ExoPlayer cho video (dựa trên ghi chú W1-TV3-05) và cách làm Calendar (tự làm, không thêm thư viện) | Quyết định ghi vào `ARCHITECTURE.md` | T3 | [ ] |
| W2-TV1-07 | Dựa trên báo cáo khảo sát, chọn tối đa 2 tính năng mới cho tuần 3–4, cập nhật phạm vi và `docs/API.md`/`docs/DATABASE.md` nếu cần | Backlog tuần 3 (`docs/TASKS_WEEK3.md`, bản nháp) | T6 | [ ] |
| W2-TV1-08 | Chuẩn bị gói nộp bài nhóm: danh sách minh chứng AdMob, kịch bản demo, người đại diện nộp, kiểm tra không có secret trong repo | `docs/SUBMISSION.md` | T6 | [ ] |

## TV4 – Agent 4: Backend và Admin Web (ưu tiên đầu tuần vì Android phụ thuộc)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W2-TV4-01 | Hoàn thiện các việc tuần 1 còn dở: Flyway V1/V2, Auth API, `GET /categories`, `GET /exercises`, `GET/PUT /me` | API cho app | T2–T3 | [ ] |
| W2-TV4-02 | `POST /workout-sessions` idempotent theo `clientUuid` (201 mới, 200 đã có), `POST /workout-sessions/batch`, `GET /workout-sessions?from&to`; UNIQUE (`user_id`, `client_uuid`) | Đồng bộ lịch sử | T3 | [ ] |
| W2-TV4-03 | Admin Web: CRUD bài tập (lọc theo danh mục, `mediaUrl` có xem trước, kiểm tra đúng một trong `durationSec` hoặc `reps`, xóa mềm) | `/admin/exercises` | T4 | [ ] |
| W2-TV4-04 | Admin Web: Dashboard (tổng người dùng, tổng lượt tập), danh sách người dùng với nút bật/tắt Premium (để kiểm tra việc ẩn quảng cáo) | `/admin` dashboard | T5 | [ ] |
| W2-TV4-05 | Google login backend: xác thực ID token, tạo hoặc liên kết user theo email (nếu chưa xong ở tuần 1) | `/auth/google` | T4 | [ ] |
| W2-TV4-06 | Test tích hợp trên MySQL test (schema riêng): đăng ký/đăng nhập/refresh, đồng bộ buổi tập trùng `clientUuid`, dữ liệu người dùng luôn lọc theo user trong token | Test chạy bằng `mvnw verify` | T5 | [ ] |
| W2-TV4-07 | Cập nhật `docs/SETUP.md`: cách chạy backend, biến môi trường, tạo admin đầu tiên, dữ liệu mẫu, để TV5/TV6 và máy khác chạy được | Tài liệu chạy backend | T3 | [ ] |
| W2-TV4-08 | Nhận `docs/seed/exercises.csv` bản mở rộng của TV5, viết `V4__seed_more_exercises.sql` | Bài tập mẫu mở rộng | T5 | [ ] |

Thứ tự: 01 → 02 → 03; 05 và 07 chạy song song.

## TV2 – Agent 2: Android A (Tài khoản, Lịch sử, Nhắc tập)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W2-TV2-01 | Hoàn thiện tồn đọng tuần 1: Splash, Đăng ký, Đăng nhập (Email, Google), Profile và BMI (hiển thị phân loại bằng chuỗi tiếng Việt) | Luồng tài khoản chạy thật | T3 | [ ] |
| W2-TV2-02 | Xử lý hết phiên: khi `Resource` trả `UNAUTHORIZED` hoặc `AuthRepository.isLoggedIn()` sai thì xóa phiên và về màn Đăng nhập; nút Đăng xuất | Đăng xuất và hết phiên | T3 | [ ] |
| W2-TV2-03 | Màn Lịch sử: Calendar tháng **tự làm** (không thêm thư viện), đánh dấu ngày có buổi tập (`getDatesWithWorkout`), chọn ngày xem danh sách buổi `[UI]` | Calendar lịch sử | T5 | [ ] |
| W2-TV2-04 | Thống kê: tổng số buổi và tổng số phút (`getTotalCount`, `getTotalSeconds`) | Màn thống kê | T5 | [ ] |
| W2-TV2-05 | Nhắc tập hằng ngày: màn Cài đặt (bật/tắt, chọn giờ) `[UI]`; `ReminderScheduler` (AlarmManager không chính xác), `ReminderReceiver`, đăng ký lại sau `BOOT_COMPLETED`; xin quyền `POST_NOTIFICATIONS` trên Android 13+ | Nhắc tập chạy được | T6 | [ ] |
| W2-TV2-06 | AdMob (W2-TV2-ADS): gọi `AdsManager.setAdsEnabled(!isPremium)` sau khi có hồ sơ; banner ở màn Lịch sử | Quảng cáo màn Lịch sử | T5 | [ ] |
| W2-TV2-07 | Gọi `WorkoutLogRepository.syncPending()` sau đăng nhập và khi mở app; sau đăng nhập trên máy mới gọi `restoreHistory()` | Đồng bộ lịch sử | T4 | [ ] |
| W2-TV2-08 | Unit test: logic ngày trên Calendar (múi giờ máy, ranh giới tháng) và tính tổng thời gian | Test | T6 | [ ] |

Phụ thuộc: API Auth và `/workout-sessions` từ TV4 (W2-TV4-01, W2-TV4-02). Trước đó dùng dữ liệu giả.

## TV3 – Agent 3: Android B (Thư viện bài tập, Workout Player)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W2-TV3-01 | Hoàn thiện tồn đọng tuần 1: màn Danh mục và Danh sách bài đọc từ Room, `ExerciseRepository.refresh()` khi mở app, hiển thị trạng thái tải/rỗng/lỗi `[UI]` | Thư viện bài tập chạy thật | T3 | [ ] |
| W2-TV3-02 | Màn Chi tiết bài tập: tên, mô tả, GIF (Glide), thời gian hoặc số lần, nút "Bắt đầu buổi tập" cho nhóm cơ đã chọn `[UI]` | Chi tiết bài | T4 | [ ] |
| W2-TV3-03 | Workout Player hoàn chỉnh theo state machine ở `ARCHITECTURE.md` 2.4: đếm ngược từng bài và thời gian nghỉ, Pause/Resume/Skip, bài theo số lần có nút "Xong", `SoundPool` bíp khi chuyển bài và hết giờ, `FLAG_KEEP_SCREEN_ON`, giữ `remainingMs` khi xoay màn hình | Player đầy đủ | T5 | [ ] |
| W2-TV3-04 | Kết thúc buổi: lưu bằng `WorkoutLogRepository.saveWorkout(...)`, màn tổng kết (số bài, tổng thời gian) `[UI]` | Lưu lịch sử khi xong | T5 | [ ] |
| W2-TV3-05 | Video bằng Media3 ExoPlayer cho bài `mediaType = VIDEO` (sau khi Agent 1 duyệt thêm thư viện ở W2-TV1-06) | Phát video | T6 | [ ] |
| W2-TV3-06 | AdMob (W2-TV3-ADS): banner ở màn Danh mục và Danh sách bài; interstitial sau trạng thái FINISHED, **trước** màn tổng kết, bằng `AdsManager.showInterstitialIfReady`; không có quảng cáo trong lúc tập | Quảng cáo đúng vị trí | T6 | [ ] |
| W2-TV3-07 | Unit test logic Player (tách bộ đếm khỏi Android): chuyển trạng thái, pause giữ thời gian, skip, bài theo số lần | Test | T6 | [ ] |

Phụ thuộc: bài tập mẫu mở rộng từ TV5 (W2-TV5-06) và API đọc từ TV4. Trước đó dùng dữ liệu chèn vào Room.

## TV5 – Agent 5: UI/UX, QA, Content và **Khảo sát thị trường**

Khảo sát thị trường là phần **bắt buộc** của tuần 2: giảng viên yêu cầu app có tính ứng dụng thực tế, nên nhóm phải có bằng chứng về nhu cầu người dùng, đối thủ và định hướng tính năng. Toàn nhóm cùng tham gia thu thập phản hồi (mục W2-ALL-01).

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W2-TV5-01 | **Khảo sát đối thủ**: chọn 5–7 app tập tại nhà trên Google Play (ví dụ nhóm app tập không dụng cụ; tự kiểm tra app còn hoạt động). Với mỗi app ghi: tính năng chính, mô hình thu phí (quảng cáo, Premium, mua trong app), điểm mạnh, điểm yếu rút từ đánh giá 1–3 sao của người dùng, đánh giá chung | `docs/research/competitors.md` | T3 | [ ] |
| W2-TV5-02 | **Thiết kế bảng hỏi** (Google Form), 12–15 câu, ẩn danh, không thu số điện thoại. Gồm: nhóm tuổi và nghề nghiệp, thói quen tập hiện tại, lý do không tập đều, thời gian tập mong muốn, thiết bị hay dụng cụ có sẵn, app đã từng dùng và lý do bỏ, tính năng quan trọng nhất (xếp hạng: nhắc tập, lịch sử, gợi ý theo BMI, video hướng dẫn, chương trình tuần), mức sẵn sàng trả phí và chịu quảng cáo. Agent 1 duyệt trước khi phát | `docs/research/questionnaire.md` và link form | T2 | [ ] |
| W2-TV5-03 | **Thu thập phản hồi**: mục tiêu từ 50 phản hồi trở lên, đối tượng sinh viên và người đi làm văn phòng. Tổng hợp dữ liệu thô đã bỏ thông tin nhận dạng | `docs/research/responses.csv` | T4 | [ ] |
| W2-TV5-04 | **Phỏng vấn sâu** 5 người (mỗi người khoảng 15 phút, có sự đồng ý), rút ra 2–3 persona người dùng và hành trình sử dụng | `docs/research/personas.md` | T4 | [ ] |
| W2-TV5-05 | **Báo cáo thị trường**: vấn đề cần giải quyết, người dùng mục tiêu, đối thủ và khoảng trống thị trường, giá trị khác biệt của HomeFit, kết quả khảo sát (có biểu đồ), mô hình doanh thu (AdMob, Premium sau này), kết luận và đề xuất **tối đa 2 tính năng ưu tiên** theo ma trận giá trị và chi phí | `docs/research/market-report.md` | T5 | [ ] |
| W2-TV5-06 | Content: mở rộng lên 30–40 bài đủ 4 nhóm cơ, mỗi bài có GIF host ngoài, kiểm tra link còn sống và quyền sử dụng | `docs/seed/exercises.csv` bản mở rộng | T4 | [ ] |
| W2-TV5-07 | UI/UX: bản thiết kế hoàn chỉnh cho Lịch sử (Calendar), Thống kê, Cài đặt nhắc tập, tổng kết buổi tập và Admin Web (Dashboard, bài tập); bảng màu, cỡ chữ, khoảng cách ghi thành design tokens để TV6 dùng | `docs/design/` và `docs/design/tokens.md` | T3 | [ ] |
| W2-TV5-08 | Test case tuần 2: Player (pause, skip, xoay màn hình), Calendar, nhắc tập, đồng bộ offline rồi online, AdMob (banner, interstitial, Premium tắt quảng cáo) | `docs/test/week2.md` | T4 | [ ] |
| W2-TV5-09 | Test tích hợp cuối tuần trên bản debug với backend thật, báo lỗi theo mức độ | Danh sách lỗi | T6 | [ ] |

Quy tắc khảo sát: tự nguyện, ẩn danh, không thu số điện thoại hoặc email; chỉ dùng dữ liệu cho bài học; ghi rõ nguồn và ngày truy cập khi trích dẫn thông tin đối thủ; nội dung media phải có quyền sử dụng hợp lệ.

## W2-ALL – Việc chung cho cả nhóm

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W2-ALL-01 | Mỗi thành viên gửi bảng hỏi cho ít nhất 10 người ngoài nhóm và nhắc điền; báo số phản hồi đạt được lên `BOARD.md` | Từ 50 phản hồi trở lên | T4 | [ ] |
| W2-ALL-02 | Mỗi thành viên đọc báo cáo thị trường và góp ý một điểm (phản biện, bổ sung hoặc câu hỏi) trước buổi chốt | Góp ý ghi vào `BOARD.md` | T5 | [ ] |

## TV6 – Agent 6: Frontend (chỉ khi nhóm dùng Agent 6, xem `AGENT6_FRONTEND.md`)

Phạm vi: lớp giao diện Android (layout XML, theme, điều hướng, Fragment hiển thị, adapter) và, sau khi xong Android, template Admin Web. Không viết logic nghiệp vụ, không gọi Room/Retrofit trực tiếp.

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W2-TV6-01 | Nền giao diện: theme sáng/tối, `colors.xml`, `styles.xml`, `dimens.xml` theo `docs/design/tokens.md` của TV5; kiểu nút, ô nhập, thẻ dùng chung | Design system dạng XML | T3 | [ ] |
| W2-TV6-02 | `MainActivity` làm host: Navigation Component, `nav_graph.xml` và thanh điều hướng dưới (Tập luyện, Lịch sử, Hồ sơ, Cài đặt) | Khung điều hướng | T3 | [ ] |
| W2-TV6-03 | Màn Splash, Đăng nhập, Đăng ký, Profile/BMI nối vào ViewModel của TV2 | Màn tài khoản | T4 | [ ] |
| W2-TV6-04 | Màn Danh mục (lưới), Danh sách bài, Chi tiết bài nối vào ViewModel của TV3 | Màn thư viện | T4 | [ ] |
| W2-TV6-05 | Màn Workout Player (bộ đếm lớn, nút Pause/Resume/Skip, bài theo số lần, trạng thái nghỉ) và màn tổng kết, nối vào ViewModel của TV3 | Màn Player | T5 | [ ] |
| W2-TV6-06 | Màn Lịch sử (Calendar tháng), Thống kê, Cài đặt nhắc tập, nối vào ViewModel của TV2 | Màn lịch sử và cài đặt | T5 | [ ] |
| W2-TV6-07 | Đặt vùng banner AdMob (`FrameLayout` `adContainer`) đúng vị trí ở `docs/ADS.md` mục 2 và 5 | Layout có chỗ cho quảng cáo | T4 | [ ] |
| W2-TV6-08 | Admin Web: layout chung Thymeleaf + Bootstrap, trang Dashboard và Bài tập theo thiết kế của TV5; dùng thuộc tính model do TV4 cung cấp | Template `/admin` | T6 | [ ] |

Phụ thuộc: design tokens của TV5 (W2-TV5-07) và ViewModel của TV2/TV3. Trước đó dùng dữ liệu giả trong layout (`tools:` attributes).

## Mốc theo ngày

| Ngày | Việc chính |
|---|---|
| **T2** | Chốt sổ tuần 1; duyệt bảng hỏi khảo sát và phát form; TV4 hoàn thiện Auth và API đọc |
| **T3** | API `/workout-sessions` xong; design tokens; nền giao diện và điều hướng; khảo sát đối thủ xong |
| **T4** | Luồng tài khoản và thư viện chạy thật; Admin CRUD bài tập; thu đủ phản hồi khảo sát; bài tập mẫu mở rộng |
| **T5** | Player hoàn chỉnh; Calendar và thống kê; báo cáo thị trường; chốt nơi deploy |
| **T6** | Nhắc tập; AdMob đúng vị trí; test tích hợp; demo nội bộ end-to-end; chốt backlog tuần 3 |

## Tiêu chí hoàn thành tuần 2 (demo T6)

1. Đăng nhập, chọn một nhóm cơ, xem danh sách bài lấy từ server (tắt mạng vẫn xem được từ Room).
2. Bắt đầu buổi tập: Player đếm ngược từng bài và thời gian nghỉ, Pause/Resume/Skip hoạt động, bài theo số lần có nút "Xong", xoay màn hình không mất tiến trình.
3. Hoàn thành buổi tập: lịch sử lưu ngay vào Room, đồng bộ lên server khi có mạng và **không bị trùng** khi gửi lại.
4. Màn Lịch sử đánh dấu đúng ngày vừa tập; thống kê tổng số buổi và tổng số phút đúng.
5. Nhắc tập hằng ngày hiện thông báo vào giờ đã đặt (kể cả sau khi khởi động lại máy).
6. Admin thêm/sửa/xóa mềm bài tập; thay đổi hiện ra trên app sau khi làm mới. Dashboard hiện tổng người dùng và tổng lượt tập.
7. AdMob: banner ở Danh mục, Danh sách bài và Lịch sử; interstitial sau khi hoàn thành buổi tập (tối đa một lần mỗi 3 phút); người dùng Premium không thấy quảng cáo; không có quảng cáo trong lúc tập.
8. Có đủ tài liệu khảo sát: đối thủ, bảng hỏi, từ 50 phản hồi, persona, báo cáo thị trường với đề xuất tối đa 2 tính năng cho tuần 3–4.
9. `main` build được (`gradlew assembleDebug`, `mvnw verify`), không có secret trong repo.

## Rủi ro tuần 2

- Tồn đọng tuần 1 làm trễ cả tuần → TV1 rà ngay T2 và chuyển việc cho agent còn rảnh; TV2/TV3 vẫn dùng dữ liệu giả, không chờ.
- Không đủ 50 phản hồi → bắt đầu phát form từ T2, mỗi thành viên có chỉ tiêu riêng (W2-ALL-01); chấp nhận từ 30 phản hồi nếu giải thích rõ trong báo cáo.
- Quảng cáo thật chưa hiện (báo "No fill") → dùng ID thử nghiệm khi demo và nộp bài (xem `docs/ADS.md`); không bấm vào quảng cáo thật của chính mình.
- Nhắc tập không chạy trên một số máy do tiết kiệm pin của hãng → ghi vào test case của TV5, hướng dẫn tắt tối ưu pin khi demo.
- Hai agent cùng sửa một file giao diện → mỗi màn chỉ có một chủ sở hữu (bảng phạm vi ở `AGENT6_FRONTEND.md`), chuỗi để trong file `strings_<tính-năng>.xml` riêng.
- Link GIF chết hoặc chậm → TV5 kiểm tra lại link trước khi đưa vào seed; Glide hiển thị ảnh thay thế khi lỗi.
