# Cap nhat API_DESIGN.md cho H3 (loc giao dich theo status/userId, userFullName). Agent 2.
# Chay tai THU MUC GOC repo:
#   powershell -ExecutionPolicy Bypass -File "<duong dan>\update-api-design-h3.ps1"
$ErrorActionPreference = 'Stop'
$f = Join-Path (Get-Location) 'API_DESIGN.md'
if (-not (Test-Path $f)) { Write-Host 'Khong thay API_DESIGN.md o thu muc hien tai'; exit 1 }
$t = [IO.File]::ReadAllText($f, [Text.Encoding]::UTF8)

# Khop ca ban cu (truoc H2) lan ban sau H2, vi chi can phan dau dong.
$p = '(?m)^\| GET \| `/api/v1/payments\?from=\.\.\.&to=\.\.\.` \|[^\r\n]*'
$n = [regex]::Matches($t, $p).Count
if ($n -ne 1) { throw "Mau khop $n dong (can dung 1). Dung lai va bao Agent 2." }

$row = '| GET | `/api/v1/payments?from=&to=&status=&userId=` | Receptionist, Super Admin — tra cứu giao dịch, phân trang. Tất cả tham số đều tùy chọn: `status` (`PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`; sai giá trị trả `VALIDATION_ERROR`) và `userId` (người mua). Không truyền ngày: không có bộ lọc nào thì lấy hôm nay, có `status` hoặc `userId` thì lấy mọi ngày (để tìm giao dịch chờ xác nhận cũ); truyền `from` hoặc `to` thì lọc theo ngày. Mỗi dòng có `userFullName` (`null` với đơn POS của khách vãng lai) |'
$ev = [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $row }
$t = [regex]::Replace($t, $p, $ev)
[IO.File]::WriteAllText($f, $t, (New-Object Text.UTF8Encoding($false)))
Write-Host 'Da sua API_DESIGN.md'
