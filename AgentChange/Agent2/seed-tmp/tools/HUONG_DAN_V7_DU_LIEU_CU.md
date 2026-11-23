# Thử V7 với dữ liệu cũ và thứ tự migration thật (Agent 2)

Mục đích: bản thử trên database trống đã qua. Bản này kiểm tra hai thứ chưa được thử: (1) `UPDATE` chuyển dữ liệu cũ
và các ràng buộc mới khi đã có dòng thật, (2) V7 chạy **sau V9** (ngoài thứ tự), đúng như sẽ xảy ra khi merge vào `master`.

Chỉ dùng database thử riêng `gms_db_agent2_v7data`. Làm trong MỘT cửa sổ PowerShell cho tới hết bước E.

## A. Chuẩn bị
```powershell
cd D:\SrpingBoot-GYM\gms-agent2
$c = "gms-sqlserver"
$pw = (docker inspect $c --format "{{range .Config.Env}}{{println .}}{{end}}" |
       Select-String '^(MSSQL_SA_PASSWORD|SA_PASSWORD)=' | Select-Object -First 1).ToString().Split('=',2)[1]

$tb = "D:\AgentChange\Agent2\_batch_tmp"
if (Test-Path $tb) { Remove-Item $tb -Recurse -Force }
Expand-Archive "D:\AgentChange\Agent2\agent2-batch-h2-seed-v7test.zip" -DestinationPath $tb
$tv = "D:\AgentChange\Agent2\_v7_tmp"
if (Test-Path $tv) { Remove-Item $tv -Recurse -Force }
Expand-Archive "D:\AgentChange\Agent2\agent2-v7-payment-split.zip" -DestinationPath $tv
```

## B. Schema TRƯỚC V7 (master + V5), database mới
```powershell
git fetch origin --prune
git switch -c tmp/v7-data origin/master
git checkout feature/agent5-pos-commission -- src/main/resources/db/migration/V5__pos_commission.sql

docker exec $c /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$pw" -C -Q "IF DB_ID('gms_db_agent2_v7data') IS NULL CREATE DATABASE gms_db_agent2_v7data"
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=gms_db_agent2_v7data;encrypt=true;trustServerCertificate=true"
$env:DB_PASSWORD = $pw
$env:GMS_JWT_SECRET = -join ((48..57)+(65..90)+(97..122) | Get-Random -Count 60 | ForEach-Object {[char]$_})
$env:SERVER_PORT = "8082"
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```
Khi thấy `Started GymManagementApplication` thì `Ctrl+C`. Kiểm tra database đã migrate đến V9 và **chưa có V7**:
```powershell
docker exec $c /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$pw" -C -d gms_db_agent2_v7data -W -Q "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank"
```

## C. Chèn 2 giao dịch ở định dạng cũ
```powershell
docker cp "$tb\tools\v7-insert-legacy-data.sql" "${c}:/tmp/v7-insert.sql"
docker exec $c /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$pw" -C -d gms_db_agent2_v7data -b -i /tmp/v7-insert.sql
```
Phải in `Da chen 2 giao dich cu`. Nếu lỗi, dán lại, đừng làm tiếp.

## D. Thêm V7 vào cùng nhánh rồi chạy lại trên CÙNG database
```powershell
Copy-Item "$tv\*" . -Recurse -Force
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```
Trong log phải thấy Flyway áp dụng `7 - payment transactions split related` (có thể ghi `out of order`), rồi `Started`. `Ctrl+C`.

Nếu thấy lỗi dạng `Detected resolved migration not applied to database: 7`: cấu hình `out-of-order` không có hiệu lực. Dừng và báo ngay,
vì khi V5 và V7 merge vào `master` thì Jenkins cũng sẽ gặp lỗi này.

## E. Kiểm tra kết quả
```powershell
docker cp "$tb\tools\v7-check.sql" "${c}:/tmp/v7-check.sql"
docker exec $c /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$pw" -C -d gms_db_agent2_v7data -W -i /tmp/v7-check.sql
docker exec $c /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$pw" -C -d gms_db_agent2_v7data -W -Q "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank"
```
Dán toàn bộ kết quả. Mong đợi:
1. `0001` có `member_package_id`, `0002` có `pos_order_id`.
2. Hai dòng `OK`.
3. Dòng `OK: bi chan` (ràng buộc "đúng một trong hai" hoạt động).
4. `revenueByDay`: hai dòng `MEMBERSHIP 500000` và `POS 45000`.
5. `topPackages`: `Goi V7`, 1 lượt, 500000.
6. `flyway_schema_history`: V7 `success = 1`, đứng sau V9.

## F. Dọn
```powershell
git reset --hard
git clean -fd -- src
git switch feature/agent2-public-packages
git branch -D tmp/v7-data
```
