# Luat hoan thanh buoi moi: PT khong can quet mat vao/ra; duoi 45 phut bat buoc co ly do.
# Chay o goc repo (D:\SrpingBoot-GYM\gms-agent3), nhanh moi tu origin/master, cay sach,
# SAU KHI da chep CompleteBookingRequest.java va BookingCompleteRulesTest.java (xem huong dan).
# Moi thay doi phai khop dung 1 cho. Neu khong, script dung va KHONG ghi file nao.
$ErrorActionPreference = "Stop"
$enc = New-Object System.Text.UTF8Encoding($false)
$root = (Get-Location).Path
$main = "src\main\java\com\gym\management"
$test = "src\test\java\com\gym\management"

if (-not (Test-Path (Join-Path $root "$main\dto\request\CompleteBookingRequest.java"))) {
  throw "Chua co CompleteBookingRequest.java trong $main\dto\request. Chep file do vao truoc."
}

function Read-File($rel) {
  $p = Join-Path $root $rel
  if (-not (Test-Path $p)) { throw "Khong thay file: $rel" }
  return [System.IO.File]::ReadAllText($p, $enc)
}
function Patch($text, $name, $pattern, $replacement) {
  $n = [regex]::Matches($text, $pattern).Count
  if ($n -ne 1) { throw "[$name] mong doi khop dung 1 cho, thuc te $n. Dung lai, chua ghi file nao." }
  $crlf = $text.Contains("`r`n")
  $rep = if ($crlf) { $replacement -replace "`r?`n", "`r`n" } else { $replacement }
  $ev = [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $rep }
  return [regex]::Replace($text, $pattern, $ev)
}

$out = @{}

# ---------- BookingService (interface) ----------
$f = "$main\service\BookingService.java"
$t = Read-File $f
$t = Patch $t "interface complete" 'BookingResponse completeBooking\(UUID trainerId, UUID bookingId\);' @'
default BookingResponse completeBooking(UUID trainerId, UUID bookingId) {
        return completeBooking(trainerId, bookingId, null);
    }

    /**
     * PT hoàn thành buổi. Không cần quét mặt vào/ra: thời lượng tính từ lúc PT quét vào (chưa quét thì từ giờ bắt
     * đầu theo lịch) đến lúc PT quét ra (chưa quét ra thì đến lúc bấm hoàn thành, ghi lại làm giờ ra với cách MANUAL).
     * Dưới số phút tối thiểu (gms.booking.min-complete-minutes, mặc định 45) thì bắt buộc có lý do.
     * Luật như nhau cho buổi trong ca và buổi ngoài giờ.
     */
    BookingResponse completeBooking(UUID trainerId, UUID bookingId, String reason);
'@
$out[$f] = $t

# ---------- BookingServiceImpl ----------
$f = "$main\service\impl\BookingServiceImpl.java"
$t = Read-File $f
$t = Patch $t "hang so MANUAL" 'private static final String METHOD_FACE = "FACE";' @'
private static final String METHOD_FACE = "FACE";
    /** PT không quét ra mà bấm hoàn thành: giờ bấm được ghi làm giờ ra. */
    private static final String METHOD_MANUAL = "MANUAL";
'@
$t = Patch $t "cau hinh so phut" 'private int lateCancelHours;' @'
private int lateCancelHours;

    /** Buổi thực tế dưới số phút này thì PT phải nhập lý do mới hoàn thành được. */
    @Value("${gms.booking.min-complete-minutes:45}")
    private int minCompleteMinutes = 45;
'@
$t = Patch $t "javadoc G4" '\(G4\): booking[^\r\n]*' '(G4, đã đổi): booking đang CONFIRMED và học viên đã quét vào. PT không cần quét vào/ra: thời lượng tính từ lúc PT quét vào (hoặc giờ bắt đầu theo lịch) đến lúc quét ra (hoặc lúc bấm hoàn thành); dưới mức tối thiểu thì phải có lý do.'
$t = Patch $t "chu ky complete" 'public BookingResponse completeBooking\(UUID trainerId, UUID bookingId\) \{' 'public BookingResponse completeBooking(UUID trainerId, UUID bookingId, String reason) {'
$t = Patch $t "khoi kiem tra PT" 'if \(booking\.getPtCheckInTime\(\) == null \|\| booking\.getPtCheckOutTime\(\) == null\) \{\s*throw new BusinessException\(ErrorCode\.BOOKING_PT_NOT_CHECKED_IN,\s*"[^"]*"\);\s*\}' @'
// Thời lượng thực tế: từ lúc PT quét vào (chưa quét thì từ giờ bắt đầu theo lịch) đến lúc PT quét ra
        // (chưa quét ra thì đến lúc bấm hoàn thành). Dưới mức tối thiểu thì bắt buộc có lý do.
        LocalDateTime actualStart = booking.getPtCheckInTime() != null
                ? booking.getPtCheckInTime()
                : LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime actualEnd = booking.getPtCheckOutTime() != null
                ? booking.getPtCheckOutTime()
                : LocalDateTime.now(clock);
        long workedMinutes = java.time.Duration.between(actualStart, actualEnd).toMinutes();

        String given = reason == null ? "" : reason.trim();
        if (!given.isEmpty() && given.length() < MIN_EARLY_LEAVE_REASON_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Lý do phải có ít nhất " + MIN_EARLY_LEAVE_REASON_LENGTH + " ký tự");
        }
        String existingReason = booking.getPtEarlyLeaveReason();
        boolean hasReason = !given.isEmpty() || (existingReason != null && !existingReason.isBlank());
        if (workedMinutes < minCompleteMinutes && !hasReason) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Buổi tập chưa đủ " + minCompleteMinutes + " phút, cần nhập lý do (ít nhất "
                            + MIN_EARLY_LEAVE_REASON_LENGTH + " ký tự)");
        }
        if (!given.isEmpty()) {
            booking.setPtEarlyLeaveReason(given);
        }
        if (booking.getPtCheckOutTime() == null) {
            // PT không quét ra: ghi giờ bấm hoàn thành làm giờ ra để lương và báo cáo dùng chung một mốc.
            booking.setPtCheckOutTime(actualEnd);
            booking.setPtCheckOutMethod(METHOD_MANUAL);
        }
'@
$out[$f] = $t

# ---------- BookingController ----------
$f = "$main\controller\BookingController.java"
$t = Read-File $f
$t = Patch $t "import dto" 'import com\.gym\.management\.dto\.request\.EarlyLeaveReasonRequest;' @'
import com.gym.management.dto.request.CompleteBookingRequest;
import com.gym.management.dto.request.EarlyLeaveReasonRequest;
'@
$t = Patch $t "chu ky endpoint" 'public ApiResponse<BookingResponse> complete\(@PathVariable UUID id\) \{' 'public ApiResponse<BookingResponse> complete(@PathVariable UUID id, @Valid @RequestBody(required = false) CompleteBookingRequest request) {'
$t = Patch $t "goi service" 'bookingService\.completeBooking\(CurrentUserId\.get\(\), id\)\);' 'bookingService.completeBooking(CurrentUserId.get(), id, request == null ? null : request.getReason()));'
$out[$f] = $t

# ---------- API_DESIGN.md ----------
$f = "API_DESIGN.md"
$t = Read-File $f
$t = Patch $t "API_DESIGN complete" '\| POST \| `/api/v1/bookings/\{id\}/complete` \|[^\r\n]*' '| POST | `/api/v1/bookings/{id}/complete` | Trainer — chỉ khi `CONFIRMED` và học viên đã check-in; PT không cần quét mặt vào/ra. Thời lượng thực tế tính từ lúc PT quét vào (chưa quét thì từ giờ bắt đầu theo lịch) đến lúc PT quét ra (chưa quét thì đến lúc bấm; ghi lại làm giờ ra, `ptCheckOutMethod = MANUAL`). Body tuỳ chọn `{ "reason": "..." }`: bắt buộc (từ 5 ký tự) khi thời lượng dưới `gms.booking.min-complete-minutes` (mặc định 45), thiếu thì `VALIDATION_ERROR`; lý do lưu vào `ptEarlyLeaveReason`. Buổi chưa đủ giờ kết thúc theo lịch và không có lý do vẫn hoàn thành nhưng `fullDuration = false` (không tính công). Giống nhau cho buổi trong ca và buổi ngoài giờ |'
$out[$f] = $t

# ---------- BookingServiceImplTest: go 3 test kiem tra luat cu ----------
$f = "$test\service\impl\BookingServiceImplTest.java"
$t = Read-File $f
foreach ($name in @("throwsPtNotCheckedIn_whenPtNeverScanned", "throwsPtNotCheckedIn_whenPtCheckedInButNotOut", "publishesCompleted_fullDurationFalse_whenPtLeftEarlyWithoutReason")) {
  $pat = '(?m)^        @Test\r?\n        void ' + $name + '\(\) \{[\s\S]*?\r?\n        \}\r?\n(\r?\n)?'
  $t = Patch $t "go test $name" $pat ''
}
$out[$f] = $t

foreach ($rel in $out.Keys) {
  [System.IO.File]::WriteAllText((Join-Path $root $rel), $out[$rel], $enc)
  Write-Host "Da sua: $rel"
}
Write-Host "Xong. Chay: git diff --stat"
