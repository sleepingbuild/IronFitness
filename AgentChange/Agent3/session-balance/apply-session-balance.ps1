# Hien thi so buoi con lai o man hinh dat lich cua hoi vien (BookingCreate.tsx).
# Chay o goc repo (D:\SrpingBoot-GYM\gms-agent3), nhanh moi tu origin/master, cay sach,
# SAU KHI da chep sessionRules.ts, sessionRules.test.ts, SessionBanner.tsx vao frontend\src\features\bookings.
# Moi thay doi phai khop dung 1 cho. Neu khong, script dung va KHONG ghi file nao.
$ErrorActionPreference = "Stop"
$enc = New-Object System.Text.UTF8Encoding($false)
$root = (Get-Location).Path
$dir = "frontend\src\features\bookings"
foreach ($n in "sessionRules.ts", "SessionBanner.tsx") {
  if (-not (Test-Path (Join-Path $root "$dir\$n"))) { throw "Chua co $n trong $dir. Chep file do vao truoc." }
}
$f = "$dir\BookingCreate.tsx"
$p = Join-Path $root $f
if (-not (Test-Path $p)) { throw "Khong thay file: $f" }
$t = [System.IO.File]::ReadAllText($p, $enc)

function Patch($text, $name, $pattern, $replacement) {
  $n = [regex]::Matches($text, $pattern).Count
  if ($n -ne 1) { throw "[$name] mong doi khop dung 1 cho, thuc te $n. Dung lai, chua ghi file nao." }
  $crlf = $text.Contains("`r`n")
  $rep = if ($crlf) { $replacement -replace "`r?`n", "`r`n" } else { $replacement }
  $ev = [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $rep }
  return [regex]::Replace($text, $pattern, $ev)
}

$t = Patch $t "import" 'import \{ MemberPicker \} from "\./MemberPicker";' @'
import { getMyMembership } from "@/features/membership/api";
import type { MemberPackage } from "@/features/membership/types";
import { MemberPicker } from "./MemberPicker";
'@
$t = Patch $t "import rules" 'import \{ bookedMessage, slotLabel, toTrainerChoices \} from "\./offHoursRules";' @'
import { bookedMessage, slotLabel, toTrainerChoices } from "./offHoursRules";
import { SessionBanner } from "./SessionBanner";
import { sessionSummary } from "./sessionRules";
'@
$t = Patch $t "state so buoi" 'const \[busyStart, setBusyStart\] = useState<string \| null>\(null\);' @'
const [busyStart, setBusyStart] = useState<string | null>(null);

  // Hội viên tự đặt: xem số buổi còn lại (GET /member-packages/me). Lỗi tải thì bỏ qua, không chặn đặt.
  const [membership, setMembership] = useState<MemberPackage | null | undefined>(undefined);
  useEffect(() => {
    if (staff) return;
    let cancelled = false;
    getMyMembership()
      .then((m) => !cancelled && setMembership(m))
      .catch(() => !cancelled && setMembership(undefined));
    return () => {
      cancelled = true;
    };
  }, [staff, reloadKey]);
  const summary = !staff && membership !== undefined ? sessionSummary(membership) : null;
  const sessionBlocked = summary?.blocked ?? false;
'@
$t = Patch $t "banner" '\{staff && <MemberPicker value=\{member\} onChange=\{setMember\} />\}' @'
{staff && <MemberPicker value={member} onChange={setMember} />}
      {summary && <SessionBanner summary={summary} />}
'@
$t = Patch $t "khoa nut dat" 'disabled=\{busyStart !== null \|\| memberHint !== null\}' 'disabled={busyStart !== null || memberHint !== null || sessionBlocked}'

[System.IO.File]::WriteAllText($p, $t, $enc)
Write-Host "Da sua: $f"
Write-Host "Xong. Chay: git diff"
