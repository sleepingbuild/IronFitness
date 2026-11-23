# Them OpenBookingCountPort cho BookingServiceImpl (Agent 2 se goi de tra heldSessions).
# Chay o goc repo (D:\SrpingBoot-GYM\gms-agent3), nhanh moi tu origin/master, cay sach,
# SAU KHI da chep OpenBookingCountPort.java vao src\main\java\com\gym\management\service\.
# Moi thay doi phai khop dung 1 cho. Neu khong, script dung va KHONG ghi file nao.
$ErrorActionPreference = "Stop"
$enc = New-Object System.Text.UTF8Encoding($false)
$root = (Get-Location).Path
$main = "src\main\java\com\gym\management"

function Read-File($rel) {
  $p = Join-Path $root $rel
  if (-not (Test-Path $p)) { throw "Khong thay file: $rel" }
  return [System.IO.File]::ReadAllText($p, $enc)
}
function Fix-Nl($text, $s) {
  if ($text.Contains("`r`n")) { return ($s -replace "`r?`n", "`r`n") }
  return $s
}
function Patch($text, $name, $pattern, $make) {
  $n = [regex]::Matches($text, $pattern).Count
  if ($n -ne 1) { throw "[$name] mong doi khop dung 1 cho, thuc te $n. Dung lai, chua ghi file nao." }
  $ev = [System.Text.RegularExpressions.MatchEvaluator]{ param($m) Fix-Nl $text (& $make $m) }
  return [regex]::Replace($text, $pattern, $ev)
}

if (-not (Test-Path (Join-Path $root "$main\service\OpenBookingCountPort.java"))) {
  throw "Chua co OpenBookingCountPort.java trong $main\service. Chep file do vao truoc."
}

$f = "$main\service\impl\BookingServiceImpl.java"
$t = Read-File $f
$t = Patch $t "import" 'import com\.gym\.management\.service\.MembershipLookupPort;' {
  param($m) "import com.gym.management.service.MembershipLookupPort;`nimport com.gym.management.service.OpenBookingCountPort;"
}
$t = Patch $t "khai bao class" 'public class BookingServiceImpl implements BookingService \{' {
  param($m) 'public class BookingServiceImpl implements BookingService, OpenBookingCountPort {'
}
$t = Patch $t "them ham cong khai" '(private long countOpenBookings\(UUID memberId, UUID exclude\) \{[\s\S]*?\.count\(\);\s*\})' {
  param($m)
  $m.Groups[1].Value + "`n`n" + @'
    /**
     * Cổng cho Agent 2 (heldSessions): cùng một con số với luật đặt lịch bước 5c, đã áp lazy-status.
     * Lazy-status có thể ghi DB nên chạy trong transaction ghi; người gọi không được đánh dấu readOnly.
     */
    @Override
    @Transactional
    public int countOpenBookings(UUID memberId) {
        return Math.toIntExact(countOpenBookings(memberId, BookingRepository.NO_EXCLUDE));
    }
'@
}

[System.IO.File]::WriteAllText((Join-Path $root $f), $t, $enc)
Write-Host "Da sua: $f"
Write-Host "Xong. Chay: git diff"
