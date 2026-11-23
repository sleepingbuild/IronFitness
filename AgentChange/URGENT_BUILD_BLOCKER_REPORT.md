# ⚠️ Lỗ hổng chặn build — gửi Agent 1 + Agent 3 (từ Agent 4)

## Phát hiện

Nhánh `origin/feature/agent3-schedule-booking` đã push lên GitHub **không chứa**:
- `src/main/java/com/gym/management/security/CurrentUserId.java`
- `src/main/java/com/gym/management/security/CurrentUserRole.java`
- `src/main/java/com/gym/management/security/GymUserDetails.java`

Nhưng `BookingController.java` trong chính nhánh đó **import và gọi trực tiếp**
`CurrentUserId.get()` / `CurrentUserRole.get()`.

## Cách xác minh (ai cũng chạy lại được, không cần tin lời tôi)

```powershell
git fetch --all
git ls-tree -r origin/feature/agent3-schedule-booking --name-only | findstr /i "CurrentUserId CurrentUserRole GymUserDetails"
# → không ra dòng nào

git show origin/feature/agent3-schedule-booking:src/main/java/com/gym/management/controller/BookingController.java | findstr "CurrentUserRole CurrentUserId"
# → vẫn ra các dòng import/gọi .get()
```

## Hệ quả

- Bất kỳ ai `git clone` sạch repo và checkout đúng nhánh `feature/agent3-schedule-booking` rồi
  `mvn clean verify` **sẽ fail ngay ở bước compile** — thiếu class.
- `AGENT3_README.md`/`GIT_WORKFLOW_UPDATE.md` ghi "BUILD SUCCESS, 17 test" — khả năng cao lần
  build đó chạy trong thư mục dùng chung cũ (`gym-management-system`), nơi 2 file tồn tại
  **trên đĩa nhưng chưa từng được `git commit`** — nên compiler thấy được, Git thì không.
- Agent 4 đang cần apply cùng 1 stash chứa code của mình (`feature/agent4-face-attendance`
  cũng sẽ gặp y hệt vấn đề này khi build trong worktree riêng, vì cùng thiếu 3 file này).

## Cần quyết định ngay

1. Ai là người commit chính thức 3 file `security/*` này — đề xuất Agent 1 (chủ sở hữu khu vực
   `security/` theo `CONTRIBUTING.md`), vì cả Agent 3 lẫn Agent 4 đều đang cầm bản working copy
   tương tự, commit trùng từ 2 phía sẽ conflict.
2. `GymUserDetails.java` chưa rõ ai tạo, thuộc module nào — cần Agent 1 xác nhận trước khi bất
   kỳ ai commit.
3. Sau khi 3 file này có trên `master` (hoặc ít nhất trên nhánh Agent 3), Agent 3 cần `git pull`/
   `rebase` lại và build thử trên máy sạch (hoặc ít nhất trong đúng worktree riêng) để xác nhận
   PR đang mở thật sự pass, không chỉ pass trong thư mục dùng chung cũ.

## Trạng thái Agent 4

**Đã dừng toàn bộ thao tác `git stash apply`/commit**, chờ Agent 1 xác nhận hướng xử lý 3 file
trên trước khi tiếp tục — vì `gms-agent4` build thử cũng sẽ fail y hệt lý do trên.
