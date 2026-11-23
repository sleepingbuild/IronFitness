# Phản hồi cho Agent 4 (từ Agent 1 + Phi)

## Phần B — xác nhận file thật

- **B.1 `pom.xml`:** không có dependency xử lý ảnh/ML nào (chỉ web, JPA, security, Redis, JWT, springdoc, mssql-jdbc, flyway). → quyết định A.1 bên dưới đi theo hướng KHÔNG cần thêm thư viện.
- **B.2 `roles.name`:** đúng như giả định — DB lưu `SUPER_ADMIN`/`RECEPTIONIST`/`TRAINER`/`SALES`/`MEMBER`, in hoa, không có tiền tố `ROLE_`. Dùng an toàn để route quét mặt sang Booking hay StaffAttendance.
- **B.3 `UserRepository`:** đúng chuẩn `JpaRepository<User, UUID>`, có sẵn `findById`/`getReferenceById`, cộng thêm `findByEmail`/`existsByEmail`.

## Phần A — quyết định của Phi

### A.1 Đăng ký hộ bằng ảnh tĩnh — chọn hướng thứ 3, không phải (a) hay (b)

Không cần thêm thư viện Java, cũng không bắt Lễ tân dùng webcam trực tiếp. `face-api.js` trích descriptor được từ cả thẻ `<img>`, không chỉ từ luồng webcam — nên:

- Lễ tân chọn file ảnh trong trình duyệt (`<input type="file">`).
- Trình duyệt tự load ảnh vào `<img>` ẩn, `face-api.js` trích descriptor y hệt luồng webcam.
- Backend **chỉ nhận descriptor JSON**, giống hệt luồng tự đăng ký — không có endpoint multipart, không xử lý ảnh phía server.

**Việc cần sửa:** bỏ `registerForWithImage()` và nhánh `UnsupportedOperationException`. Đổi `POST /api/v1/face-profiles/{userId}` trong `API_DESIGN.md` từ multipart sang nhận `FaceDescriptorRequest` (JSON) giống `POST /face-profiles/me`, chỉ khác `{userId}` trong path và role yêu cầu (Lễ tân/Admin thay vì chính chủ).

### A.2 Ca chấm công mặc định toàn hệ thống — giữ nguyên cho v1, có hướng cải thiện sau

Ca cố định 07:00–21:00 cho mọi người sẽ làm sai lệch "Đi muộn/Về sớm" với PT có ca lệch giờ (vd ca 14:00 vào lúc 8h vẫn bị tính "đúng giờ"). Hướng đúng hơn: lấy ca từ `trainer_schedules` của chính ngày đó (cần Agent 3 cung cấp 1 port đọc, giống `MembershipLookupPort` Agent 3 đã làm cho Agent 2). Không có ca hôm đó → hiển thị `NOT_SCHEDULED` thay vì áp ca mặc định.

**Cho v1 bây giờ:** giữ nguyên giải pháp ca cố định 07:00–21:00 của bạn, không chặn tiến độ. Ghi việc này vào mục "chưa làm" để làm ở bản sau nếu còn thời gian (không bắt buộc trong 10 tuần).

### A.3 Race-condition — thêm chống quét đúp bằng Redis, bỏ `userLocks` in-memory

Lock in-memory đúng cho 1 instance, nhưng **không cứu được** lỗi "1 lần quét nhảy thành cả check-in lẫn check-out" — lỗi đó là 2 request cách nhau vài giây, không phải race condition đồng thời. Cách xử lý: dùng key `gms.face-attendance.min-scan-interval-seconds` (đã thêm = 120 giây vào `application.yml.example`) — lượt quét thứ 2 trong khoảng đó thì không đổi trạng thái, trả về response "đã ghi nhận trước đó" thay vì tạo bản ghi mới.

Cách làm: `StringRedisTemplate.opsForValue().setIfAbsent("face-scan:" + userId, "1", Duration.ofSeconds(minScanIntervalSeconds))` — nếu trả `false` (key đã tồn tại) thì bỏ qua lượt quét này. `RedisConfig`/`StringRedisTemplate` đã có sẵn (Agent 1), dùng lại luôn, không cần tự cấu hình Redis. Thay thế được `userLocks` (không bao giờ dọn, tăng dần theo số user).

### A.4 `reference_image` — không lưu ảnh, chỉ lưu descriptor

v1 chỉ lưu `descriptor` (JSON 128 số), **không lưu ảnh gốc**. Ảnh mặt là dữ liệu nhạy cảm; vì descriptor được trích ở client nên server chưa từng thấy ảnh gốc để lưu. Nhu cầu "lưu mặt vào DB" đã được đáp ứng đầy đủ qua `descriptor`. Để cột `reference_image` nullable trong entity, không dùng đến ở v1 — không cần xoá khỏi migration, chỉ đơn giản luôn `null`.

## Việc phải làm ngay (bên cạnh 4 quyết định trên)

Xoá file tạm không có trong bản giao của bạn (`CurrentUserId`, `CurrentUserRole` nếu bạn tự tạo thêm bản riêng) — 2 file thật đã push lên `master`, cách gọi giữ nguyên như bạn đang dùng.

## Việc còn thiếu trước khi coi Issue 4.x là "Done"

- Sửa theo A.1, A.3 ở trên trước khi viết test.
- 3 bộ test đã liệt kê trong README của bạn (`FaceMatchServiceImplTest`, `FaceAttendanceServiceImplTest`, `StaffAttendanceServiceImplTest`) — thêm test cho nhánh chống quét đúp (A.3) vào `FaceAttendanceServiceImplTest`.
- `mvn clean verify` thật — làm sau Agent 2 và Agent 3 đã ghép xong (bạn phụ thuộc `BookingService.recordFaceScan`).