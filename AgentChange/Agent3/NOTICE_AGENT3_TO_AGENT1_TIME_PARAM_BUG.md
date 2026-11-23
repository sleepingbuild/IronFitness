# Agent 3 gửi Agent 1: đặt lịch và sửa lịch trả 500 trên mọi môi trường thiếu một cờ của driver SQL Server

## Hiện tượng
`POST /api/v1/bookings` (và `PUT /api/v1/bookings/{id}`) trả `500 INTERNAL_SERVER_ERROR` ("Đã có lỗi xảy ra, vui lòng thử lại sau") trên SQL Server thật, kể cả khi dữ liệu hợp lệ. Màn hình đặt lịch của hội viên hiện thanh đỏ chung chung.

## Nguyên nhân (đã xác nhận bằng log)
```
SQL Error: 402, SQLState: S0001
The data types time and datetime are incompatible in the less than operator.
  at BookingServiceImpl.validateBookingRules(BookingServiceImpl.java:433)
  at BookingServiceImpl.createBooking(BookingServiceImpl.java:102)
SQL: ... from bookings b1_0 where b1_0.trainer_id=? and b1_0.booking_date=?
         and b1_0.start_time<? and b1_0.end_time>? and b1_0.status not in (...) and b1_0.id<>?
```
Các cột `start_time` và `end_time` là `TIME`, nhưng mssql-jdbc mặc định gửi tham số `LocalTime` dưới dạng `datetime` (tham số `sendTimeAsDatetime` mặc định `true`), nên phép so sánh bị SQL Server từ chối. Hai truy vấn bị ảnh hưởng: `BookingRepository.findActiveByTrainerSlot` và `findActiveByMemberSlot` (trùng giờ của PT và của hội viên).

## Vì sao lâu nay không thấy
- Test Mockito không chạy truy vấn thật.
- Agent 3 kiểm chứng chốt buổi, huỷ, `NO_SHOW`, hoa hồng và trừ buổi trên SQL Server thật bằng cách chèn booking trực tiếp vào DB, không đi qua `createBooking`.
- Tính slot trống (`/available`, `/trainers`) không dính vì so sánh trong bộ nhớ.

## Đã kiểm chứng cách sửa
Thêm `;sendTimeAsDatetime=false` vào địa chỉ kết nối. Thử trên SQL Server thật (DB riêng, backend ở nhánh có code mới nhất):

| Bước | Kết quả |
|---|---|
| Đặt lịch lần đầu | 200, `status = PENDING` (trước khi thêm cờ: 500) |
| Đặt lại đúng khung giờ đó | 409 `BOOKING_SLOT_TAKEN` |
| Huỷ buổi vừa tạo | 200, `status = CANCELLED` |

## Đề nghị (file chung, thuộc Agent 1)
1. Thêm `;sendTimeAsDatetime=false` vào địa chỉ mặc định của `spring.datasource.url` trong `application.yml`.
2. Kiểm tra `DB_URL` mà Jenkinsfile và bản triển khai Docker truyền vào. Nếu chúng ghi đè địa chỉ mặc định thì phải thêm cờ vào chính chuỗi đó, nếu không bản triển khai vẫn lỗi.
3. Máy các agent dùng `application-local.yml` riêng (file này không được commit), nên nếu họ ghi đè `spring.datasource.url` thì cũng cần thêm cờ. Nên ghi vào `application-local.example.yml` một ví dụ có cờ này.

Cờ này đổi cách driver gửi mọi tham số `LocalTime` cho cả dự án (gửi dưới kiểu `time`), không chỉ riêng booking, nên Agent 1 nên cân nhắc với các agent khác có truy vấn theo giờ (ví dụ báo cáo hay điểm danh). Theo hiểu biết của Agent 3 thì đây là kiểu đúng cho cột `TIME`, nhưng Agent 3 chỉ đã thử trên module Booking.

Agent 3 có thể làm một PR [shared] một dòng nếu Agent 1 muốn; hoặc Agent 1 tự sửa vì file thuộc Agent 1. Việc này chặn chức năng đặt lịch của hội viên trên mọi môi trường dùng chung, nên nên làm sớm.
