# HANDOFF ĐẦY ĐỦ — GMS (Agent 4) + CI/CD Jenkins

Cập nhật: 2026-10-21. File này đủ để bắt đầu làm việc ở phiên mới mà không cần đọc lại lịch sử chat.
Đính kèm file này ở tin nhắn đầu tiên của phiên mới, kèm file `Jenkinsfile` nếu còn.

---

## 0. Bắt đầu phiên mới như thế nào

1. Đọc mục 9 (việc còn lại) và mục 11 (CI/CD) trước, vì hai phần này còn việc dang dở.
2. Hỏi người dùng **đang ở bước nào**, và nhờ dán kết quả các lệnh kiểm tra trạng thái ở mục 9.1. Không giả định trạng thái Git/Jenkins đã đổi hay chưa đổi kể từ ngày 2026-10-18.
3. Nếu người dùng dán log hoặc ảnh chụp, phân tích log thật trước khi đề xuất bước tiếp theo.

---

## 1. Người dùng và cách làm việc

- Người dùng vừa là **Agent 4** (Face Attendance & Staff Attendance) vừa là **Phi** (Product Owner/reviewer, người chốt các quyết định thiết kế). Khi tài liệu của agent khác ghi "chờ Phi xác nhận", đó là người dùng.
- Nói và trả lời **tiếng Việt**.
- Thích hướng dẫn **từng bước, kèm lệnh PowerShell cụ thể** và kết quả mong đợi. Người dùng dán log thật để phân tích.
- Hay cần **file `.md`** (báo cáo, phản hồi, thông báo cho agent khác) và **file `.zip`** chứa code để chép vào repo.
- Nói rõ **giả định chưa kiểm chứng**, dừng lại hỏi khi thiếu dữ kiện, không đoán. Nhiều lần đoán sai đã tốn công (xem mục 13).
- Từng bị sai khi đoán "chưa có PR": luôn kiểm tra trên GitHub/Git trước khi kết luận.
- Sandbox của Claude có thể reset giữa các lượt (`/home/claude` mất). File gửi người dùng nằm ở `/mnt/user-data/outputs/`. Khi cần sửa code đã gửi, giải nén lại từ zip trong outputs hoặc viết lại từ mô tả ở file này.

---

## 2. Môi trường máy của người dùng (Windows, PowerShell)

| Thứ | Giá trị |
|---|---|
| Thư mục gốc | `D:\SrpingBoot-GYM\` (tên có chữ "Srping", đúng như thật) |
| Worktree Agent 4 | `D:\SrpingBoot-GYM\gms-agent4`, nhánh `feature/agent4-face-attendance` |
| Worktree các agent khác | `gms-agent1` … `gms-agent6` cùng cấp |
| Thư mục chỉ để pull `master` | `D:\SrpingBoot-GYM\gym-management-system` (không code ở đây) |
| Worktree dự kiến cho CI | `D:\SrpingBoot-GYM\gms-ci`, nhánh `ci/jenkins-pipeline` (chưa tạo) |
| Thư mục tải zip/md | `D:\AgentChange\Agent4\` |
| Jenkins | `C:\jenkins\jenkins.war` |
| JDK 17 | `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot` |
| JDK 21 | chưa biết đã cài chưa (xem mục 11) |
| Docker containers | `gms-sqlserver` (cổng 1433, healthy), `gms-redis` (6379), `gms-postgres` (5432, **dự án khác**, bỏ qua) |
| Maven | có lệnh `mvn` dùng được (build đã chạy nhiều lần) |
| ngrok | đã tải, chưa rõ đã gắn authtoken chưa |

**SQL Server:** database `gms_db` trong container. Kết nối SSMS: server `localhost,1433` (dấu phẩy), SQL Server Authentication, user `sa`, mật khẩu trong `docker-compose.yml`, tick Trust server certificate. Máy còn một SQL Server khác `localhost\MSSQLSERVER01` (có `CustomerDB`, `GymManagementSystemDB`), **đừng nhầm**. Tại thời điểm kiểm tra, `gms_db` có 9 bảng (`branches`, `flyway_schema_history`, `member_packages`, `packages`, `payment_transactions`, `roles`, `user_profiles`, `user_roles`, `users`). Bảng V3/V4 chỉ có sau khi chạy app với migration đó.

Lệnh kiểm tra DB không cần SSMS:
```powershell
docker ps -a --filter "name=gms"
docker exec -it gms-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "<mật khẩu trong docker-compose.yml>" -C -d gms_db -Q "SELECT name FROM sys.tables ORDER BY name"
docker exec -it gms-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "<mật khẩu>" -C -d gms_db -Q "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank"
```

---

## 3. Dự án GMS

- Hệ thống quản lý phòng gym. Backend **Spring Boot 3.3.4, Java 17, Maven**, Spring Data JPA, Spring Security + JWT, SQL Server 2022, Redis (JWT blacklist, chống quét đúp), Flyway, springdoc. Khoá chính UUID (`UNIQUEIDENTIFIER`), `BaseEntity` có `id`, `createdAt`, `updatedAt` (`LocalDateTime`, auditing).
- `application-local.yml` đặt `ddl-auto: validate`, Flyway quản lý schema. Cổng app 8080.
- Repo: `https://github.com/sleepingbuild/SpringBoot-GYM-MANAGEMENT-SYSTEM`, nhánh chính **`master`**. Người dùng là Owner repo.
- 6 agent làm song song:

| Agent | Phạm vi |
|---|---|
| 1 | Nền tảng, Auth/JWT/RBAC, Profile, file SHARED (`User`, `Role`, `BaseEntity`, `security/*`, `exception/*`, `pom.xml`, `application*.yml`), review PR |
| 2 | Membership, Package, Payment (state machine gói tập, `consumeSession`) |
| 3 | Lịch PT và Booking (`BookingService`, lazy status, `V3`) |
| **4** | **Face Attendance & Staff Attendance (`V4`)** |
| 5 | POS và lương PT theo buổi (`V5`) |
| 6 | Leads/CRM, báo cáo (`V6`) |

- 5 role: `SUPER_ADMIN`, `RECEPTIONIST`, `TRAINER`, `SALES`, `MEMBER` (in hoa, không tiền tố `ROLE_` trong DB). Mỗi người dùng giả định đúng 1 role hiệu lực.
- Quy ước: không `git add .`; không tự sửa file SHARED; migration đã merge không được sửa (chưa merge thì sửa trực tiếp); mọi response qua `ApiResponse<T>`; lỗi ném `BusinessException(ErrorCode, message)`; `CurrentUserId.get()` và `CurrentUserRole.get()` (không có `ROLE_`) lấy từ SecurityContext.

---

## 4. Git: trạng thái và quy trình

**PR của Agent 4: #43** (`feature/agent4-face-attendance` → `master`), đang mở. Commit đã push (mới nhất ở trên):
```
d3a29e1 feat(face-attendance): Sales cham cong khuon mat, test FaceProfileService, Swagger cho controller
8153200 feat(face-attendance): PT quet mat theo buoi qua recordTrainerFaceScan, dedup rieng cho PT
6944654 fix(face-attendance): bo sung cot updated_at cho staff_attendances
4cf12cd feat(face-attendance): REST endpoint, migration V4, unit test
```
Agent 1 đã **chấp nhận** toàn bộ, PR giữ nguyên, merge sau PR Agent 3.

**Thứ tự merge Agent 1 chốt:**
1. PR `config/application-yml` (chuẩn hoá cấu hình)
2. PR `shared/error-code-booking-pt` (thêm mã lỗi cho Agent 6)
3. PR A của Agent 2 (kèm sửa vòng phụ thuộc `MembershipServiceImpl ↔ PaymentServiceImpl`; hiện app với profile `local` **không khởi động được** vì lỗi này)
4. PR Agent 3 (Agent 3 còn 2 mục: chốt chặn PT quét vào/ra trong `completeBooking`, endpoint `PUT /bookings/{id}/early-leave-reason`)
5. **PR #43 Agent 4** → PR B của Agent 2 → PR Agent 5, 6

Tại lần kiểm tra cuối, `master` đã có PR #42 (Agent 2 phần đầu), các nhánh `docs/*`, `shared/error-code-booking-pt`, nhưng **chưa có** nhánh Agent 3.

**Quy trình push/verify đã dùng cho Agent 4** (nhánh Agent 4 đứng riêng chưa compile vì thiếu `BookingService` của Agent 3):
```powershell
git switch -c tmp-verify
git merge origin/master
git merge origin/feature/agent3-schedule-booking
mvn clean verify
git switch feature/agent4-face-attendance
git branch -D tmp-verify
git push            # chỉ khi git log không có commit của Agent 3
```
Không push khi đang ở `tmp-verify`.

`stash@{0}` trong `gym-management-system` (code chưa commit cũ của Agent 2/4): các agent đã lấy phần của mình, được coi là an toàn để drop, nhưng **chưa rõ đã ai drop chưa**.

---

## 5. Module Agent 4 — chi tiết

### 5.1 Các file (đường dẫn dưới `src/main/java/com/gym/management/` trừ khi nêu khác)
- `entity/FaceProfile.java`, `entity/StaffAttendance.java` (cả hai `extends BaseEntity`)
- `repository/FaceProfileRepository.java`, `repository/StaffAttendanceRepository.java`
- `integration/hardware/FaceMatchProperties.java` (đọc `gms.face-attendance.*`), `FaceMatchService.java`, `FaceMatchServiceImpl.java`
- `dto/request/FaceDescriptorRequest.java` (đúng 128 số)
- `dto/response/FaceProfileResponse.java`, `FaceAttendanceResponse.java`, `StaffAttendanceResponse.java`
- `service/FaceProfileService.java`, `FaceAttendanceService.java`, `StaffAttendanceService.java` và `service/impl/*Impl.java`
- `controller/FaceProfileController.java`, `FaceAttendanceController.java`, `StaffAttendanceController.java` (có Swagger `@Tag/@Operation`)
- `src/main/resources/db/migration/V4__face_attendance.sql`
- Test (`src/test/java/...`): `integration/hardware/FaceMatchServiceImplTest`, `service/impl/FaceAttendanceServiceImplTest`, `FaceProfileServiceImplTest`, `StaffAttendanceServiceImplTest`

### 5.2 Endpoint và quyền
| Endpoint | Role |
|---|---|
| `POST /api/v1/face-profiles/me` | MEMBER, TRAINER, RECEPTIONIST, SALES |
| `POST /api/v1/face-profiles/{userId}` (JSON descriptor) | RECEPTIONIST, SUPER_ADMIN |
| `GET`, `DELETE /api/v1/face-profiles/{userId}` | RECEPTIONIST, SUPER_ADMIN |
| `POST /api/v1/face-attendance/kiosk` | RECEPTIONIST, SUPER_ADMIN |
| `POST /api/v1/face-attendance/self` | MEMBER, TRAINER, RECEPTIONIST, SALES |
| `GET /api/v1/staff-attendance/me` | RECEPTIONIST, SALES |
| `GET /api/v1/staff-attendance?date=` | SUPER_ADMIN |

### 5.3 Luồng quét mặt (`FaceAttendanceServiceImpl`)
1. Kiosk: `matchAgainstAll` (1:N, lấy khoảng cách Euclidean nhỏ nhất, phải `< 0.45`). Self: `matchAgainstUser` (1:1). Không khớp → `FACE_NOT_RECOGNIZED`.
2. Nạp `User` và role. Chống quét đúp: Redis `SETNX` key `face-scan:{userId}`, TTL **120 giây**, riêng `TRAINER` **15 giây** (`gms.face-attendance.trainer-min-scan-interval-seconds`). Nếu key đã có → trả `duplicateScan = true`, `action = "NONE"`, không ghi gì.
3. Trong `TransactionTemplate`, theo role người **được nhận diện**:
   - `MEMBER` → `BookingService.recordFaceScan(memberId, now)`; có `checkOutTime` thì `CHECK_OUT`, ngược lại `CHECK_IN`; không có booking hôm nay thì `bookingLinked = false` (không phải lỗi).
   - `TRAINER` → `BookingService.recordTrainerFaceScan(trainerId, now)` trả `Optional<TrainerScanResult>`. Có kết quả: `action = type`, `bookingId`, `earlyLeave` (chỉ khi `CHECK_OUT`), `reasonRequired = earlyLeave`. `Optional.empty()`: `action = "NONE"`, `message = "Không có buổi nào cần điểm danh"`, không phải lỗi. **PT không ghi `staff_attendances`.**
   - `RECEPTIONIST` hoặc `SALES` → `StaffAttendanceService.recordScan(id, now, "FACE")` (lần 1 vào, lần 2 ra, lần 3 báo `VALIDATION_ERROR`).
   - Role khác (VD `SUPER_ADMIN`) → `FACE_NOT_RECOGNIZED`.
- Chấm công động (`StaffAttendanceServiceImpl.computeStatus`): ca mặc định `gms.face-attendance.default-shift-start/end` = 07:00/21:00; trạng thái `ON_TIME/LATE/LEFT_EARLY/LATE_AND_LEFT_EARLY/ABSENT`; "về sớm" chỉ xác định sau khi đã check-out.

### 5.4 DB V4
- `face_profiles`: `id`, `user_id` UNIQUE, `descriptor` NVARCHAR(MAX) (JSON 128 số), `reference_image` (luôn null), `registered_by`, `created_at`, `updated_at`.
- `staff_attendances`: `id`, `staff_id`, `date`, `check_in_time`, `check_out_time`, `method` (`MANUAL`/`FACE`), `notes`, `created_at`, `updated_at`; UNIQUE `(staff_id, date)`.
- Cả hai cột `updated_at` phải có vì entity `extends BaseEntity`. Thiếu cột này từng là lỗi thật (đã sửa ở `6944654`).

### 5.5 Test
`FaceMatchServiceImplTest` 9, `FaceAttendanceServiceImplTest` 20, `FaceProfileServiceImplTest` 8, `StaffAttendanceServiceImplTest` 7. Toàn bộ là unit test Mockito (không `@SpringBootTest`, không cần Docker). Khi merge tạm `master` + nhánh Agent 3: `mvn clean verify` **BUILD SUCCESS, 107 test** (tổng, gồm test Agent 2/3).

---

## 6. Phụ thuộc vào agent khác

- **Agent 3 — `BookingService`:** `recordFaceScan(UUID memberId, LocalDateTime scanTime)` → `Optional<BookingResponse>`; `recordTrainerFaceScan(UUID trainerId, LocalDateTime scanTime)` → `Optional<TrainerScanResult>`. Ngoài ra `createBooking`, `updateBooking`, `confirmBooking`, `completeBooking`, `cancelBooking`, `getById`, `findMyBookings`, `findByBranchAndDate`.
- `TrainerScanResult` là **record top-level** `com.gym.management.dto.response.TrainerScanResult(BookingResponse booking, String type, boolean earlyLeave)` với hằng `CHECK_IN`, `CHECK_OUT`.
- `BookingResponse` có `id`, `memberId`, `memberName`, `trainerId`, `trainerName`, `branchId`, `bookingDate`, `startTime`, `endTime`, `status`, `checkInTime`, `checkInMethod`, `checkOutTime`, `checkOutMethod`, `ptCheckInTime`, `ptCheckInMethod`, `ptCheckOutTime`, `ptCheckOutMethod`, `ptEarlyLeaveReason`, `fullDuration` (Boolean, tính động), `notes`.
- Agent 4 **không** gọi `MembershipService`. Trừ buổi gói xảy ra ở phía Booking/Agent 2 khi booking chốt, không phải lúc quét mặt.
- Agent 4 không tự `bookingRepository.save()`; chỉ đi qua `BookingService`.

---

## 7. Nhật ký quyết định (đã chốt)

| Mã | Quyết định |
|---|---|
| A.1 | Đăng ký hộ vẫn chỉ bằng descriptor JSON. Lễ tân chọn ảnh trong trình duyệt, `face-api.js` trích descriptor từ `<img>`. Không multipart, không xử lý ảnh ở server (pom không có thư viện ảnh/ML) |
| A.2 | Ca chấm công mặc định 07:00–21:00 cho toàn hệ thống (v1). Sau này có thể đọc ca thật từ `trainer_schedules` qua một port của Agent 3 |
| A.3 | Chống quét đúp bằng Redis `SETNX` (thay lock in-memory); không dùng `userLocks` |
| A.4 | Không lưu ảnh, `reference_image` luôn null |
| G3 | PT quét mặt chỉ ghi vào Booking (cột `pt_*`), không ghi `staff_attendances` |
| — | Phi chốt: **Sales có chấm công khuôn mặt** như Lễ tân |
| — | Cửa sổ chống quét đúp của PT 15 giây (Agent 1 đã đưa vào `application.yml`) |
| — | `GET /staff-attendance/me` mở cho RECEPTIONIST và SALES |
| — | Response điểm danh có thêm `bookingId`, `message` (ngoài yêu cầu gốc, Agent 1 đã chấp nhận) |
| — | Phân biệt "quá sớm" và "không có buổi" khi PT quét: **để sau** |

---

## 8. Quy tắc nghiệp vụ PT/lương (`CHANGE_PT_ATTENDANCE_AND_PAY.md`, do Phi quyết)

- Buổi bắt đầu S, kết thúc E, ân hạn 15 phút (`gms.booking.no-show-grace-minutes`).
- Sau S+15 PT chưa quét vào → `PT_NO_SHOW`. PT đã vào mà học viên chưa → `NO_SHOW`. Cả hai vắng → `PT_NO_SHOW`.
- PT quét ra trước E → hiện ô lý do (`PUT /bookings/{id}/early-leave-reason`). Không có lý do thì buổi không được tính lương.
- Học viên huỷ muộn (dưới 2 giờ trước giờ bắt đầu, booking `CONFIRMED`) → `LATE_CANCELLED`, PT vẫn được trả công, không phải đến. Mất 1 buổi gói với `NO_SHOW` và `LATE_CANCELLED`.
- Chỉ PT được trả lương theo buổi; Lễ tân/Sales lương cứng; **bỏ hoa hồng Sales**.
- PT check-in được trong khoảng `[S-30 phút, S+15 phút]`. Học viên chỉ check-in vào booking `CONFIRMED`.
- Sự kiện `BookingSettledEvent(bookingId, trainerId, memberId, outcome COMPLETED|MEMBER_NO_SHOW|LATE_CANCEL, fullDuration, settledAt)` do Agent 3 phát; Agent 2 trừ buổi, Agent 5 tính lương.

---

## 9. Việc còn lại

### 9.1 Kiểm tra trạng thái đầu phiên (nhờ người dùng dán kết quả)
```powershell
cd D:\SrpingBoot-GYM\gms-agent4
git status
git fetch origin
git log --oneline -5
git branch -r --merged origin/master
```
Từ đó biết: PR Agent 2 PR A, PR Agent 3 đã vào `master` chưa.

### 9.2 Việc của Agent 4 (theo thứ tự)
1. **Chờ** PR A (Agent 2) và PR Agent 3 vào `master`.
2. Khi cả hai đã vào: merge `master` vào nhánh Agent 4, `mvn clean verify`, rồi chạy app thật:
   ```powershell
   git fetch origin
   git merge origin/master
   mvn clean verify
   docker compose down -v          # xoá dữ liệu DB local, bắt buộc nếu từng áp V3/V4 bản cũ
   docker compose up -d            # chờ ~30 giây cho SQL Server healthy
   mvn spring-boot:run "-Dspring-boot.run.profiles=local"
   ```
   Thành công khi log có `Started GymManagementApplication`. Nếu thấy `Schema-validation: missing column ...` hoặc lỗi Flyway/checksum, gửi nguyên log. Đây là lần **đầu tiên** kiểm tra thật việc entity Agent 4 khớp migration V4.
3. **Test tích hợp khởi động `ApplicationContext`:** Agent 1 sẽ thêm Testcontainers (SQL Server) và Maven profile `integration` vào `pom.xml`; test gắn `@Tag("integration")`, không chạy mặc định. Agent 1 sẽ báo sau khi PR A merge. Khi đó Agent 4 viết test: context lên, Flyway áp hết migration, Hibernate `validate` qua. Cần hỏi Agent 1: tên profile, class container dùng chung, có cần Redis không (code Agent 4 dùng `StringRedisTemplate`, nhưng Spring thường không kết nối Redis lúc khởi động).
4. Việc để sau (không chặn merge): API chấm công thủ công (`MANUAL`), pagination (`?page=&size=&sort=`), phân biệt "quá sớm"/"không có buổi", ca chấm công theo `trainer_schedules`.
5. Nếu chưa gửi: `AGENT4_REPLY_TO_AGENT1.md` cho Agent 1.

### 9.3 Quyết định đang chờ **Phi** (có trong thông báo của Agent 1, chưa thấy người dùng trả lời trong phiên này)
- Agent 2: tính hạn gói `end_date = start_date + duration_days - 1`.
- Agent 3 (G7): khi so `remaining_sessions`, trừ thêm số booking `PENDING/CONFIRMED` chưa chốt (chỉ khi `remaining_sessions` khác null), ném `MEMBERSHIP_NO_SESSION`.
- Agent 3: booking `CONFIRMED` đã quét đủ nhưng PT không bấm hoàn thành thì xử lý thế nào.

---

## 10. Các thông tin phụ về agent khác (để khỏi hỏi lại)
- Agent 2: PR A có `tryConsumeSession` và listener `BookingSettledMembershipListener`; PR B chờ `BookingSettledEvent` của Agent 3; sẽ thêm `findExpiringWithin(int days)` cho Agent 6.
- Agent 6 (Leads/CRM): làm từ repo, không từ bộ file rời; cần `AuthService.createMemberAccount(...)` do Agent 1 thêm; Agent 1 sẽ thêm migration filtered unique index cho `users.phone`, bổ sung 4 handler cho `GlobalExceptionHandler`, bật `spring.flyway.out-of-order: true` (dev). V5 có khoá ngoại tới `bookings` nên V3 phải có trước.

---

## 11. CI/CD Jenkins (bài tập môn học, chỉ Yêu cầu 1–3)

### 11.1 Đề bài và cách áp dụng
Đề gốc (Bài 4-5): GitHub → Jenkins → Build → Test → Publish → IIS cho ASP.NET WebBanHang. Người dùng muốn **áp dụng cho chính dự án GMS (Spring Boot)**. Chỉ làm **Yêu cầu 1–3**; Yêu cầu 4 (IIS, triển khai thật) và 5 (3 tình huống kiểm thử) chưa đụng tới. Spring Boot không chạy trên IIS nên Yêu cầu 4 sẽ phải thiết kế lại sau.

| Đề (.NET) | GMS |
|---|---|
| `dotnet restore` | `mvn dependency:resolve` |
| `dotnet build -c Release` | `mvn clean compile` |
| `dotnet test` | `mvn test` (unit test, không cần Docker) |
| `dotnet publish` | `mvn package -DskipTests` → `target/gym-management-system-0.1.0-dev.jar` |
| Deploy IIS | tạm là stub |
| nhánh `main` | nhánh `master` |

### 11.2 Trạng thái
- Đã soạn: `Jenkinsfile` (nội dung ở 11.5), `CICD_GUIDE_YEU_CAU_1-3.md`, `NOTICE_CICD_JENKINS_ALL_AGENTS.md` (thông báo gửi Agent 1, 2, 3, 5, 6; chưa rõ người dùng đã gửi chưa).
- **Chưa** đưa `Jenkinsfile` vào repo, **chưa** tạo job Jenkins, **chưa** tạo webhook.
- Jenkins chạy `java -jar jenkins.war --httpPort=8081` từ `C:\jenkins` bị lỗi: *Running with Java 17 ... older than the minimum required version (Java 21). Supported Java versions are: [21, 25]*. Hướng dẫn đã đưa (chưa biết kết quả): cài JDK 21 song song, chạy Jenkins bằng đường dẫn đầy đủ tới `java.exe` của JDK 21. JDK 17 giữ nguyên làm công cụ build.

### 11.3 Các bước tiếp theo (lệnh)
1. Cài JDK 21 (không tick Set JAVA_HOME/Add to PATH):
   ```powershell
   winget install EclipseAdoptium.Temurin.21.JDK
   Get-ChildItem "C:\Program Files\Eclipse Adoptium"
   java -version        # phải vẫn là 17.0.20
   ```
2. Chạy Jenkins (cửa sổ phải để mở):
   ```powershell
   cd C:\jenkins
   & "C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot\bin\java.exe" -jar jenkins.war --httpPort=8081
   ```
   Chờ dòng `Jenkins is fully up and running`. Mật khẩu admin lần đầu in trong cửa sổ, hoặc ở `C:\Users\<tên>\.jenkins\secrets\initialAdminPassword`. Mở `http://localhost:8081`, **Install suggested plugins**, tạo admin mật khẩu mạnh.
3. Plugin cần có: Git, Pipeline, GitHub, JUnit, Timestamper, Credentials Binding.
4. `Manage Jenkins → Tools`: JDK tên **`JDK17`** (JAVA_HOME = đường dẫn JDK 17 ở mục 2, bỏ Install automatically); Maven tên **`Maven3`** (Install automatically hoặc trỏ Maven đã cài); Git. Tên phải khớp `Jenkinsfile`.
5. Tạo job: New Item → `GMS-CICD` → Pipeline. Trigger: *GitHub hook trigger for GITScm polling*. Pipeline: *Pipeline script from SCM*, Git, URL repo, branch `*/master`, Script Path `Jenkinsfile`. Nếu repo private thêm credential (username + Personal Access Token quyền `repo`).
6. ngrok (cửa sổ mới, không cần admin):
   ```powershell
   ngrok config add-authtoken <TOKEN>      # một lần, lấy ở dashboard.ngrok.com
   ngrok http 8081                         # cửa sổ phải để mở
   ```
   Copy địa chỉ `https://xxxx.ngrok-free.app`. Kiểm tra giao dịch ở `http://127.0.0.1:4040`.
7. GitHub → repo → Settings → Webhooks → Add: Payload URL `https://xxxx.ngrok-free.app/github-webhook/` (giữ dấu `/` cuối), Content type `application/json`, *Just the push event*, Secret tuỳ chọn. Recent Deliveries phải tick xanh (200). Mỗi lần chạy lại ngrok miễn phí, URL đổi: sửa lại Payload URL.
8. Đưa `Jenkinsfile` vào repo qua PR (không commit vào nhánh agent nào):
   ```powershell
   cd D:\SrpingBoot-GYM\gym-management-system
   git fetch origin
   git worktree add D:\SrpingBoot-GYM\gms-ci -b ci/jenkins-pipeline origin/master
   # chép Jenkinsfile vào D:\SrpingBoot-GYM\gms-ci\Jenkinsfile
   cd D:\SrpingBoot-GYM\gms-ci
   git add Jenkinsfile
   git commit -m "ci: them Jenkinsfile (Checkout, Restore, Build, Test, Publish, Deploy, Verify)"
   git push -u origin ci/jenkins-pipeline
   ```
   Mở PR vào `master` cho Agent 1 review. Trước khi PR merge, `master` chưa có `Jenkinsfile`; muốn thử sớm, tạm đổi Branch Specifier thành `*/ci/jenkins-pipeline`, thử xong đổi lại.
9. Bấm **Build Now**, lần đầu tải thư viện mất vài phút; xem Console Output và Stage View.

### 11.4 Câu hỏi chưa được trả lời
1. Repo public hay private? (private cần credential)
2. Máy đã có Maven chưa? (`mvn -v`)
3. Deploy/Verify tạm là stub, có đồng ý không?
4. Một Pipeline job `GMS-CICD` (đúng đề, chỉ build `master`) hay Multibranch Pipeline (build mọi nhánh và PR)?
5. Có đưa `Jenkinsfile` qua PR cho Agent 1 review không?

### 11.5 Nội dung `Jenkinsfile` (để tái tạo nếu file không còn)
```groovy
// Jenkinsfile — CI/CD cho Gym Management System (Spring Boot 3, Java 17, Maven)
pipeline {
    agent any

    tools {
        jdk   'JDK17'
        maven 'Maven3'
    }

    options {
        skipDefaultCheckout(true)
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    triggers { githubPush() }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                bat 'git log -1 --oneline'
            }
        }
        stage('Restore') {
            steps { bat 'mvn -B -ntp dependency:resolve' }
        }
        stage('Build') {
            steps { bat 'mvn -B -ntp clean compile' }
        }
        stage('Test') {
            steps { bat 'mvn -B -ntp test' }
            post {
                always { junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true }
            }
        }
        stage('Publish') {
            steps {
                bat 'mvn -B -ntp package -DskipTests'
                archiveArtifacts artifacts: 'target/gym-management-system-*.jar',
                                 excludes: 'target/*.original',
                                 fingerprint: true
            }
        }
        stage('Deploy') {   // TẠM: Yêu cầu 4 làm sau
            when { expression { env.GIT_BRANCH in ['origin/master', 'master'] } }
            steps { echo 'Deploy: chưa cấu hình (Yêu cầu 4). File JAR đã được lưu ở bước Publish.' }
        }
        stage('Verify') {   // TẠM: chỉ kiểm tra JAR tồn tại
            when { expression { env.GIT_BRANCH in ['origin/master', 'master'] } }
            steps { bat 'dir target\\gym-management-system-*.jar' }
        }
    }

    post {
        success { echo 'Pipeline thành công.' }
        failure { echo 'Pipeline thất bại: không publish/deploy phiên bản lỗi.' }
    }
}
```

### 11.6 Rủi ro và lưu ý
- Cổng 8080 của Jenkins mặc định trùng cổng ứng dụng Spring Boot → dùng 8081 cho Jenkins.
- ngrok miễn phí đổi URL mỗi lần chạy; khi ngrok chạy, trang đăng nhập Jenkins lộ ra Internet → cần admin mật khẩu mạnh, tắt ngrok (`Ctrl+C`) khi xong.
- Với job đơn build `master`, Jenkins **không** build nhánh feature hay PR → vẫn phải `mvn clean verify` cục bộ trước khi mở PR.
- Test mặc định phải chạy không cần Docker/DB/Redis; profile `integration` của Agent 1 phải **không** chạy mặc định, nếu không bước Test trên Jenkins sẽ đỏ.
- Build đầu tiên trên `master` sẽ cho biết trạng thái thật của `master` (có thể đỏ vì lý do của module khác).
- Đề gốc yêu cầu bảo vệ endpoint webhook (phần lưu ý cuối đề); chưa làm.
- Trong báo cáo (nếu có), giữ nhánh feature đã merge và không force-push để làm minh chứng Yêu cầu 1; ảnh cần chụp: repo, nhánh, PR, cấu hình job, Tools, plugin, webhook (Recent Deliveries 200), ngrok, Stage View và lịch sử build.

---

## 12. File đã tạo (thư mục outputs, có thể mất khi sang phiên mới)

- Agent 4: `AGENT4_README.md` (changelog đầy đủ), `AGENT4_PR_REVIEW.md`, `AGENT4_PROGRESS_REPORT.md`, `AGENT4_RESPONSE_TO_ERD.md`, `AGENT4_RESPONSE_TO_CHANGE_REQUEST.md`, `AGENT4_REPLY_TO_AGENT1.md`, `URGENT_BUILD_BLOCKER_REPORT.md`.
- Zip code (đã áp vào repo và commit, không cần áp lại): `agent4-face-attendance.zip`, `agent4-change-pt-attendance.zip`, `agent4-change-sales-attendance.zip`.
- Cũ/không dùng: `AGENT4_OPEN_QUESTIONS.md`, `AGENT1_AND_PHI_TO_CONFIRM.md`, `AGENT2_AND_3_TO_CONFIRM.md`.
- CI/CD: `Jenkinsfile`, `CICD_GUIDE_YEU_CAU_1-3.md` (có mục B1b về Java 21), `NOTICE_CICD_JENKINS_ALL_AGENTS.md`.
- Các file từ agent khác người dùng từng đưa: `REQUIREMENTS.md`, `API_DESIGN.md`, `DATABASE_SCHEMA.md`, `ARCHITECTURE.md`, `TASK_ASSIGNMENT.md`, `ISSUES.md`, `ROADMAP.md`, `CONTRIBUTING.md`, `DESIGN_SYSTEM.md`, `ERD_TONG_QUAT.md`, `CHANGE_PT_ATTENDANCE_AND_PAY.md`, `REPLIES_TO_AGENTS_*.md`.

---

## 13. Sự cố đã gặp và bài học

| Sự cố | Nguyên nhân | Bài học |
|---|---|---|
| `staff_attendances` thiếu `updated_at` | Entity `extends BaseEntity` nhưng migration không có cột; test Mockito không khởi động Spring nên không bắt được | Build xanh bằng unit test **không** chứng minh app chạy; cần test khởi động `ApplicationContext` |
| `BUILD SUCCESS` nhưng worktree sạch fail | Từng build trong thư mục dùng chung có file chưa commit (`security/*`, module Agent 2) | Luôn build trong worktree sạch |
| Nhầm "chưa có PR" | Đoán từ việc `git push` chỉ in link tạo PR | PR #43 đã có từ trước; kiểm tra GitHub trước khi kết luận |
| Test import sai `TrainerScanResult` | Giả định record lồng trong `BookingService` | Xem code thật (`git show origin/<nhánh>:<path>`) trước khi viết |
| Dùng `git reset --hard` làm mất commit chưa push | Lệnh xoá commit local | Dùng nhánh tạm `tmp-verify` thay vì reset; chỉ `amend` khi chưa push |
| Không thấy `gms_db` trong SSMS | Kết nối nhầm instance `localhost\MSSQLSERVER01` | Dùng `localhost,1433` + SQL Authentication |
| Jenkins không chạy | Jenkins mới cần Java 21, máy có Java 17 | Cài JDK 21 song song, giữ JDK 17 cho build |
| Tin app chạy được | Vòng phụ thuộc `MembershipServiceImpl ↔ PaymentServiceImpl` (Agent 2) chỉ lộ khi khởi động | Chạy `spring-boot:run` trước khi merge |

---

## 14. Lệnh hay dùng

```powershell
# Build (từ worktree)
mvn clean verify
# Chạy app (cần Docker lên và code compile được)
docker compose up -d
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
# Reset DB local (XOÁ dữ liệu)
docker compose down -v
# Kiểm tra nhanh Git
git status
git log --oneline -5
git branch -r --merged origin/master
git show origin/<nhánh>:<đường-dẫn-file>
git grep -n "<từ khoá>" origin/<nhánh> -- src/main
```

---

## 15. Đề bài CI/CD (Bài 4-5) — tóm tắt đầy đủ yêu cầu 1 đến 5

Đề gốc là bài nhóm 3 sinh viên: tự động hoá GitHub → Jenkins → Build → Test → Publish → IIS cho ASP.NET Core MVC (.NET 8) `WebBanHang`, truy cập `http://localhost/webbanhang`. Jenkins, .NET SDK và IIS cùng một máy Windows. Người dùng chỉ làm **Yêu cầu 1–3** và thay ứng dụng bằng GMS.

**Yêu cầu 1 — GitHub:** tạo project, làm các chức năng cơ bản (sản phẩm, danh mục, đơn hàng), tạo repo, mỗi sinh viên một nhánh riêng, hợp nhất vào `main` bằng Pull Request.

**Yêu cầu 2 — Jenkins:** cài Jenkins trên Windows; cấu hình SDK, Git và plugin; tạo Pipeline Job tên `WebBanHang-CICD` (ở đây dùng `GMS-CICD`); kết nối repo; cấu hình GitHub Webhook (dùng ngrok nếu Jenkins chạy localhost) để kích hoạt khi có thay đổi trên nhánh chính.

**Yêu cầu 3 — Jenkinsfile** ở thư mục gốc repo, các giai đoạn: Checkout, Restore, Build (Release), Test, Publish, Deploy (chép vào thư mục IIS), Verify. Lỗi ở Build hoặc Test phải dừng pipeline, không triển khai bản lỗi.

**Yêu cầu 4 — IIS (CHƯA LÀM):** thư mục `C:\wwwroot\webbanhang`; cài ASP.NET Core Hosting Bundle; Application Pool `WebBanHangPool` (No Managed Code); ứng dụng `webbanhang` dưới Default Web Site; địa chỉ `http://localhost/webbanhang`; cấp quyền thư mục; Deploy thay bản cũ bằng bản mới và ứng dụng chạy bình thường.

**Yêu cầu 5 — 3 tình huống kiểm thử (CHƯA LÀM):**
1. Triển khai thành công: đổi nội dung giao diện trang chủ, push, merge vào nhánh chính, Jenkins tự chạy, website hiển thị nội dung mới.
2. Biên dịch thất bại: cố tình tạo lỗi cú pháp trên nhánh kiểm thử, Jenkins phát hiện lỗi Build, dừng pipeline, giữ nguyên bản đang chạy.
3. Kiểm thử thất bại: sửa hàm tính tiền đơn hàng làm Unit Test hỏng, Jenkins dừng ở bước Test, không Publish/Deploy.

**Sản phẩm cần nộp:** (1) URL repo GitHub kèm các nhánh và `Jenkinsfile`; (2) `Jenkinsfile` hoàn chỉnh; (3) ảnh cấu hình Jenkins Job và lịch sử các lần chạy; (4) ảnh cấu hình IIS, Application Pool, thư mục triển khai; (5) minh chứng truy cập `http://localhost/webbanhang`; (6) báo cáo kết quả ba tình huống.

**Lưu ý kỹ thuật cuối đề:** tài khoản dịch vụ Jenkins phải có quyền ghi vào thư mục triển khai và các thao tác IIS cần thiết; ứng dụng chạy dưới `/webbanhang` phải xử lý PathBase; cần cơ chế tạm ngừng ứng dụng khi thay DLL (`app_offline.htm`); giữ nguyên cấu hình và dữ liệu môi trường khi cập nhật; nếu dùng ngrok nhận webhook thì phải bảo vệ endpoint và xác thực phù hợp.

**Kết quả mong đợi:** chứng minh quy trình tự động GitHub → Jenkins → Build → Test → Publish → IIS; mỗi thay đổi hợp lệ merge vào nhánh chính tự cập nhật ứng dụng.

### Điểm cần quyết định khi đến Yêu cầu 4–5 (chưa bàn, chưa quyết)
- Spring Boot không chạy trên IIS. Cần thống nhất cách "Deploy" thay thế (ví dụ chạy file JAR từ một thư mục triển khai cố định, dừng bản cũ rồi khởi động bản mới) và cách "Verify" (ví dụ gọi một URL kiểm tra sau khi khởi động; chưa biết `pom.xml` có actuator hay không).
- Ứng dụng GMS là API, không có trang chủ giao diện; tình huống 1 cần một thay đổi nhìn thấy được qua HTTP (VD nội dung một endpoint hoặc tiêu đề Swagger). Tình huống 3 cần một hàm tính toán có test rõ ràng (VD logic tính trạng thái chấm công hoặc hạn gói).
- Ứng dụng khi chạy cần SQL Server và Redis (Docker) đang bật; ảnh hưởng đến bước Verify.
- Tình huống 2 và 3 yêu cầu kích hoạt pipeline trên "nhánh kiểm thử": với job đơn chỉ build `master` cần cách chạy trên nhánh khác (đổi Branch Specifier, tham số hoá nhánh, hoặc dùng Multibranch Pipeline). Đây là lý do câu hỏi số 4 ở mục 11.4 cần được trả lời trước khi làm Yêu cầu 5.
