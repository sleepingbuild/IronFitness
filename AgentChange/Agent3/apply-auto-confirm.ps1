# Ap dung "dat lich tu xac nhan + khung ngoai gio 21:00-23:59".
# Chay o goc repo (D:\SrpingBoot-GYM\gms-agent3), nhanh moi tu origin/master, cay sach.
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

$out = @{}

# ---------- BookingServiceImpl ----------
$f = "$main\service\impl\BookingServiceImpl.java"
$t = Read-File $f
$t = Patch $t "create: goi validate" '(?m)^([ \t]*)validateBookingRules\(booking, null\);' {
  param($m) $i = $m.Groups[1].Value
  "${i}boolean offHours = validateBookingRules(booking, null);`n${i}booking.setStatus(offHours ? BookingStatus.PENDING : BookingStatus.CONFIRMED);"
}
$t = Patch $t "update: goi validate" '(?m)^([ \t]*)validateBookingRules\(booking, bookingId\);' {
  param($m) $i = $m.Groups[1].Value
  "${i}boolean offHours = validateBookingRules(booking, bookingId);`n${i}booking.setStatus(offHours ? BookingStatus.PENDING : BookingStatus.CONFIRMED);"
}
$t = Patch $t "chu ky validate" 'private void validateBookingRules\(Booking booking, UUID excludeBookingId\) \{' {
  param($m) 'private boolean validateBookingRules(Booking booking, UUID excludeBookingId) {'
}
$t = Patch $t "buoc 2: ngoai ca" 'if \(!withinWorkingHours\) \{\s*throw new BusinessException\(ErrorCode\.BOOKING_OUT_OF_WORKING_HOURS, ("[^"]*")\);\s*\}' {
  param($m) $lit = $m.Groups[1].Value
@"
boolean offHours = false;
        if (!withinWorkingHours) {
            // Ngoài ca: chỉ cho đặt trong khung ngoài giờ (booking chờ PT xác nhận), còn lại từ chối.
            offHours = trainerScheduleService.isWithinOffHours(booking.getStartTime(), booking.getEndTime());
            if (!offHours) {
                throw new BusinessException(ErrorCode.BOOKING_OUT_OF_WORKING_HOURS, $lit);
            }
        }
"@
}
$t = Patch $t "return offHours" '(ErrorCode\.MEMBERSHIP_NO_SESSION,[^;]*;\s*\}\s*\})(\s*\})' {
  param($m) $m.Groups[1].Value + "`n`n        // true = đặt ngoài ca (khung ngoài giờ), cần PT xác nhận; false = trong ca, tự xác nhận.`n        return offHours;" + $m.Groups[2].Value
}
$out[$f] = $t

# ---------- TrainerScheduleService (interface) ----------
$f = "$main\service\TrainerScheduleService.java"
$t = Read-File $f
$t = Patch $t "interface: them 2 ham" '\}\s*\z' {
  param($m)
@'

    /**
     * Khung giờ [start, end] có nằm trọn trong khung "ngoài giờ" toàn hệ thống không
     * (cấu hình gms.booking.off-hours-start / off-hours-end, mặc định 21:00 đến 23:59).
     * Đặt trong khung này mà ngoài ca của PT thì booking ở trạng thái PENDING, chờ PT xác nhận.
     */
    boolean isWithinOffHours(java.time.LocalTime start, java.time.LocalTime end);

    /** Slot ngoài giờ còn trống của PT trong 1 ngày: trừ slot nằm trong ca của PT, slot đã có người đặt, slot quá sát giờ. */
    List<AvailableSlotResponse> findOffHoursSlots(UUID trainerId, LocalDate date);
}
'@
}
$out[$f] = $t

# ---------- TrainerScheduleServiceImpl ----------
$f = "$main\service\impl\TrainerScheduleServiceImpl.java"
$t = Read-File $f
$t = Patch $t "impl: import" 'import java\.time\.LocalDate;' {
  param($m) "import java.time.LocalDate;`nimport java.time.LocalDateTime;"
}
$t = Patch $t "impl: cau hinh khung ngoai gio" 'private int slotDurationMinutes;' {
  param($m)
@'
private int slotDurationMinutes;

    /** Khung ngoài giờ toàn hệ thống (HH:mm). Cuối khung 23:59 hiển thị là 24:00 vì cột time không lưu được 24:00. */
    @Value("${gms.booking.off-hours-start:21:00}")
    private String offHoursStart = "21:00";

    @Value("${gms.booking.off-hours-end:23:59}")
    private String offHoursEnd = "23:59";
'@
}
$t = Patch $t "impl: ham ngoai gio" '(\.anyMatch\(s -> !start\.isBefore\(s\.getStartTime\(\)\) && !end\.isAfter\(s\.getEndTime\(\)\)\);\s*\})' {
  param($m)
  $m.Groups[1].Value + "`n`n" + @'
    @Override
    public boolean isWithinOffHours(LocalTime start, LocalTime end) {
        return !start.isBefore(offStart()) && !end.isAfter(offEnd());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> findOffHoursSlots(UUID trainerId, LocalDate date) {
        LocalTime windowStart = offStart();
        LocalTime windowEnd = offEnd();

        List<Booking> taken = bookingRepository
                .findByTrainerIdOrderByBookingDateDescStartTimeDesc(trainerId).stream()
                .filter(b -> b.getBookingDate().equals(date) && !SLOT_RELEASED_STATUSES.contains(b.getStatus()))
                .toList();

        LocalDateTime earliest = LocalDateTime.now(clock).plusMinutes(30);

        List<AvailableSlotResponse> result = new ArrayList<>();
        LocalTime cursor = windowStart;
        while (cursor.isBefore(windowEnd)) {
            LocalTime next = cursor.plusMinutes(slotDurationMinutes);
            // Cộng quá nửa đêm (quay về 00:xx) hoặc vượt cuối khung thì cắt ở cuối khung.
            LocalTime slotEnd = (next.isAfter(cursor) && !next.isAfter(windowEnd)) ? next : windowEnd;
            final LocalTime slotStart = cursor;
            final LocalTime end = slotEnd;

            boolean occupied = taken.stream()
                    .anyMatch(b -> b.getStartTime().isBefore(end) && b.getEndTime().isAfter(slotStart));
            boolean tooSoon = LocalDateTime.of(date, slotStart).isBefore(earliest);
            // Slot nằm trong ca của PT đã có ở danh sách slot trong ca (tự xác nhận), không lặp lại ở đây.
            boolean inShift = isWithinWorkingHours(trainerId, date, slotStart, end);

            if (!occupied && !tooSoon && !inShift) {
                result.add(AvailableSlotResponse.builder()
                        .trainerId(trainerId)
                        .date(date)
                        .startTime(slotStart)
                        .endTime(end)
                        .build());
            }
            cursor = slotEnd;
        }
        return result;
    }

    private LocalTime offStart() {
        return LocalTime.parse(offHoursStart);
    }

    private LocalTime offEnd() {
        return LocalTime.parse(offHoursEnd);
    }
'@
}
$out[$f] = $t

# ---------- ScheduleController ----------
$f = "$main\controller\ScheduleController.java"
$t = Read-File $f
$t = Patch $t "controller: endpoint off-hours" '\}\s*\z' {
  param($m)
@'

    @GetMapping("/off-hours")
    @PreAuthorize("hasAnyRole('MEMBER', 'RECEPTIONIST', 'SUPER_ADMIN')")
    @Operation(summary = "Khung giờ ngoài giờ còn trống của PT trong 1 ngày (đặt xong phải chờ PT xác nhận)")
    public ApiResponse<List<AvailableSlotResponse>> offHours(
            @RequestParam UUID trainerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(scheduleService.findOffHoursSlots(trainerId, date));
    }
}
'@
}
$out[$f] = $t

# ---------- API_DESIGN.md ----------
$f = "API_DESIGN.md"
$t = Read-File $f
$t = Patch $t "API_DESIGN: them dong off-hours" '(\|\s*GET\s*\|\s*`/api/v1/trainer-schedules/trainers\?date=\.\.\.`[^\r\n]*)' {
  param($m)
  $m.Groups[1].Value + "`n" + '| GET | `/api/v1/trainer-schedules/off-hours?trainerId=...&date=...` | Member / Lễ tân / Admin — khung giờ ngoài giờ còn trống của PT (mặc định 21:00 đến 23:59, cấu hình `gms.booking.off-hours-start` / `off-hours-end`; trừ slot nằm trong ca của PT, slot đã có người đặt và slot quá sát giờ). Đặt trong ca làm việc của PT: booking `CONFIRMED` ngay. Đặt trong khung ngoài giờ: booking `PENDING`, chờ PT xác nhận. Giờ khác: `BOOKING_OUT_OF_WORKING_HOURS`. Cuối khung 23:59 hiển thị là 24:00 |'
}
$out[$f] = $t

# ---------- ghi tat ca (chi toi day neu moi thay doi deu khop) ----------
foreach ($rel in $out.Keys) {
  [System.IO.File]::WriteAllText((Join-Path $root $rel), $out[$rel], $enc)
  Write-Host "Da sua: $rel"
}
Write-Host "Xong. Chay: git diff --stat"
