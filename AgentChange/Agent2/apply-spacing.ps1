# Chay tu THU MUC GOC repo, SAU KHI PR feature/agent1-ui-spacing da merge va nhanh da o tren origin/master.
# Vi du: powershell -File D:\AgentChange\Agent2\apply-spacing.ps1
# File nay chi chua ky tu ASCII; emoji duoc tao bang ma Unicode, tieng Viet trong file dich duoc giu nguyen
# (doc va ghi UTF-8 khong BOM). Chay lai nhieu lan khong sao (bo qua cho da sua).
$dir = Join-Path (Get-Location).Path "frontend\src\features\membership"
$enc = New-Object System.Text.UTF8Encoding($false)

$muscle = [char]::ConvertFromUtf32(0x1F4AA)   # bap tay
$weights = [char]::ConvertFromUtf32(0x1F3CB)  # nguoi tap ta
$card = [char]::ConvertFromUtf32(0x1F4B3)     # the thanh toan
$tools = [char]::ConvertFromUtf32(0x1F6E0)    # cong cu

function Rep($name, $old, $new) {
  $path = Join-Path $dir $name
  if (-not (Test-Path $path)) { Write-Host "KHONG CO FILE $name"; return }
  $text = [System.IO.File]::ReadAllText($path)
  if ($text.Contains($new)) { Write-Host "$name : da sua roi ($old)"; return }
  $count = ([regex]::Matches($text, [regex]::Escape($old))).Count
  if ($count -eq 0) { Write-Host "KHONG THAY '$old' trong $name"; return }
  [System.IO.File]::WriteAllText($path, $text.Replace($old, $new), $enc)
  Write-Host "$name : thay $count cho ($old)"
}

# 1) Luoi the tu chia 2, 3 cot theo be rong (lop card-grid cua Agent 1).
Rep "MyMembership.tsx" 'className="grid gap-4 md:grid-cols-2"' 'className="card-grid"'
Rep "MyMembership.tsx" '"panel p-5 md:col-span-2"' '"panel p-5 col-span-full"'
Rep "PackageList.tsx" 'className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3"' 'className="card-grid"'

# 2) Bieu tuong cho trang thai trong (EmptyState) va tieu de trang (PageHeader).
Rep "MyMembership.tsx" 'title={EMPTY_TEXT[tab].title}' ('icon="' + $muscle + '" title={EMPTY_TEXT[tab].title}')
Rep "MyMembership.tsx" '<EmptyState title="Ch' ('<EmptyState icon="' + $card + '" title="Ch')
Rep "PackageList.tsx" '<PageHeader' ('<PageHeader icon="' + $weights + '"')
Rep "PackageList.tsx" '<EmptyState' ('<EmptyState icon="' + $weights + '"')
Rep "PaymentsDesk.tsx" '<PageHeader' ('<PageHeader icon="' + $card + '"')
Rep "AdminPackages.tsx" '<PageHeader' ('<PageHeader icon="' + $tools + '"')

# 3) Hover o danh sach ket qua tim hoi vien: bg-raised (bg-plate-tint danh cho dong dang chon).
Rep "MemberPicker.tsx" 'hover:bg-plate-tint' 'hover:bg-raised'
