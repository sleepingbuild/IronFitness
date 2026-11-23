# Kịch bản thuyết trình: Phân tích yêu cầu và thiết kế CSDL hệ thống GMS

Dùng cùng `SLIDES_CONTENT.md` (số slide khớp nhau). Thời lượng dưới đây là gợi ý, tổng khoảng **17 phút**, điều chỉnh theo thời gian thật. Giảng viên chấm Bước 2 (CSDL) 60% nên phần CSDL chiếm hơn một nửa thời gian.

| Phần | Slide | Thời lượng |
|---|---|---|
| Mở đầu | 1 | 0:30 |
| Bước 1: Phân tích yêu cầu | 2 đến 8 | 6:45 |
| Bước 2: Thiết kế CSDL | 9 đến 18 | 9:15 |
| Kết luận | 19 | 0:30 |

Ký hiệu: **(thao tác)** là việc cần làm khi nói; **"..."** là lời thoại gợi ý, nói lại bằng lời của mình cho tự nhiên.

## Từ khoá nên nói rõ vì giảng viên chấm theo đó

"tác nhân và giới hạn quyền hạn", "Động từ + Danh từ", "luồng chính (MVP)", "nhánh ngoại lệ", "khoá chính, khoá ngoại", "quan hệ 1-1, 1-N, nhiều-nhiều tách bằng bảng trung gian", "chuẩn hoá 2NF, 3NF".

---

# PHẦN 1: PHÂN TÍCH YÊU CẦU

## Slide 1 (0:30): Mở đầu

**(thao tác)** Đứng thẳng, nhìn khán giả, giới thiệu tên nhóm.

"Xin chào thầy cô và các bạn. Nhóm em là [tên nhóm]. Hôm nay nhóm trình bày hai việc: một là phân tích yêu cầu, hai là thiết kế cơ sở dữ liệu cho hệ thống quản lý phòng gym tên là GMS. Hệ thống làm bằng Spring Boot, dữ liệu nằm trong SQL Server."

## Slide 2 (0:45): Bài toán và người dùng

"Phòng gym thường quản lý bằng sổ sách: ai mua gói nào, ai đặt lịch với huấn luyện viên, ai đã đến tập, và trả lương huấn luyện viên thế nào. Nhóm em làm ứng dụng này cho sáu nhóm người: chủ phòng gym là quản trị viên, lễ tân, huấn luyện viên, nhân viên sales, hội viên, và khách chưa có tài khoản.

Về quy trình, nhóm làm theo thứ tự: xác định yêu cầu trước, rồi thiết kế cơ sở dữ liệu, rồi mới code. Sau đó nhóm còn đối chiếu ngược thiết kế dữ liệu với các user story để tìm chỗ thiếu."

## Slide 3 (1:00): Tác nhân và giới hạn quyền hạn

**(thao tác)** Chỉ lần lượt từng hàng, không đọc hết bảng.

"Đây là các tác nhân và giới hạn quyền hạn của từng người. Em nêu nguyên tắc chung: hội viên chỉ làm việc trên dữ liệu của chính mình, không xem được hội viên khác. Huấn luyện viên quản lý ca dạy và lịch dạy của mình, nhưng không bán gói. Lễ tân vận hành tại quầy: bán gói, xác nhận thanh toán, bán lẻ, nhưng không cấu hình hệ thống hay xem báo cáo tài chính toàn hệ thống. Sales chỉ chăm sóc khách tiềm năng. Quản trị viên là người duy nhất có toàn quyền cấu hình và xem mọi báo cáo.

Có một điểm nhỏ: việc hệ thống tự chuyển trạng thái theo thời gian, ví dụ gói hết hạn, nhóm không coi là một tác nhân."

## Slide 4 (1:00): Chức năng cốt lõi (MVP)

"Khi xác định chức năng cốt lõi, nhóm tập trung vào hai luồng chính. Luồng một: hội viên xem gói, mua gói, thanh toán tại quầy, rồi gói được kích hoạt. Luồng hai: hội viên đặt lịch với huấn luyện viên, huấn luyện viên xác nhận, hai bên điểm danh bằng khuôn mặt, huấn luyện viên hoàn thành buổi, và hệ thống trừ buổi của hội viên đồng thời tính công cho huấn luyện viên.

Nhóm cố ý bỏ qua những phần rườm rà ở phiên bản đầu: lớp tập nhóm, nhắc gia hạn qua Zalo, chốt ca thu ngân, báo cáo nâng cao theo chi nhánh. Hoa hồng cho sales cũng bỏ, chỉ huấn luyện viên được trả lương theo buổi."

## Slide 5 (0:45): Danh sách user story

"Từ các tác nhân đó, nhóm viết được 83 user story. Tên mỗi chức năng theo đúng dạng động từ cộng danh từ, ví dụ 'Mua gói tập', 'Xác nhận thanh toán tại quầy', 'Hoàn thành buổi tập'. Lễ tân và quản trị viên có nhiều story nhất vì đây là hai vai trò vận hành và quản lý. Bao gồm cả các chức năng CRUD cơ bản như quản lý gói tập, sản phẩm, ca làm việc, và các nghiệp vụ đặc thù như điểm danh khuôn mặt hay tính lương theo buổi. Danh sách đầy đủ nằm ở phụ lục."

## Slide 6 (0:30): Mẫu user story

"Mỗi story theo mẫu: là ai, muốn làm gì, để được gì. Ví dụ story đặt lịch của hội viên có kèm điều kiện: hệ thống chặn trùng giờ, vượt giới hạn buổi mỗi tuần hoặc hết buổi. Những điều kiện này sau đó trở thành ràng buộc ở dữ liệu và ở luồng nghiệp vụ."

## Slide 7 (1:15): Luồng nghiệp vụ 1: Mua và thanh toán gói tập

**(thao tác)** Chèn ảnh Activity Diagram. Dùng bút chỉ hoặc con trỏ đi theo luồng.

"Đây là luồng mua và thanh toán gói tập. Bắt đầu từ lúc hội viên chọn gói. Hệ thống kiểm tra trước: nếu đang có một gói chờ thanh toán thì từ chối, tránh tạo rác. Nếu hội viên chưa có gói đang hoạt động thì tạo đăng ký ở trạng thái chờ thanh toán.

Có hai nhánh thú vị khi hội viên đang có gói. Nâng cấp sang gói giá cao hơn thì gói cũ bị huỷ ngay và gói mới chờ thanh toán. Hạ cấp sang gói rẻ hơn thì gói cũ vẫn chạy đến hết hạn, gói mới được xếp lịch bắt đầu từ ngày hôm sau.

Khi lễ tân xác nhận đã thu tiền, gói chuyển sang hoạt động, ngày bắt đầu tính từ ngày thanh toán. Về ngoại lệ: gói đã ngừng bán thì không mua được, và nếu lễ tân bấm xác nhận hai lần thì gói không bị kích hoạt lặp."

## Slide 8 (1:30): Luồng nghiệp vụ 2: Vòng đời buổi tập với PT

**(thao tác)** Chèn Activity Diagram. Sau đó chuyển sang bảng tình huống, chỉ nói 3 đến 4 dòng quan trọng.

"Luồng thứ hai phức tạp hơn nên nhóm vẽ thêm bảng tình huống. Buổi tập bắt đầu lúc S, kết thúc lúc E, và có thời gian ân hạn 15 phút.

Nếu huấn luyện viên không quét mặt vào sau S cộng 15 phút thì buổi là PT_NO_SHOW: hội viên không mất buổi, huấn luyện viên không có công, và nhóm ưu tiên lỗi của huấn luyện viên kể cả khi cả hai cùng vắng. Nếu huấn luyện viên đã vào mà hội viên chưa tới thì là NO_SHOW: hội viên mất một buổi, huấn luyện viên vẫn được công.

Về huỷ lịch: huỷ buổi chưa được xác nhận thì miễn phí; huỷ buổi đã xác nhận mà còn từ hai giờ trở lên cũng không mất buổi; huỷ muộn dưới hai giờ là LATE_CANCELLED, hội viên mất một buổi và huấn luyện viên vẫn có công. Cuối cùng, nếu huấn luyện viên ra sớm trước giờ kết thúc thì phải nhập lý do, không có lý do thì buổi không được tính công."

---

# PHẦN 2: THIẾT KẾ CƠ SỞ DỮ LIỆU

## Slide 9 (0:45): Từ yêu cầu đến bảng dữ liệu

"Sang phần thiết kế cơ sở dữ liệu. Nhóm không chỉ liệt kê bảng theo cảm tính, mà lập ma trận từ 83 user story sang các bảng cần để lưu dữ liệu của story đó. Ví dụ story đặt lịch cần các bảng đặt lịch, ca làm việc của huấn luyện viên, gói tập của hội viên. Kết quả: mọi story có thao tác dữ liệu đều có bảng đỡ, và ngược lại mọi bảng đều phục vụ ít nhất một story, nên không có bảng thừa. Quá trình đối chiếu này cũng giúp nhóm phát hiện vài thiếu sót, em sẽ nói ở slide 17."

## Slide 10 (1:00): Tổng quan 19 bảng

**(thao tác)** Chèn ảnh ERD tổng quan. Chỉ vào bảng `users` ở giữa.

"Đây là toàn cảnh 19 bảng, chia theo sáu phân hệ: người dùng và phân quyền, gói tập và thanh toán, lịch tập và điểm danh, bán lẻ, lương huấn luyện viên, và khách tiềm năng. Ảnh này vẽ bằng dbdiagram.io. Bảng users ở giữa vì hầu hết các bảng đều nối về người dùng. Sơ đồ tổng quan chỉ giữ cột khoá để nhìn quan hệ; đủ cột và kiểu dữ liệu nằm ở ba sơ đồ tiếp theo."

## Slide 11 (1:00): Phân hệ Người dùng và Gói tập

**(thao tác)** Chèn ảnh phân hệ 1. Chỉ vào `user_roles` và `member_packages`.

"Phân hệ đầu tiên. Một người dùng có thể có nhiều vai trò và một vai trò có nhiều người dùng, đây là quan hệ nhiều-nhiều nên nhóm tách ra bảng trung gian user_roles. Bảng hồ sơ cá nhân là quan hệ một-một với người dùng. Bảng member_packages là bảng trung gian giữa người dùng và gói tập, nhưng nó không chỉ nối hai bảng mà còn mang thuộc tính riêng như trạng thái, ngày bắt đầu, ngày hết hạn và số buổi còn lại. Mỗi cột đều có kiểu dữ liệu phù hợp, ví dụ giá tiền dùng decimal, ngày dùng date, các cờ dùng bit."

## Slide 12 (1:00): Phân hệ Lịch tập, Điểm danh và Lương PT

**(thao tác)** Chèn ảnh phân hệ 2. Chỉ vào `bookings`.

"Phân hệ thứ hai. Bảng bookings nối hội viên và huấn luyện viên, cả hai đều là bảng người dùng, nên đây là quan hệ nhiều-nhiều giữa hai người, được tách bằng bảng đặt lịch với ngày giờ và trạng thái riêng. Ca làm việc của huấn luyện viên lưu theo ngày cụ thể chứ không theo thứ lặp lại. Hồ sơ khuôn mặt là một-một với người dùng. Bảng commissions gắn một-một với bookings: mỗi buổi tối đa một bản ghi trả công, tránh tính lương hai lần. Bảng gym_check_ins là bảng mới, em nói ở slide 17."

## Slide 13 (1:00): Phân hệ Thanh toán và Bán lẻ

**(thao tác)** Chèn ảnh phân hệ 3. Chỉ vào `pos_order_items`.

"Phân hệ thứ ba, bán lẻ. Đây là ví dụ kinh điển của quan hệ nhiều-nhiều: một đơn hàng có nhiều sản phẩm, một sản phẩm nằm trong nhiều đơn hàng, nên cần bảng trung gian pos_order_items, tương đương bảng chi tiết đơn hàng. Bảng thanh toán có hai khoá ngoại tuỳ chọn, một trỏ tới gói tập, một trỏ tới đơn bán lẻ, và ràng buộc để mỗi giao dịch chỉ gắn với đúng một thứ. Cột người trả tiền cho phép trống để ghi nhận khách vãng lai mua lẻ tại quầy."

## Slide 14 (0:45): Khoá chính và khoá ngoại

"Về khoá chính: cả 19 bảng đều có khoá chính, kiểu UUID. Riêng bảng user_roles dùng khoá chính kép gồm hai cột. Về khoá ngoại: có 28 khoá ngoại, và không còn cột nào mang tên kết thúc bằng id mà thiếu khoá ngoại. Để không có dữ liệu mồ côi, mặc định khi xoá là không cho xoá bản ghi cha nếu còn bản ghi con. Chỉ ba chỗ cho phép xoá theo dây chuyền vì dữ liệu con vô nghĩa khi mất cha: vai trò của người dùng, hồ sơ cá nhân, và chi tiết đơn bán lẻ. Gói tập thì không xoá cứng mà chỉ ngừng bán, vì đã có hội viên đăng ký."

## Slide 15 (1:00): Mối quan hệ 1-1, 1-N, N-N

**(thao tác)** Nếu có ảnh cắt vùng `pos_orders`, `pos_order_items`, `products` thì chỉ vào đó.

"Tổng hợp lại các quan hệ: có 4 quan hệ một-một, 24 quan hệ một-nhiều, và 4 quan hệ nhiều-nhiều. Điều giảng viên yêu cầu bắt buộc là xử lý triệt để nhiều-nhiều bằng bảng trung gian, và nhóm đã tách cả bốn: người dùng với vai trò, người dùng với gói tập, đơn bán lẻ với sản phẩm, và hội viên với huấn luyện viên qua bảng đặt lịch. Không còn quan hệ nhiều-nhiều nào để nguyên."

## Slide 16 (1:00): Chuẩn hoá 2NF và 3NF

"Về chuẩn hoá. Dạng chuẩn hai: 18 bảng có khoá chính đơn nên không có phụ thuộc vào một phần của khoá; bảng user_roles có khoá kép nhưng không có thuộc tính nào ngoài khoá. Dạng chuẩn ba: nhóm loại bỏ các cột có thể suy ra hoặc trùng lặp, ví dụ lưu tuổi thì đổi thành ngày sinh vì tuổi thay đổi theo thời gian, bỏ cột ảnh đại diện bị trùng, bỏ thành tiền và tổng tiền trong bán lẻ.

Nhóm cũng giữ có chủ ý vài cột trông như suy ra được, và giải thích lý do. Ví dụ ngày hết hạn của gói là giá trị đã chốt lúc thanh toán, nếu sau này thời hạn gói đổi thì gói đã bán không đổi theo. Đơn giá trong chi tiết đơn là bản chụp giá lúc bán để hoá đơn cũ không bị đổi khi giá sản phẩm thay đổi."

## Slide 17 (0:45): Cải tiến thiết kế từ phân tích

**(thao tác)** Nếu có ảnh trước và sau của bảng thanh toán thì chỉ vào thay đổi.

"Quá trình đối chiếu user story với bảng giúp nhóm cải tiến thiết kế năm điểm. Quan trọng nhất là bảng thanh toán: bản cũ dùng một cặp cột loại và mã để trỏ tới hai bảng khác nhau nên không khai báo được khoá ngoại, và còn không ghi nhận được thanh toán của khách vãng lai. Bản mới dùng hai khoá ngoại tách riêng. Thứ hai là thêm bảng gym_check_ins vì bản cũ chỉ ghi điểm danh vào bảng đặt lịch, nên hội viên vào phòng mà không có buổi với huấn luyện viên thì không có chỗ lưu. Ba điểm còn lại là chuẩn hoá. Cả năm thay đổi đã được nhóm duyệt."

## Slide 18 (0:30): Hiện trạng triển khai

**(thao tác)** Cập nhật bảng trạng thái cho đúng thực tế trước khi nói. Nói thật, không nói quá.

"Về hiện trạng: phần nền tảng và phần gói tập, thanh toán đã chạy thật trên nhánh chính. [Nói thêm trạng thái V3, V4, V5, V6 theo đúng thực tế.] Năm cải tiến ở slide trước sẽ được triển khai dần theo thứ tự ưu tiên; phần nào chưa kịp, nhóm trình bày như thiết kế mục tiêu đã được duyệt."

## Slide 19 (0:30): Kết luận

"Tóm lại, nhóm làm rõ yêu cầu trước với sáu tác nhân, 83 user story và hai luồng cốt lõi. Cơ sở dữ liệu truy vết được về yêu cầu, mỗi bảng phục vụ ít nhất một user story, đạt các tiêu chí về khoá chính, khoá ngoại, bốn quan hệ nhiều-nhiều tách bằng bảng trung gian và chuẩn hoá đến dạng ba. Thiết kế cũng được cải tiến sau khi đối chiếu. Em xin cảm ơn thầy cô và các bạn, nhóm sẵn sàng trả lời câu hỏi."

---

# BẢN RÚT GỌN (khoảng 10 phút)

Nếu thời gian ngắn, nói theo thứ tự: slide 1 (0:20), 2 (0:30), 3 (0:40), 4 (0:50), 5 (0:30), 8 (1:00, dùng làm ví dụ luồng có ngoại lệ), 9 (0:30), 10 (0:50), 11, 12, 13 (mỗi slide 0:40), 15 (0:50), 16 (0:50), 19 (0:30). Bỏ slide 6, 7, 14, 17, 18, và nhắc vắn tắt hiện trạng triển khai ở câu kết.

---

# CÂU HỎI DỰ KIẾN

**1. Tại sao khoá chính dùng UUID?**
Lý do thường dùng: khó đoán hơn số tăng dần nên không lộ số lượng bản ghi qua đường dẫn, sinh được ngay không cần phụ thuộc bộ đếm của một bảng, thuận lợi khi gộp dữ liệu nhiều chi nhánh. Đánh đổi: dài hơn nên chỉ mục lớn hơn. (Đây là lý do chung; nhóm nên thống nhất câu trả lời trước khi trình bày.)

**2. Tại sao cột trạng thái dùng VARCHAR mà không dùng CHECK hay enum?**
Nhóm chủ ý không dùng ràng buộc `CHECK` cứng để dễ mở rộng giá trị mà không phải đổi kiểu cột. Đánh đổi: việc kiểm tra giá trị hợp lệ nằm ở tầng ứng dụng.

**3. Quan hệ nhiều-nhiều nằm ở đâu?**
Bốn chỗ: `user_roles`, `member_packages`, `pos_order_items`, `bookings` (slide 15).

**4. Làm sao đảm bảo không có dữ liệu mồ côi?**
28 khoá ngoại, mặc định không cho xoá cha khi còn con; ba chỗ xoá dây chuyền có chủ ý; ràng buộc `CHECK` ở bảng thanh toán. Cần nói thật: liên kết giữa `trainer_schedules` và `bookings` là liên kết logic, không có khoá ngoại cứng, vì một buổi đã huỷ vẫn có thể tồn tại cùng khung giờ với buổi đang hoạt động; luật một người một khung giờ được kiểm soát ở tầng dịch vụ.

**5. Tại sao `bookings` có hai nhóm cột điểm danh (hội viên và PT)?**
Mỗi buổi tập luôn có đúng hai người với vai trò cố định, và hai nhóm này có luật khác nhau: PT điểm danh để tính công, hội viên điểm danh để tránh bị tính vắng. Nếu cần chuẩn hoá chặt hơn, có thể tách một bảng điểm danh riêng; nhóm chọn giữ cấu trúc hiện tại vì đã triển khai.

**6. Tại sao giữ `end_date`, `remaining_sessions` dù suy ra được?**
Xem bảng ở slide 16: đó là giá trị đã chốt theo thời điểm (bản chụp), không phải dữ liệu dư thừa.

**7. Bản cũ có khoá đa hình, tại sao sửa?**
Cặp cột loại và mã trỏ tới hai bảng khác nhau nên không khai báo được khoá ngoại, có thể trỏ tới dòng không tồn tại. Bản mới dùng hai khoá ngoại riêng cộng ràng buộc đúng một cột có giá trị. Việc sửa cũng cho phép ghi nhận thanh toán của khách vãng lai.

**8. Bảng nào đã chạy thật, bảng nào mới là thiết kế?**
Trả lời theo bảng cập nhật ở slide 18. Không nói quá: phần đã merge thì nói đã chạy, phần chưa merge thì nói là thiết kế đã duyệt.

**9. Xoá gói tập thì sao?**
Không xoá cứng, chỉ ngừng bán (`is_active = 0`), vì `member_packages` đang tham chiếu gói đó và hội viên cũ phải giữ lịch sử.

**10. Dữ liệu khuôn mặt lưu thế nào, có lưu ảnh không?**
Trình duyệt trích đặc trưng khuôn mặt thành vector 128 số; mỗi lần quét chỉ gửi vector lên, không gửi ảnh. Máy chủ tự tính khoảng cách giữa vector gửi lên và vector đã đăng ký (ngưỡng khớp 0,45), không tin kết quả khớp do máy khách tự gửi. Vector lưu dạng chuỗi JSON; ảnh gốc chỉ là tuỳ chọn làm bằng chứng, không dùng để so khớp. Vector được coi là một giá trị nguyên khối, không truy vấn từng phần tử.

**11. Tại sao chia thành 4 sơ đồ?**
19 bảng đủ cột trên một ảnh quá dày, đường nối kéo rất dài, khó đọc. Nhóm có một sơ đồ tổng quan để xem quan hệ và ba sơ đồ phân hệ đủ cột.

**12. `users.phone` vừa duy nhất vừa cho phép trống có vấn đề gì không?**
Có. SQL Server chỉ cho phép một dòng `NULL` trên cột `UNIQUE`, nên người dùng thứ hai không nhập số điện thoại sẽ bị lỗi. Nhóm đã biết và có kế hoạch sửa bằng chỉ mục duy nhất có điều kiện (chỉ áp dụng khi số điện thoại khác trống).

**13. Hệ thống có hỗ trợ nhiều chi nhánh không?**
Có bảng `branches`, và `branch_id` có ở `member_packages`, `bookings`, `pos_orders` (và `gym_check_ins` trong bản v2). Báo cáo nâng cao theo chi nhánh nằm ngoài phạm vi phiên bản 1.0.

---

# LƯU Ý KHI TRÌNH BÀY

- Không đọc nguyên bảng. Mỗi bảng chỉ nêu 1 đến 2 dòng quan trọng nhất, phần còn lại để khán giả tự đọc.
- Khi có ảnh sơ đồ, luôn chỉ vào đúng bảng đang nói (con trỏ hoặc bút chỉ).
- Nói thật về trạng thái triển khai: phần đã chạy thật khác với thiết kế mục tiêu đã duyệt. Giảng viên đánh giá cao việc nhóm biết rõ mình đang ở đâu.
- Nếu trình bày theo nhóm, đề xuất chia: một người nói Phần 1 (slide 1 đến 8), một người nói Phần 2 (slide 9 đến 19).
- Trước buổi trình bày, chạy thử một lần có bấm giờ và dùng bản rút gọn nếu bị vượt thời gian.
