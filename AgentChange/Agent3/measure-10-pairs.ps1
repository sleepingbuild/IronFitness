# Do lai 10 cap booking (Agent 2 sua tryConsumeSession). Backend master moi phai chay o cong 8084.
# Khong chay khi gio Viet Nam nam trong 00:10 den 01:10. trainer1 phai co dong trainer_pay_rates.
$saPass = Read-Host "Mat khau sa (xem docker-compose.yml)"
& {
  $api = "http://localhost:8084"
  function Run-SqlI($q) { docker exec gms-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$saPass" -C -d gms_db_agent3 -I -Q $q }
  Add-Type -AssemblyName System.Net.Http
  $client = New-Object System.Net.Http.HttpClient
  try { $login = Invoke-RestMethod -Method Post -Uri "$api/api/v1/auth/login" -ContentType "application/json" -Body '{"email":"trainer1@gym.test","password":"Test@12345"}' } catch { "KHONG DANG NHAP DUOC: " + $_.Exception.Message; return }
  $token = $login.data.accessToken
  function Send-Complete($id) {
    $req = New-Object System.Net.Http.HttpRequestMessage([System.Net.Http.HttpMethod]::Post, "$api/api/v1/bookings/$id/complete")
    $req.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", $token)
    return $client.SendAsync($req)
  }
  $vn = [TimeZoneInfo]::FindSystemTimeZoneById("SE Asia Standard Time")
  $now = [TimeZoneInfo]::ConvertTimeFromUtc([DateTime]::UtcNow, $vn)
  $s = $now.AddMinutes(-70); $e = $s.AddMinutes(60)
  $d = $s.ToString('yyyy-MM-dd'); $st = $s.ToString('HH:mm:ss'); $en = $e.ToString('HH:mm:ss')
  $ci = $s.AddMinutes(2).ToString('yyyy-MM-dd HH:mm:ss'); $pi = $s.ToString('yyyy-MM-dd HH:mm:ss'); $po = $now.AddMinutes(-5).ToString('yyyy-MM-dd HH:mm:ss')
  $member = "(SELECT id FROM users WHERE email='member.active@gym.test')"
  $trainer = "(SELECT id FROM users WHERE email='trainer1@gym.test')"
  $mpId = "dddddddd-0000-0000-0000-0000000000c1"
  Run-SqlI "DELETE FROM member_packages WHERE CAST(id AS VARCHAR(36)) LIKE 'dddddddd-%'; DELETE FROM packages WHERE CAST(id AS VARCHAR(36)) LIKE 'dddddddd-%'" | Out-Null
  Run-SqlI "INSERT INTO packages (id, name, price, session_count, package_type, peak_type, is_active, created_at) VALUES ('dddddddd-0000-0000-0000-000000000003', N'Goi thu do lai', 0, 100, 'SESSION_BASED', 'FULL_TIME', 1, SYSUTCDATETIME()); INSERT INTO member_packages (id, user_id, package_id, status, start_date, remaining_sessions, created_at) VALUES ('$mpId', $member, 'dddddddd-0000-0000-0000-000000000003', 'ACTIVE', DATEADD(DAY,-7,CAST(SYSUTCDATETIME() AS DATE)), 60, SYSUTCDATETIME())" | Out-Null
  $ids1 = 1..5  | ForEach-Object { "eeeeeeee-0000-0000-0000-0000000000{0:D2}" -f $_ }
  $ids2 = 1..20 | ForEach-Object { "ffffffff-0000-0000-0000-0000000000{0:D2}" -f $_ }
  $rows = ($ids1 + $ids2) | ForEach-Object { "('$_', $member, $trainer, '$d', '$st', '$en', 'CONFIRMED', '$ci', 'FACE', '$pi', 'FACE', '$po', 'FACE', N'[test-credit]')" }
  Run-SqlI ("INSERT INTO bookings (id, member_id, trainer_id, booking_date, start_time, end_time, status, check_in_time, check_in_method, pt_check_in_time, pt_check_in_method, pt_check_out_time, pt_check_out_method, notes) VALUES " + ($rows -join ",")) | Out-Null
  "--- truoc:"; Run-SqlI "SELECT remaining_sessions FROM member_packages WHERE id='$mpId'"
  "--- Phan 1: moi booking 2 request dong thoi (5 booking), ky vong con 55"
  $n = 0
  foreach ($id in $ids1) { $n++; $t1 = Send-Complete $id; $t2 = Send-Complete $id; [void][System.Threading.Tasks.Task]::WaitAll([System.Threading.Tasks.Task[]]@($t1, $t2)); "booking $n : $([int]$t1.Result.StatusCode) / $([int]$t2.Result.StatusCode)" }
  Run-SqlI "SELECT remaining_sessions AS sau_phan_1 FROM member_packages WHERE id='$mpId'"
  "--- Phan 2: 10 cap booking khac nhau cua cung hoi vien, ky vong con 35"
  for ($i = 0; $i -lt 20; $i += 2) { $t1 = Send-Complete $ids2[$i]; $t2 = Send-Complete $ids2[$i + 1]; [void][System.Threading.Tasks.Task]::WaitAll([System.Threading.Tasks.Task[]]@($t1, $t2)); "cap $(($i / 2) + 1) : $([int]$t1.Result.StatusCode) / $([int]$t2.Result.StatusCode)" }
  Run-SqlI "SELECT remaining_sessions AS con_lai FROM member_packages WHERE id='$mpId'"
  Run-SqlI "SELECT status, COUNT(*) AS so_booking FROM bookings WHERE notes = N'[test-credit]' GROUP BY status"
  Run-SqlI "SELECT COUNT(*) AS so_hoa_hong FROM commissions c JOIN bookings b ON b.id = c.booking_id WHERE b.notes = N'[test-credit]'"
}
