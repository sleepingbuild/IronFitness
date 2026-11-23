# Chạy trong thư mục gốc repo, trên nhánh mới tạo từ origin/master.
# Thêm 3 mã lỗi POS/hoa hồng (404) vào ErrorCode và bảng mã lỗi của API_DESIGN.md.
$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding($false)

# 1. ErrorCode.java: chèn ngay sau PRODUCT_OUT_OF_STOCK
$p = Join-Path $PWD "src\main\java\com\gym\management\exception\ErrorCode.java"
$t = [IO.File]::ReadAllText($p, $utf8)
if ($t.Contains("POS_ORDER_NOT_FOUND")) {
    Write-Host "ErrorCode: da co, bo qua" -ForegroundColor Yellow
} else {
    $nl = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }
    $anchor = "    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST),"
    if (-not $t.Contains($anchor)) { throw "Khong thay dong PRODUCT_OUT_OF_STOCK trong ErrorCode.java" }
    $add = $anchor + $nl +
        "    POS_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND)," + $nl +
        "    COMMISSION_NOT_FOUND(HttpStatus.NOT_FOUND)," + $nl +
        "    TRAINER_PAY_RATE_NOT_FOUND(HttpStatus.NOT_FOUND),"
    [IO.File]::WriteAllText($p, $t.Replace($anchor, $add), $utf8)
    Write-Host "ErrorCode: da them 3 ma loi" -ForegroundColor Green
}

# 2. API_DESIGN.md: chèn 3 dòng vào bảng mã lỗi, sau dòng PRODUCT_OUT_OF_STOCK nếu có
$p = Join-Path $PWD "API_DESIGN.md"
$t = [IO.File]::ReadAllText($p, $utf8)
if ($t.Contains("POS_ORDER_NOT_FOUND")) {
    Write-Host "API_DESIGN: da co, bo qua" -ForegroundColor Yellow
} else {
    $nl = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }
    $lines = New-Object 'System.Collections.Generic.List[string]'
    $lines.AddRange([string[]]($t -split "\r?\n"))
    $idx = -1
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i].StartsWith('| `PRODUCT_OUT_OF_STOCK`')) { $idx = $i; break }
    }
    if ($idx -lt 0) {
        Write-Host "API_DESIGN: khong thay dong PRODUCT_OUT_OF_STOCK trong bang ma loi - hay them tay 3 dong sau:" -ForegroundColor Yellow
        Write-Host '| `POS_ORDER_NOT_FOUND` | Không tìm thấy đơn POS | 404 |'
        Write-Host '| `COMMISSION_NOT_FOUND` | Không tìm thấy bản ghi lương | 404 |'
        Write-Host '| `TRAINER_PAY_RATE_NOT_FOUND` | Chưa có mức lương theo buổi của PT | 404 |'
    } else {
        $rows = @(
            '| `POS_ORDER_NOT_FOUND` | Không tìm thấy đơn POS | 404 |',
            '| `COMMISSION_NOT_FOUND` | Không tìm thấy bản ghi lương | 404 |',
            '| `TRAINER_PAY_RATE_NOT_FOUND` | Chưa có mức lương theo buổi của PT | 404 |'
        )
        $lines.InsertRange($idx + 1, [string[]]$rows)
        [IO.File]::WriteAllText($p, ($lines -join $nl), $utf8)
        Write-Host "API_DESIGN: da them 3 dong" -ForegroundColor Green
    }
}
git diff --stat
