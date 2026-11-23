# Gửi Agent 6: `AuthService.createMemberAccount` đã có trên `master`

## Tình hình
Phần SHARED bạn đang chờ đã vào `master`, Jenkins build xanh. Bạn được gỡ chặn phần chuyển lead thành hội viên.

## Cách dùng
Inject `AuthService` (đừng tự tạo `User`, đừng viết `existsByPhone` riêng):

```java
User member = authService.createMemberAccount(fullName, email, phone, rawPassword);
```

| Mục | Nội dung |
|---|---|
| Trả về | `User` đã lưu (có `id`, role `MEMBER`) |
| Phát token | Không. Không đăng nhập, không trả `AuthResponse` |
| Mật khẩu | Truyền mật khẩu thô; hàm tự mã hoá. Không lưu mật khẩu thô ở đâu khác |
| Phone | Tự cắt khoảng trắng; chuỗi rỗng thành `null` |
| Trùng email | Ném `BusinessException(AUTH_EMAIL_ALREADY_EXISTS)` |
| Trùng phone | Ném `BusinessException(AUTH_PHONE_ALREADY_EXISTS)` |
| Transaction | `@Transactional`: nếu bạn gọi trong một transaction thì tham gia chung. Lỗi ở bước sau của luồng chuyển lead sẽ rollback cả tài khoản vừa tạo |

Không cần bắt hai lỗi trùng ở Leads: `GlobalExceptionHandler` đã chuyển thành response chuẩn. Tin nhắn cho người dùng là "Email đã được sử dụng" / "Số điện thoại đã được sử dụng".

## Lưu ý quan trọng
1. **Truyền số điện thoại thật cho tới khi V9 lên `master`.** Hiện `users.phone` là `UNIQUE` thường, SQL Server chỉ cho đúng một dòng `NULL`. Nếu nhiều lead không có số mà đều tạo tài khoản với phone trống, lần thứ hai sẽ lỗi 409 (`DATA_CONFLICT`). V9 (filtered unique index) do Agent 1 làm, sẽ báo khi xong.
2. Mật khẩu ban đầu của hội viên do luồng Leads quyết định (ví dụ sinh ngẫu nhiên rồi gửi qua kênh thông báo). Không dùng mật khẩu cố định cho mọi tài khoản.
3. `register` (đăng ký công khai) vẫn hoạt động như cũ nhưng nay dùng chung hàm này, nên cũng kiểm tra trùng phone.

## Thêm mới trong `GlobalExceptionHandler` (không viết handler riêng nữa)
- Body JSON sai, tham số sai kiểu (id không phải UUID...), `ConstraintViolationException`: trả **400** (`VALIDATION_ERROR`).
- `DataIntegrityViolationException` (trùng unique, vi phạm khoá ngoại): trả **409** (`DATA_CONFLICT`).

## Việc cần làm trước khi mở PR

```powershell
git fetch origin
git merge origin/master
mvn clean verify
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

- Nếu báo `Migration checksum mismatch for migration version 3`: xoá và tạo lại **database của bạn** `gms_db_agent6` rồi chạy lại. Không chạy `flyway repair`, không dùng `gms_db_ci` (chỉ dành cho Jenkins).
- Không sửa `AuthService`, `AuthServiceImpl`, `UserRepository`, `GlobalExceptionHandler`, `ApiResponse` (SHARED, thuộc Agent 1). Cần thêm gì thì nhờ Agent 1.
- Điều kiện Done như thông báo trước: `mvn clean verify` xanh, khởi động được, có test, cập nhật `API_DESIGN.md`, có Swagger annotation.

Báo lại khi PR sẵn sàng.
