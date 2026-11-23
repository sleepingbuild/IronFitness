# Chạy trong thư mục gốc repo (cùng cấp pom.xml), trên nhánh mới tạo từ origin/master.
# Cần các file V9 nằm trong D:\AgentChange\Agent1\v9\
$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$src = "D:\AgentChange\Agent1\v9"
$m = "src\main\java\com\gym\management"

function Copy-V9($name, $dest) {
    $from = Join-Path $src $name
    if (-not (Test-Path $from)) { throw "Thieu file: $from" }
    $dir = Split-Path $dest -Parent
    New-Item -ItemType Directory -Force $dir | Out-Null
    Copy-Item $from $dest -Force
    Write-Host "da chep $name" -ForegroundColor Green
}

Copy-V9 "V9__users_phone_profile_dob.sql" "src\main\resources\db\migration\V9__users_phone_profile_dob.sql"
Copy-V9 "User.java"                       "$m\entity\User.java"
Copy-V9 "UserProfile.java"                "$m\entity\UserProfile.java"
Copy-V9 "UpdateProfileRequest.java"       "$m\dto\request\UpdateProfileRequest.java"
Copy-V9 "ProfileResponse.java"            "$m\dto\response\ProfileResponse.java"
Copy-V9 "ProfileServiceImpl.java"         "$m\service\impl\ProfileServiceImpl.java"
Copy-V9 "ProfileServiceImplDobTest.java"  "src\test\java\com\gym\management\service\impl\ProfileServiceImplDobTest.java"

# Cập nhật chữ trong Swagger của ProfileController (chỉ thay đúng hai cụm từ, không đụng phần còn lại)
$ctl = Join-Path $PWD "$m\controller\ProfileController.java"
$t = [IO.File]::ReadAllText($ctl, $utf8)
$new = $t.Replace("tuổi, cân nặng", "ngày sinh, cân nặng").Replace("tuổi/cân nặng", "ngày sinh/cân nặng")
if ($new -ne $t) {
    [IO.File]::WriteAllText($ctl, $new, $utf8)
    Write-Host "da sua chu Swagger trong ProfileController" -ForegroundColor Green
} else {
    Write-Host "ProfileController: khong doi (khong thay cum tu can sua)" -ForegroundColor Yellow
}

Write-Host ""
git status --short
