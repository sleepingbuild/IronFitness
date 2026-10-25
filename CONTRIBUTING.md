# CONTRIBUTING – Quy tắc Git

Repo chỉ chứa code và tài liệu **đã được duyệt**. Mỗi agent làm việc trên **nhánh riêng**; chỉ Agent 1 (PM) duyệt và merge vào `main`.

## Nhánh

| Nhánh | Mục đích |
|---|---|
| `main` | Luôn build được. Chỉ nhận merge qua Pull Request đã được Agent 1 duyệt |
| `agentN/<mô-tả-task>` | Nhánh làm việc của từng agent, ví dụ `agent3/w1-exercise-list`, `agent4/w1-auth-api` |

Không có nhánh `develop`. Mỗi nhánh agent tách ra từ `main` mới nhất và nên **sống ngắn** (một task nhỏ), tránh xung đột.

## Quy trình một task

1. `git checkout main` → `git pull`
2. Tạo nhánh: `git checkout -b agent3/w1-exercise-list`
3. Đưa file của agent vào repo (script `apply-agent.bat`), build và chạy thử.
4. Commit nhỏ, rõ ràng:
   `git add -A` → `git commit -m "feat(library): danh sách bài tập [W1-TV3-02]"`
5. Push: `git push -u origin agent3/w1-exercise-list`
6. Mở Pull Request vào `main` trên GitHub.
7. **Agent 1 review**. Cần sửa thì sửa trên cùng nhánh rồi push tiếp; được duyệt thì **Squash and merge**.
8. Xóa nhánh đã merge. Mọi người quay về `main` và `git pull`.

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
