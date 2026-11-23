# Them nhom ma loi PAYMENT_* vao bang ma loi cua API_DESIGN.md (PR [shared], theo yeu cau cua Agent 1).
# Chay tai THU MUC GOC repo, tren nhanh shared/error-code-payment-not-refundable:
#   powershell -ExecutionPolicy Bypass -File "<duong dan day du toi file .ps1>"
$ErrorActionPreference = 'Stop'
$f = Join-Path (Get-Location) 'API_DESIGN.md'
if (-not (Test-Path $f)) { Write-Host 'Khong thay API_DESIGN.md o thu muc hien tai'; exit 1 }
$t = [IO.File]::ReadAllText($f, [Text.Encoding]::UTF8)
$eol = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }

# Bang chua co dong PAYMENT_* nao. Them PAYMENT_NOT_REFUNDABLE (yeu cau cua Agent 1) va 2 ma da co san trong ErrorCode.java
# nhung chua co trong bang (PAYMENT_NOT_FOUND, PAYMENT_DUPLICATE_TRANSACTION), chen ngay sau nhom MEMBERSHIP_*.
$rows = [ordered]@{
  'PAYMENT_NOT_FOUND'             = '| `PAYMENT_NOT_FOUND` | Không tìm thấy giao dịch thanh toán | 404 |'
  'PAYMENT_DUPLICATE_TRANSACTION' = '| `PAYMENT_DUPLICATE_TRANSACTION` | Trùng mã giao dịch (rất hiếm, thử lại) | 409 |'
  'PAYMENT_NOT_REFUNDABLE'        = '| `PAYMENT_NOT_REFUNDABLE` | Không hoàn tiền được: đơn POS chưa có giao dịch, giao dịch chưa thu tiền thành công, hoặc giao dịch không thuộc đơn POS | 409 |'
}
$toAdd = @()
foreach ($k in $rows.Keys) { if (-not $t.Contains('`' + $k + '`')) { $toAdd += $rows[$k] } }
if ($toAdd.Count -eq 0) { Write-Host 'Bang da co du 3 dong PAYMENT_*, khong lam gi'; exit 0 }

$p = '(?m)^\| `MEMBERSHIP_PENDING_EXISTS` \|[^\r\n]*'
$n = [regex]::Matches($t, $p).Count
if ($n -ne 1) { throw "Mau MEMBERSHIP_PENDING_EXISTS khop $n dong (can dung 1). Dung lai va bao Agent 2." }
$block = $toAdd -join $eol
$ev = [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $m.Value + $eol + $block }
$t = [regex]::Replace($t, $p, $ev)
[IO.File]::WriteAllText($f, $t, (New-Object Text.UTF8Encoding($false)))
Write-Host ("Da them " + $toAdd.Count + " dong PAYMENT_* sau dong MEMBERSHIP_PENDING_EXISTS")
