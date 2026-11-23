# Don dep du lieu do (thu tu nay vi co khoa ngoai).
$saPass = Read-Host "Mat khau sa (xem docker-compose.yml)"
function Run-SqlI($q) { docker exec gms-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $saPass -C -d gms_db_agent3 -I -Q $q }
Run-SqlI "DELETE c FROM commissions c JOIN bookings b ON b.id = c.booking_id WHERE b.notes = N'[test-credit]'"
Run-SqlI "DELETE FROM bookings WHERE notes = N'[test-credit]'; DELETE FROM member_packages WHERE CAST(id AS VARCHAR(36)) LIKE 'dddddddd-%'; DELETE FROM packages WHERE CAST(id AS VARCHAR(36)) LIKE 'dddddddd-%'"
