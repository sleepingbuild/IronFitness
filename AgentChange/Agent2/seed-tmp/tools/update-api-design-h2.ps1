# Cap nhat API_DESIGN.md cho H2 + H5 (Agent 2). Chay tai THU MUC GOC repo:
#   powershell -ExecutionPolicy Bypass -File "<duong dan>\update-api-design-h2.ps1"
$ErrorActionPreference = 'Stop'
$f = Join-Path (Get-Location) 'API_DESIGN.md'
if (-not (Test-Path $f)) { Write-Host 'Khong thay API_DESIGN.md o thu muc hien tai'; exit 1 }
$t = [IO.File]::ReadAllText($f, [Text.Encoding]::UTF8)
$eol = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }

function Replace-Line($text, $pattern, $replacement) {
  $n = [regex]::Matches($text, $pattern).Count
  if ($n -ne 1) { throw "Mau '$pattern' khop $n dong (can dung 1). Dung lai va bao Agent 2." }
  $ev = [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $replacement }
  return [regex]::Replace($text, $pattern, $ev)
}

$me = @(
 '| GET | `/api/v1/member-packages/me` | Member, Trainer, Receptionist, Super Admin — gói ACTIVE của chính mình (dùng `getCurrentMembership`); không có gói ACTIVE trả `MEMBERSHIP_NOT_FOUND` |',
 '| GET | `/api/v1/member-packages/me/history` | Như trên — lịch sử toàn bộ gói của chính mình (mọi trạng thái), phân trang |',
 '| GET | `/api/v1/member-packages/current?userId={id}` | Receptionist, Super Admin — gói ACTIVE của một hội viên (để nâng, hạ cấp hộ); không có gói ACTIVE trả `MEMBERSHIP_NOT_FOUND` |',
 '| GET | `/api/v1/member-packages?userId={id}` | Receptionist, Super Admin — lịch sử gói của một hội viên (mọi trạng thái), phân trang |'
) -join $eol

$confirm = '| POST | `/api/v1/payments/{transactionId}/confirm` | Receptionist, Super Admin — xác nhận thanh toán tại chỗ. Body tùy chọn `{ "paymentMethod": "..." }` (`CASH` hoặc `LOCAL_CONFIRM`); bỏ trống thì giữ phương thức đã ghi lúc tạo giao dịch (mặc định `CASH`) |'
$payMe   = '| GET | `/api/v1/payments/me` | Member, Trainer, Receptionist, Super Admin — lịch sử thanh toán của chính mình, phân trang |'
$payAll  = '| GET | `/api/v1/payments?from=...&to=...` | Receptionist, Super Admin — giao dịch theo khoảng ngày [from, to] (mặc định hôm nay), phân trang |'

$t = Replace-Line $t '(?m)^\| GET \| `/api/v1/member-packages/me` \|[^\r\n]*' $me
$t = Replace-Line $t '(?m)^\| POST \| `/api/v1/payments/\{transactionId\}/confirm` \|[^\r\n]*' $confirm
$t = Replace-Line $t '(?m)^\| GET \| `/api/v1/payments/me` \|[^\r\n]*' $payMe
$t = Replace-Line $t '(?m)^\| GET \| `/api/v1/payments\?from=\.\.\.&to=\.\.\.` \|[^\r\n]*' $payAll

[IO.File]::WriteAllText($f, $t, (New-Object Text.UTF8Encoding($false)))
Write-Host 'Da sua API_DESIGN.md'
