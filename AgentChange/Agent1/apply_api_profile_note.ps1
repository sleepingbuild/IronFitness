# Chạy trong thư mục gốc repo, trên nhánh mới tạo từ origin/master.
# Thêm ghi chú về body/response hồ sơ (dateOfBirth) ngay sau dòng endpoint avatar trong API_DESIGN.md.
$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$p = Join-Path $PWD "API_DESIGN.md"
$t = [IO.File]::ReadAllText($p, $utf8)
if ($t.Contains("dateOfBirth")) { Write-Host "API_DESIGN.md da co ghi chu ho so, bo qua" -ForegroundColor Yellow; return }

$nl = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }
$lines = New-Object 'System.Collections.Generic.List[string]'
$lines.AddRange([string[]]($t -split "\r?\n"))

$idx = -1
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].Contains('/api/v1/profile/me/avatar')) { $idx = $i; break }
}
if ($idx -lt 0) { throw "Khong tim thay dong endpoint avatar" }

$note = @(
'',
'> **Hồ sơ cá nhân:** `PUT /api/v1/profile/me` nhận `dateOfBirth` (`yyyy-MM-dd`), `weightKg`, `heightCm`, `goal`. Ngày sinh phải cho tuổi từ 18 trở lên tính đến ngày đăng ký của tài khoản và không quá 120 tuổi; vi phạm trả 400 `PROFILE_INVALID_AGE`. Response trả `dateOfBirth` và `age` (tính sẵn từ ngày sinh, `null` nếu chưa có). Ảnh đại diện chỉ lưu ở hồ sơ (`avatarUrl`), không còn ở tài khoản.'
)
$lines.InsertRange($idx + 1, [string[]]$note)

[IO.File]::WriteAllText($p, ($lines -join $nl), $utf8)
Write-Host "da them ghi chu ho so vao API_DESIGN.md" -ForegroundColor Green
git diff --stat -- API_DESIGN.md
