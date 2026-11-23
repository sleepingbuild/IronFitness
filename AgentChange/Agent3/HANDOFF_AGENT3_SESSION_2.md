# HANDOFF: Agent 3 (Booking Engine) — tóm tắt phiên làm việc để tiếp tục ở phiên chat sau

Cập nhật lần cuối: 2026-11-06. Người dùng gõ tiếng Việt, dán output PowerShell vào chat, Claude trả lời bằng tiếng Việt.
Đọc mục 0 và mục 9 trước. Những chỗ ghi "CHƯA KIỂM CHỨNG" nghĩa là chưa có output thật chứng minh.

---

## 0. Vai trò và cách làm việc

- Claude đóng vai **Agent 3** của dự án Gym Management System (Spring Boot + SQL Server + Next.js), phụ trách module **Lịch PT và Booking** (backend và frontend của module này).
- Có 6 agent (mỗi agent một module); **Agent 1** là Backend Lead, sở hữu file chung và merge PR. Người dùng làm cầu nối: dán output, gửi file thông báo cho các agent khác (nhiều lần người dùng nói chưa liên lạc được).
- **Không sửa file chung** (thuộc Agent 1): `ErrorCode`, `SecurityConfig`, `GlobalExceptionHandler`, `UserRepository`, `application.yml`, `pom.xml`, `Jenkinsfile`; ở frontend: `lib/`, `components/`, `config/` (ngoại trừ **đúng dòng của Agent 3** trong `src/config/navigation.ts`). Chỉ được sửa `src/app/(app)/<route Agent 3>/` và `src/features/<module Agent 3>/`. Route của Agent 3: `/schedule` (PT) và `/bookings`.
- **Điều kiện "Done" của mọi PR:** `mvn clean verify` xanh; frontend `npm run lint`, `typecheck`, `test`, `build` xanh; có test; cập nhật `API_DESIGN.md`; kèm ảnh chụp màn hình với PR frontend.
- Người dùng muốn **đẩy nhanh, không chờ duyệt** để làm việc tiếp: mỗi nhánh mới xuất phát từ `origin/master` mới nhất; nếu phụ thuộc nhánh chưa merge thì chồng nhánh (PR chồng, base là nhánh trước).

### Sở thích của người dùng (đã lưu trong bộ nhớ)
- Với lệnh git commit: đưa **từng bước nhỏ, mỗi nhóm một bước**, không gộp thành một script.
- **Trước khi commit, người dùng dán `git status`**; Claude phải đối chiếu với danh sách file dự kiến rồi mới đưa lệnh commit.
- Script sửa file phải **an toàn**: kiểm tra mỗi chỗ thay khớp đúng một lần, không khớp thì không ghi gì. Không dùng `Set-Content` để sửa file có tiếng Việt (dễ hỏng); với file có tiếng Việt thì tải file về chép đè, hoặc dùng script ghi UTF-8 không BOM.
- Các tin nhắn gửi agent khác được viết thành **file `.md`** trong thư mục outputs.

---

## 1. Môi trường và bẫy đã gặp

- Repo/worktree của Agent 3: `D:\SrpingBoot-GYM\gms-agent3` (có thư mục `frontend\`). Các worktree khác: `gms-agent1/2/4/5...`. Thư mục `gms-agent3-master` (worktree phụ để chạy code master) đã đề xuất nhưng **CHƯA CHẮC đã tạo**.
- Docker: `gms-sqlserver` (cổng 1433, DB riêng của Agent 3 là **`gms_db_agent3`**, tài khoản `sa`, mật khẩu dev mặc định theo `docker-compose.yml`), `gms-redis`, `gms-postgres`, và bộ **`gms-demo-*`** (do người dùng và Agent 1 triển khai): `gms-demo-frontend-1` giữ cổng **3000**, `gms-demo-backend-1` giữ cổng **8082** (map vào 8080 trong container), `gms-demo-sqlserver-1` cổng 11433. **Không dừng container `gms-demo-*` hay tiến trình `wslrelay.exe`/`com.docker.backend.exe`.**
- Jenkins UI ở cổng 8080; bản do Jenkins triển khai ở 8081 (mặc định của `.env.example` frontend).
- Vì 8082 và 3000 bị Docker giữ: backend của Agent 3 chạy ở **8083**, frontend dev chạy ở **3001**:
  - Backend: `mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--server.port=8083"`
  - Frontend (thư mục `frontend`): `npm run dev -- -p 3001`
- `src/main/resources/application-local.yml` (bị `.gitignore` bỏ qua): có `gms.jwt.secret` riêng và `spring.datasource.url` trỏ `gms_db_agent3` **kèm `;sendTimeAsDatetime=false`** (xem mục 6, đây là cờ bắt buộc).
- `sqlcmd` qua docker: `docker exec gms-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "<mật khẩu dev>" -C -d gms_db_agent3 -I -Q "<sql>"`. **Dùng cờ `-I`** (QUOTED_IDENTIFIER) khi INSERT vào `users` (bảng có chỉ mục lọc từ V9), nếu không sẽ lỗi 1934. Hàm `Run-SqlI` định nghĩa trong cửa sổ nào thì chỉ sống ở cửa sổ đó, phải định nghĩa lại khi mở cửa sổ mới.
- Git: `user.name` đang là "Agent 1 (Claude - Backend Lead)" (cả dự án dùng chung danh tính, đã nêu, chưa đổi); `core.autocrlf=true` (cảnh báo `LF will be replaced by CRLF` là vô hại). Nhánh tạo bằng `git checkout -b X origin/master` sẽ **theo dõi `origin/master`**, nên phải push bằng **`git push -u origin <tên-nhánh>`**. **Tuyệt đối không dùng `git push origin HEAD:master`.**
- **IDE của người dùng từng tự định dạng lại cả file (đổi thụt lề 2 thành 4 dấu cách)** khiến diff phình 250 dòng. Quy ước dự án là 2 dấu cách, không có `.prettierrc`. Cần tắt "Format On Save".
- **Không đặt hai file chỉ khác nhau hoa/thường** trong cùng thư mục (đã dính: `MemberBookings.tsx` và `memberBookings.ts` làm TypeScript lỗi trên Windows). File hàm thuần của hội viên tên `memberBookingRules.ts`.
- Mật khẩu thử trong DB riêng: `Test@12345` (hash BCrypt cost 10, dùng được với ứng dụng). Tài khoản chính thức của Agent 1 (`00_users.sql`) dùng `Gym@12345` và email khác; **CHƯA CHẮC đã vào master**.
- Cột `users.phone` là UNIQUE; sau V9 nhiều giá trị NULL hợp lệ, nhưng DB riêng của Agent 3 từng gặp lỗi khi để NULL, nên các user thử đều có số điện thoại riêng.
- Vai trò nằm ở `user_roles(user_id, role_id)` + bảng `roles` (MEMBER, TRAINER, RECEPTIONIST, SALES, SUPER_ADMIN đã seed); bảng `users` không có cột `role`.

---

## 2. Dữ liệu thử trong `gms_db_agent3`

Tài khoản (mật khẩu `Test@12345`): `trainer1@gym.test`, `trainer2@gym.test` (id `33333333-3333-3333-3333-333333333331` và `...332`, tên "Trainer Seed 1/2"), `member.active@gym.test` (`44444444-...-441`), `member.expired@gym.test` (`...442`), `reception.test@gym.test` (`55555555-...-551`, vai trò RECEPTIONIST), cùng `member.test@gms.local`/`trainer.test@gms.local` cũ.
- `trainer_pay_rates`: một dòng 150000 cho `trainer1` (thêm khi thử hoa hồng).
- Gói tập thử (`packages`/`member_packages`) có id bắt đầu `dddddddd-`: `member.active` có một gói ACTIVE 20 buổi (tạo khi thử đặt lịch). Các phép đo khác tự tạo rồi xoá.
- Seed: `scripts/dev-seed/20_bookings.sql` (đã ở master) tạo lịch 06:00 đến 22:00 cho 2 PT trong 8 ngày và 8 booking mẫu (đánh dấu `notes = N'[dev-seed]'`), ngày giờ tính tương đối theo giờ Việt Nam; chạy lại để làm tươi: `docker cp scripts\dev-seed\20_bookings.sql gms-sqlserver:/tmp/20_bookings.sql` rồi `sqlcmd ... -b -i /tmp/20_bookings.sql`. Cần các user `@gym.test` có sẵn, thiếu thì chỉ in `THIEU USER`. Chuỗi phương thức điểm danh là `'FACE'` (Agent 4 đã xác nhận, hằng `METHOD_FACE` trong `BookingServiceImpl`).

---

## 3. Công việc backend đã làm (đã merge vào master)

| PR | Nội dung |
|---|---|
| #41 | V3 (lịch PT, booking), `BookingSettledEvent`, khoá lạc quan `bookings.version` + `@Version`, `flush()` trước `publishEvent` (xung đột thì ném `BusinessException(VALIDATION_ERROR)` "Booking vừa được xử lý bởi yêu cầu khác...") |
| #74 | `@PreAuthorize` cho `GET /trainer-schedules/available` (MEMBER, RECEPTIONIST, SUPER_ADMIN) |
| #73 | `scripts/dev-seed/20_bookings.sql` |
| nhánh `feature/agent3-booking-api-completion` (đã vào master) | `GET /trainer-schedules/trainers?date=` (PT còn slot), `GET /bookings?date=` không cần `branchId`, `GET /bookings/{id}` (chủ buổi hoặc nhân viên), lọc `/bookings/me` theo `status`/`from`/`to` (lọc trong bộ nhớ sau lazy-status), 256 test, cập nhật `API_DESIGN.md` |

**Event `BookingSettledEvent(bookingId, trainerId, memberId, outcome, fullDuration, settledAt)`**, `Outcome {COMPLETED, MEMBER_NO_SHOW, LATE_CANCEL}`. `PT_NO_SHOW` và `CANCELLED` không phát event. Listener của người khác: Agent 2 `BookingSettledMembershipListener` (`@EventListener` đồng bộ, `tryConsumeSession`); Agent 5 `BookingSettledCommissionListener` (`AFTER_COMMIT`, transaction riêng, bỏ qua khi `fullDuration=false`, cần dòng `trainer_pay_rates`, `commissions` có `UNIQUE booking_id`).

**Endpoint của Agent 3:** `/api/v1/trainer-schedules` (GET/POST/PUT/DELETE), `/trainer-schedules/available`, `/trainer-schedules/trainers`; `/api/v1/bookings` (POST tạo; PUT `/{id}` sửa; POST `/{id}/confirm|complete|cancel`; PUT `/{id}/early-leave-reason` body `{"reason":"..."}` 5 đến 255 ký tự; GET `/me`, GET `/{id}`, GET `?date=&branchId=`). Mã lỗi: `BOOKING_SLOT_TAKEN` 409, `RESOURCE_NOT_FOUND` 404, `AUTH_FORBIDDEN_ROLE` 403, `VALIDATION_ERROR` 400.

### Đã kiểm chứng trên SQL Server thật (kết quả thật)
- Hai request huỷ muộn đồng thời lên một booking: đúng một `200`, một `400`, `version = 1` (5/5).
- Đường lazy `NO_SHOW` qua `GET /bookings/me` đồng thời: 5/5 `NO_SHOW`, `version = 1`, mỗi round `200/400`.
- `complete` đồng thời + listener hoa hồng của Agent 5: 6 booking, mỗi booking đúng một dòng `commissions`, không 500.
- T1 đến T4 (tin mới): hội viên thấy 2 PT với 15 và 16 slot, PT gọi `/trainers` nhận 403; lễ tân liệt kê hôm qua 4 buổi và ngày mai 2 buổi, xem chi tiết 200; hội viên khác 403, không tồn tại 404; bộ lọc `/me` khớp đếm tay.
- Đặt lịch qua API (sau khi có cờ driver): 200 `PENDING`, đặt trùng 409 `BOOKING_SLOT_TAKEN`, huỷ 200 `CANCELLED`.

### Lỗi mất cập nhật phía Agent 2 (đã báo, Agent 2 đã sửa)
Đo trước bản sửa: 60 buổi, phần 1 (5 booking, mỗi booking 2 request đồng thời) còn 55 (đúng); phần 2 (10 cặp booking khác nhau của cùng hội viên) còn **45** thay vì 35, tức mất 10 lần trừ. Agent 2 đã sửa `tryConsumeSession` bằng `UPDATE ... remaining_sessions - 1` nguyên tử (có trên master: `@Modifying` ở `MemberPackageRepository`). **Agent 2 đã nhờ Agent 3 đo lại sau khi merge; kỳ vọng phần 1 còn 55 và phần 2 còn 35. CHƯA ĐO.** Xem phụ lục A.

---

## 4. Công việc frontend

Frontend: Next.js 14 (App Router), TypeScript, Tailwind, Vitest. Đã merge vào master: PR #75 (`/schedule`: khai ca làm việc cho PT), #76 (tab "Buổi dạy": xác nhận, hoàn thành, huỷ, nộp lý do rời sớm), #78 (`/bookings`: "Buổi tập của tôi" cho hội viên, có `memberBookingRules.ts` + 7 test), nhánh `feature/agent3-complete-warning` (hộp xác nhận khi PT bấm "Hoàn thành buổi" lúc quét ra sớm mà chưa nộp lý do, kèm `trainerBookingRules.ts` + 7 test; đã kiểm chứng: buổi bỏ qua lý do không có hoa hồng, buổi đã nộp lý do có hoa hồng).

Quy ước frontend cần nhớ: gọi API chỉ qua `apiFetch` (`@/lib/api/client`), lỗi bằng `ApiError`/`errorMessage` (`@/lib/api/errors`, xử lý theo `err.errorCode`), `useToast` (`@/lib/toast`), `Alert` (tone `error`/`warning`, có `title`), `Button` (có `loading`), `Spinner` (export từ `Button`), `Field`, `PageHeader`, `RequireRole`, `useAuth()` (`@/lib/auth/AuthContext`, `user.roles`), `hasAnyRole` (`@/lib/roles`). Mỗi màn hình đủ 4 trạng thái (đang tải, rỗng, lỗi, không có quyền). Class có sẵn: `panel`, `text-steel`.

### Nhánh đang làm dở: `feature/agent3-booking-frontend` (CHƯA COMMIT)
Xuất phát từ `feature/agent3-booking-api-completion` (đã vào master nên PR có thể dùng base `master`). Các file chưa commit (theo `git status` lần cuối):
- Sửa: `frontend/src/app/(app)/bookings/page.tsx` (bọc `RequireRole` cho MEMBER/RECEPTIONIST/SUPER_ADMIN, render `BookingsWorkspace`), `frontend/src/config/navigation.ts` (đổi dòng `/bookings` thành `ready`).
- Mới trong `frontend/src/features/bookings/`: `types.ts`, `api.ts`, `dates.ts`, `dates.test.ts`, `staffBookingRules.ts`, `staffBookingRules.test.ts`, `BookingCreate.tsx` (hội viên: chọn ngày, chọn PT từ `/trainers`, chọn slot từ `/available`, đặt; xử lý 409 `BOOKING_SLOT_TAKEN` bằng tải lại slot), `StaffBookings.tsx` (lễ tân/admin: danh sách buổi theo ngày + huỷ), `BookingsWorkspace.tsx` (hội viên: 2 tab "Buổi của tôi"/"Đặt lịch"; vai trò khác: `StaffBookings`).
- File trong repo giữ nguyên, đừng ghi đè: `MemberBookings.tsx`, `memberBookingRules.ts`, `memberBookingRules.test.ts`.
- **CHƯA chạy `typecheck`/`lint`/`test`/`build` cho nhánh này.** Kỳ vọng 38 test. Điểm dễ lỗi: `Field` có nhận `min` không, `ApiError` có xuất ra không.
- **CHƯA thử trên trình duyệt** (xem mục 5).
- Trước khi commit: `git merge origin/master` (master đã đổi `navigation.ts` do agent khác đánh dấu `ready`; giữ nguyên dòng của họ, chỉ giữ dòng `/bookings` của Agent 3), `git status`, dán cho Claude đối chiếu.
- Chưa làm: lễ tân **đặt hộ** cho hội viên (cần ô chọn hội viên), Super Admin xem/khai lịch PT chưa có ca (cần danh sách PT), trang chi tiết một buổi (API `GET /bookings/{id}` đã có, giao diện chưa dùng). Phụ thuộc endpoint của Agent 1 (mục 7).

---

## 5. Vấn đề đang dở (ưu tiên giải quyết đầu phiên sau)

### 5.1 Frontend dev vẫn gọi 8081 dù `.env.local` ghi 8083 — CHƯA GIẢI QUYẾT
Đã kiểm chứng: `frontend/.env.local` dòng 3 là `NEXT_PUBLIC_API_BASE_URL=http://localhost:8083`; chỉ có `.env.example` (8081) và `.env.local`, không có `.env.development*`; biến môi trường cửa sổ PowerShell rỗng; `src/lib/config.ts` dùng 8081 làm mặc định khi biến vắng. Cổng 3000 do container `gms-demo-frontend-1` giữ (frontend của bản demo, build sẵn với địa chỉ mặc định), nên `npm run dev` mới chạy ở 3001 (`-p 3001`). Người dùng báo: backend chạy ở 8083, nhưng "trên web thì 8081", và vẫn đăng nhập được. **CHƯA BIẾT** người dùng đang mở `localhost:3000` (bản demo) hay `localhost:3001`, và URL request `login` thật là gì.
Chẩn đoán tiếp theo:
1. Xác nhận trình duyệt đang mở **`http://localhost:3001`**, bấm Ctrl+F5, F12 → Network, đăng nhập, ghi lại **URL đầy đủ của request `login`** và tài khoản dùng.
2. Thử cô lập: `$env:NEXT_PUBLIC_API_BASE_URL="http://localhost:8083"; npm run dev -- -p 3001` (biến shell thắng file `.env`).
3. Xem `frontend/next.config.*` và `package.json` (mục `scripts`) có ghi cứng giá trị này không.
4. Xoá `.next` rồi chạy lại.
Lưu ý: đăng nhập thành công ở 8081 nghĩa là có một backend ở 8081 (bản Jenkins) và tài khoản đó đăng ký ở đó (ví dụ tài khoản `Test1`); các tài khoản `@gym.test` chỉ có trong `gms_db_agent3`.

### 5.2 Lỗi `sendTimeAsDatetime` làm đặt lịch trả 500 trên mọi môi trường thiếu cờ — ĐÃ XÁC NHẬN, CHƯA SỬA CHUNG
`POST /bookings` và `PUT /bookings/{id}` trả 500: `SQL Error 402: The data types time and datetime are incompatible in the less than operator` tại `BookingServiceImpl.validateBookingRules` (dòng ~433), truy vấn `findActiveByTrainerSlot` (và `findActiveByMemberSlot` cùng dạng). Nguyên nhân: mssql-jdbc mặc định gửi `LocalTime` dưới dạng `datetime`. Chữa: thêm **`;sendTimeAsDatetime=false`** vào địa chỉ kết nối (đã kiểm chứng trên máy Agent 3 qua `application-local.yml`). Bản triển khai Docker `gms-demo-backend-1` có `DB_URL=jdbc:sqlserver://sqlserver:1433;databaseName=gms_demo;encrypt=true;trustServerCertificate=true` (không có cờ) nên đặt lịch ở bản demo sẽ lỗi (suy ra từ chuỗi, chưa thử trực tiếp). Bản Jenkins (8081) CHƯA kiểm tra.
Việc cần làm: gửi Agent 1 file `NOTICE_AGENT3_TO_AGENT1_TIME_PARAM_BUG.md` (đã có trong outputs) đề nghị thêm cờ vào `application.yml`, vào `DB_URL` của docker compose/Jenkinsfile và vào ví dụ `application-local.example.yml`. Phương án dự phòng (CHƯA CHẮC cú pháp Hibernate 6, phải thử trên DB thật): ép kiểu `time` ở hai truy vấn trong `BookingRepository`. Một test đi qua đường tạo booking trên DB thật vẫn chưa có.

### 5.3 Đo lại cho Agent 2 — CHƯA CHẠY
Phải chạy **backend từ code `master` mới** (có bản sửa của Agent 2). Backend đang chạy của Agent 3 (nhánh frontend) chưa chắc có bản sửa. Cách: `git worktree add --detach D:\SrpingBoot-GYM\gms-agent3-master origin/master`, chép `application-local.yml` sang, `mvn clean verify`, chạy ở cổng 8083, rồi chạy khối đo ở phụ lục A (đổi cổng cho khớp). Kỳ vọng sau phần 1 còn 55, sau phần 2 còn 35, 25 booking `COMPLETED`, 25 dòng hoa hồng. Dán output cho Claude trước khi dọn dẹp; sau đó soạn tin trả lời Agent 2 và Agent 1.

---

## 6. Các file thông báo đã soạn (trong outputs)

- `NOTICE_AGENT3_TO_AGENT1_TIME_PARAM_BUG.md` — **còn hiệu lực, nên gửi Agent 1 sớm.**
- `NOTICE_AGENT3_TO_AGENT2_CREDIT_RACE.md` — đã được Agent 2 xử lý; số liệu đo trước sửa vẫn đúng; sau khi đo lại cần soạn tin mới.
- `NOTICE_AGENT3_DONE.md`, `NOTICE_AGENT3_PRS_AND_CONFIRMATIONS.md`, `NOTICE_AGENT3_OPEN_PRS_AND_REQUESTS.md`, `AGENT3_TO_AGENT1_FRONTEND_DATA.md` — **phần lớn đã lỗi thời** (PR đã merge, đường `NO_SHOW` nay đã kiểm chứng, Agent 4 đã xác nhận `'FACE'`); chỉ dùng làm tham khảo.

---

## 7. Phụ thuộc vào agent khác

- **Agent 1:** có vẻ đã làm `GET /api/v1/users?role=MEMBER&q=`, `GET /api/v1/trainers`, `GET /api/v1/branches` (master có commit thêm tag `Users`, `Trainers`, `Branches` vào `OpenApiConfig`; nhánh `feature/agent1-directory-endpoints`). **CHƯA KIỂM CHỨNG**; kiểm tra bằng `git grep -n "api/v1/trainers\|api/v1/branches\|role=MEMBER" origin/master -- "*.java"`. Nếu có, làm tiếp: lễ tân đặt hộ (chọn hội viên), Super Admin xem/khai lịch PT bất kỳ.
- **Agent 2:** đo lại sau bản sửa (mục 5.3); `10_packages_memberships.sql` (seed gói) có thể đã có.
- **Agent 4:** đã xác nhận `'FACE'`; trang kiosk/điểm danh của họ ghi `pt_check_in/out` cho màn hình "Buổi dạy".
- **Agent 5:** listener hoa hồng bỏ qua `fullDuration=false` và cần cấu hình lương PT; seed của họ nên có `trainer_pay_rates` cho `trainer1/2`.

---

## 8. Trạng thái master lần cuối kiểm tra
HEAD `84fa534` (merge #81 agent5-frontend); master chứa hầu hết PR của Agent 2, 4, 5, 6 và tất cả PR của Agent 3. Luôn `git fetch origin` rồi kiểm tra lại.

---

## 9. Việc cần làm tiếp (theo thứ tự đề nghị)

1. Giải quyết 5.1 (frontend gọi sai cổng) để thử được giao diện.
2. Trên `http://localhost:3001` với backend 8083 và tài khoản `@gym.test`, thử: hội viên (`member.active@gym.test`) đặt lịch (khung trống, đặt trùng ra lỗi đúng, ngày không có ca ra khung rỗng, buổi mới hiện ở "Buổi của tôi" trạng thái "Chờ PT xác nhận"); lễ tân (`reception.test@gym.test`) xem lịch theo ngày và huỷ.
3. Chạy `typecheck`, `lint`, `test` (kỳ vọng 38), `build` cho nhánh `feature/agent3-booking-frontend`; `git merge origin/master`; dán `git status`; commit từng nhóm; `git push -u origin feature/agent3-booking-frontend`; mở PR kèm ảnh chụp.
4. Gửi Agent 1 file về cờ `sendTimeAsDatetime`.
5. Đo lại 10 cặp cho Agent 2 (phụ lục A) rồi báo kết quả.
6. Kiểm tra endpoint thư mục của Agent 1 (mục 7); nếu có thì làm "lễ tân đặt hộ" và phần của Super Admin.
7. Tuỳ chọn: trang chi tiết buổi; test đi qua đường tạo booking trên DB thật.
8. Dọn dẹp DB riêng khi cần (user `reception.test`, `trainer_pay_rates`, gói `dddddddd-*`); các file `boot*.log` ở thư mục gốc repo là log cục bộ, đừng `git add`.

---

## Phụ lục A: khối đo 10 cặp booking (cho Agent 2)

Backend phải chạy ở `$api` (đổi cổng nếu cần). Đừng chạy khi giờ Việt Nam nằm trong 00:10 đến 01:10. `trainer1` phải có dòng `trainer_pay_rates` thì mới có hoa hồng.

```powershell
& {
  $api = "http://localhost:8083"
  function Run-SqlI($q) { docker exec gms-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "<mat khau dev trong docker-compose.yml>" -C -d gms_db_agent3 -I -Q $q }
  Add-Type -AssemblyName System.Net.Http
  $client = New-Object System.Net.Http.HttpClient
  try { $login = Invoke-RestMethod -Method Post -Uri "$api/api/v1/auth/login" -ContentType "application/json" -Body '{"email":"trainer1@gym.test","password":"Test@12345"}' } catch { "KHONG DANG NHAP DUOC: " + $_.Exception.Message; return }
  $token = $login.data.accessToken
  function Send-Complete($id) {
    $req = New-Object System.Net.Http.HttpRequestMessage([System.Net.Http.HttpMethod]::Post, "$api/api/v1/bookings/$id/complete")
    $req.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", $token)
    return $client.SendAsync($req)
  }
  $vn = [TimeZoneInfo]::FindSystemTimeZoneById("SE Asia Standard Time")
  $now = [TimeZoneInfo]::ConvertTimeFromUtc([DateTime]::UtcNow, $vn)
  $s = $now.AddMinutes(-70); $e = $s.AddMinutes(60)
  $d = $s.ToString('yyyy-MM-dd'); $st = $s.ToString('HH:mm:ss'); $en = $e.ToString('HH:mm:ss')
  $ci = $s.AddMinutes(2).ToString('yyyy-MM-dd HH:mm:ss'); $pi = $s.ToString('yyyy-MM-dd HH:mm:ss'); $po = $now.AddMinutes(-5).ToString('yyyy-MM-dd HH:mm:ss')
  $member = "(SELECT id FROM users WHERE email='member.active@gym.test')"
  $trainer = "(SELECT id FROM users WHERE email='trainer1@gym.test')"
  $mpId = "dddddddd-0000-0000-0000-0000000000c1"
  Run-SqlI "DELETE FROM member_packages WHERE CAST(id AS VARCHAR(36)) LIKE 'dddddddd-%'; DELETE FROM packages WHERE CAST(id AS VARCHAR(36)) LIKE 'dddddddd-%'" | Out-Null
  Run-SqlI "INSERT INTO packages (id, name, price, session_count, package_type, peak_type, is_active, created_at) VALUES ('dddddddd-0000-0000-0000-000000000003', N'Goi thu do lai', 0, 100, 'SESSION_BASED', 'FULL_TIME', 1, SYSUTCDATETIME()); INSERT INTO member_packages (id, user_id, package_id, status, start_date, remaining_sessions, created_at) VALUES ('$mpId', $member, 'dddddddd-0000-0000-0000-000000000003', 'ACTIVE', DATEADD(DAY,-7,CAST(SYSUTCDATETIME() AS DATE)), 60, SYSUTCDATETIME())" | Out-Null
  $ids1 = 1..5  | ForEach-Object { "eeeeeeee-0000-0000-0000-0000000000{0:D2}" -f $_ }
  $ids2 = 1..20 | ForEach-Object { "ffffffff-0000-0000-0000-0000000000{0:D2}" -f $_ }
  $rows = ($ids1 + $ids2) | ForEach-Object { "('$_', $member, $trainer, '$d', '$st', '$en', 'CONFIRMED', '$ci', 'FACE', '$pi', 'FACE', '$po', 'FACE', N'[test-credit]')" }
  Run-SqlI ("INSERT INTO bookings (id, member_id, trainer_id, booking_date, start_time, end_time, status, check_in_time, check_in_method, pt_check_in_time, pt_check_in_method, pt_check_out_time, pt_check_out_method, notes) VALUES " + ($rows -join ",")) | Out-Null
  "--- truoc:"; Run-SqlI "SELECT remaining_sessions FROM member_packages WHERE id='$mpId'"
  "--- Phan 1: moi booking 2 request dong thoi (5 booking), ky vong con 55"
  $n = 0
  foreach ($id in $ids1) { $n++; $t1 = Send-Complete $id; $t2 = Send-Complete $id; [void][System.Threading.Tasks.Task]::WaitAll([System.Threading.Tasks.Task[]]@($t1, $t2)); "booking $n : $([int]$t1.Result.StatusCode) / $([int]$t2.Result.StatusCode)" }
  Run-SqlI "SELECT remaining_sessions AS sau_phan_1 FROM member_packages WHERE id='$mpId'"
  "--- Phan 2: 10 cap booking khac nhau cua cung hoi vien, ky vong con 35"
  for ($i = 0; $i -lt 20; $i += 2) { $t1 = Send-Complete $ids2[$i]; $t2 = Send-Complete $ids2[$i + 1]; [void][System.Threading.Tasks.Task]::WaitAll([System.Threading.Tasks.Task[]]@($t1, $t2)); "cap $(($i / 2) + 1) : $([int]$t1.Result.StatusCode) / $([int]$t2.Result.StatusCode)" }
  Run-SqlI "SELECT remaining_sessions AS con_lai FROM member_packages WHERE id='$mpId'"
  Run-SqlI "SELECT status, COUNT(*) AS so_booking FROM bookings WHERE notes = N'[test-credit]' GROUP BY status"
  Run-SqlI "SELECT COUNT(*) AS so_hoa_hong FROM commissions c JOIN bookings b ON b.id = c.booking_id WHERE b.notes = N'[test-credit]'"
}
```

Dọn dẹp (thứ tự này vì có khoá ngoại):
```powershell
Run-SqlI "DELETE c FROM commissions c JOIN bookings b ON b.id = c.booking_id WHERE b.notes = N'[test-credit]'"
Run-SqlI "DELETE FROM bookings WHERE notes = N'[test-credit]'; DELETE FROM member_packages WHERE CAST(id AS VARCHAR(36)) LIKE 'dddddddd-%'; DELETE FROM packages WHERE CAST(id AS VARCHAR(36)) LIKE 'dddddddd-%'"
```

## Phụ lục B: kiểm tra nhanh đặt lịch bằng dòng lệnh (không in token)

```powershell
$api = "http://localhost:8083"
$login = Invoke-RestMethod -Method Post -Uri "$api/api/v1/auth/login" -ContentType "application/json" -Body (@{email="member.active@gym.test"; password="Test@12345"} | ConvertTo-Json)
$token = $login.data.accessToken
function PostJson($path, $json) { $f = Join-Path $env:TEMP "req.json"; [System.IO.File]::WriteAllText($f, $json, (New-Object System.Text.UTF8Encoding($false))); curl.exe -s -w "`nHTTP %{http_code}" -X POST "$api$path" -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d "@$f" }
$body = '{"trainerId":"33333333-3333-3333-3333-333333333331","bookingDate":"<ngay mai yyyy-MM-dd>","startTime":"14:00:00","endTime":"15:00:00"}'
PostJson "/api/v1/bookings" $body      # ky vong 200 PENDING
PostJson "/api/v1/bookings" $body      # ky vong 409 BOOKING_SLOT_TAKEN
```
(Huỷ buổi vừa tạo bằng `POST /api/v1/bookings/{id}/cancel` để DB sạch lại.)

## Phụ lục C: khi bắt đầu phiên mới, Claude nên hỏi/kiểm tra trước

1. `git fetch origin` rồi `git log --oneline origin/master -8` và `git status -sb` ở `D:\SrpingBoot-GYM\gms-agent3` (nhánh hiện tại, file chưa commit).
2. Kết quả chẩn đoán 5.1 (URL request `login`, địa chỉ đang mở).
3. Agent 1 đã trả lời về cờ `sendTimeAsDatetime` chưa; endpoint thư mục của Agent 1 đã có chưa (mục 7).
4. Phép đo cho Agent 2 đã chạy chưa.
