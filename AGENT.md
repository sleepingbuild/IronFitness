# AGENT.md – Hướng dẫn cho AI agent và thành viên mới

Đọc file này **trước khi làm bất kỳ việc gì**. Sau đó đọc theo thứ tự: `README.md` → `ARCHITECTURE.md` → `docs/DATABASE.md` → `docs/API.md` → `docs/TASKS_WEEK1.md` (hoặc file tuần hiện tại).

## 1. Dự án là gì

HomeFit: app Android luyện tập tại nhà (Java) + backend Spring Boot (Java) + MySQL + Admin Web (Thymeleaf). MVP gồm tài khoản/BMI, thư viện bài tập, Workout Player, lịch sử tập và nhắc tập; Admin quản lý bài tập và xem thống kê cơ bản.

## 2. Công nghệ cố định (không tự ý đổi)

- Android: **Java** (không Kotlin), XML + ViewBinding, MVVM, Room, Retrofit, Glide, Media3 ExoPlayer, Navigation Component.
- Backend: Java 17, Spring Boot 4.x (bản ổn định mới nhất), Spring Security + JWT, Spring Data JPA, Flyway, MySQL 8, Thymeleaf.
- Không dùng Firebase. Không đưa đăng nhập SĐT/OTP vào MVP.
- Media bài tập lưu bằng **URL**, backend không lưu file.

## 3. Các nguồn sự thật

| Việc | File quyết định |
|---|---|
| Schema MySQL / Room | `docs/DATABASE.md` |
| Hợp đồng API | `docs/API.md` |
| Kiến trúc, cấu trúc package | `ARCHITECTURE.md` |
| Việc cần làm, người nhận | `docs/TASKS_WEEK*.md` |
| Quy tắc Git | `CONTRIBUTING.md` |

Nếu code mâu thuẫn tài liệu: **dừng lại, hỏi Tech Lead (TV1)**, không tự chọn bên nào. Thay đổi hợp đồng (API, schema) thì **sửa tài liệu trong cùng PR**.

## 4. Quy tắc làm việc

1. Chỉ làm task được giao; ghi rõ task ID (ví dụ `W1-TV3-02`) trong commit và PR.
2. Chỉ sửa trong phạm vi thư mục của vai trò mình (xem bảng bên dưới). Cần sửa chỗ khác thì báo chủ sở hữu.
3. Làm nhỏ, commit thường xuyên, PR nhỏ dễ review.
4. Không commit secret (`application-local.yml`, `local.properties`, keystore, JWT secret, Google client secret).
5. Không dùng `ddl-auto=update`; không `fallbackToDestructiveMigration` ở release.
6. Không thêm thư viện mới khi chưa hỏi TV1.
7. Mọi tính năng mới phải chạy được và có ít nhất cách kiểm tra thủ công ghi trong PR.
8. Không bịa endpoint hay trường dữ liệu: dùng đúng `docs/API.md`.

### Phạm vi thư mục theo vai trò

| Vai trò | Thư mục chính |
|---|---|
| TV1 Tech Lead | toàn repo (review), `docs/`, cấu hình CI |
| TV2 Android A | `android/.../ui/auth`, `ui/profile`, `ui/history`, `notification`, `data/prefs` |
| TV3 Android B | `android/.../ui/library`, `ui/player`, `data/local` (phần bài tập) |
| TV4 Backend | `backend/` |
| TV5 UI/QA/Content | `docs/design`, `docs/test`, dữ liệu seed, tài nguyên `res/` (icon, âm thanh) |

Phần dùng chung trong Android (`data/remote`, `data/repository`, `util`) do **TV1 giữ cấu trúc**, TV2/TV3 thêm file riêng và không sửa file của nhau nếu chưa báo.

## 5. Quy ước code

**Android**
- Package gốc `com.homefit.app`. Activity/Fragment không gọi trực tiếp Room/Retrofit; phải qua ViewModel → Repository.
- Việc nặng (DB, mạng) chạy ngoài main thread (Executor / Retrofit callback / Room async).
- Chuỗi hiển thị đặt trong `strings.xml` (tiếng Việt), không hard-code.
- Tên lớp: `XxxFragment`, `XxxViewModel`, `XxxRepository`, `XxxDao`, `XxxEntity`, `XxxDto`.
- Layout: `fragment_xxx.xml`, `item_xxx.xml`, `activity_xxx.xml`.

**Backend**
- Package gốc `com.homefit.server`. Controller mỏng, logic ở Service.
- DTO tách khỏi Entity; dùng Bean Validation (`@Valid`).
- Lỗi trả theo định dạng trong `docs/API.md` qua `GlobalExceptionHandler`.
- Mọi thay đổi schema = file Flyway mới `V{n}__mo_ta.sql` (không sửa migration đã merge).
- Truy vấn dữ liệu người dùng luôn lọc theo user đang đăng nhập.

## 6. Định nghĩa "xong" (Definition of Done)

- Build thành công (Android: `./gradlew assembleDebug`; Backend: `./mvnw verify`).
- Chạy thử đúng luồng chính, không crash khi xoay màn hình, mất mạng, hoặc dữ liệu rỗng.
- Tài liệu liên quan đã cập nhật.
- PR được Agent 1 (PM) duyệt trước khi merge vào `main`.
- Đánh dấu hoàn thành trong file tasks.

## 7. Điều agent KHÔNG được làm

- Không xóa/ghi đè file của người khác khi chưa được yêu cầu.
- Không sửa trực tiếp repo, không push, không merge vào `main` (xem mục 10).
- Không thay đổi công nghệ chốt ở mục 2.
- Không thêm tính năng ngoài MVP (lịch Premium, thanh toán, SĐT/OTP…) khi chưa có task.
- Không đặt mật khẩu hay khóa thật trong code hoặc tài liệu.
- Không viết test chỉ để pass (mock mọi thứ) mà không kiểm tra hành vi thực.

## 8. Trạng thái hiện tại (cập nhật mỗi khi xong một mốc)

**Giai đoạn:** Tuần 1 – Setup và nền tảng

- [ ] Repo và cấu trúc thư mục (TV1)
- [ ] Project Android khởi tạo, có Room + Retrofit khung (TV1/TV2/TV3)
- [ ] Backend khởi tạo, MySQL, Flyway V1 (TV4)
- [ ] API Auth hoạt động (TV4)
- [ ] Đăng ký/đăng nhập trên app (TV2)
- [ ] Màn danh mục và danh sách bài tập, đọc từ Room (TV3)
- [ ] Admin CRUD danh mục (TV4)
- [ ] Wireframe và dữ liệu bài tập mẫu (TV5)

**Quyết định mở / cần hỏi:**
- Nơi deploy backend + MySQL (chốt tuần 3–4).
- Thư viện lịch (Calendar View): tự làm hay dùng thư viện (TV2 đề xuất, TV1 duyệt).

## 9. Khi bị kẹt

Ghi rõ: đang làm task nào, đã thử gì, lỗi gì (log), rồi hỏi TV1. Không đoán mò và không thay đổi tài liệu hợp đồng để "cho chạy được".

## 10. Cách làm việc: thư mục Changes (ngoài repo) và nhánh Git

Có **hai nơi tách biệt**:

| Nơi | Đường dẫn | Chứa gì | Push lên Git? |
|---|---|---|---|
| Repo dự án | `D:\IronFitness` | Chỉ code và tài liệu **đã được duyệt** | Có |
| Khu vực làm việc của agent | `D:\IronFitnessChanges` | File agent soạn, ghi chú, thông báo cho nhau | **Không** |

Mỗi agent có một thư mục riêng trong `D:\IronFitnessChanges\`:

| Agent | Vai trò | Thư mục |
|---|---|---|
| Agent 1 | TV1 Tech Lead / PM (duyệt và merge) | `agent1-techlead` |
| Agent 2 | TV2 Android A | `agent2-android-account` |
| Agent 3 | TV3 Android B | `agent3-android-workout` |
| Agent 4 | TV4 Backend + Admin | `agent4-backend` |
| Agent 5 | TV5 UI/QA/Content | `agent5-ui-qa-content` |

**Quy trình:**
1. Agent chỉ ghi vào thư mục của mình: file thay đổi đặt ở `<thư-mục-agent>\files\<đường-dẫn-giống-repo>`, giao file **đầy đủ nội dung**.
2. Sau mỗi lượt giao, thêm mục mới (trên cùng) vào `CHANGELOG.md` của mình: task ID, danh sách file, cách kiểm tra, phụ thuộc.
3. Muốn báo cho agent khác (câu hỏi, thay đổi API, phụ thuộc), ghi vào `D:\IronFitnessChanges\BOARD.md`.
4. Người điều phối chạy `apply-agent.bat`: tạo **nhánh riêng** `agentN/<tên-task>` từ `main`, chép file vào repo.
5. Build, chạy thử, commit, push nhánh đó, mở Pull Request vào `main`.
6. **Agent 1 (PM) review PR**. Chỉ khi Agent 1 duyệt mới merge vào `main`. Agent khác không tự merge.
7. Sau khi merge, mọi người `git pull` trên `main` để lấy bản mới nhất trước khi làm tiếp.

Agent 1 duyệt theo checklist: đúng task và phạm vi vai trò, khớp `docs/API.md` và `docs/DATABASE.md`, build được, không có secret, tài liệu liên quan đã cập nhật.

Chi tiết nằm ở `D:\IronFitnessChanges\README.md`; quy tắc Git ở `CONTRIBUTING.md`; khởi tạo ở `docs/SETUP.md`.
