# Ap dung H4 cho PackageDetail.tsx (hoi vien tu dang ky goi). Chay tai THU MUC GOC repo.
# Chi sua cac dong ma (ASCII), khong ghi chu Viet nen khong lam hong dau tieng Viet cua file.
$ErrorActionPreference = 'Stop'
$f = Join-Path (Get-Location) 'frontend\src\features\membership\PackageDetail.tsx'
if (-not (Test-Path $f)) { throw 'Khong thay PackageDetail.tsx, hay chay o thu muc goc repo' }
$t = [IO.File]::ReadAllText($f, [Text.Encoding]::UTF8)
$nl = if ($t.Contains("`r`n")) { "`r`n" } else { "`n" }
if ($t.Contains('BranchSelect')) { throw 'PackageDetail.tsx da co BranchSelect, khong ap dung lai' }

function Swap([string]$text, [string]$old, [string]$new) {
  $n = ([regex]::Matches($text, [regex]::Escape($old))).Count
  if ($n -ne 1) { throw "Mau '$old' khop $n lan (can dung 1). Dung lai va bao Agent 2." }
  return $text.Replace($old, $new)
}

$t = Swap $t 'import { getPackage, registerPackage } from "./api";' ('import { getPackage, registerPackage } from "./api";' + $nl + 'import { BranchSelect } from "./BranchSelect";' + $nl + 'import { branchIdOrUndefined } from "./branchOptions";')
$t = Swap $t 'const [confirming, setConfirming] = useState(false);' ('const [confirming, setConfirming] = useState(false);' + $nl + '  const [branchId, setBranchId] = useState("");')
$t = Swap $t 'registerPackage({ packageId: pkg.id })' 'registerPackage({ packageId: pkg.id, branchId: branchIdOrUndefined(branchId) })'
$t = Swap $t '<div className="flex gap-2">' ('<BranchSelect value={branchId} onChange={setBranchId} />' + $nl + '          <div className="flex gap-2">')
[IO.File]::WriteAllText($f, $t, (New-Object Text.UTF8Encoding($false)))
Write-Host 'Da sua PackageDetail.tsx'
