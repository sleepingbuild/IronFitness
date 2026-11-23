# Chạy trong thư mục gốc repo (cùng cấp pom.xml), trên nhánh mới tạo từ origin/master.
# Sửa tại chỗ 4 file SHARED, KHÔNG ghi đè cả file, nên không làm mất mã lỗi hay logic đã có trên master.
$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$base = "src\main\java\com\gym\management"

function Patch-File($path, $label, [scriptblock]$edit) {
    $full = Join-Path $PWD $path
    if (-not (Test-Path $full)) { Write-Host "[$label] KHONG TIM THAY: $path" -ForegroundColor Red; return }
    $t = [IO.File]::ReadAllText($full, $utf8)
    $nl = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }
    $new = & $edit $t $nl
    if ($new -eq $t) { Write-Host "[$label] khong doi (da co san hoac khong khop mau)" -ForegroundColor Yellow; return }
    [IO.File]::WriteAllText($full, $new, $utf8)
    Write-Host "[$label] da sua" -ForegroundColor Green
}

# 1. SecurityConfig: cho phep /swagger-ui.html
Patch-File "$base\config\SecurityConfig.java" "SecurityConfig" {
    param($t, $nl)
    if ($t.Contains('"/swagger-ui.html"')) { return $t }
    $t.Replace('"/swagger-ui/**",', '"/swagger-ui/**", "/swagger-ui.html",')
}

# 2. JwtTokenProvider: kiem tra gms.jwt.secret >= 32 byte khi khoi dong
Patch-File "$base\security\JwtTokenProvider.java" "JwtTokenProvider" {
    param($t, $nl)
    if ($t.Contains("validateSecret")) { return $t }
    $t = $t.Replace("import org.springframework.beans.factory.annotation.Value;",
        "import jakarta.annotation.PostConstruct;$nl" + "import org.springframework.beans.factory.annotation.Value;")
    $method = @(
        "    @PostConstruct",
        "    void validateSecret() {",
        "        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {",
        "            throw new IllegalStateException(",
        "                    ""gms.jwt.secret phải dài tối thiểu 32 byte (256 bit) cho HS256; """,
        "                            + ""đặt qua biến môi trường hoặc application-local.yml"");",
        "        }",
        "    }",
        "",
        "    private SecretKey key() {"
    ) -join $nl
    $t.Replace("    private SecretKey key() {", $method)
}

# 3. ErrorCode: them DATA_CONFLICT (409)
Patch-File "$base\exception\ErrorCode.java" "ErrorCode" {
    param($t, $nl)
    if ($t.Contains("DATA_CONFLICT")) { return $t }
    $t.Replace("    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),",
        "    DATA_CONFLICT(HttpStatus.CONFLICT),$nl" + "    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),")
}

# 4. GlobalExceptionHandler: dung hang enum thay cho chuoi
Patch-File "$base\exception\GlobalExceptionHandler.java" "GlobalExceptionHandler" {
    param($t, $nl)
    $t.Replace('"DATA_CONFLICT")', 'ErrorCode.DATA_CONFLICT)')
}

Write-Host ""
git diff --stat
