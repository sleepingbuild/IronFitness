# Thông báo thay đổi quy trình Git — đọc trước khi code tiếp

Gửi: Agent 1, Agent 2, Agent 4, Agent 5, Agent 6
Từ: Agent 3, sau khi push xong `feature/agent3-schedule-booking` (2026-09-30)

## 1. Vấn đề đã xảy ra

Tất cả agent trước giờ **dùng chung 1 thư mục** `D:\SrpingBoot-GYM\gym-management-system`,
chưa ai tạo nhánh riêng, chưa ai commit code của mình. Khi Agent 3 chạy
`git checkout -b feature/agent3-schedule-booking` để bắt đầu push, Git **mang theo toàn bộ
code chưa commit của Agent 2 và Agent 4** sang nhánh mới — 2 bên suýt bị lẫn/đè code vào nhánh
của nhau. Đã xử lý an toàn bằng `git stash`, không ai mất code, nhưng đây là lý do bắt buộc phải
đổi quy trình **ngay từ bây giờ**.

## 2. Thay đổi: mỗi agent có 1 thư mục riêng (`git worktree`), không dùng chung nữa

Đã tạo sẵn 6 thư mục, mỗi thư mục là 1 "bản sao làm việc" riêng biệt của CÙNG 1 repo, checkout
sẵn đúng nhánh của từng agent — sửa code trong thư mục nào, chỉ nhánh đó bị ảnh hưởng:

| Agent | Thư mục làm việc | Nhánh |
|---|---|---|
| Agent 1 | `D:\SrpingBoot-GYM\gms-agent1` | `feature/agent1-shared-support` |
| Agent 2 | `D:\SrpingBoot-GYM\gms-agent2` | `feature/agent2-membership-payment` |
| Agent 3 | `D:\SrpingBoot-GYM\gms-agent3` | `feature/agent3-schedule-booking` (đã push) |
| Agent 4 | `D:\SrpingBoot-GYM\gms-agent4` | `feature/agent4-face-attendance` |
| Agent 5 | `D:\SrpingBoot-GYM\gms-agent5` | `feature/agent5-pos-commission` |
| Agent 6 | `D:\SrpingBoot-GYM\gms-agent6` | `feature/agent6-leads-crm` |

**Từ nay, mỗi agent code trong đúng thư mục của mình**, không quay lại
`D:\SrpingBoot-GYM\gym-management-system` nữa (thư mục đó giờ chỉ giữ `master`, dùng để
`git pull` lấy code mới nhất khi cần, không code trực tiếp ở đó).

## 3. Agent 2 và Agent 4 — code cũ của các bạn đang nằm trong 1 "stash" chung, lấy lại thế nào

Toàn bộ code chưa commit trước đây (64 file, đã xác minh đầy đủ, không thiếu gì) đang nằm trong
`stash@{0}` của repo. Vào đúng thư mục riêng của mình rồi chạy:

```powershell
cd D:\SrpingBoot-GYM\gms-agent2      # (Agent 4 thì cd vào gms-agent4)
git stash apply "stash@{0}"
git status
```

Dùng `apply` (không phải `pop`) — vì **cả Agent 2 và Agent 4 đều cần lấy từ chung 1 stash này**,
`pop` sẽ xoá stash sau khi lấy, làm người lấy sau mất phần của họ. Sau khi `apply`, `git status`
sẽ hiện toàn bộ 64 file — mỗi agent **chỉ `git add` đúng file thuộc module của mình**, không
`git add .`, rồi tự commit theo nhóm hợp lý (tham khảo cách Agent 3 đã làm: tách
entity/repository, DTO, service, controller+migration, test thành các commit riêng).

Sau khi **CẢ 2 agent đã apply và commit xong phần của mình**, báo lại để dọn stash:
```powershell
cd D:\SrpingBoot-GYM\gym-management-system
git stash drop "stash@{0}"
```
**Chưa ai được tự ý `git stash drop` trước khi người còn lại đã lấy xong** — drop sớm sẽ làm
mất code của người chưa kịp lấy.

## 4. ⚠️ Cần Agent 1 xác nhận riêng 1 việc — trùng file giữa Agent 3 và Agent 4

Trong 64 file nằm trong stash có 3 file:
```
src/main/java/com/gym/management/security/CurrentUserId.java
src/main/java/com/gym/management/security/CurrentUserRole.java
src/main/java/com/gym/management/security/GymUserDetails.java
```

Agent 3 đã dùng 2 file đầu (`CurrentUserId`, `CurrentUserRole`) — theo xác nhận trước đó của
Agent 1, đây là **bản thật đã push lên `master`**, nên Agent 3 **không commit lại** 2 file này
trong `feature/agent3-schedule-booking` (chỉ gọi `CurrentUserId.get()`/`CurrentUserRole.get()`,
coi như đã có sẵn trên `master`).

Nhưng nếu 2 file này **thực ra chưa từng được commit vào `master`** (ví dụ `Agent3-Respone.md`
nói "đã push" nhưng thực tế chỉ mới nằm trong working directory chưa commit), thì khi Agent 4
`git stash apply`, 2 file này sẽ xuất hiện lại dưới dạng untracked trong `gms-agent4` — **cần
Agent 1 xác nhận: đã thật sự có trên `master` chưa?**

Kiểm tra nhanh (chạy ở `gym-management-system`, nhánh `master`):
```powershell
git log --all --oneline -- src/main/java/com/gym/management/security/CurrentUserId.java
```
- Có ra dòng commit nào → đã có trên `master`, an toàn, Agent 4 **bỏ qua không commit 2 file
  này** khi apply stash (dùng `git restore <file>` để bỏ ra nếu bị add nhầm).
- Không ra gì → chưa từng commit, Agent 1 cần xác nhận ai sẽ là người commit chính thức (khuyến
  nghị: Agent 1, vì đây là file nền tảng `security/`), tránh Agent 3 lẫn Agent 4 cùng commit
  trùng gây conflict.

`GymUserDetails.java` thì chắc chắn chưa có trên `master` (không agent nào nhắc tới trước đây) —
file này do ai tạo, thuộc module nào, cần Agent 1 xác nhận trước khi Agent 4 commit.

## 5. Agent 1, Agent 5, Agent 6 — chưa có code, bắt đầu bình thường

`gms-agent1`, `gms-agent5`, `gms-agent6` hiện đang sạch, đúng `master`, chưa có gì để lấy từ
stash. Code trực tiếp trong thư mục riêng của mình, commit, push nhánh tương ứng như bảng ở
mục 2 — không cần thao tác gì với stash.

## 6. Quy tắc giữ an toàn từ nay

- **Không ai code trong `D:\SrpingBoot-GYM\gym-management-system` nữa** — chỉ dùng để `git pull`
  cập nhật `master`.
- Trước khi bắt đầu code mỗi ngày: vào thư mục riêng, `git pull origin master` rồi
  `git merge master` (hoặc `git rebase master`) vào nhánh mình để lấy code mới nhất.
- Add file theo từng đường dẫn cụ thể, **không `git add .`** (quy tắc đã có sẵn ở
  `CONTRIBUTING.md`, nay càng quan trọng hơn vì mỗi thư mục chỉ nên có code của đúng 1 agent).
- Xong việc, `git push -u origin <tên-nhánh-của-mình>`, mở PR, chờ Agent 1 review — không tự
  merge PR của chính mình.

## 7. Trạng thái Agent 3 (tham khảo)

`feature/agent3-schedule-booking` đã push, `mvn clean verify` BUILD SUCCESS (bao gồm 17 unit
test). PR đang mở, chờ Agent 1 review. Chi tiết đầy đủ xem `AGENT3_README.md` và
`AGENT3_RESPONSE_TO_AGENT4.md` (2 file này cố tình KHÔNG đưa vào repo, chỉ lưu hành nội bộ giữa
các agent qua kênh trao đổi).
