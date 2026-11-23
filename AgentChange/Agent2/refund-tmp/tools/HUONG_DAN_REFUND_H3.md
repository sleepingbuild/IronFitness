# Gói refund và H3 (chồng lên V7), cách thử

Gói này viết trên nền V7. Nó chỉ chạy được khi đã có cả ba thứ: mã lỗi `PAYMENT_NOT_REFUNDABLE`
(nhánh `shared/error-code-payment-not-refundable`), bảng `pos_orders` (V5) và V7. Vì vậy thử trên nhánh tạm,
KHÔNG commit nhánh tạm.

## Thử trên nhánh tạm
```powershell
cd D:\SrpingBoot-GYM\gms-agent2
git fetch origin --prune
git switch -c tmp/l2-verify shared/error-code-payment-not-refundable      # da co ma loi moi
git merge origin/master --no-edit
git checkout origin/feature/agent5-pos-commission -- src/main/resources/db/migration/V5__pos_commission.sql

$tv = "D:\AgentChange\Agent2\_v7_tmp"
if (Test-Path $tv) { Remove-Item $tv -Recurse -Force }
Expand-Archive "D:\AgentChange\Agent2\agent2-v7-payment-split.zip" -DestinationPath $tv
Copy-Item "$tv\*" . -Recurse -Force                                       # buoc 1: V7

$tl = "D:\AgentChange\Agent2\_l2_tmp"
if (Test-Path $tl) { Remove-Item $tl -Recurse -Force }
Expand-Archive "D:\AgentChange\Agent2\agent2-refund-h3.zip" -DestinationPath $tl
Copy-Item "$tl\repo\*" . -Recurse -Force                                  # buoc 2: refund + H3
powershell -ExecutionPolicy Bypass -File "$tl\tools\update-api-design-h3.ps1"
git status -sb
mvn clean verify
```
Nếu `merge origin/master` hoặc `checkout` V5 báo lỗi, dừng và dán mình. Nếu `verify` xanh, chạy app trên database trống
mới để kiểm tra khởi động (kiểm tra tên thuộc tính của các truy vấn dẫn xuất) và thử bằng `curl`:
```powershell
# dung lai $c, $pw nhu cac buoc truoc; database moi gms_db_agent2_l2
docker exec $c /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$pw" -C -Q "IF DB_ID('gms_db_agent2_l2') IS NULL CREATE DATABASE gms_db_agent2_l2"
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=gms_db_agent2_l2;encrypt=true;trustServerCertificate=true"
$env:DB_PASSWORD = $pw
$env:GMS_JWT_SECRET = -join ((48..57)+(65..90)+(97..122) | Get-Random -Count 60 | ForEach-Object {[char]$_})
$env:SERVER_PORT = "8082"
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```
Phải thấy `Started GymManagementApplication`. Nếu lỗi `PropertyReferenceException` hoặc `No property ... found`, đó là tên truy vấn dẫn xuất sai: dán lại.

## Dọn
```powershell
git reset --hard
git clean -fd -- src
git switch feature/agent2-membership-lookup
git branch -D tmp/l2-verify
```

## Khi nộp PR thật
Chỉ nộp sau khi V5 và V7 đã vào `master`, và mã lỗi `[shared]` đã merge. Khi đó tạo nhánh mới từ `master`,
chép `repo\*` của gói này, và chạy `git diff`: phải chỉ có thay đổi của refund và H3. Nếu thấy thay đổi khác (ví dụ
phần V7 bị đảo ngược do V7 được sửa khi review), dừng lại và báo mình để tạo lại gói trên bản V7 cuối.
