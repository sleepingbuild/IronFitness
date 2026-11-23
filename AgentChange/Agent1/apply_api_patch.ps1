# Chạy trong thư mục gốc repo, trên nhánh đang sửa (feature/agent1-shared-fixes).
# Thay các dòng của bảng "Leads/CRM (Agent 6)" trong API_DESIGN.md và thêm quy tắc trạng thái + ghi chú báo cáo.
$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$p = Join-Path $PWD "API_DESIGN.md"
$t = [IO.File]::ReadAllText($p, $utf8)

if ($t.Contains("Quy tắc trạng thái lead")) { Write-Host "API_DESIGN.md da co noi dung nay, bo qua" -ForegroundColor Yellow; return }

$nl = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }
$lines = New-Object 'System.Collections.Generic.List[string]'
$lines.AddRange([string[]]($t -split "\r?\n"))

$h = -1
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].StartsWith("### Leads/CRM (Agent 6)")) { $h = $i; break }
}
if ($h -lt 0) { throw "Khong tim thay muc '### Leads/CRM (Agent 6)'" }

$start = $h + 3                      # bo qua dong tieu de bang va dong |---|
$end = $start
while ($end -lt $lines.Count -and $lines[$end].StartsWith("|")) { $end++ }
Write-Host "Thay $($end - $start) dong cu bang bang moi"

$rows = @(
'| GET | `/api/v1/leads?status=&keyword=&page=&size=&sort=` | Sales, Super Admin. Sales chỉ thấy lead của mình hoặc chưa gán. `sort`: `createdAt`, `updatedAt`, `fullName`, `status` |',
'| POST | `/api/v1/leads` | Sales, Super Admin. Response có `duplicatePhone` (true nếu số đã có ở lead khác, lead vẫn được tạo) |',
'| GET | `/api/v1/leads/{id}` | Sales, Super Admin. Lead của Sales khác trả 404 |',
'| PUT | `/api/v1/leads/{id}` | Sales, Super Admin. Lead `CONVERTED` trả 409 `LEAD_ALREADY_CONVERTED` |',
'| PATCH | `/api/v1/leads/{id}/status` (body: `status` = `CONTACTED` hoặc `LOST`) | Sales, Super Admin. Chuyển không hợp lệ trả 400 `LEAD_INVALID_STATUS_TRANSITION` |',
'| POST | `/api/v1/leads/{id}/convert` (body: `email` bắt buộc; `password` tuỳ chọn, tối thiểu 8 ký tự) | Sales phụ trách lead, Super Admin. Tạo user `MEMBER`, không sinh hoa hồng. Bỏ trống `password` thì sinh ngẫu nhiên 12 ký tự và trả `temporaryPassword` đúng một lần (không ghi log, không lưu dạng thô) |',
'| GET | `/api/v1/leads/expiring-soon?days=N` | Sales, Super Admin — hội viên có gói `ACTIVE` sắp hết hạn. *(chưa triển khai, chờ Agent 2)* |',
'| GET | `/api/v1/reports/revenue?from=&to=&groupBy=DAY\|MONTH` | Super Admin. `from`, `to` dạng `yyyy-MM-dd`, mặc định từ ngày 1 tháng này đến hôm nay, tối đa 366 ngày |',
'| GET | `/api/v1/reports/renewal-rate?from=&to=&windowDays=` | Super Admin. `windowDays` 1 đến 365, mặc định 30 |',
'| GET | `/api/v1/reports/checkin-traffic?from=&to=` | Super Admin. Lượt vào buổi PT theo ngày và giờ. *(tạm thời, nguồn dữ liệu sẽ chốt sau khi V8 `gym_check_ins` vào `master`)* |',
'| GET | `/api/v1/reports/top-packages?from=&to=&limit=` | Super Admin. `limit` 1 đến 50, mặc định 10 |',
'| GET | `/api/v1/reports/commissions-summary` | Super Admin — tổng lương theo buổi của PT. *(chưa triển khai, chờ Agent 5)* |'
)

$extra = @(
'',
'> **Quy tắc trạng thái lead:** `NEW → CONTACTED | LOST | CONVERTED`; `CONTACTED → LOST | CONVERTED`. `LOST` và `CONVERTED` là trạng thái cuối. `CONVERTED` chỉ được đặt qua `POST /leads/{id}/convert`.',
'',
'**Ghi chú cho mục báo cáo**',
'- Báo cáo là truy vấn chỉ đọc. Ngày "hôm nay" và khoảng mặc định tính theo `Asia/Ho_Chi_Minh` (dùng bean `Clock`).',
'- `revenue`: giao dịch `SUCCESS`, ngày ghi nhận là thời điểm chuyển sang thành công; là doanh thu gói tập (gộp), chưa trừ đơn POS đã huỷ.',
'- `renewal-rate`: tính theo `end_date`, đếm theo gói; `rate` nằm trong [0, 1] và là `null` nếu không có gói hết hạn trong kỳ.',
'- `checkin-traffic`: hiện chỉ đếm lượt vào buổi PT (`bookings.check_in_time`); chưa có dữ liệu lượt vào phòng chung.',
'- `top-packages`: mỗi giao dịch `MEMBERSHIP` `SUCCESS` là một lượt đăng ký.'
)

$lines.RemoveRange($start, $end - $start)
$lines.InsertRange($start, [string[]]$rows)
$lines.InsertRange($start + $rows.Count, [string[]]$extra)

[IO.File]::WriteAllText($p, ($lines -join $nl), $utf8)
Write-Host "API_DESIGN.md da cap nhat" -ForegroundColor Green
git diff --stat -- API_DESIGN.md
