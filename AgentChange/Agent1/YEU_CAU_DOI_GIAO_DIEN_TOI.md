# Yêu cầu đổi giao diện toàn dự án GMS sang phong cách tối (theo ảnh mẫu)

Gửi: Agent 1 đến Agent 6. Người giao: Phi (nhóm trưởng). Người soạn: Agent 1.
Kèm theo file này **phải có ảnh mẫu** (trang "Gói tập của tôi" của bản IronFit). Agent nào chưa nhận được ảnh thì hỏi Phi, đừng tự đoán giao diện.

## 1. Mục tiêu và giới hạn

**Mục tiêu:** đổi giao diện **toàn bộ frontend** (`frontend/`) từ nền sáng xanh ngọc hiện tại sang phong cách trong ảnh mẫu: nền tối, thẻ tối bo góc, chữ sáng, nút chính màu cam, menu trên cùng.

**Giới hạn bắt buộc (mọi agent):**
1. **Chỉ đổi giao diện**: class CSS, bố cục, thành phần hiển thị. **Không đổi logic, API, kiểu dữ liệu, quy tắc nghiệp vụ.** Các file `*.test.ts` hiện có phải vẫn đạt mà không phải sửa.
2. **Chỉ lấy phong cách, không lấy tính năng** của IronFit. Ảnh mẫu có các mục menu "Huấn luyện viên", "Tiến trình", "Trợ lý AI", "Điểm danh khuôn mặt" và nút "Hủy gói", "Gia hạn". Đó là của bản IronFit. GMS giữ nguyên menu theo vai trò trong `frontend/src/config/navigation.ts`, không thêm trang mới.
3. Không hard-code màu (`#xxxxxx`, `bg-white`, `text-black`). Hiện **0 chỗ** hard-code, hãy giữ như vậy: toàn bộ màu đi qua token trong `tailwind.config.ts`.

## 2. Phân tích ảnh mẫu (những gì thấy được)

| Thành phần | Mô tả trong ảnh |
|---|---|
| Nền trang | rất tối, gần đen xanh navy |
| Thẻ (card/panel) | nền tối hơn một chút so với nền trang, viền mảnh, bo góc khoảng 12px, đặt trong **lưới 2 cột** |
| Chữ | tiêu đề và giá trị màu trắng đậm; nhãn phụ (như "Ngày bắt đầu", "Trạng thái") xám nhạt, cỡ nhỏ |
| Tiêu đề trang | có biểu tượng emoji đứng trước ("💪 Gói tập của tôi"), dưới là một dòng mô tả xám |
| Menu | **thanh ngang trên cùng**: logo vuông bo góc màu cam + tên, các mục menu chữ xám, bên phải là tài khoản (biểu tượng người + email + mũi tên xổ xuống) |
| Tab lọc | dạng viên thuốc (pill): tab đang chọn nền xanh dương, chữ trắng; tab khác chữ xanh dương, không nền |
| Nhãn trạng thái (badge) | bo tròn hẳn, chữ màu trên nền cùng màu nhưng rất đậm: xanh lá "Đang hoạt động", đỏ "Đã hết hạn", trung tính "Đã lên lịch" |
| Chip thông tin | nhỏ, bo tròn: xanh lá "Còn 176 ngày", đỏ "Đã sử dụng 30 ngày" |
| Nút chính | **cam, gradient nhẹ**, chiều rộng đầy thẻ, chữ trắng đậm, có biểu tượng ("Gia hạn") |
| Nút nguy hiểm | **viền đỏ, nền đỏ rất nhạt, chữ đỏ** ("Hủy gói"), không phải nền đỏ đặc |
| Banner thông tin | nền xanh dương nhạt trong suốt, viền xanh dương, có biểu tượng đồng hồ |

**Lưu ý về độ chính xác:** các giá trị màu ở mục 3 là **ước lượng từ ảnh chụp**. Muốn đúng tuyệt đối, Agent 1 lấy mã màu bằng công cụ chấm màu trên bản IronFit gốc hoặc sao chép token từ mã nguồn IronFit, rồi cập nhật mục 3 trước khi các agent khác bắt đầu.

## 3. Bảng màu đề xuất (giữ nguyên tên token để không phải sửa hàng trăm file)

Mã nguồn dùng token theo ý nghĩa (`text-steel` 164 lần, `border-line` 34, `text-ink` 19, `bg-chalk` 10, `bg-plate` 12, `text-plate` 12...). Đổi **giá trị** của token là phần lớn trang đổi theo. Tên token cũ sẽ không còn khớp nghĩa (ví dụ `chalk` từng là "phấn trắng"), nên ghi chú rõ trong `tailwind.config.ts`; đổi tên token là việc **không làm** ở đợt này để tránh xung đột giữa các PR.

| Token hiện có | Vai trò mới | Giá trị đề xuất (ước lượng) |
|---|---|---|
| `chalk` | nền trang | `#0B0F19` (trùng `primary-900` trong `DESIGN_SYSTEM.md`) |
| `paper` | nền thẻ, panel, ô nhập | `#151B2C` (trùng `primary-700`) |
| `ink` | chữ chính | `#F4F5F7` |
| `steel` | chữ phụ, nhãn | `#8A8F9C` (kiểm tra độ tương phản, mục 6) |
| `line` | viền, đường kẻ | `#262E42` |
| `plate` (DEFAULT, dark, tint) | màu chọn, liên kết, tab đang chọn, viền focus: **xanh dương** | `#2F7BFF` / `#1F5FD6` / nền trong suốt khoảng 14% |
| **`accent` (token mới)** | nút hành động chính: **cam**, dùng gradient | từ `#FF6A00` đến `#FF8A1F` (ước lượng) |
| `rubber` | nguy hiểm, hết hạn: chữ `#F2645B`, tint = đỏ trong suốt khoảng 14% | |
| `moss` | thành công, đang hoạt động: chữ `#3FD07F`, tint = xanh lá trong suốt khoảng 14% | |
| `amber` | cảnh báo, chờ: chữ `#F5B83D`, tint = vàng trong suốt khoảng 14% | |

**Thay đổi quy tắc cũ:** `tailwind.config.ts` hiện ghi "chỉ có MỘT màu nhấn là plate". Sau đợt này có **hai màu nhấn có phân vai rõ ràng**: xanh dương cho trạng thái chọn và liên kết, cam cho **một** nút hành động chính trên mỗi màn hình (đúng tinh thần `DESIGN_SYSTEM.md` mục 2: cam chỉ cho hành động chính). Agent 1 cập nhật chú thích đó.

Phông chữ: ảnh mẫu dùng phông sans không chân. GMS đang dùng Be Vietnam Pro, **giữ nguyên** (hỗ trợ tiếng Việt tốt, đã cài).

## 4. Quyết định cần Phi chốt (đã chọn sẵn mặc định để các agent không bị chặn)

| # | Câu hỏi | Mặc định nếu Phi chưa trả lời |
|---|---|---|
| 1 | Tên thương hiệu trên menu: giữ "GMS" hay đổi sang "IronFit"? | Giữ **GMS**, logo vuông bo góc màu cam như ảnh |
| 2 | Menu: thanh ngang trên cùng như ảnh, hay giữ cột bên trái nhưng đổi sang tối? | **Thanh ngang trên cùng** như ảnh (xem mục 5.1 cách xử lý nhiều mục) |
| 3 | Emoji trước tiêu đề trang như ảnh? | **Có**, là tham số tùy chọn `icon` của `PageHeader`, trang nào không truyền thì không hiện |

## 5. Phân công và thứ tự

### Giai đoạn 1: nền tảng (chỉ Agent 1, làm xong và merge trước)

Nhánh `feature/agent1-ui-foundation`. Các agent khác **chưa** sửa file dùng chung trong lúc này để không xung đột.

| Việc | File |
|---|---|
| Đổi giá trị token, thêm token `accent`, cập nhật chú thích | `frontend/tailwind.config.ts` |
| Đổi nền, chữ, viền, vòng focus; viết lại `.btn-primary` (cam gradient), `.btn-secondary`, `.btn-danger` (viền đỏ, nền đỏ nhạt), `.input`, `.panel`; bỏ hoặc đổi `.floor-lanes` | `frontend/src/app/globals.css` |
| Chuyển khung trang sang thanh menu trên cùng, menu tài khoản xổ xuống (đăng xuất), logo mới | `frontend/src/components/layout/AppShell.tsx` |
| Chữ màu theo tông trong `Alert`; badge bo tròn có chữ màu; thêm `icon` cho `PageHeader`; kiểm tra `Field`, `EmptyState`, `ModulePlaceholder` | `frontend/src/components/ui/*` |
| Thêm thành phần dùng chung `Badge`/`Chip` để các agent dùng lại thay cho bản riêng | `frontend/src/components/ui/Badge.tsx` (mới) |
| Màn hình đăng nhập, đăng ký, hồ sơ, người dùng (phần của Agent 1) | `features/auth`, `features/profile`, `features/admin-users` |

#### 5.1 Menu trên cùng khi có nhiều mục
Ảnh mẫu có 8 mục, vừa một hàng. GMS có tối đa khoảng 12 mục cho quản trị viên và 10 cho lễ tân. Cách xử lý: trên màn hình lớn, **hiện mỗi nhóm trong `NAVIGATION` thành một mục có danh sách xổ xuống** (Gói tập, Lịch tập, Điểm danh, Bán hàng, Kinh doanh); nhóm chỉ có một mục thì hiện thẳng như liên kết. Trên màn hình nhỏ giữ ngăn kéo hiện tại. Logic lọc theo vai trò (`hasAnyRole`) **giữ nguyên**.

### Giai đoạn 2: từng agent rà phần của mình (sau khi giai đoạn 1 merge)

Mỗi agent rebase lên `origin/master`, mở nhánh `feature/agentN-ui-dark`. Chủ module theo `navigation.ts`:

| Agent | Thư mục | Việc cụ thể cần kiểm tra |
|---|---|---|
| **Agent 2** | `features/membership` (18 file) | Làm đúng như ảnh mẫu cho **trang Gói của tôi** (xem mục 5.2). Danh sách gói, chi tiết gói, bảng thanh toán, form quản trị gói, bảng lịch sử |
| **Agent 3** | `features/bookings`, `features/schedule` | Lưới khung giờ (ô chọn, ô bị khóa, ô đã chọn), bảng booking, hộp thoại; màu trạng thái `PENDING`, `CONFIRMED`, `NO_SHOW` |
| **Agent 4** | `features/face`, `features/attendance` | Màn hình camera và kiosk (nền tối toàn màn hình), thẻ kết quả quét, bảng chấm công; kiểm tra viền khung khuôn mặt vẫn thấy rõ |
| **Agent 5** | `features/pos`, `features/commissions` | Quầy bán hàng, giỏ hàng, thẻ sản phẩm, bảng đơn và hoa hồng; số tiền phải đọc rõ trên nền tối |
| **Agent 6** | `features/leads`, `features/reports` | Bảng khách tiềm năng, hộp thoại; **biểu đồ** `BarChart.tsx` dùng class `stroke-line`, `fill-steel`, cần xem lại màu cột và chữ trục trên nền tối. Sau cùng chạy lại `scripts/e2e` và `scripts/demo-smoke.ps1` |

Việc chung cho mỗi agent: bảng (`table`, hàng sọc, dòng được chọn), hộp thoại, ô nhập và thông báo lỗi, trạng thái tải và trống, nút ở mọi biến thể, `StatusBadge` riêng của module (chuyển sang `Badge` dùng chung nếu có thể).

#### 5.2 Trang "Gói của tôi" theo ảnh mẫu (Agent 2)

Hiện trang có 3 tab (Gói hiện tại, Lịch sử gói, Lịch sử thanh toán) và chỉ hiện gói đang dùng. Theo ảnh, làm lại **bố cục** như sau, nhưng **chỉ dùng dữ liệu và API đã có**:
- Tab lọc dạng pill: Tất cả, Đang hoạt động, Lịch sử (nối với `listMyMemberships`).
- Mỗi gói là một thẻ trong lưới 2 cột: tên gói, giá, badge trạng thái ở góc phải, hai ô ngày bắt đầu và kết thúc (nhãn xám trên giá trị trắng đậm), chip "Còn N ngày" hoặc "Đã sử dụng N ngày".
- Gói chờ kích hoạt: banner xanh dương "Sẽ tự động kích hoạt từ ngày …".
- Nút "Gia hạn" (cam, đầy thẻ) cho gói đã hết hạn: **nối với API mua gói đã có** (`POST /api/v1/member-packages/purchase`, chọn lại đúng gói đó). Nút nâng hoặc hạ cấp hiện có giữ nguyên.
- **Nút "Hủy gói": không làm.** Backend hiện **không có endpoint hủy gói** của hội viên (chỉ có `/upgrade` và `/downgrade`). Nếu Phi muốn có tính năng này thì đó là một yêu cầu backend riêng, giao cho Agent 1, không gộp vào đợt đổi giao diện.

### Giai đoạn 3: kiểm tra chéo (Agent 6, sau khi mọi nhánh đã merge)

Chạy toàn bộ trang với từng tài khoản mẫu (`docs/TEST_ACCOUNTS.md`: quản trị, lễ tân, sales, PT, hội viên), chụp ảnh từng trang, tìm chỗ còn nền sáng, chữ chìm, biểu tượng mất.

## 6. Tiêu chuẩn hoàn thành (mỗi PR)

- `npm run lint`, `npm run typecheck`, `npm test`, `npm run build` đều đạt trong thư mục `frontend`; stage `Frontend CI` của Jenkins xanh.
- Không sửa file `*.test.ts` để cho qua. Nếu test hỏng vì đổi giao diện thì báo Agent 1 trước.
- **Độ tương phản chữ so với nền tối đạt tối thiểu 4,5:1** (chữ thường), 3:1 (chữ lớn từ 18px hoặc đậm từ 14px). Đặc biệt kiểm tra `text-steel` và chữ trong badge.
- Vòng focus bằng bàn phím vẫn nhìn thấy rõ trên nền tối.
- Mỗi PR đính kèm ảnh **trước và sau** cho mỗi trang đã sửa, ít nhất ở một vai trò. Chụp cả màn hình điện thoại cho trang chính.
- Không thêm màu hard-code. Kiểm tra nhanh: `grep -rnE "#[0-9a-fA-F]{6}|bg-white|text-black" frontend/src` phải không ra dòng nào (trừ file token và SVG logo).
- Không đụng vào file dùng chung ngoài phần mình được giao ở mục 5. Cần sửa file dùng chung thì nhờ Agent 1.

## 7. Cạm bẫy cần tránh

- Sửa giá trị token xong mà quên các chỗ dùng **tint** (`bg-rubber-tint`, `bg-moss-tint`, `bg-amber-tint`, `bg-plate-tint`): nếu để màu pastel sáng trên nền tối sẽ chói và chữ không đọc được.
- `ring-offset-chalk` trong `globals.css` phải theo nền mới, nếu không viền focus có quầng sáng lạ.
- Ảnh kiểu "phấn trắng" trong `public/` hoặc logo SVG nếu có nền trắng sẽ hiện thành ô trắng giữa nền tối.
- Trang đăng nhập đang dùng `.floor-lanes` (vạch làn sàn xanh ngọc). Đổi theo tông mới, không để lại xanh ngọc.
- Bản chạy Docker cần build lại để thấy giao diện mới: `docker compose -f docker-compose.demo.yml -f docker-compose.monitoring.yml up -d --build frontend`.

## 8. Thứ tự merge gợi ý

1. `feature/agent1-ui-foundation` (nền tảng, chờ Jenkins xanh, Phi chụp ảnh các trang chính để duyệt tông màu).
2. Các nhánh `feature/agent2..6-ui-dark`, có thể merge song song vì mỗi agent chỉ sửa thư mục riêng.
3. Agent 6 chạy kiểm tra chéo và báo lại những chỗ còn sót cho đúng chủ module.
