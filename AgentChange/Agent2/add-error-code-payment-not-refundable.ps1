# Them ma loi PAYMENT_NOT_REFUNDABLE vao ErrorCode.java (PR [shared], Agent 2).
# Chay tai THU MUC GOC repo, tren nhanh moi tao tu origin/master:
#   powershell -ExecutionPolicy Bypass -File "<duong dan>\add-error-code-payment-not-refundable.ps1"
$ErrorActionPreference = 'Stop'
$f = Join-Path (Get-Location) 'src\main\java\com\gym\management\exception\ErrorCode.java'
if (-not (Test-Path $f)) { Write-Host 'Khong thay ErrorCode.java o thu muc hien tai'; exit 1 }
$t = [IO.File]::ReadAllText($f, [Text.Encoding]::UTF8)
if ($t.Contains('PAYMENT_NOT_REFUNDABLE')) { Write-Host 'Da co PAYMENT_NOT_REFUNDABLE, khong lam gi'; exit 0 }
$eol = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }

$p = '(?m)^([ \t]*)PAYMENT_DUPLICATE_TRANSACTION\(HttpStatus\.CONFLICT\),[^\r\n]*'
$n = [regex]::Matches($t, $p).Count
if ($n -ne 1) { throw "Mau khop $n dong (can dung 1). Dung lai va bao Agent 2." }

$ev = [System.Text.RegularExpressions.MatchEvaluator]{
  param($m)
  $m.Value + $eol + $m.Groups[1].Value + 'PAYMENT_NOT_REFUNDABLE(HttpStatus.CONFLICT), // hoàn tiền: chỉ giao dịch POS đang SUCCESS'
}
$t = [regex]::Replace($t, $p, $ev)
[IO.File]::WriteAllText($f, $t, (New-Object Text.UTF8Encoding($false)))
Write-Host 'Da them PAYMENT_NOT_REFUNDABLE vao ErrorCode.java'
