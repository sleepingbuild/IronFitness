# Bàn giao phiên chat: Agent 2 (Gói tập, Thanh toán) của dự án GMS Spring Boot

Cập nhật ngày 2026-10-30. File này thay thế `CONVERSATION_HANDOFF.md` cũ (file cũ nói về phần viết tài liệu quan hệ giữa các bảng cho buổi thuyết trình; phần đó người dùng đã báo xong).
Đọc hết file này trước khi làm gì. Mục 9 có danh sách việc tiếp theo theo thứ tự; mục 10 có các câu cần hỏi người dùng ngay đầu phiên.

## 1. Người dùng và cách làm việc

- Người dùng là **Agent 2** (Membership, Package, Payment) trong dự án nhóm 6 agent "Gym Management System (GMS)" viết lại bằng Spring Boot 3.3 / Java 17 / SQL Server / Flyway. Giao tiếp bằng **tiếng Việt**.
- Máy Windows, **PowerShell 5.1**. Người dùng dán log console vào chat; mình (Claude) **không chạy được Maven, SQL Server, git của họ** trong sandbox. Mình chỉ viết file, kiểm tra tĩnh, và với frontend có dựng lại khung để chạy `tsc`, `vitest`, `next lint`, `next build`. Mọi khẳng định "chạy được" phải dựa vào log người dùng dán, không dựa vào mình tự nghĩ.
- Mình giao file qua zip hoặc `.ps1`; người dùng tải về `D:\AgentChange\Agent2\` rồi giải nén. Hướng dẫn kèm trong zip (`tools\HUONG_DAN_*.md`).
- Quy trình quen thuộc: **nhánh tạm `tmp/...` để thử, không commit nhánh tạm**; nhánh thật tạo từ `origin/master`; mỗi PR một việc; commit tách logic và test; trước khi mở PR phải `mvn clean verify` xanh **và** `spring-boot:run` khởi động được **và** có test **và** cập nhật `API_DESIGN.md` nếu đổi API **và** Swagger `@Operation`.
- Người dùng hay quên đặt lại biến trong cửa sổ PowerShell mới. Mỗi khối lệnh dùng `$c`, `$pw` phải kèm đoạn đặt lại hai biến (mục 8).

## 2. Môi trường

| Thứ | Giá trị |
|---|---|
| Repo | `sleepingbuild/SpringBoot-GYM-MANAGEMENT-SYSTEM`. Worktree của Agent 2: `D:\SrpingBoot-GYM\gms-agent2` (các worktree khác: `gms-agent1`, `gms-agent3` ... `gms-agent6`, `gym-management-system` giữ `master`, `gms-docs`) |
| SQL Server | container Docker **`gms-sqlserver`**, cổng 1433, user `sa`, mật khẩu lấy từ `docker inspect` (mục 8). **Không đụng** `gms-agent5-sqlserver` (cổng 14330, của Agent 5) |
| Database | `gms_db_ci` chỉ dành cho Jenkins, **tuyệt đối không dùng**. Của Agent 2: `gms_db_agent2` (backend đang dùng), `gms_db_agent2_v7` (đã thử V7 trên DB trống), `gms_db_agent2_v7data` (thử dữ liệu cũ, có thể chưa tạo), `gms_db_agent2_l2` (dự kiến cho gói refund và H3) |
| Cổng backend | **8082** (8080 là Jenkins, 8081 là bản Jenkins triển khai). Frontend 3000 |
| Biến backend | `DB_URL`, `DB_PASSWORD`, `GMS_JWT_SECRET` (**tối thiểu 32 ký tự**), `SERVER_PORT` |
| JDK | 17 (đặt `JAVA_HOME` trong mỗi cửa sổ mới, ví dụ `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`) |
| Frontend | Next.js 14.2.35, Node 24 (yêu cầu từ 20), `frontend\.env.local` với `NEXT_PUBLIC_API_BASE_URL=http://localhost:8082` |
| Git | `autocrlf` bật: working tree CRLF, index LF. Cảnh báo "LF will be replaced by CRLF" là bình thường |

## 3. Vai trò các agent và quy tắc của Agent 1 (chủ dự án kỹ thuật, duyệt và merge mọi PR)

- Agent 1: auth, file SHARED, khung frontend, Jenkins. **File SHARED không tự sửa, phải mở PR riêng tiêu đề `[shared]`:** `SecurityConfig`, `ErrorCode`, `GlobalExceptionHandler`, `AuthService`, `UserRepository`, `application.yml`, `Jenkinsfile`, `pom.xml`, `frontend/src/lib`, `frontend/src/components`.
- Agent 3: lịch PT, booking (`BookingSettledEvent`). Agent 4: điểm danh khuôn mặt, `gym_check_ins` (V8). Agent 5: POS, tồn kho, lương PT (V5). Agent 6: leads, báo cáo (V6, `ReportRepository`).
- **Migration:** không sửa file đã merge vào `master`; đổi schema bằng migration mới. `spring.flyway.out-of-order: true` đã bật nên V5, V7 áp dụng sau V9 được. Bảng đích của khoá ngoại phải có trước. Không `flyway repair`.
- **Seed:** `scripts/dev-seed/NN_ten.sql`, không nằm trong Flyway; chạy lại nhiều lần không lỗi; tham chiếu user theo email; ngày giờ tương đối theo giờ VN (`SYSUTCDATETIME() AT TIME ZONE 'UTC' AT TIME ZONE 'SE Asia Standard Time'`). File của Agent 2: `10_packages_memberships.sql`.
- **Tài khoản thử** (Agent 1 sẽ có trong `00_users.sql` và `docs/TEST_ACCOUNTS.md`, mật khẩu chung `Gym@12345`): `admin@gym.test` (SUPER_ADMIN), `reception@gym.test`, `sales@gym.test`, `trainer1@`/`trainer2@gym.test`, `member.active@`, `member.nopackage@`, `member.expired@gym.test`. **Hiện 00_users.sql chưa merge** (theo thông báo mới nhất).
- **Frontend:** chỉ sửa `src/app/(app)/<route của mình>/`, `src/features/<module>/` và dòng `owner: "Agent 2"` trong `src/config/navigation.ts`. Gọi API chỉ qua `apiFetch`, xử lý theo `errorCode`, mỗi màn đủ 4 trạng thái (đang tải, rỗng, lỗi, không có quyền), PR kèm ảnh chụp màn hình, và `lint`, `typecheck`, `test`, `build` đều xanh.
- Agent 1 trả lời bằng các file `REPLIES_*.md` và `NOTICE_*.md` (người dùng tải lên chat). Thông báo mới nhất là `NOTICE_MERGE_WAVE_DONE.md`.

## 4. Mã nguồn module Agent 2 đã có trên `master`

- **Chống vòng phụ thuộc bean:** `PaymentServiceImpl` không inject `MembershipService`. `confirmPayment` phát `PaymentConfirmedEvent(transactionId, relatedType, relatedId)` (record, `service/event`), `MembershipPaymentListener` (`service/listener`) kích hoạt gói cho `RelatedType.MEMBERSHIP`. Có `MembershipPaymentWiringTest` dựng Spring context cấm vòng phụ thuộc. **Không bật `allow-circular-references`, không inject ngược.** (PR A #48)
- `BookingSettledMembershipListener` (PR B #63): nhận `BookingSettledEvent` của Agent 3, gọi `tryConsumeSession(memberId)`, cả 3 kết cục (COMPLETED, MEMBER_NO_SHOW, LATE_CANCEL) đều trừ 1 buổi, `fullDuration` bỏ qua, không ném lỗi nghiệp vụ.
- G3 (#65): `GET /packages/{id}` chỉ trả gói đang bán (404 `PACKAGE_NOT_FOUND`), `GET /packages/admin/{id}` cho Super Admin; PR `[shared]` mở công khai hai đường GET trong `SecurityConfig`.
- H2: `GET /api/v1/member-packages/current?userId=` và `GET /api/v1/member-packages?userId=` (Lễ tân, Super Admin), kèm cập nhật `API_DESIGN.md`.
- `ErrorCode.PAYMENT_NOT_REFUNDABLE` (409) đã vào `master` theo thông báo; chưa kiểm tra bảng mã lỗi trong `API_DESIGN.md` đã có nhóm `PAYMENT_*` (script `add-error-code-doc-row.ps1` thêm 3 dòng sau `MEMBERSHIP_PENDING_EXISTS`).
- Lớp `RelatedType` **không phải enum**: là lớp hằng chuỗi (`RelatedType.MEMBERSHIP = "MEMBERSHIP"`, `RelatedType.POS = "POS"`). Agent 5 chỉ dùng `RelatedType.POS`.
- Nhà của logic: `PaymentServiceImpl` (confirm, createPending, getMy/getAll), `MembershipServiceImpl` (đăng ký, nâng/hạ cấp, `getCurrentMembership` là **nguồn chân lý duy nhất** cho "gói hiện tại", lazy-check, `tryConsumeSession`, `consumeSession`).
- API backend Agent 2 (theo `API_DESIGN.md` và controller): `/packages`, `/packages/{id}`, `/packages/admin`, `/packages/admin/{id}`, POST/PUT/DELETE `/packages`; `/member-packages` (POST), `/me`, `/me/history`, `/{id}/upgrade`, `/{id}/downgrade`, `/current?userId=`, `?userId=`; `/payments` (GET, Lễ tân và Admin), `/payments/me`, `/payments/{id}/confirm` (POST, body tuỳ chọn).

## 5. Việc đang dở, theo từng hạng mục

### 5.1 LỖI mất cập nhật khi trừ buổi (ưu tiên cao nhất, chưa sửa)
Agent 3 đo trên SQL Server thật: hai booking khác nhau của cùng một hội viên chốt đồng thời thì `remaining_sessions` chỉ giảm 1 (10/10 cặp). Nguyên nhân: `tryConsumeSession` đọc, trừ trong bộ nhớ, `save`. Phần "một booking phát hai event" đã đúng.
**Hướng sửa đã chọn:** cập nhật nguyên tử trong DB.
- Thêm vào `MemberPackageRepository`: `@Modifying @Query("UPDATE MemberPackage m SET m.remainingSessions = m.remainingSessions - 1 WHERE m.id = :id AND m.status = :active AND m.remainingSessions > 0") int consumeOneSession(UUID id, String active)` (thêm `flushAutomatically = true` để ghi trước các thay đổi đang chờ; **không dùng `clearAutomatically`** vì sẽ tách `Booking` của Agent 3 khỏi phiên làm việc trong cùng giao dịch).
- `tryConsumeSession`: lấy `getCurrentMembership`; rỗng thì `NO_ACTIVE_MEMBERSHIP`; `remainingSessions == null` thì `UNLIMITED` (không cập nhật); ngược lại gọi câu cập nhật, 1 dòng là `CONSUMED`, 0 dòng là `NO_SESSION_LEFT`. Không ném lỗi nghiệp vụ. Giá trị trong bộ nhớ sau khi trừ sẽ cũ, vô hại vì quyết định nằm ở DB.
- **Không dùng `@Version`**: xung đột phiên bản sẽ rollback việc chốt buổi của Agent 3.
- Tuỳ chọn: `@DynamicUpdate` trên `MemberPackage` để một lần đổi `status` không ghi đè `remaining_sessions` bằng giá trị cũ.
- Test: Mockito không bắt được tranh chấp. Thêm test đơn vị (dùng câu cập nhật nguyên tử, không `save` để trừ, 4 kết quả) và **nhờ Agent 3 chạy lại phép đo** của họ (họ đã đề nghị).
- Cần các file: `MembershipServiceImpl.java`, `MemberPackage.java`, `MemberPackageRepository.java`, `MembershipServiceImplTest.java` bản trên `master` (lệnh ở mục 9, bước 1; người dùng chưa tải `race-prep.txt`).
- Sau khi sửa: soạn tin ngắn cho Agent 3 và Agent 1.

### 5.2 V7 (CH1): tách `payment_transactions.related_id`
Gói: `agent2-v7-payment-split.zip` (đã chép vào `D:\AgentChange\Agent2\`). Nội dung: migration `V7__payment_transactions_split_related.sql` (thêm `member_package_id`, `pos_order_id`, chuyển dữ liệu, `CHECK` đúng một trong hai, khoá ngoại, bỏ `related_type`/`related_id`, `user_id` nullable, có `GO` ngăn batch), `PaymentTransaction` (hai cột, `getRelatedType/getRelatedId/setRelated` suy ra), `PaymentTransactionRepository` (thay truy vấn dẫn xuất cũ), `PaymentServiceImpl` (kiểm tra `relatedType`, `toResponse` an toàn khi `user` null, bean `Clock`), `ReportRepository` (sửa hai chuỗi SQL `revenueByDay` và `topPackages` của Agent 6), 3 file test.
- Đã có bằng chứng: trên database trống (với V5 mượn từ nhánh Agent 5) V1 đến V9 và V7 đều `success = 1`, danh sách cột `payment_transactions` đúng.
- **Chưa có:** kết quả thử với dữ liệu cũ và V7 chạy sau V9 (`tools/HUONG_DAN_V7_DU_LIEU_CU.md`, các file `v7-insert-legacy-data.sql`, `v7-check.sql` nằm trong `agent2-batch-h2-seed-v7test.zip`; người dùng nói đã làm "xong hết" nhưng **chưa dán kết quả bước E**). Nay V5 đã trong `master` nên bỏ dòng `git checkout ... V5__pos_commission.sql` ở bước B.
- Chưa mở PR V7. Mô tả PR soạn sẵn: `PR_V7_DESCRIPTION.md` (có chỗ cần dán bằng chứng). Điều kiện Agent 1 đặt trước khi merge: (a) bằng chứng trên DB có dữ liệu cũ, (b) Agent 6 xem và xác nhận hai truy vấn trong `ReportRepository`, (c) Agent 1 chạy ba truy vấn kiểm tra trên `gms_db_ci` (có trong mục 5 của mô tả PR). Rủi ro: dòng giao dịch cũ không khớp trong `gms_db_ci` làm V7 dừng và ứng dụng không khởi động.
- Khi nộp PR thật: nhánh mới từ `origin/master`, chép gói, `mvn clean verify`, chạy app, dán bằng chứng. Tên nhánh gợi ý `feature/agent2-v7-payment-split`.
- Đơn khách vãng lai của POS (user null) chỉ chạy sau V7. Sau V7, Agent 5 phải thử một đơn có hội viên và một đơn vãng lai qua Swagger (khoá ngoại đòi dòng `pos_orders` được ghi trước dòng giao dịch).

### 5.3 Hoàn tiền (refund) và H3: viết xong nhưng cần sửa tên và làm sau V7
Gói: `agent2-refund-h3.zip` (viết chồng lên V7). Nội dung: `PaymentStatus.REFUNDED`; `refundPayment(transactionId)` (chỉ hoàn giao dịch POS đã `SUCCESS`; gọi lần hai khi đã REFUNDED thì không lỗi, không hoàn thêm; giao dịch gói tập hoặc chưa SUCCESS ném `PAYMENT_NOT_REFUNDABLE`; không có giao dịch ném `PAYMENT_NOT_FOUND`); H3: `GET /payments` thêm `status`, `userId`, `userFullName` trong response.
- **Tên method theo quyết định mới nhất của Agent 1 (`NOTICE_MERGE_WAVE_DONE.md`):** `refundByRelated`. Vì `RelatedType` không phải enum, chữ ký thực tế là **`refundByRelated(String relatedType, UUID relatedId)`**: `POS` thì tìm giao dịch mới nhất theo `pos_order_id` rồi hoàn tiền; `MEMBERSHIP` thì ném `PAYMENT_NOT_REFUNDABLE`; không có giao dịch của đơn thì `PAYMENT_NOT_REFUNDABLE` (quy tắc Agent 1 đã chốt cho trường hợp theo đơn POS). Gói hiện còn tên cũ `refundPaymentByPosOrder` (và ném `PAYMENT_NOT_FOUND` khi thiếu): **phải sửa trước khi nộp**. Giữ thêm `refundPayment(transactionId)`.
- H3, quy tắc ngày: truyền `from` hoặc `to` thì lọc theo ngày (thiếu vế nào mặc định hôm nay theo `Clock`); không truyền cả hai và không có `status`/`userId` thì lấy hôm nay; có `status` hoặc `userId` thì lấy mọi ngày. `status` ngoài PENDING, SUCCESS, FAILED, REFUNDED ném `VALIDATION_ERROR`. Dùng truy vấn dẫn xuất riêng cho từng tổ hợp (SQL Server + Hibernate bind UUID null thành varbinary nên dễ lỗi chuyển kiểu) và `@EntityGraph(attributePaths = "user")` để lấy tên không bị N+1; thêm `@Transactional(readOnly = true)` cho các hàm đọc. `API_DESIGN.md` cập nhật bằng `update-api-design-h3.ps1` trong zip.
- Thứ tự Agent 1 đã chốt: V7 trước, rồi refund và H3 (cùng đợt với mã lỗi `[shared]` đã merge). Cách thử trong `tools/HUONG_DAN_REFUND_H3.md` (nhánh tạm ghép V7 + gói này).
- Agent 5 gọi `refundByRelated` khi huỷ đơn POS; **Agent 5 và Agent 1 cần được báo** rằng tham số là `String`, không phải enum.

### 5.4 Frontend đợt 1 (nhánh `feature/agent2-frontend`)
Gói: `agent2-frontend-batch1.zip` (35 file). Đã áp dụng trên máy người dùng; **`npm run lint`, `typecheck`, `test` (49 test, 31 của module), `build` đều xanh** trên máy thật. Chưa rõ đã `commit`/`push` và mở PR chưa (người dùng cần: `git add frontend/src/features/membership` rồi `git add frontend/src/config/navigation.ts "frontend/src/app/(app)"`, **không** `git add` đường dẫn có `[id]`).
- Cấu trúc `src/features/membership/`: `types.ts`, `labels.ts`, `format.ts`, `messages.ts`, `packageFormLogic.ts` (+test, **đã đổi tên từ `packageForm.ts`** vì trùng tên với `PackageForm.tsx` khi bỏ qua hoa thường trên Windows), `changeOptions.ts`, `api.ts`, `useLoad.ts`, `Feedback.tsx`, `StatusBadge.tsx`, `Pagination.tsx`, `Tabs.tsx`, `FormControls.tsx`, `Tables.tsx`, `PackageList.tsx`, `PackageDetail.tsx`, `MyMembership.tsx`, `ChangePlan.tsx`, `PaymentsDesk.tsx`, `PendingPayments.tsx`, `SellForMember.tsx`, `MemberPicker.tsx`, `AdminPackages.tsx`, `PackageForm.tsx`. Trang: `(app)/packages/page.tsx`, `packages/[id]/page.tsx`, `my-membership`, `payments`, `admin/packages`.
- Quyết định đáng nhớ: backend trả `message` bằng đúng tên mã lỗi khi không có lời giải thích riêng, nên `membershipErrorMessage` coi `message === errorCode` là "chưa có message" và dùng bảng tiếng Việt (đề xuất Agent 1 sửa gốc trong `lib/api/errors.ts`; chưa gửi). Nâng cấp huỷ gói cũ NGAY (cảnh báo rõ); hạ cấp giữ gói cũ đến hết hạn. `/me` chỉ trả gói ACTIVE nên màn dùng thêm lịch sử để báo gói chờ thanh toán. Xác nhận thanh toán không gửi body (giữ phương thức mặc định `CASH`). `MemberPicker` gọi `GET /users` (Agent 1, chưa có), lỗi thì cho nhập UUID. Quy tắc form gói theo loại (thời hạn cần số ngày; theo buổi và PT cần số buổi) do giao diện áp, chặt hơn backend. Chưa gửi `branchId`.
- Đã thử bằng tay (người dùng báo): thấy 4 gói đang bán và trang Gói của tôi. **Chưa xác nhận:** đăng ký gói, báo "đang có gói chờ thanh toán", đổi gói, và các màn Lễ tân/Admin (chưa có tài khoản vai trò; bảng vai trò chưa được liệt kê nên chưa viết SQL nâng quyền).
- **Hai bẫy đã gặp:** `.env.local` bị ghi UTF-16 làm biến API biến mất (giao diện lặng lẽ trỏ về 8081, là bản Jenkins với `gms_db_ci` trống); nay đã ghi đúng UTF-8 không BOM. `curl -s` không in gì khi 403.
- Còn thiếu cho PR: ảnh chụp màn hình (danh sách gói, chi tiết sau đăng ký, Gói của tôi có cảnh báo chờ, hộp xác nhận nâng cấp, Thanh toán, Quản trị gói, một màn lỗi hoặc không có quyền), rồi mở PR theo mô tả trong `tools/HUONG_DAN_FRONTEND.md`.
- Việc frontend kế tiếp: UI cho H2 (Lễ tân tra gói hội viên, nâng/hạ cấp hộ; H2 đã vào `master`), UI cho H3 (sau V7 và H3), `GET /branches` khi Agent 1 có (cần xác nhận hành vi `branchId` bỏ trống, mục H4).

### 5.5 Seed `10_packages_memberships.sql`
Có trong `agent2-batch-h2-seed-v7test.zip` (`repo\scripts\dev-seed\`). Đã chạy thật trên `gms_db_agent2`: tạo 5 gói (4 đang bán, 1 ngừng bán), bỏ qua phần hội viên vì chưa có tài khoản, bỏ qua giao dịch mẫu vì chưa qua V7. **Chưa xác nhận chạy lần hai không nhân đôi** (lệnh kiểm tra `COUNT(*)` đã đưa). Chưa commit/PR (PR nhỏ riêng, `chore(seed): ...`). Phần giao dịch mẫu chỉ chạy sau V7 (kiểm tra bằng `COL_LENGTH`).

### 5.6 H4: hành vi khi bỏ trống `branchId`
Chưa trả lời. Từ log: `MembershipServiceImpl` có tra chi nhánh (`RESOURCE_NOT_FOUND` "Không tìm thấy chi nhánh") khi có `branchId`. Cần đọc `registerNewPackage` để xác nhận `branchId` null có hợp lệ không. Agent 1 sẽ thêm `GET /branches`.

## 6. Trạng thái các agent khác (theo `NOTICE_MERGE_WAVE_DONE.md`)

- Đã vào `master`: khung frontend, G3 + mã lỗi hoàn tiền + H2 của Agent 2, V5 của Agent 5, V8 và `GET /check-ins/me` của Agent 4, frontend của Agent 3 và 4.
- **Chờ merge (Agent 1):** `GET /users`, `/trainers`, `/branches`, 401 JSON; `00_users.sql` + `docs/TEST_ACCOUNTS.md`; ba mã lỗi POS.
- V8 và các PR khác: không thuộc Agent 2.

## 7. Bẫy kỹ thuật đã gặp (tránh lặp lại)

- PowerShell 5.1: `>` và `Out-File` mặc định ghi UTF-16 (hỏng `.env.local`); dùng `[IO.File]::WriteAllText($f,$t,(New-Object Text.UTF8Encoding($false)))`. Đường dẫn .NET (`[IO.File]::`) tính từ thư mục tiến trình, **không** theo `cd` của PowerShell: dùng `Join-Path (Get-Location) ...`.
- Regex trong script cho file Windows: **không đặt `$` ở cuối mẫu** (CRLF làm `$` không khớp), chỉ dùng `[^\r\n]*`.
- `.ps1` có tiếng Việt phải lưu **UTF-8 có BOM**. `powershell -File` cần đường dẫn đầy đủ tới file `.ps1` (đường dẫn thư mục sẽ mở một PowerShell lồng bên trong).
- `git add` với đường dẫn có `[id]` bị hiểu là mẫu ký tự; thêm theo thư mục cha.
- Windows không phân biệt hoa thường: hai file chỉ khác hoa thường (`packageForm.ts` và `PackageForm.tsx`) gây lỗi import mà Linux không thấy.
- `docker exec $c ...` khi `$c` rỗng báo "No such container: opt/mssql-tools18/bin/sqlcmd"; `docker cp ... "${c}:/..."` báo "must specify at least one container source". Nguyên nhân là cửa sổ mới chưa có biến.
- SQL Server: cột vừa `ADD` không dùng được trong cùng batch, phải ngăn bằng `GO`. Tránh một truy vấn có tham số UUID tuỳ chọn.
- `spring-boot:run` dừng "Port 8081 already in use" thì đổi `SERVER_PORT`; thiếu `GMS_JWT_SECRET` thì dừng ngay lúc khởi động.
- Sandbox của mình: dùng `mkdir -p` từng đường dẫn riêng (shell không mở rộng `{a,b}`, tạo thư mục tên sai); thư mục làm việc bị dọn giữa các phiên, nên **các file trong `/mnt/user-data/outputs` có thể không còn**; bản người dùng đã tải nằm ở `D:\AgentChange\Agent2\`.

## 8. Khối lệnh dùng lại

Đặt lại biến (cửa sổ PowerShell mới):
```powershell
$c = "gms-sqlserver"
$pw = (docker inspect $c --format "{{range .Config.Env}}{{println .}}{{end}}" |
       Select-String '^(MSSQL_SA_PASSWORD|SA_PASSWORD)=' | Select-Object -First 1).ToString().Split('=',2)[1]
if (-not $pw) { Write-Host "KHONG lay duoc mat khau" } else { Write-Host "Da lay duoc mat khau" }
```
Tạo database riêng và chạy backend:
```powershell
docker exec $c /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$pw" -C -Q "IF DB_ID('<ten_db>') IS NULL CREATE DATABASE <ten_db>"
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=<ten_db>;encrypt=true;trustServerCertificate=true"
$env:DB_PASSWORD = $pw
$env:GMS_JWT_SECRET = -join ((48..57)+(65..90)+(97..122) | Get-Random -Count 60 | ForEach-Object {[char]$_})
$env:SERVER_PORT = "8082"
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```
Kiểm tra Flyway: `docker exec $c /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$pw" -C -d <ten_db> -W -Q "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank"`.
Dọn nhánh tạm: `git reset --hard; git clean -fd -- src; git switch <nhanh>; git branch -D tmp/<ten>`.

## 9. Việc tiếp theo, theo thứ tự đề xuất

1. **Sửa lỗi tranh chấp trừ buổi (5.1).** Nhờ người dùng chạy và tải `race-prep.txt`:
```powershell
cd D:\SrpingBoot-GYM\gms-agent2
git fetch origin --prune
[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
$o = "origin/master"
$out = "D:\AgentChange\Agent2\race-prep.txt"
& {
  "##### MembershipServiceImpl.java"; git show "${o}:src/main/java/com/gym/management/service/impl/MembershipServiceImpl.java"
  "##### MemberPackage.java"; git show "${o}:src/main/java/com/gym/management/entity/MemberPackage.java"
  "##### MemberPackageRepository.java"; git show "${o}:src/main/java/com/gym/management/repository/MemberPackageRepository.java"
  "##### MembershipServiceImplTest.java"; git show "${o}:src/test/java/com/gym/management/service/MembershipServiceImplTest.java"
} | Out-File -FilePath $out -Encoding utf8
```
   Viết bản sửa (gói zip kèm `HUONG_DAN`), cập nhật test, rồi soạn tin trả lời Agent 3 (nhờ chạy lại phép đo) và Agent 1. Cũng dùng file này trả lời H4.
2. **Hoàn tất bằng chứng V7** (bước E với dữ liệu cũ, trên `master` có sẵn V5), điền mô tả PR, mở PR V7. Nhắc Agent 6 xác nhận `ReportRepository`, Agent 1 chạy ba truy vấn trên `gms_db_ci`.
3. **Frontend:** commit/push và mở PR; thử đăng ký gói và hết các bước; tạo tài khoản Lễ tân/Admin để thử (cần danh sách cột bảng vai trò: `INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME LIKE '%role%'`, hoặc chờ `00_users.sql`); chụp ảnh.
4. **Seed PR** (xác nhận chạy lại không nhân đôi, commit, mở PR).
5. Sau V7 vào `master`: sửa gói refund và H3 theo `refundByRelated(String, UUID)` (mục 5.3), thử, mở PR (nhánh mới từ `master`, `git diff` chỉ được có thay đổi của refund và H3).
6. UI cho H2 (và sau đó H3); H4 khi có `GET /branches`.

## 10. Câu cần hỏi người dùng ngay đầu phiên

- Đã tải `race-prep.txt` chưa?
- PR frontend (`feature/agent2-frontend`) đã push và mở chưa? PR seed? Số các PR đã mở (H2, `[shared]` mã lỗi, v.v.) nếu có.
- Kết quả bước E của thử V7 với dữ liệu cũ (nếu đã chạy).
- Kết quả `git log --oneline -12 origin/master` mới nhất (để biết 00_users, `GET /users`, V7 đã vào chưa).
- Họ đã nhắn Agent 5 về tên `refundByRelated`, nhắn Agent 3 về lỗi tranh chấp chưa.

## 11. Danh sách file đã giao (bản người dùng giữ ở `D:\AgentChange\Agent2\`)

| File | Dùng cho |
|---|---|
| `agent2-v7-payment-split.zip` | V7 (5.2) |
| `PR_V7_DESCRIPTION.md` | Mô tả PR V7 |
| `agent2-batch-h2-seed-v7test.zip` | H2 (đã merge), seed `10_`, script `update-api-design-h2.ps1`, SQL và hướng dẫn thử V7 với dữ liệu cũ |
| `agent2-refund-h3.zip` | Refund và H3 (5.3), cần sửa tên method |
| `agent2-frontend-batch1.zip` | Frontend đợt 1 (5.4) |
| `add-error-code-payment-not-refundable.ps1`, `add-error-code-doc-row.ps1` | PR `[shared]` mã lỗi hoàn tiền (đã merge theo thông báo) |
| `update-navigation.ps1` (bản sửa lỗi CRLF) | Đổi 4 dòng menu của Agent 2 sang `ready` |
| `NOTICE_AGENT2_STATUS.md`, `NOTICE_AGENT2_V7_IMPACT.md`, `NOTICE_AGENT2_FRONTEND_PLAN.md` | Thông báo đã soạn gửi Agent 1, 5, 6 |
| `agent2-fix-circular-dependency-v2.zip`, `agent2-g3-public-packages.zip`, `agent2-prb-booking-settled-listener.zip` | Các gói cũ, đã merge, chỉ để tham khảo |
