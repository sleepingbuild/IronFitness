# Tach "Lich day" (PT) va "Quan ly PT" (Super Admin) trong navigation.ts.
# Chay o goc repo (D:\SrpingBoot-GYM\gms-agent3), nhanh moi tu origin/master, cay sach.
# Moi thay doi phai khop dung 1 cho. Neu khong, script dung va KHONG ghi file nao.
$ErrorActionPreference = "Stop"
$enc = New-Object System.Text.UTF8Encoding($false)
$root = (Get-Location).Path
$f = "frontend\src\config\navigation.ts"
$p = Join-Path $root $f
if (-not (Test-Path $p)) { throw "Khong thay file: $f" }
$t = [System.IO.File]::ReadAllText($p, $enc)
$crlf = $t.Contains("`r`n")

function Patch($text, $name, $pattern, $replacement) {
  $n = [regex]::Matches($text, $pattern).Count
  if ($n -ne 1) { throw "[$name] mong doi khop dung 1 cho, thuc te $n. Dung lai, chua ghi file nao." }
  $rep = if ($crlf) { $replacement -replace "`r?`n", "`r`n" } else { $replacement }
  $ev = [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $rep }
  return [regex]::Replace($text, $pattern, $ev)
}

# Dong /schedule chi con TRAINER, them ngay duoi dong "Quan ly PT" cho SUPER_ADMIN.
$t = Patch $t "schedule + quan ly PT" '(\{ href: "/schedule", label: "[^"]*", roles: \["TRAINER"), "SUPER_ADMIN"\](, status: "ready", owner: "Agent 3" \},)' @'
{ href: "/schedule", label: "Lịch dạy", roles: ["TRAINER"], status: "ready", owner: "Agent 3" },
      { href: "/admin/trainers", label: "Quản lý PT", roles: ["SUPER_ADMIN"], status: "ready", owner: "Agent 3" },
'@

[System.IO.File]::WriteAllText($p, $t, $enc)
Write-Host "Da sua: $f"
Write-Host "Xong. Chay: git diff -- $f"
