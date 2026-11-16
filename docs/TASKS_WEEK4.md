# TASKS – Tuần 4 (tuần cuối): Sửa lỗi, hoàn thiện, báo cáo và nộp bài
## TV1 – Phi: Tech Lead / PM

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

## Quang Anh - Phi: Backend và Admin Web

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV4-01 | **Sửa lỗi backend** theo danh sách đã phân loại (chỉ lỗi, không tính năng) | PR sửa lỗi | T2–T4 | [ ] |
| W4-TV4-02 | Chạy lại toàn bộ test (đơn vị và tích hợp) và `scripts/perf-api.ps1`; ghi số liệu cuối (số test đạt, thời gian phản hồi) cho báo cáo mục 10 | Số liệu cuối | T4 | [ ] |
| W4-TV4-03 | **Củng cố bản chạy:** tắt devtools khỏi bản chạy demo, log mức INFO, đổi mật khẩu admin mặc định, kiểm tra thời hạn phiên admin, giới hạn kích thước yêu cầu, kiểm tra không còn dữ liệu thử `it-` hoặc `perf-` | Checklist bản chạy | T4 | [ ] |
| W4-TV4-04 | **Sao lưu và khôi phục dữ liệu demo:** `scripts/backup-db.ps1` (`mysqldump`) và `scripts/restore-db.ps1`; thử khôi phục trên cơ sở dữ liệu trống để chắc chắn dựng lại được môi trường demo | Script + xác nhận đã thử | T4 | [ ] |
| W4-TV4-05 | Chuẩn bị máy demo: chạy `scripts/start-demo.ps1` trên đúng máy sẽ trình bày, kiểm tra cổng 8090, tường lửa, địa chỉ LAN cho điện thoại thật; ghi các bước vào `docs/DEMO.md` | Máy demo sẵn sàng | T5 | [ ] |
| W4-TV4-06 | Nếu triển khai lên máy chủ (theo `docs/DEPLOY.md`): kiểm tra từ xa bằng điện thoại, đo độ trễ, có phương án quay về máy cá nhân | Kết quả kiểm tra triển khai | T4 (nếu có) | [ ] |
| W4-TV4-07 | Bản cuối `docs/BACKEND.md`, `docs/API.md`, `docs/DATABASE.md` và hình ERD khớp schema thật (đối chiếu với MySQL) | Tài liệu và hình khớp | T4 | [ ] |

## TV2 – Kiệt: Android A

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV2-01 | Sửa lỗi phần tài khoản, lịch sử, thống kê, nhắc tập theo danh sách đã phân loại | PR sửa lỗi | T2–T4 | [ ] |
| W4-TV2-02 | Dọn mã: gỡ log gỡ lỗi, mã chết, chuỗi thử; kiểm tra quyền trong `AndroidManifest.xml` (chỉ quyền cần thiết); đặt `versionName` 1.0 | PR dọn dẹp | T4 | [ ] |
| W4-TV2-03 | Chạy lại toàn bộ unit test; bổ sung test cho lỗi vừa sửa | Test xanh | T4 | [ ] |

## TV3 – Quang: Android B

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV3-01 | Sửa lỗi thư viện và Workout Player theo danh sách đã phân loại; ưu tiên mọi lỗi nằm trong kịch bản demo | PR sửa lỗi | T2–T4 | [ ] |
| W4-TV3-02 | Chốt hiệu năng app (khởi động, danh sách, bộ nhớ khi tập dài), ghi số đo cuối vào `docs/test/performance.md` | Số đo cuối | T4 | [ ] |
| W4-TV3-03 | Dọn mã Player và thư viện như W4-TV2-02; kiểm tra khi thiếu mạng, GIF hỏng, danh sách rỗng | PR dọn dẹp | T4 | [ ] |

## TV5 – Huân: UI/UX, QA, Content

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-TV5-01 | **Chạy lại toàn bộ test case** trên bản `v1.0.0-rc1` và bản chốt; ghi đạt/không đạt từng ca; tổng hợp thành bảng số lượng cho báo cáo mục 10.3 | `docs/test/summary.md` | T3–T5 | [ ] |
| W4-TV5-02 | Kiểm tra lại mọi lỗi đã sửa (xác nhận đóng lỗi) và quét nhanh lỗi mới sau mỗi bản | Danh sách lỗi cập nhật | T2–T5 | [ ] |
| W4-TV5-03 | **Quay video màn hình** toàn bộ kịch bản demo (dự phòng khi mất mạng hoặc lỗi máy) trên bản chốt; chụp **ảnh cuối** mọi màn (sáng và tối) đặt tên theo quy ước | Video + ảnh trong `docs/report/images/` | T5 | [ ] |
| W4-TV5-04 | Đóng góp báo cáo đợt 4: số liệu kiểm thử cuối, hiệu năng cuối, kết quả thử nghiệm người dùng, biểu đồ đạt/không đạt, danh sách lỗi đã sửa | `docs/report/contrib/agent5-dot4-*.md` | T3 | [ ] |
| W4-TV5-05 | Kiểm tra bản nộp: cài APK trên máy sạch, chạy theo kịch bản demo từ đầu đến cuối, kiểm tra các link và ảnh trong báo cáo hiển thị đúng | Xác nhận bản nộp | T5 | [ ] |



## W4-ALL – Việc chung cho cả nhóm

| ID | Việc | Sản phẩm | Hạn | TT |
|---|---|---|---|---|
| W4-ALL-01 | **Tập dượt demo lần 1** (T4) và **lần 2 trọn vẹn có bấm giờ** (T5): mỗi người nắm phần mình, chuẩn bị chuyển cảnh giữa người trình bày | Kịch bản và phân vai demo | T4, T5 | [ ] |
| W4-ALL-02 | Hoàn tất Google Sheet phân công (người, việc, tỷ lệ đóng góp) và gửi link cho Agent 1 | Sheet cuối | T3 | [ ] |
| W4-ALL-03 | **Đọc toàn văn báo cáo** trước khi nộp, mỗi người ghi lỗi chính tả, số liệu sai, ảnh sai vào `BOARD.md`; chỉ sửa theo danh sách, không sửa tự do | Danh sách chỉnh sửa | T4 | [ ] |
| W4-ALL-04 | **Nộp bài** (người đại diện) theo `docs/SUBMISSION.md`; chụp ảnh xác nhận nộp thành công | Xác nhận nộp | T5 hoặc T6 | [ ] |
