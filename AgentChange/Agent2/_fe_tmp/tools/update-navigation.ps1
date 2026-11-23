# Doi trang thai cac muc cua Agent 2 trong navigation.ts tu "planned" sang "ready".
# Chi sua dong co owner: "Agent 2", giu nguyen moi dong khac (cua Agent 1 va cac agent khac).
# Chay tai THU MUC GOC repo:
#   powershell -ExecutionPolicy Bypass -File "<duong dan day du toi file .ps1>"
$ErrorActionPreference = 'Stop'
$f = Join-Path (Get-Location) 'frontend\src\config\navigation.ts'
if (-not (Test-Path $f)) { Write-Host 'Khong thay frontend\src\config\navigation.ts'; exit 1 }
$t = [IO.File]::ReadAllText($f, [Text.Encoding]::UTF8)

# Khong dung "$" o cuoi mau: file Windows ket thuc dong bang CRLF, ma "$" cua .NET khong khop truoc "\r\n".
$p = '(?m)^[^\r\n]*owner: "Agent 2"[^\r\n]*'
$found = [regex]::Matches($t, $p).Count
Write-Host "Tim thay $found dong cua Agent 2"
if ($found -eq 0) {
  Write-Host 'Khong thay dong nao co owner: "Agent 2". Dan ket qua: Select-String -Path frontend\src\config\navigation.ts -Pattern "Agent 2"'
  exit 1
}

$script:count = 0
$ev = [System.Text.RegularExpressions.MatchEvaluator]{
  param($m)
  if ($m.Value.Contains('status: "planned"')) { $script:count++; return $m.Value.Replace('status: "planned"', 'status: "ready"') }
  return $m.Value
}
$t2 = [regex]::Replace($t, $p, $ev)
if ($script:count -eq 0) {
  Write-Host 'Cac dong cua Agent 2 khong o trang thai "planned" (co the da la "ready" hoac dung gia tri khac). Khong sua gi.'
  Select-String -Path $f -Pattern 'owner: "Agent 2"' | ForEach-Object { $_.Line.Trim() }
  exit 0
}
[IO.File]::WriteAllText($f, $t2, (New-Object Text.UTF8Encoding($false)))
Write-Host "Da doi $($script:count) dong cua Agent 2 sang ready"
