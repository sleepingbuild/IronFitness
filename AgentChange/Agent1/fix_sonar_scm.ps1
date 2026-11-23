#requires -Version 5.1
<#
 fix_sonar_scm.ps1 (Agent 1) - sua scripts\sonar-analyze.ps1 de chay duoc khi thu muc la git worktree.
 JGit (thu vien Git cua SonarQube) khong doc duoc worktree: "Unable to open Git repository: repository not found".
 Them -Dsonar.scm.disabled=true vao lenh quet va ghi chu vao docs\SONARQUBE.md.
 Chay o goc repo, tren nhanh feature/agent1-sonarqube:
   & D:\AgentChange\Agent1\fix_sonar_scm.ps1
#>
$ErrorActionPreference = 'Stop'
$root = (Get-Location).Path
$ps = Join-Path $root 'scripts\sonar-analyze.ps1'
$md = Join-Path $root 'docs\SONARQUBE.md'
if (-not (Test-Path $ps)) { throw 'Khong thay scripts\sonar-analyze.ps1 (dung nhanh feature/agent1-sonarqube chua?)' }
if (-not (Test-Path $md)) { throw 'Khong thay docs\SONARQUBE.md' }

$bom = New-Object System.Text.UTF8Encoding($true)
$nobom = New-Object System.Text.UTF8Encoding($false)

# 1. Script quet
$s = [IO.File]::ReadAllText($ps, $bom)
if ($s.Contains('sonar.scm.disabled')) {
  Write-Host '  = sonar-analyze.ps1 da co sonar.scm.disabled, bo qua' -ForegroundColor Yellow
} else {
  $old = '"-Dsonar.host.url=$HostUrl" "-Dsonar.token=$Token"'
  if (-not $s.Contains($old)) { throw 'Khong thay dong lenh quet trong sonar-analyze.ps1' }
  $s = $s.Replace($old, '"-Dsonar.host.url=$HostUrl" "-Dsonar.token=$Token" "-Dsonar.scm.disabled=true"')
  [IO.File]::WriteAllText($ps, $s, $bom)
  Write-Host '  ~ scripts\sonar-analyze.ps1' -ForegroundColor Green
}

# 2. Tai lieu
$d = [IO.File]::ReadAllText($md, $nobom)
if ($d.Contains('worktrees')) {
  Write-Host '  = SONARQUBE.md da co ghi chu worktree, bo qua' -ForegroundColor Yellow
} else {
  $nl = "`n"; if ($d.Contains("`r`n")) { $nl = "`r`n" }
  $anchor = '- Lỗi xác thực'
  if (-not $d.Contains($anchor)) { throw 'Khong thay dong neo "- Lỗi xác thực" trong docs\SONARQUBE.md' }
  $note = '- Lỗi `Unable to open Git repository: repository not found ... \.git\worktrees\...`: thư mục đang dùng là git worktree, thư viện Git của SonarQube không đọc được. Script đã tắt việc đọc Git (`-Dsonar.scm.disabled=true`), nên không có thông tin tác giả từng dòng và việc xác định mã mới không dựa trên Git; kết quả quét lỗi, lỗ hổng, độ phủ và trùng lặp không đổi.'
  $d = $d.Replace($anchor, $note + $nl + $anchor)
  [IO.File]::WriteAllText($md, $d, $nobom)
  Write-Host '  ~ docs\SONARQUBE.md' -ForegroundColor Green
}
Write-Host ''
Write-Host 'Xong. Kiem tra: git diff (2 file M, moi file them 1 dong; sonar-analyze.ps1 sua 1 dong).'
