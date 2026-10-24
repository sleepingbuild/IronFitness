# CONTRIBUTING – Quy tắc Git

## Nhánh

| Nhánh | Mục đích |
|---|---|
| `main` | Luôn chạy được, chỉ merge qua PR, chỉ TV1 merge |
| `develop` | Tích hợp hằng ngày, các nhánh tính năng merge vào đây |
| `feature/<task-id>-<mo-ta>` | Một task, ví dụ `feature/W1-TV3-02-exercise-list` |
| `fix/<mo-ta>` | Sửa lỗi |
| `docs/<mo-ta>` | Chỉ sửa tài liệu |

Quy trình: tạo nhánh từ `develop` → commit nhỏ → push → mở PR vào `develop` → có ít nhất 1 người review → squash merge. Cuối tuần TV1 merge `develop` vào `main` sau khi demo.

## Commit message

Định dạng: `<loại>(<phạm vi>): <mô tả ngắn> [<task-id>]`

Loại: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.

Ví dụ:
- `feat(auth): thêm màn đăng nhập email [W1-TV2-01]`
- `fix(player): sửa timer chạy tiếp khi pause [W2-TV3-03]`
- `docs(api): bổ sung endpoint batch sessions`

## Pull Request

Mô tả cần có:
1. Task ID và việc đã làm.
2. Cách kiểm tra thủ công (các bước, dữ liệu mẫu).
3. Ảnh chụp màn hình nếu có thay đổi giao diện.
4. Có sửa `docs/API.md` hoặc `docs/DATABASE.md` không (nếu có, báo người liên quan).

Checklist trước khi mở PR:
- [ ] Build thành công
- [ ] Không có secret trong diff
- [ ] Không có code debug thừa (log, TODO không rõ)
- [ ] Tài liệu liên quan đã cập nhật

## .gitignore tối thiểu

```
# Android
android/.gradle/
android/build/
android/app/build/
android/local.properties
android/.idea/
*.keystore
*.jks

# Backend
backend/target/
backend/src/main/resources/application-local.yml
.idea/
*.iml

# Chung
.DS_Store
.env
```

## Review

- Review trong vòng 1 ngày làm việc.
- Góp ý rõ ràng, nêu lý do; tác giả sửa hoặc giải thích, không tự merge khi chưa được duyệt.
- Xung đột merge do người đến sau tự giải quyết.
