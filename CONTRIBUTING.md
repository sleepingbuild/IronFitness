# CONTRIBUTING – Quy tắc Git

Repo chỉ chứa code và tài liệu **đã được duyệt**. Chúng ta sử dụng mô hình **nhánh cố định theo thành viên**.

## Nhánh

Dự án chỉ duy trì 5 nhánh cố định tương ứng với 5 thành viên (5 Agent chính):
- `phi` (Agent 1 - Tech Lead / PM)
- `tuan-kiet` (Agent 2 - Android A)
- `van-quang` (Agent 3 - Android B)
- `quang-anh` (Agent 4 - Backend & Admin Web)
- `van-huan` (Agent 5 - UI/QA/Content)

*(Lưu ý: Các task frontend UI do Agent 6 hỗ trợ sẽ commit thẳng vào nhánh của người phụ trách logic màn hình đó).*

| Nhánh | Mục đích |
|---|---|
| `main` | Luôn build được. Chỉ nhận merge qua Pull Request đã được Agent 1 duyệt |
| `[tên-thành-viên]` | Nhánh làm việc tồn tại lâu dài của từng agent (ví dụ: `quang-anh`, `tuan-kiet`) |

**Quy tắc quan trọng:** Bất kỳ nhánh tạm nào (nếu bắt buộc phải tạo) đều phải được **xoá ngay sau khi xong việc**. Không để lại nhánh rác.

## Quy trình một task

1. Chuyển sang nhánh cá nhân: `git checkout [tên-của-bạn]` (ví dụ: `git checkout quang-anh`).
2. **LUÔN LUÔN CẬP NHẬT TỪ MAIN:** `git pull origin main` (Để lấy code mới nhất, tránh conflict do nhánh cá nhân là long-running).
3. Làm việc, đưa file vào repo, build và chạy thử.
4. Commit nhỏ, rõ ràng:
   `git add -A` → `git commit -m "feat(library): danh sách bài tập [W1-TV3-02]"`
5. Push lên nhánh cá nhân: `git push origin [tên-của-bạn]`
6. Mở Pull Request từ nhánh cá nhân vào `main` trên GitHub.
7. **Agent 1 review**. Cần sửa thì sửa trên cùng nhánh rồi push tiếp; được duyệt thì **Squash and merge**.
8. Sau khi merge, quay về nhánh cá nhân và `git pull origin main` để đồng bộ lại.

Nên bật bảo vệ nhánh `main` trên GitHub: **Settings → Branches → Add branch ruleset**, yêu cầu Pull Request trước khi merge.

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

Checklist của người mở PR:
- [ ] Build thành công
- [ ] Không có secret trong diff
- [ ] Không có code debug thừa
- [ ] Tài liệu liên quan đã cập nhật

Checklist Agent 1 khi duyệt:
- [ ] Đúng task và nằm trong phạm vi thư mục của vai trò (xem `AGENT.md` mục 4)
- [ ] Khớp `docs/API.md` và `docs/DATABASE.md`
- [ ] Build được, luồng chính chạy đúng
- [ ] Không có secret, không thêm thư viện ngoài danh sách khi chưa hỏi

## .gitignore tối thiểu

File `.gitignore` đã có sẵn trong repo (Android, backend, IDE, secret). `IronFitnessChanges` nằm ngoài repo nên không cần ignore.
