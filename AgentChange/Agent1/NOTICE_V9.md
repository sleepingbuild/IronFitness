# THÔNG BÁO: V9 đã vào `master` (gửi Agent 2 đến 6)

## 1. Thay đổi
Migration `V9__users_phone_profile_dob.sql` (đã kiểm thử trên SQL Server thật, database trống, áp dụng V1 đến V9 sạch):

| Thay đổi | Chi tiết |
|---|---|
| `users.phone` | Bỏ `UNIQUE` thường, thay bằng filtered unique index `uq_users_phone` (`WHERE phone IS NOT NULL`). **Nhiều tài khoản không có số điện thoại vẫn hợp lệ**; số đã có thì không được trùng |
| `user_profiles.age` | Đổi thành `date_of_birth DATE`. Hồ sơ cũ có `date_of_birth = NULL` (tuổi cũ không đổi chính xác được) |
| `users.avatar_url` | Đã bỏ. Chỉ còn `user_profiles.avatar_url` |

Code kèm theo: entity `User` không còn `avatarUrl`; API hồ sơ nhận `dateOfBirth` (thay cho `age`) và trả `dateOfBirth` cùng `age` tính sẵn. Quy tắc: từ 18 tuổi trở lên tính đến ngày đăng ký, vi phạm trả `PROFILE_INVALID_AGE`.

## 2. Việc cần làm (mọi agent)

```powershell
git fetch origin
git merge origin/master
mvn clean verify
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

- Database riêng của bạn tự áp dụng V9 khi khởi động. Không cần chạy tay.
- Nếu bạn đã merge `master` trong khoảng V9 còn lỗi cú pháp (bản đầu) và ứng dụng không khởi động được: bản đã sửa nằm trên `master` mới nhất. Merge lại là chạy được; Flyway không để lại bản ghi hỏng nên không cần `repair`. Nếu vẫn báo lỗi liên quan V9, tạo lại database riêng của bạn.
- Không dùng `gms_db_ci` (chỉ dành cho Jenkins).

## 3. Lưu ý theo từng agent
- **Agent 6:** không còn phải truyền số điện thoại thật khi `createMemberAccount` để tránh trùng `NULL`. Phone trống được chuẩn hoá thành `NULL` và nhiều tài khoản như vậy hợp lệ.
- **Agent 2, 4, 5:** nếu code hoặc test của bạn dùng `User.getAvatarUrl()`, `UserProfile.getAge()` hoặc `ProfileResponse` với `age`, sẽ không biên dịch được. Dùng `UserProfile.getAvatarUrl()` và `getDateOfBirth()`. Báo Agent 1 nếu cần thêm hàm.
- **Agent 4:** V8 (`gym_check_ins`) của bạn không được tham chiếu `users.avatar_url` hay `user_profiles.age`.

## 4. Thứ tự migration hiện tại
`V1, V2, V2.1, V2.2, V3, V4, V6, V9` đã có. `V5` (Agent 5), `V7` (Agent 2), `V8` (Agent 4) sẽ vào sau nhờ `out-of-order`. Không đổi số phiên bản đã dùng.
