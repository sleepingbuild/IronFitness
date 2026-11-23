# Gửi Agent 2: `master` không khởi động được (vòng phụ thuộc bean)

## Tình hình
Jenkins đã dựng pipeline CI/CD cho `master`. Các stage Checkout, Restore, Build, Test, Publish đều **xanh**, nhưng ứng dụng **không khởi động được** khi chạy bản jar:

```
APPLICATION FAILED TO START
Relying upon circular references is discouraged and they are prohibited by default.
Update your application to remove the dependency cycle between beans.
```

Nguyên nhân: vòng phụ thuộc constructor `MembershipServiceImpl <-> PaymentServiceImpl`. `mvn verify` với Mockito không bắt được lỗi này, chỉ khi chạy thật mới thấy. Kết nối SQL Server, Flyway và Hibernate đều đã qua, lỗi nằm ở bước tạo bean.

## Cần làm (trong PR A của bạn)
1. **Gỡ vòng**, không dùng cờ cho qua:
   - KHÔNG đặt `spring.main.allow-circular-references=true`, vì vòng qua constructor không được cứu và cờ này chỉ che lỗi.
   - Ưu tiên tách một chiều: Payment phát sự kiện (`ApplicationEventPublisher`), Membership lắng nghe bằng `@EventListener`. Hoặc cho Payment không gọi ngược Membership.
   - `@Lazy` trên một tham số constructor chỉ là giải pháp tạm, dùng khi không còn cách khác.
2. Giữ nguyên chữ ký `PaymentService` và `RelatedType`.
3. Giữ các việc đã có trong PR A: `end_date = start_date + duration_days - 1`, `tryConsumeSession` không ném lỗi.

## Cách tái hiện và kiểm tra
Cần container SQL Server đang chạy (`docker ps`, cổng 1433, database `gms_db`) và `application-local.yml` đã điền.

```powershell
mvn clean verify
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Lỗi hiện tại sẽ in ra danh sách bean trong vòng (đoạn "The dependencies of some of the beans in the application context form a cycle"). Sau khi sửa phải thấy `Started GymManagementApplication`, và các đường dẫn Swagger (`/v3/api-docs`) trả `200`.

## Điều kiện để PR A được merge
- `mvn clean verify` xanh.
- `mvn spring-boot:run` khởi động được (không còn `APPLICATION FAILED TO START`).
- Có test, có cập nhật `API_DESIGN.md`, có Swagger annotation.

## Sau khi merge
Jenkins sẽ tự chạy lại pipeline và triển khai bản mới. Nếu stage Deploy/Verify đỏ thì lỗi nằm ở khởi động ứng dụng; hãy đọc Console Output của build và sửa tiếp trước khi báo xong.

Báo lại ngay khi PR A đã sửa vòng, vì Agent 3 và Agent 4 đang chờ nó lên `master` mới merge được.
