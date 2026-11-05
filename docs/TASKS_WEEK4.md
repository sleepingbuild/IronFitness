# TASKS – Tuần 4 (tuần cuối): Sửa lỗi, hoàn thiện, báo cáo và nộp bài

**Mục tiêu:** giao một sản phẩm **ổn định và đủ minh chứng**: ứng dụng Android và máy chủ chạy đúng, kiểm thử đầy đủ, báo cáo và slide hoàn chỉnh, buổi demo đã tập dượt, gói nộp đầy đủ. **Không thêm tính năng mới.** Mọi công sức dành cho độ tin cậy, chất lượng tài liệu và minh chứng.

**Công nghệ:** như tuần 3. Không thêm thư viện mới.

Trạng thái: `[ ]` chưa làm, `[~]` đang làm / chờ duyệt, `[x]` xong và đã merge vào `main`.

## Giả định về lịch

Tuần 4 là tuần cuối trước hạn nộp (T6 là ngày nộp hoặc ngày dự phòng). Nếu hạn nộp rơi vào ngày khác trong tuần, người điều phối báo Agent 1 để dời các mốc tương ứng. Nếu đồ án còn thêm tuần, thêm tuần đệm giữa tuần 3 và tuần này (dùng để hoàn thành tính năng bị cắt), không kéo dài tuần 3.

## Phân công thực tế

- **Backend (TV4) do Agent 1 phụ trách toàn bộ**, chỉ chuyển cho agent khác khi người điều phối yêu cầu.
- Android: TV2, TV3, TV6 như `docs/TASKS_WEEK3.md`. TV5 kiểm thử và nội dung. Agent viết báo cáo (không viết mã) dựng báo cáo và slide từ `docs/report/REPORT_CONTENT.md` và các file `contrib`.

## Quy tắc tuần cuối

1. **Đóng băng tính năng.** PR chỉ gồm: sửa lỗi, tối ưu hiệu năng, chỉnh giao diện nhỏ không đổi logic, tài liệu. PR thêm tính năng bị từ chối.
2. **Mọi lỗi được phân loại** (Agent 1 chủ trì, T2): *bắt buộc sửa* (crash, mất dữ liệu, sai kết quả, lỗi bảo mật, chức năng trong kịch bản demo hỏng), *nên sửa*, *chấp nhận và ghi vào hạn chế đã biết*.
3. **Mỗi thay đổi sau T4 phải qua chạy lại test** (`gradlew testDebugUnitTest`, `mvnw verify`) và thử lại kịch bản demo liên quan.
4. **Bản phát hành:** ứng viên `v1.0.0-rc1` vào T3, bản chốt `v1.0.0` vào T5 (git tag trên `main`). Sau T5 chỉ sửa lỗi bắt buộc.
5. Không có secret trong Git; mật khẩu admin demo và mật khẩu MySQL chỉ nằm trên máy.

## Quy trình mỗi task

Như `docs/TASKS_WEEK3.md`: nhánh `agentN/<tên-task>`, `apply-agent.bat` khi `main` sạch, build, commit, push, PR, Agent 1 duyệt rồi Squash and merge.

## Việc tồn đọng từ tuần 3

Mọi task tuần 3 chưa `[x]` được xử lý theo quy tắc đóng băng: task thuộc diện "nếu kịp" mà chưa xong **bị cắt** (ghi vào hạn chế đã biết); task lõi chưa xong là **bắt buộc**, làm trước mọi thứ khác và báo ngay `BOARD.md`.

## TV1 – Agent 1: Tech Lead / PM

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV1-01 | Rà tồn đọng tuần 3, merge PR, cập nhật `TASKS_WEEK3.md`; **phân loại toàn bộ lỗi** trong bảng lỗi chung (bắt buộc sửa, nên sửa, chấp nhận) | Danh sách lỗi đã phân loại | T2 | [ ] |
| W4-TV1-02 | Phát hành: tạo tag `v1.0.0-rc1` (T3) rồi `v1.0.0` (T5) trên `main` sau khi mọi test xanh và kịch bản demo chạy được | Tag phát hành | T3, T5 | [ ] |
| W4-TV1-03 | **Đóng gói APK nộp:** `app-debug.apk` bản mới nhất; nếu cần bản release thì tạo keystore của nhóm (**giữ ngoài Git**, ghi nơi cất trong `docs/SUBMISSION.md` mà không ghi mật khẩu) và build `assembleRelease`; kiểm tra cài và chạy trên máy sạch | APK nộp + hướng dẫn cài | T5 | [ ] |
| W4-TV1-04 | **Rà soát cuối kho mã:** quét bí mật trong toàn bộ lịch sử (`git log -p` với từ khóa mật khẩu, `secret`, `token`, `.jks`), `.gitignore`, giấy phép ảnh và media, xóa nhánh đã merge, `README.md` giới thiệu và cách chạy | Checklist kho mã sạch | T4 | [ ] |
| W4-TV1-05 | **Tài liệu cuối:** `README.md`, `docs/SETUP.md`, `docs/API.md`, `docs/DATABASE.md`, `ARCHITECTURE.md`, `docs/DECISIONS.md`, `docs/BACKEND.md` khớp với bản phát hành (đối chiếu từng endpoint và từng bảng với mã thật); ghi mọi hạn chế đã biết vào `docs/KNOWN_ISSUES.md` | Bộ tài liệu khớp mã | T4 | [ ] |
| W4-TV1-06 | **Báo cáo đợt 4:** hoàn tất `REPORT_CONTENT.md` (kết luận, hướng phát triển, số liệu kiểm thử cuối, khó khăn và bài học), rà soát toàn bộ để mọi số liệu và ảnh khớp bản phát hành, gửi agent báo cáo; trả lời các chỗ `[CHỜ]` còn lại | `REPORT_CONTENT.md` bản cuối | T3 (bản gần cuối), T4 (chốt) | [ ] |
| W4-TV1-07 | **Gói nộp bài:** `docs/SUBMISSION.md` liệt kê báo cáo (Word hoặc PDF), slide, link kho mã, link Google Sheet phân công, APK, video demo dự phòng, minh chứng AdMob (ảnh banner Test Ad, interstitial, mã `AdsManager`), người đại diện nộp, hạn và nơi nộp | `docs/SUBMISSION.md` | T4 | [ ] |
| W4-TV1-08 | **Tổng kết nhóm:** bài học làm việc nhóm (nhánh riêng, duyệt PR, hợp đồng API/DB, gói phụ thuộc, lỗi thường gặp) cho Chương 6 của báo cáo | Đoạn nội dung cho báo cáo | T4 | [ ] |
| W4-TV1-09 | Chuẩn bị **câu hỏi giảng viên có thể hỏi** và câu trả lời (xem Phụ lục) | `docs/QA_PREP.md` | T5 | [ ] |
| W4-TV1-10 | Chốt bản nộp cuối: kiểm tra `main` đúng tag `v1.0.0`, tải kho mã về thư mục mới, chạy theo `SETUP.md` từ đầu để chắc người khác chạy được | Xác nhận chạy từ đầu | T5 | [ ] |

## TV4 – Agent 1 (kiêm backend): Backend và Admin Web

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV4-01 | **Sửa lỗi backend** theo danh sách đã phân loại (chỉ lỗi, không tính năng) | PR sửa lỗi | T2–T4 | [ ] |
| W4-TV4-02 | Chạy lại toàn bộ test (đơn vị và tích hợp) và `scripts/perf-api.ps1`; ghi số liệu cuối (số test đạt, thời gian phản hồi) cho báo cáo mục 10 | Số liệu cuối | T4 | [ ] |
| W4-TV4-03 | **Củng cố bản chạy:** tắt devtools khỏi bản chạy demo, log mức INFO, đổi mật khẩu admin mặc định, kiểm tra thời hạn phiên admin, giới hạn kích thước yêu cầu, kiểm tra không còn dữ liệu thử `it-` hoặc `perf-` | Checklist bản chạy | T4 | [ ] |
| W4-TV4-04 | **Sao lưu và khôi phục dữ liệu demo:** `scripts/backup-db.ps1` (`mysqldump`) và `scripts/restore-db.ps1`; thử khôi phục trên cơ sở dữ liệu trống để chắc chắn dựng lại được môi trường demo | Script + xác nhận đã thử | T4 | [ ] |
| W4-TV4-05 | Chuẩn bị máy demo: chạy `scripts/start-demo.ps1` trên đúng máy sẽ trình bày, kiểm tra cổng 8090, tường lửa, địa chỉ LAN cho điện thoại thật; ghi các bước vào `docs/DEMO.md` | Máy demo sẵn sàng | T5 | [ ] |
| W4-TV4-06 | Nếu triển khai lên máy chủ (theo `docs/DEPLOY.md`): kiểm tra từ xa bằng điện thoại, đo độ trễ, có phương án quay về máy cá nhân | Kết quả kiểm tra triển khai | T4 (nếu có) | [ ] |
| W4-TV4-07 | Bản cuối `docs/BACKEND.md`, `docs/API.md`, `docs/DATABASE.md` và hình ERD khớp schema thật (đối chiếu với MySQL) | Tài liệu và hình khớp | T4 | [ ] |

## TV2 – Agent 2: Android A

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV2-01 | Sửa lỗi phần tài khoản, lịch sử, thống kê, nhắc tập theo danh sách đã phân loại | PR sửa lỗi | T2–T4 | [ ] |
| W4-TV2-02 | Dọn mã: gỡ log gỡ lỗi, mã chết, chuỗi thử; kiểm tra quyền trong `AndroidManifest.xml` (chỉ quyền cần thiết); đặt `versionName` 1.0 | PR dọn dẹp | T4 | [ ] |
| W4-TV2-03 | Chạy lại toàn bộ unit test; bổ sung test cho lỗi vừa sửa | Test xanh | T4 | [ ] |

## TV3 – Agent 3: Android B

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV3-01 | Sửa lỗi thư viện và Workout Player theo danh sách đã phân loại; ưu tiên mọi lỗi nằm trong kịch bản demo | PR sửa lỗi | T2–T4 | [ ] |
| W4-TV3-02 | Chốt hiệu năng app (khởi động, danh sách, bộ nhớ khi tập dài), ghi số đo cuối vào `docs/test/performance.md` | Số đo cuối | T4 | [ ] |
| W4-TV3-03 | Dọn mã Player và thư viện như W4-TV2-02; kiểm tra khi thiếu mạng, GIF hỏng, danh sách rỗng | PR dọn dẹp | T4 | [ ] |

## TV5 – Agent 5: UI/UX, QA, Content

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV5-01 | **Chạy lại toàn bộ test case** trên bản `v1.0.0-rc1` và bản chốt; ghi đạt/không đạt từng ca; tổng hợp thành bảng số lượng cho báo cáo mục 10.3 | `docs/test/summary.md` | T3–T5 | [ ] |
| W4-TV5-02 | Kiểm tra lại mọi lỗi đã sửa (xác nhận đóng lỗi) và quét nhanh lỗi mới sau mỗi bản | Danh sách lỗi cập nhật | T2–T5 | [ ] |
| W4-TV5-03 | **Quay video màn hình** toàn bộ kịch bản demo (dự phòng khi mất mạng hoặc lỗi máy) trên bản chốt; chụp **ảnh cuối** mọi màn (sáng và tối) đặt tên theo quy ước | Video + ảnh trong `docs/report/images/` | T5 | [ ] |
| W4-TV5-04 | Đóng góp báo cáo đợt 4: số liệu kiểm thử cuối, hiệu năng cuối, kết quả thử nghiệm người dùng, biểu đồ đạt/không đạt, danh sách lỗi đã sửa | `docs/report/contrib/agent5-dot4-*.md` | T3 | [ ] |
| W4-TV5-05 | Kiểm tra bản nộp: cài APK trên máy sạch, chạy theo kịch bản demo từ đầu đến cuối, kiểm tra các link và ảnh trong báo cáo hiển thị đúng | Xác nhận bản nộp | T5 | [ ] |

## TV6 – Agent 6: Frontend (nếu nhóm dùng Agent 6)

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV6-01 | Sửa lỗi giao diện theo danh sách đã phân loại (lệch bố cục, chế độ tối, chuỗi sai chính tả, vùng chạm nhỏ) | PR sửa lỗi | T2–T4 | [ ] |
| W4-TV6-02 | **Đóng góp báo cáo đợt 4** theo `AGENT6_REPORT_TASK.md`: ảnh và mô tả bản cuối mọi màn app và admin, sơ đồ điều hướng cuối, **khó khăn và giải pháp**, **hướng phát triển giao diện** | `docs/report/contrib/agent6-dot4-*.md` và ảnh | T3 | [ ] |
| W4-TV6-03 | Rà lại toàn bộ chuỗi tiếng Việt (chính tả, nhất quán thuật ngữ) và `contentDescription` | PR chỉnh chuỗi | T4 | [ ] |

## W4-ALL – Việc chung cho cả nhóm

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-ALL-01 | **Tập dượt demo lần 1** (T4) và **lần 2 trọn vẹn có bấm giờ** (T5): mỗi người nắm phần mình, chuẩn bị chuyển cảnh giữa người trình bày | Kịch bản và phân vai demo | T4, T5 | [ ] |
| W4-ALL-02 | Hoàn tất Google Sheet phân công (người, việc, tỷ lệ đóng góp) và gửi link cho Agent 1 | Sheet cuối | T3 | [ ] |
| W4-ALL-03 | **Đọc toàn văn báo cáo** trước khi nộp, mỗi người ghi lỗi chính tả, số liệu sai, ảnh sai vào `BOARD.md`; chỉ sửa theo danh sách, không sửa tự do | Danh sách chỉnh sửa | T4 | [ ] |
| W4-ALL-04 | **Nộp bài** (người đại diện) theo `docs/SUBMISSION.md`; chụp ảnh xác nhận nộp thành công | Xác nhận nộp | T5 hoặc T6 | [ ] |

## Phụ lục – Câu hỏi giảng viên có thể hỏi (chuẩn bị trả lời)

| Câu hỏi | Gợi ý trả lời và nơi có minh chứng |
|---|---|
| Vì sao chọn AdMob mà không phải mua hàng trong ứng dụng? | ADR 8: không cần tài khoản trả phí, chạy được bằng ID thử nghiệm, không cần xác thực biên lai; `docs/ADS.md` |
| Quảng cáo đặt ở đâu và vì sao? | Banner ở Danh mục, Danh sách bài, Lịch sử; interstitial sau khi tập xong (tối đa 1 lần/3 phút); không quảng cáo trong lúc tập; Premium không thấy quảng cáo; `ADS.md` mục 2 |
| Thế nào là offline-first? | Room là nguồn dữ liệu cho giao diện; buổi tập ghi Room trước (`synced=false`) rồi đồng bộ; `ARCHITECTURE.md` 2.3 |
| Làm sao tránh ghi trùng buổi tập khi đồng bộ lại? | `clientUuid` + ràng buộc UNIQUE (`user_id`, `client_uuid`); API trả 201 hoặc 200; test tích hợp |
| Bảo mật được xử lý thế nào? | BCrypt, JWT + refresh token xoay vòng chỉ lưu băm SHA-256, CSRF cho admin, dữ liệu luôn lọc theo userId trong token, không secret trong Git |
| Vì sao một backend cho cả app và admin? | ADR 1: cùng ngôn ngữ, ít thành phần phải vận hành |
| Ứng dụng khác gì các app tập tại nhà đã có? | Báo cáo mục 6.3 với minh chứng từ khảo sát, kiểm thử offline, đồng bộ |
| Khảo sát thị trường cho kết quả gì và dẫn tới quyết định nào? | `docs/research/market-report.md`, hai tính năng đã chọn |
| Kết quả kiểm thử và hiệu năng? | `docs/test/summary.md`, `docs/test/performance.md`, bảng chương 5 |
| Nhóm làm việc thế nào? | Nhánh riêng cho từng vai trò, Pull Request do trưởng nhóm kỹ thuật duyệt, hợp đồng API và CSDL, Google Sheet phân công |
| Hạn chế hiện tại và hướng phát triển? | `docs/KNOWN_ISSUES.md`, báo cáo mục 11: Premium thật, thanh toán, đăng nhập Google nếu bị cắt, mã hóa token bằng Keystore, triển khai đám mây |

## Mốc theo ngày

| Ngày | Việc chính |
|---|---|
| **T2** | Phân loại lỗi; bắt đầu sửa lỗi bắt buộc; gửi đóng góp báo cáo đợt 4 sớm nhất có thể |
| **T3** | Tag `v1.0.0-rc1`; chạy lại toàn bộ test case; đóng góp báo cáo đợt 4 xong; báo cáo bản gần cuối; Sheet phân công hoàn tất |
| **T4** | Dọn mã, rà bí mật, tài liệu cuối, chốt báo cáo; sao lưu dữ liệu demo; tập dượt demo lần 1 |
| **T5** | Tag `v1.0.0`; APK nộp; video dự phòng và ảnh cuối; tập dượt demo lần 2 có bấm giờ; kiểm tra bản nộp từ máy sạch |
| **T6** | Nộp bài; ngày dự phòng, **không làm việc mới** |

## Tiêu chí hoàn thành (bản nộp)

1. Bản `v1.0.0` build được (`gradlew assembleDebug`, `mvnw verify`), mọi test xanh, **không có lỗi bắt buộc sửa còn mở**.
2. Kịch bản demo (10–12 phút) chạy được từ đầu đến cuối trên máy sẽ trình bày, có video dự phòng.
3. Chạy được từ đầu theo `docs/SETUP.md` trên một máy khác (hoặc thư mục sạch).
4. **Báo cáo** đủ mọi phần giảng viên yêu cầu: bìa với họ tên và MSSV, mục lục, lời nói đầu, bảng phân công kèm link Google Sheet, công nghệ, chương 1 đến 6, và **slide**. Mọi số liệu, ảnh và sơ đồ khớp bản phát hành; không còn `[CHỜ]`.
5. **Minh chứng AdMob** có trong báo cáo (banner, interstitial, Premium tắt quảng cáo) và trong mã (`AdsManager`).
6. Có APK nộp, tag `v1.0.0`, `docs/SUBMISSION.md` đầy đủ, `docs/KNOWN_ISSUES.md` trung thực.
7. Kho mã sạch: không secret, không mã demo, không file tạm; tài liệu khớp mã.
8. Đã nộp và có xác nhận nộp.

## Rủi ro tuần 4

- Lỗi nghiêm trọng xuất hiện sát ngày nộp → sửa chỉ lỗi bắt buộc, chạy lại test liên quan; có sẵn bản `rc1` và video dự phòng để quay về; không sửa theo kiểu "tiện tay" sau T5.
- Báo cáo bị dồn lại cuối tuần → mỗi vai trò gửi đóng góp **trước T3** (W4-TV5-04, W4-TV6-02); chốt nội dung ngày T4, chỉ chỉnh lỗi chính tả sau đó.
- Demo hỏng vì máy chủ hoặc mạng → máy demo đã thử trước (W4-TV4-05), bản sao lưu dữ liệu (W4-TV4-04), video dự phòng (W4-TV5-03); điện thoại thật cần cùng Wi-Fi và cổng 8090 mở.
- Sai khác giữa báo cáo và sản phẩm (số liệu, ảnh cũ) → W4-TV1-06 đối chiếu từng mục với bản `v1.0.0`; ảnh chụp lại trên bản chốt.
- Nộp thiếu thành phần hoặc sai định dạng → `docs/SUBMISSION.md` có checklist; nộp thử file trước giờ chót nếu hệ thống cho phép.
- Không còn thời gian cho tính năng "nếu kịp" → đã cắt từ tuần 3; ghi vào hạn chế đã biết và hướng phát triển, không đưa vào bản nộp.
