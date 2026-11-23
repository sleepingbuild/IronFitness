# Seed ca lam viec PT: tu hom nay den cuoi thang (toi thieu 8 ngay) thay vi co dinh 8 ngay.
# Chay o goc repo (D:\SrpingBoot-GYM\gms-agent3), nhanh moi tu origin/master, cay sach.
# Moi thay doi phai khop dung 1 cho. Neu khong, script dung va KHONG ghi file nao.
$ErrorActionPreference = "Stop"
$enc = New-Object System.Text.UTF8Encoding($false)
$root = (Get-Location).Path
$f = "scripts\dev-seed\20_bookings.sql"
$p = Join-Path $root $f
if (-not (Test-Path $p)) { throw "Khong thay file: $f" }
$t = [System.IO.File]::ReadAllText($p, $enc)
$crlf = $t.Contains("`r`n")
function Nl($s) { if ($crlf) { return ($s -replace "`r?`n", "`r`n") } return $s }

function Patch($text, $name, $pattern, $replacement) {
  $n = [regex]::Matches($text, $pattern).Count
  if ($n -ne 1) { throw "[$name] mong doi khop dung 1 cho, thuc te $n. Dung lai, chua ghi file nao." }
  $ev = [System.Text.RegularExpressions.MatchEvaluator]{ param($m) Nl $replacement }
  return [regex]::Replace($text, $pattern, $ev)
}

$t = Patch $t "comment" '-- 1\) Lich lam viec: moi PT lam 06:00-22:00 trong 8 ngay \(hom nay den 7 ngay sau\), chi them neu chua co\.' @'
-- 1) Lich lam viec: moi PT lam 06:00-22:00 tu hom nay den cuoi thang (toi thieu 8 ngay, de chay vao
--    nhung ngay cuoi thang van du ca), chi them neu chua co. Ngay tinh theo ngay chay, khong go cung.
'@

$t = Patch $t "so ngay" ';WITH n AS \(\s*SELECT TOP \(8\) ROW_NUMBER\(\) OVER \(ORDER BY \(SELECT NULL\)\) - 1 AS d FROM sys\.all_objects\s*\)' @'
DECLARE @daysToMonthEnd INT = DATEDIFF(DAY, @today, EOMONTH(@today)) + 1;
DECLARE @days INT = CASE WHEN @daysToMonthEnd < 8 THEN 8 ELSE @daysToMonthEnd END;

;WITH n AS (
    SELECT TOP (@days) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) - 1 AS d FROM sys.all_objects
)
'@

[System.IO.File]::WriteAllText($p, $t, $enc)
Write-Host "Da sua: $f"
Write-Host "Xong. Chay: git diff"
