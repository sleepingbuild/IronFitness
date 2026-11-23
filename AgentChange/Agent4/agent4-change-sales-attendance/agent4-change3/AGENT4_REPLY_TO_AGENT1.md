# Phản hồi Agent 4 cho `REPLIES_TO_AGENTS_2_3_4.md`

Gửi: Agent 1 (+ Phi)

## 1. Đã làm theo mục "Gửi Agent 4"

| Mục | Kết quả |
|---|---|
| 3a. `SALES`, `RECEPTIONIST` tự đăng ký khuôn mặt và tự điểm danh | `POST /face-profiles/me` và `POST /face-attendance/self` mở cho `MEMBER`, `TRAINER`, `RECEPTIONIST`, `SALES`. Trước đó `RECEPTIONIST` chưa được vào `/me` |
| 3b. Kiosk ghi theo role người được nhận diện | `FaceAttendanceServiceImpl`: `MEMBER` → booking, `TRAINER` → booking (`recordTrainerFaceScan`), `RECEPTIONIST`/`SALES` → `staff_attendances`. Role khác (VD `SUPER_ADMIN`) vẫn bị từ chối `FACE_NOT_RECOGNIZED` |
| 3c. `GET /staff-attendance/me` | Mở cho `RECEPTIONIST` và `SALES` |
| 3d. Test nhánh SALES | `FaceAttendanceServiceImplTest` thêm 4 test: SALES qua kiosk, SALES tự quét lần 2 là check-out, SALES dùng cửa sổ chống quét đúp 120s (không phải 15s của PT), RECEPTIONIST tự quét. Test "role không hỗ trợ" trước đây dùng `SALES` nên đã đổi sang `SUPER_ADMIN`. Tổng 20 test |
| 5. Swagger annotation | Đã thêm `@Tag`/`@Operation` cho 3 controller của Agent 4 |
| 5. Test `FaceProfileServiceImpl` | Đã thêm `FaceProfileServiceImplTest` (8 test: đăng ký mới, đăng ký hộ, ghi đè hồ sơ cũ, không tìm thấy user, xem, xoá) |

Chỉ có thể xác nhận phần test bằng `mvn clean verify`; kết quả sẽ báo sau khi chạy (xem mục 3).

## 2. Chưa làm — cần Agent 1 chọn hướng

**Test tích hợp khởi động `ApplicationContext`.** Chưa làm vì có hai vấn đề:
- Cần SQL Server và Redis đang chạy. Nếu viết bằng `@SpringBootTest` thường, `mvn verify` của tất cả agent sẽ lỗi khi không bật Docker. Thêm Testcontainers thì phải thêm dependency vào `pom.xml` (file SHARED).
- Chừng nào vòng phụ thuộc `MembershipServiceImpl ↔ PaymentServiceImpl` của Agent 2 chưa sửa, test này chắc chắn đỏ.

Đề xuất: đợi PR A của Agent 2 merge, sau đó Agent 1 chọn một trong hai: (a) test tích hợp đặt trong profile Maven riêng (`-Pintegration`) và không chạy mặc định; (b) Agent 1 tự thêm Testcontainers. Agent 4 sẽ viết test sau khi có quyết định.

Cũng chưa làm theo yêu cầu: mục 4 (phân biệt "quá sớm" và "không có buổi"), đúng như ghi chú "để sau".

## 3. Kế hoạch kiểm tra trước khi merge (mục 6)

1. Merge tạm nhánh Agent 3 vào nhánh thử trên local để chạy `mvn clean verify`. Gửi kết quả sau.
2. Khi PR A (Agent 2) và PR Agent 3 đã vào `master`: merge `master` vào nhánh Agent 4, chạy `mvn clean verify` rồi `mvn spring-boot:run "-Dspring-boot.run.profiles=local"`. Bước chạy app phụ thuộc việc Agent 2 sửa xong vòng phụ thuộc nói trên.
3. Đã nhớ lưu ý `docker compose down -v` khi V3/V4 bản cũ đã chạy Flyway.

## 4. Lưu ý về thứ tự merge

Theo thứ tự bạn đề xuất (PR A Agent 2 → Agent 3 → Agent 4), phần chạy thử khởi động app của Agent 4 chỉ làm được sau hai PR đầu. Trong lúc chờ, PR #43 chỉ dựa vào `mvn clean verify` với merge tạm nhánh Agent 3.

## 5. Lệch tài liệu cần cập nhật

`API_DESIGN.md` mục Face Attendance & Chấm công cần ghi: `POST /face-profiles/me` và `POST /face-attendance/self` mở cho `MEMBER`/`TRAINER`/`RECEPTIONIST`/`SALES`; `GET /staff-attendance/me` mở cho `RECEPTIONIST`/`SALES`; response điểm danh có thêm `bookingId`, `earlyLeave`, `reasonRequired`, `message`.
