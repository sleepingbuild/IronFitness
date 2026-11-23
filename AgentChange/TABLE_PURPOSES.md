# TABLE_PURPOSES.md — Ý nghĩa và nhiệm vụ của từng bảng

Giải thích 19 bảng của cơ sở dữ liệu theo **nghiệp vụ của phòng gym**: bảng đại diện cho cái gì ngoài đời thực, ai dùng, quy tắc nghiệp vụ nào gắn với nó. Mô tả theo thiết kế v2; bảng `gym_check_ins` và các thay đổi V7 đến V9 chưa triển khai. Kiểu dữ liệu và khoá xem `DATABASE_SCHEMA.md`; quan hệ giữa các bảng xem `TABLE_RELATIONSHIPS.md`.

## 1. Bảng thuộc về nhóm nghiệp vụ nào

| Nhóm nghiệp vụ | Bảng |
|---|---|
| Người dùng và phân quyền | `roles`, `users`, `user_roles`, `user_profiles`, `branches` |
| Gói tập và thanh toán | `packages`, `member_packages`, `payment_transactions` |
| Lịch tập và điểm danh | `trainer_schedules`, `bookings`, `face_profiles`, `staff_attendances`, `gym_check_ins` |
| Bán lẻ tại quầy | `products`, `pos_orders`, `pos_order_items` |
| Lương PT | `trainer_pay_rates`, `commissions` |
| Khách tiềm năng | `leads` |

## 2. Câu chuyện xuyên suốt: một hội viên đi qua các bảng như thế nào

1. **Đăng ký tài khoản:** tạo dòng trong `users`, gán vai trò `MEMBER` trong `user_roles`, hồ sơ cá nhân trong `user_profiles`.
2. **Mua gói:** chọn một dòng của `packages`; hệ thống tạo `member_packages` (chờ thanh toán) và một dòng `payment_transactions`. Lễ tân xác nhận đã nhận tiền thì gói thành `ACTIVE` và nạp số buổi.
3. **Đăng ký khuôn mặt:** lưu vector vào `face_profiles` để điểm danh không cần thẻ.
4. **Đặt lịch với PT:** xem ca làm việc trong `trainer_schedules`, tạo `bookings`. PT xác nhận thì buổi thành `CONFIRMED`.
5. **Tập:** hội viên và PT cùng quét mặt, thời gian ghi vào `bookings`. Nếu hội viên chỉ vào tập không có PT thì lượt vào ghi ở `gym_check_ins`.
6. **Chốt buổi:** PT bấm hoàn thành. Hệ thống trừ 1 buổi ở `member_packages` và ghi một dòng lương ở `commissions` theo mức trong `trainer_pay_rates`.
7. **Mua đồ tại quầy:** lễ tân lập `pos_orders` kèm các dòng `pos_order_items` của `products`, thu tiền qua `payment_transactions`.
8. **Hết hạn:** gói chuyển `EXPIRED`; Sales thấy hội viên sắp hết hạn để chăm sóc. Khách chưa là hội viên được Sales ghi trong `leads` cho tới khi chuyển đổi thành hội viên.

## 3. Từng bảng

### Nhóm: Người dùng và phân quyền

#### 1. `roles` — Vai trò

**Ý nghĩa:** Danh mục các vai trò trong phòng gym. Hệ thống có đúng 5 vai trò cố định: `SUPER_ADMIN` (chủ phòng gym), `RECEPTIONIST` (lễ tân, thu ngân), `TRAINER` (huấn luyện viên), `SALES` (nhân viên kinh doanh, chăm sóc khách hàng) và `MEMBER` (hội viên).

**Nhiệm vụ trong hệ thống:**
- Quy định ai được làm gì: mỗi API kiểm tra vai trò của người gọi (ví dụ chỉ lễ tân mới xác nhận thanh toán tại quầy, chỉ PT mới xác nhận buổi tập).
- Là dữ liệu danh mục, được tạo sẵn lúc khởi tạo hệ thống; người dùng bình thường không thêm hay sửa vai trò.

**Ai dùng:** Chỉ hệ thống đọc khi đăng nhập và phân quyền.

**Quy tắc nghiệp vụ:**
- Tên vai trò là duy nhất; mã trong code phải khớp đúng 5 giá trị trên.

**Ví dụ:** Dòng `MEMBER` có mô tả "Hội viên - xem gói tập, đặt lịch PT".

**Use case liên quan (6):** G1, G2, R22, S4, A18, A19

#### 2. `users` — Tài khoản người dùng

**Ý nghĩa:** Tài khoản đăng nhập của **mọi người** trong phòng gym: hội viên, PT, lễ tân, Sales và quản trị viên. Đây là bảng trung tâm của hệ thống, hầu như mọi nghiệp vụ đều gắn với một người dùng.

**Nhiệm vụ trong hệ thống:**
- Xác thực: đăng nhập bằng email và mật khẩu (mật khẩu chỉ lưu dạng đã băm, không bao giờ trả ra API).
- Xác định "ai" trong mọi nghiệp vụ: ai đặt lịch, ai dạy, ai lập đơn, ai được trả lương.
- Khoá tài khoản: đổi trạng thái thay vì xoá, vì người dùng đã có lịch sử gói tập, thanh toán, buổi tập.

**Ai dùng:** Hội viên tự đăng ký; lễ tân tạo hộ hội viên; quản trị viên tạo tài khoản nhân viên; Sales chuyển khách tiềm năng thành hội viên.

**Quy tắc nghiệp vụ:**
- Email là duy nhất (dùng để đăng nhập). Số điện thoại là duy nhất nếu có, nhưng được phép để trống.
- `status`: `ACTIVE` (đang dùng), `INACTIVE`, `BANNED` (bị khoá). Tài khoản không `ACTIVE` không đăng nhập được.
- Không xoá cứng người dùng đã có lịch sử; chỉ khoá.

**Ví dụ:** Nguyễn Văn A, email `a@gmail.com`, có vai trò `MEMBER`, trạng thái `ACTIVE`.

**Use case liên quan (16):** G1, G2, M1, T1, T14, R1, R22, S3, S4, S5, A9, A10, A12, A18, A20, A22

#### 3. `user_roles` — Phân quyền người dùng

**Ý nghĩa:** Bảng trung gian nối người dùng với vai trò: một người có thể có một hoặc nhiều vai trò, một vai trò thuộc về nhiều người.

**Nhiệm vụ trong hệ thống:**
- Gán vai trò khi tạo tài khoản (hội viên mới mặc định có `MEMBER`).
- Quản trị viên đổi vai trò khi nhân sự đổi chức vụ.

**Ai dùng:** Quản trị viên (gán và đổi vai trò), hệ thống (gán `MEMBER` khi đăng ký hoặc chuyển đổi khách tiềm năng).

**Quy tắc nghiệp vụ:**
- Mỗi cặp (người dùng, vai trò) chỉ xuất hiện một lần.
- Xoá người dùng hoặc xoá vai trò thì xoá các dòng phân quyền tương ứng.

**Ví dụ:** Dòng (user = Trần Thị B, role = `TRAINER`) cho phép B vào chức năng của huấn luyện viên.

**Use case liên quan (6):** G1, G2, R22, S4, A18, A19

#### 4. `user_profiles` — Hồ sơ cá nhân

**Ý nghĩa:** Thông tin cá nhân mở rộng của hội viên và PT: ngày sinh, cân nặng, chiều cao, mục tiêu tập luyện, ảnh đại diện. Tách riêng khỏi tài khoản để bảng tài khoản chỉ giữ thông tin đăng nhập.

**Nhiệm vụ trong hệ thống:**
- Hội viên tự xem và cập nhật hồ sơ của mình, tải ảnh đại diện.
- PT xem chỉ số của hội viên đang huấn luyện để lên giáo án.
- Kiểm tra độ tuổi: ngày sinh phải cách ngày đăng ký tài khoản ít nhất 18 năm.

**Ai dùng:** Hội viên và PT (chính chủ), lễ tân khi tạo tài khoản hộ.

**Quy tắc nghiệp vụ:**
- Mỗi người có tối đa một hồ sơ.
- Mọi chỉ số đều tuỳ chọn, người dùng điền dần.
- Ảnh đại diện lưu ở đây là nguồn duy nhất (không lưu ở bảng tài khoản).

**Ví dụ:** Hội viên A: sinh 2000, 70 kg, cao 175 cm, mục tiêu "giảm mỡ, tăng cơ".

**Use case liên quan (7):** M1, M2, M3, T1, T2, T14, R22

#### 5. `branches` — Chi nhánh

**Ý nghĩa:** Các cơ sở của phòng gym. Hệ thống có sẵn một chi nhánh mặc định ("Chi nhánh Trung tâm").

**Nhiệm vụ trong hệ thống:**
- Cho biết một gói tập được bán ở đâu, một buổi tập diễn ra ở đâu, một đơn bán lẻ lập ở quầy nào, một lượt vào phòng ở cơ sở nào.
- Là cơ sở để lọc danh sách buổi tập và đơn hàng theo chi nhánh.

**Ai dùng:** Quản trị viên quản lý chi nhánh; lễ tân, hệ thống ghi chi nhánh khi phát sinh giao dịch.

**Quy tắc nghiệp vụ:**
- `status`: `ACTIVE` hoặc ngừng hoạt động.
- Cột chi nhánh ở các bảng khác là tuỳ chọn, nên hệ thống vẫn chạy khi chỉ có một chi nhánh.

**Ví dụ:** Chi nhánh "Trung tâm", địa chỉ 12 Nguyễn Trãi.

**Use case liên quan (3):** R10, A13, A21

### Nhóm: Gói tập và thanh toán

#### 6. `packages` — Gói tập

**Ý nghĩa:** Danh mục các gói tập đang bán của phòng gym, do quản trị viên định nghĩa.

**Nhiệm vụ trong hệ thống:**
- Khách và hội viên xem để chọn mua; lễ tân dùng khi bán gói hộ.
- Quyết định thời hạn, số buổi và giới hạn đặt lịch PT của hội viên sau khi mua.
- Nâng cấp hay hạ cấp được xác định bằng cách so giá gói mới với gói đang dùng.

**Ai dùng:** Quản trị viên (thêm, sửa, ngừng bán), mọi người xem.

**Quy tắc nghiệp vụ:**
- `package_type`: `TIME_BASED` (theo thời gian, ví dụ 3 tháng), `SESSION_BASED` (theo số buổi), `PT_1ON1` (gói kèm huấn luyện viên).
- `peak_type`: `OFF_PEAK` (giờ thấp điểm) hoặc `FULL_TIME` (cả ngày).
- `max_sessions_per_week`: trống là không giới hạn, `0` là không được đặt PT, `N` là tối đa N buổi mỗi tuần (tuần tính từ Thứ Hai đến Chủ Nhật).
- Gói ngừng bán chỉ đặt `is_active = 0`, không xoá vì các hội viên cũ vẫn tham chiếu tới.

**Ví dụ:** Gói "PT 12 buổi": 3.600.000 đ, 12 buổi, tối đa 3 buổi mỗi tuần.

**Use case liên quan (20):** G3, M4, M5, M6, M7, M8, M11, M12, R1, R2, R3, R4, R7, R8, S5, A1, A2, A3, A4, A8

#### 7. `member_packages` — Đăng ký gói tập của hội viên

**Ý nghĩa:** Mỗi lần một hội viên mua một gói là một dòng. Đây là "thẻ tập" của hội viên và là thực thể kết hợp giữa hội viên và gói tập. Toàn bộ lịch sử mua, nâng cấp, hạ cấp đều nằm ở đây.

**Nhiệm vụ trong hệ thống:**
- Cho biết hội viên có quyền tập hay không: **"gói hiện tại" là dòng duy nhất có trạng thái `ACTIVE`**, mọi chức năng đều dùng đúng một định nghĩa này.
- Giữ số buổi còn lại để chặn đặt lịch khi hết buổi, và trừ buổi khi buổi tập được chốt.
- Giữ ngày hết hạn để chặn đặt lịch sau ngày hết hạn và để Sales chăm sóc hội viên sắp hết hạn.

**Ai dùng:** Hội viên mua, lễ tân bán hộ và xác nhận thanh toán, hệ thống tự chuyển trạng thái theo thời gian.

**Quy tắc nghiệp vụ:**
- Vòng đời: `PENDING` (chờ thanh toán) → `ACTIVE` (đã thanh toán) → `EXPIRED` (quá ngày kết thúc, hệ thống tự cập nhật khi có người đọc dữ liệu, không cần tác vụ chạy nền).
- Ngày bắt đầu tính từ **lúc thanh toán thành công**, không phải lúc bấm đăng ký; ngày kết thúc là ngày cuối cùng còn hiệu lực (gói 30 ngày kéo dài đúng 30 ngày).
- **Nâng cấp** (gói mới giá cao hơn hoặc bằng): gói cũ bị huỷ (`CANCELLED`) ngay, gói mới `PENDING`. **Hạ cấp** (giá thấp hơn): gói cũ giữ nguyên, gói mới `SCHEDULED` và tự thành `ACTIVE` vào ngày sau khi gói cũ hết hạn.
- Không cho đăng ký thêm khi đang có gói `PENDING`; vẫn cho khi đang có gói `ACTIVE`.
- `remaining_sessions` chỉ có với gói theo buổi; trừ 1 khi buổi tập hoàn thành, hội viên vắng mặt hoặc huỷ muộn.

**Ví dụ:** Hội viên A mua gói "PT 12 buổi" ngày 1/11: trạng thái `PENDING`; lễ tân xác nhận đã nhận tiền thì thành `ACTIVE`, hạn đến 30/11, còn 12 buổi.

**Use case liên quan (20):** M4, M5, M6, M7, M8, M11, M12, M13, T11, R1, R2, R3, R4, R5, R7, R8, R9, S5, A6, A8

#### 8. `payment_transactions` — Giao dịch thanh toán

**Ý nghĩa:** Sổ ghi nhận mọi khoản tiền thu: thanh toán gói tập và thanh toán đơn bán lẻ. Mỗi lần thu là một dòng.

**Nhiệm vụ trong hệ thống:**
- Xác nhận tiền đã nhận: lễ tân xác nhận tại quầy, gói tập hoặc đơn hàng mới chuyển sang hoàn tất.
- Làm nguồn cho báo cáo doanh thu và lịch sử thanh toán của hội viên.
- Chống xác nhận trùng: mỗi giao dịch có mã duy nhất, xác nhận lần thứ hai không kích hoạt lại gói.

**Ai dùng:** Lễ tân (xác nhận, xem theo ngày), hội viên (xem lịch sử), quản trị viên (báo cáo doanh thu).

**Quy tắc nghiệp vụ:**
- Thanh toán xác nhận **tại chỗ** (tiền mặt hoặc xác nhận tại quầy), không qua cổng thanh toán ngoài ở phiên bản này.
- `status`: `PENDING` (chờ), `SUCCESS` (đã thu), `FAILED`. Doanh thu chỉ tính giao dịch `SUCCESS`.
- Mỗi giao dịch gắn với **đúng một** đối tượng: một lần đăng ký gói **hoặc** một đơn bán lẻ.
- Người thanh toán để trống với khách vãng lai mua lẻ.
- `amount` là số tiền thực thu tại thời điểm đó, không thay đổi dù sau này giá gói đổi.
- Hạ cấp gói thu tiền ngay lúc lập lịch.

**Ví dụ:** Giao dịch 3.600.000 đ, tiền mặt, trả cho đăng ký gói của hội viên A, trạng thái `SUCCESS`.

**Use case liên quan (13):** M4, M7, M8, M9, R1, R2, R3, R4, R5, R6, R20, A5, A14

### Nhóm: Lịch tập và điểm danh

#### 9. `trainer_schedules` — Ca làm việc của PT

**Ý nghĩa:** Các khung giờ PT sẵn sàng dạy, khai báo theo **ngày cụ thể** (không phải lịch lặp lại hàng tuần).

**Nhiệm vụ trong hệ thống:**
- PT tạo, sửa, xoá ca làm việc của mình.
- Hội viên xem để biết khung giờ trống mà đặt lịch; hệ thống chỉ cho đặt buổi nằm trong ca làm việc của PT.

**Ai dùng:** PT (của chính mình), quản trị viên quản lý, hội viên xem.

**Quy tắc nghiệp vụ:**
- Không được tạo ca ở quá khứ; nếu ca là hôm nay thì giờ bắt đầu phải từ thời điểm hiện tại trở đi.
- `is_active = 0` nghĩa là ca "tạm nghỉ".
- Cho phép trùng giờ giữa hai PT khác nhau (chỉ cấm trùng giờ của cùng một PT).
- Dùng ngày cụ thể để tránh lỗi hai tuần khác nhau cùng thứ cùng giờ bị coi là trùng.

**Ví dụ:** PT B, ngày 5/11, 08:00 đến 12:00.

**Use case liên quan (10):** M10, M11, M12, T3, T4, T5, T6, R7, R8, A15

#### 10. `bookings` — Buổi tập PT

**Ý nghĩa:** Mỗi buổi tập 1-1 giữa hội viên và PT là một dòng. Là bảng nghiệp vụ phức tạp nhất: vừa là lịch hẹn, vừa là bản ghi điểm danh của cả hai bên, vừa là căn cứ trừ buổi của hội viên và tính lương PT.

**Nhiệm vụ trong hệ thống:**
- Đặt lịch: hội viên tự đặt hoặc lễ tân đặt hộ, sửa hoặc huỷ.
- Ghi điểm danh bằng khuôn mặt của hội viên (`check_in_*`) và của PT (`pt_check_in_*`, `pt_check_out_*`), kèm lý do nếu PT ra sớm.
- Quyết định kết cục của buổi để trừ buổi cho hội viên và trả công cho PT.

**Ai dùng:** Hội viên, lễ tân (đặt, sửa, huỷ), PT (xác nhận, hoàn thành, nhập lý do), hệ thống (tự chuyển trạng thái theo thời gian).

**Quy tắc nghiệp vụ:**
- Khi đặt phải thoả: không ở quá khứ và cách giờ bắt đầu ít nhất 30 phút; một khung giờ chỉ một người; hội viên không trùng giờ với PT khác; nằm trong ca làm việc của PT; gói còn hạn và còn buổi; không vượt giới hạn buổi mỗi tuần.
- `status`: `PENDING` (chờ PT) → `CONFIRMED` → `COMPLETED`. Các kết cục khác: `CANCELLED` (huỷ đúng hạn, không phạt), `LATE_CANCELLED` (hội viên huỷ khi còn dưới 2 giờ: mất 1 buổi, PT vẫn có công), `NO_SHOW` (sau 15 phút PT đã đến mà hội viên chưa quét mặt: mất 1 buổi, PT có công), `PT_NO_SHOW` (sau 15 phút PT chưa quét mặt: không phạt hội viên, PT không có công).
- PT ra sớm hơn giờ kết thúc phải nhập lý do; không có lý do thì buổi vẫn hoàn thành nhưng PT không được tính công.
- PT chỉ bấm "Hoàn thành" được khi hội viên và PT đều đã quét mặt (kiểm tra ở máy chủ).

**Ví dụ:** Hội viên A, PT B, ngày 5/11 từ 09:00 đến 10:00, đã `CONFIRMED`; cả hai quét mặt, PT bấm hoàn thành: `COMPLETED`.

**Use case liên quan (22):** M10, M11, M12, M13, M14, M16, M17, T6, T7, T8, T9, T10, T11, T13, T14, R7, R8, R9, R10, R14, A7, A13

#### 11. `face_profiles` — Hồ sơ khuôn mặt

**Ý nghĩa:** Dữ liệu nhận diện khuôn mặt của từng người, dùng để điểm danh không cần thẻ.

**Nhiệm vụ trong hệ thống:**
- Làm "mẫu" để máy chủ so khớp khi có người quét mặt ở kiosk hoặc trên điện thoại.
- Cho phép ba luồng: kiosk của lễ tân (so với tất cả hồ sơ), tự điểm danh (so với chính tài khoản), đăng ký hộ bằng ảnh tĩnh.

**Ai dùng:** Hội viên, PT, lễ tân, Sales (tự đăng ký); lễ tân và quản trị viên (đăng ký hộ, xem, xoá).

**Quy tắc nghiệp vụ:**
- Trình duyệt trích vector đặc trưng 128 chiều; **máy chủ tự tính khoảng cách và quyết định khớp** (ngưỡng 0,45), không bao giờ tin kết quả "đã khớp" do máy khách gửi lên.
- Mỗi người một hồ sơ; đăng ký lại là ghi đè.
- Vector là một giá trị nguyên khối của mô hình nhận diện, chỉ so sánh toàn bộ, không đọc từng phần tử.
- Ảnh gốc (nếu có) chỉ là bằng chứng lưu một lần lúc đăng ký, không dùng để so khớp và không gửi lại mỗi lần quét.
- Ghi lại người đăng ký hộ để truy vết.

**Ví dụ:** Hồ sơ của PT B, do lễ tân đăng ký hộ.

**Use case liên quan (13):** M15, M16, T9, T12, R11, R12, R13, R14, R15, R23, S6, S8, A16

#### 12. `staff_attendances` — Chấm công nhân viên

**Ý nghĩa:** Bản ghi giờ vào và giờ ra mỗi ngày của nhân viên hưởng lương cứng (lễ tân, Sales). PT không dùng bảng này vì lương theo buổi, được chấm công ngay trong từng buổi tập.

**Nhiệm vụ trong hệ thống:**
- Nhân viên quét mặt vào ca và ra ca; xem lại bảng chấm công của mình.
- Quản trị viên xem báo cáo chấm công toàn bộ nhân viên.

**Ai dùng:** Lễ tân, Sales (tự quét hoặc quét qua kiosk), quản trị viên (báo cáo).

**Quy tắc nghiệp vụ:**
- Mỗi nhân viên mỗi ngày đúng một dòng.
- Không cần chọn vào hay ra: lần quét đầu trong ngày là vào, lần sau là ra.
- Trạng thái (đúng giờ, đi muộn, về sớm, vắng) **không lưu**, được tính khi xem báo cáo bằng cách so với ca mặc định 07:00 đến 21:00.

**Ví dụ:** Lễ tân C ngày 5/11: vào 07:05, ra 21:00.

**Use case liên quan (5):** R15, R16, S6, S7, A12

#### 13. `gym_check_ins` — Lượt vào phòng tập

**Ý nghĩa:** Ghi nhận hội viên vào phòng tập khi **không có buổi PT** nào gắn với lượt quét (ví dụ hội viên có gói theo thời gian tự đến tập).

**Nhiệm vụ trong hệ thống:**
- Lễ tân hoặc kiosk ghi nhận hội viên vào phòng, kèm kiểm tra gói tập còn hiệu lực.
- Cho hội viên xem lịch sử check-in và cho quản trị viên xem báo cáo lưu lượng (kết hợp với lượt điểm danh ở buổi PT).

**Ai dùng:** Hội viên (quét mặt), lễ tân (kiosk), hội viên và quản trị viên (xem lịch sử, báo cáo).

**Quy tắc nghiệp vụ:**
- Lượt quét có buổi PT phù hợp chỉ cập nhật bảng buổi tập, không ghi ở đây, để một lượt không bị đếm hai lần.
- Lần quét đầu là vào, lần thứ hai trong ngày là ra.
- Hội viên không có gói `ACTIVE` bị từ chối.
- **Bảng thuộc thiết kế v2, chưa triển khai.**

**Ví dụ:** Hội viên A quét mặt vào phòng lúc 17:30, ra lúc 19:00, không có buổi PT.

**Use case liên quan (4):** M16, M17, R14, A7

### Nhóm: Bán lẻ tại quầy

#### 14. `products` — Sản phẩm bán lẻ

**Ý nghĩa:** Danh mục hàng bán tại quầy: đồ uống, thực phẩm bổ sung, phụ kiện tập luyện.

**Nhiệm vụ trong hệ thống:**
- Lễ tân chọn sản phẩm khi lập đơn.
- Quản lý tồn kho: số lượng giảm khi bán, tăng khi huỷ đơn.

**Ai dùng:** Lễ tân và quản trị viên (thêm, sửa, xem); hệ thống cập nhật tồn kho.

**Quy tắc nghiệp vụ:**
- `category`: `DRINK`, `SUPPLEMENT`, `ACCESSORY`.
- Không xoá sản phẩm đã bán; ngừng bán bằng `is_active = 0`.
- Giá ở đây là giá hiện tại; giá đã bán lưu ở dòng đơn nên đổi giá không ảnh hưởng đơn cũ.

**Ví dụ:** Nước suối 500 ml, 10.000 đ, tồn 100 chai.

**Use case liên quan (6):** R17, R18, R19, R20, R21, A17

#### 15. `pos_orders` — Đơn bán lẻ

**Ý nghĩa:** Một lần bán hàng tại quầy (đầu đơn): ai lập, ai mua, ở chi nhánh nào.

**Nhiệm vụ trong hệ thống:**
- Lễ tân tạo đơn, hệ thống trừ tồn kho và tạo giao dịch thanh toán.
- Cho phép xem danh sách đơn theo chi nhánh.

**Ai dùng:** Lễ tân (lập đơn), quản trị viên (xem).

**Quy tắc nghiệp vụ:**
- Hội viên mua thì ghi hội viên; **khách vãng lai thì để trống**.
- Không lưu tổng tiền: tổng tính từ các dòng hàng (số lượng nhân đơn giá), số tiền đã thu nằm ở giao dịch thanh toán.
- Giá và tồn kho luôn lấy từ máy chủ, không tin giá do máy khách gửi.

**Ví dụ:** Đơn của khách vãng lai do lễ tân C lập lúc 18:00.

**Use case liên quan (3):** R5, R20, R21

#### 16. `pos_order_items` — Dòng hàng của đơn

**Ý nghĩa:** Chi tiết từng sản phẩm trong một đơn bán lẻ. Nối `pos_orders` với `products` (một đơn nhiều sản phẩm, một sản phẩm nằm trong nhiều đơn).

**Nhiệm vụ trong hệ thống:**
- Ghi sản phẩm nào, số lượng bao nhiêu, đơn giá lúc bán.
- Là cơ sở tính tổng đơn và hoàn tồn kho khi huỷ đơn.

**Ai dùng:** Hệ thống ghi khi lễ tân lập đơn.

**Quy tắc nghiệp vụ:**
- `unit_price` là **giá chụp tại thời điểm bán**, không đổi dù sau này sản phẩm tăng hay giảm giá.
- Số lượng phải lớn hơn 0.
- Xoá đơn thì xoá luôn các dòng hàng của đơn.

**Ví dụ:** Đơn số 1: 2 chai nước suối × 10.000 đ.

**Use case liên quan (2):** R20, R21

### Nhóm: Lương PT

#### 17. `trainer_pay_rates` — Mức lương mỗi buổi của PT

**Ý nghĩa:** Số tiền PT được trả cho mỗi buổi dạy, cấu hình riêng cho từng PT.

**Nhiệm vụ trong hệ thống:**
- Quản trị viên đặt và sửa mức lương của PT.
- Khi chốt buổi, hệ thống đọc mức này để ghi lương cho PT.

**Ai dùng:** Quản trị viên (cấu hình), hệ thống (đọc khi tính lương).

**Quy tắc nghiệp vụ:**
- Mỗi PT đúng một mức.
- Chưa cấu hình mức lương thì buổi vẫn chốt được nhưng hệ thống chỉ ghi cảnh báo và không ghi lương.

**Ví dụ:** PT B: 150.000 đ mỗi buổi.

**Use case liên quan (4):** T11, T13, A10, A11

#### 18. `commissions` — Lương PT theo buổi

**Ý nghĩa:** Mỗi buổi tập đủ điều kiện trả công tạo một bản ghi lương cho PT. **Chỉ PT hưởng lương theo buổi**; lễ tân và Sales hưởng lương cứng nên không có hoa hồng.

**Nhiệm vụ trong hệ thống:**
- PT xem lương theo buổi của mình; quản trị viên xem tổng lương PT theo tháng.
- Làm căn cứ chi trả: trạng thái chờ trả hoặc đã trả.

**Ai dùng:** Hệ thống ghi khi buổi được chốt; PT xem; quản trị viên xem và đánh dấu đã trả.

**Quy tắc nghiệp vụ:**
- PT được trả công khi: buổi `COMPLETED` và PT dạy đủ giờ hoặc có lý do ra sớm; hội viên vắng mặt (`NO_SHOW`); hội viên huỷ muộn (`LATE_CANCELLED`).
- PT **không** được trả khi: PT vắng (`PT_NO_SHOW`), buổi huỷ đúng hạn, hoặc PT ra sớm không có lý do.
- Mỗi buổi tối đa một bản ghi lương, nên chốt hai lần không trả hai lần.
- Số tiền lấy từ mức lương của PT **tại thời điểm chốt buổi** và không đổi khi mức lương đổi sau đó.
- `status`: `PENDING` (chờ trả) → `PAID` (đã trả, có ngày trả).

**Ví dụ:** Buổi 5/11 của PT B hoàn thành đủ giờ: ghi 150.000 đ, `PENDING`.

**Use case liên quan (4):** T11, T13, A9, A11

### Nhóm: Khách tiềm năng

#### 19. `leads` — Khách tiềm năng

**Ý nghĩa:** Người quan tâm đến phòng gym nhưng chưa là hội viên, do nhân viên Sales ghi nhận và chăm sóc để chuyển thành hội viên.

**Nhiệm vụ trong hệ thống:**
- Sales thêm, sửa và theo dõi khách tiềm năng; quản trị viên xem toàn bộ.
- Khi khách đồng ý, Sales **chuyển đổi** thành hội viên: hệ thống tạo tài khoản hội viên và nối với khách tiềm năng đó.

**Ai dùng:** Sales (quản lý), quản trị viên (xem).

**Quy tắc nghiệp vụ:**
- Trạng thái: `NEW` (mới) → `CONTACTED` (đã liên hệ) → `CONVERTED` (đã thành hội viên) hoặc `LOST` (mất khách). Không chuyển ngược từ `CONVERTED`.
- Chuyển đổi cần thêm email (và mật khẩu tạm) vì tài khoản bắt buộc có email; khách đã chuyển đổi không chuyển lần nữa.
- Nguồn khách (Facebook, giới thiệu, đi ngang qua...) giúp đánh giá kênh nào hiệu quả.
- Chuyển đổi khách **không sinh hoa hồng** (Sales hưởng lương cứng).
- Cùng số điện thoại có thể xuất hiện ở nhiều dòng (hệ thống chỉ cảnh báo trùng).

**Ví dụ:** Khách "Lê Văn D", SĐT 09xx, nguồn Facebook, đã liên hệ, Sales E phụ trách.

**Use case liên quan (5):** S1, S2, S3, S4, A22
