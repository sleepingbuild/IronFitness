# Chạy trong thư mục gốc repo (cùng cấp pom.xml), trên nhánh mới tạo từ origin/master.
# Cần các file trong D:\AgentChange\Agent1\backend-small\
# Làm: chép file mới (không ghi đè file đã có), sửa tại chỗ SecurityConfig và API_DESIGN.md.
$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$src = "D:\AgentChange\Agent1\backend-small"
$m = "src\main\java\com\gym\management"
$t = "src\test\java\com\gym\management"

if (-not (Test-Path (Join-Path $PWD "pom.xml"))) { throw "Hay chay trong thu muc goc repo (cung cap pom.xml)" }

function Copy-New($name, $dest) {
    $from = Join-Path $src $name
    if (-not (Test-Path $from)) { throw "Thieu file: $from" }
    $to = Join-Path $PWD $dest
    if (Test-Path $to) { throw "File da ton tai trong repo, khong ghi de: $dest" }
    New-Item -ItemType Directory -Force (Split-Path $to -Parent) | Out-Null
    Copy-Item $from $to
    Write-Host "da chep $name" -ForegroundColor Green
}

Copy-New "RestAuthenticationEntryPoint.java" "$m\security\RestAuthenticationEntryPoint.java"
Copy-New "RestAccessDeniedHandler.java"      "$m\security\RestAccessDeniedHandler.java"
Copy-New "UserSummaryResponse.java"          "$m\dto\response\UserSummaryResponse.java"
Copy-New "TrainerResponse.java"              "$m\dto\response\TrainerResponse.java"
Copy-New "BranchResponse.java"               "$m\dto\response\BranchResponse.java"
Copy-New "UserDirectoryRepository.java"      "$m\repository\UserDirectoryRepository.java"
Copy-New "DirectoryService.java"             "$m\service\DirectoryService.java"
Copy-New "DirectoryServiceImpl.java"         "$m\service\impl\DirectoryServiceImpl.java"
Copy-New "UserController.java"               "$m\controller\UserController.java"
Copy-New "TrainerController.java"            "$m\controller\TrainerController.java"
Copy-New "BranchController.java"             "$m\controller\BranchController.java"
Copy-New "RestSecurityHandlersTest.java"     "$t\security\RestSecurityHandlersTest.java"
Copy-New "DirectoryServiceImplTest.java"     "$t\service\impl\DirectoryServiceImplTest.java"

# 1. SecurityConfig: gắn trình trả 401/403 JSON (sửa tại chỗ, giữ nguyên các quy tắc của agent khác)
$p = Join-Path $PWD "$m\config\SecurityConfig.java"
$s = [IO.File]::ReadAllText($p, $utf8)
if ($s.Contains("RestAuthenticationEntryPoint")) {
    Write-Host "SecurityConfig: da gan, bo qua" -ForegroundColor Yellow
} else {
    $nl = if ($s.Contains("`r`n")) { "`r`n" } else { "`n" }
    $a1 = "import com.gym.management.security.JwtAuthenticationFilter;"
    $a2 = "    private final UserDetailsService userDetailsService;"
    $a3 = "                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))"
    foreach ($a in @($a1, $a2, $a3)) {
        if (-not $s.Contains($a)) { throw "SecurityConfig khong con dong mau: $a" }
    }
    $s = $s.Replace($a1, $a1 + $nl + "import com.gym.management.security.RestAccessDeniedHandler;" + $nl + "import com.gym.management.security.RestAuthenticationEntryPoint;")
    $s = $s.Replace($a2, $a2 + $nl + "    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;" + $nl + "    private final RestAccessDeniedHandler restAccessDeniedHandler;")
    $s = $s.Replace($a3, $a3 + $nl + "                .exceptionHandling(eh -> eh" + $nl + "                        .authenticationEntryPoint(restAuthenticationEntryPoint)" + $nl + "                        .accessDeniedHandler(restAccessDeniedHandler))")
    [IO.File]::WriteAllText($p, $s, $utf8)
    Write-Host "SecurityConfig: da gan 401/403 JSON" -ForegroundColor Green
}

# 2. API_DESIGN.md: thêm mục "Danh mục dùng chung" ngay trước mục Membership
$p = Join-Path $PWD "API_DESIGN.md"
$d = [IO.File]::ReadAllText($p, $utf8)
if ($d.Contains("Danh mục dùng chung (Agent 1)")) {
    Write-Host "API_DESIGN: da co, bo qua" -ForegroundColor Yellow
} else {
    $nl = if ($d.Contains("`r`n")) { "`r`n" } else { "`n" }
    $lines = New-Object 'System.Collections.Generic.List[string]'
    $lines.AddRange([string[]]($d -split "\r?\n"))
    $idx = -1
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i].StartsWith("### Membership, Package & Payment (Agent 2)")) { $idx = $i; break }
    }
    if ($idx -lt 0) { throw "Khong thay muc '### Membership, Package & Payment (Agent 2)' trong API_DESIGN.md" }
    $block = @(
'### Danh mục dùng chung (Agent 1)',
'| Method | Endpoint | Role |',
'|---|---|---|',
'| GET | `/api/v1/users?role=MEMBER&q=&page=&size=` | Receptionist/Super Admin — tìm hội viên đang hoạt động theo tên, số điện thoại hoặc email. Chỉ trả `id`, `fullName`, `phone`, `email`. `role` chỉ nhận `MEMBER` (giá trị khác trả 400 `VALIDATION_ERROR`). Sắp theo tên, `size` tối đa 50 |',
'| GET | `/api/v1/trainers` | Mọi người dùng đã đăng nhập — huấn luyện viên đang hoạt động: `id`, `fullName`, `avatarUrl` |',
'| GET | `/api/v1/branches` | Mọi người dùng đã đăng nhập — chi nhánh đang hoạt động: `id`, `name` |',
'',
'> **Xác thực:** người chưa đăng nhập (không có token, token sai hoặc hết hạn) nhận **401** với `errorCode = AUTH_TOKEN_INVALID`. Đã đăng nhập nhưng không đủ quyền nhận **403** với `errorCode = AUTH_FORBIDDEN_ROLE`. Cả hai đều có body JSON `ApiResponse`.',
''
    )
    $lines.InsertRange($idx, [string[]]$block)
    [IO.File]::WriteAllText($p, ($lines -join $nl), $utf8)
    Write-Host "API_DESIGN: da them muc Danh muc dung chung" -ForegroundColor Green
}

Write-Host ""
git status --short
