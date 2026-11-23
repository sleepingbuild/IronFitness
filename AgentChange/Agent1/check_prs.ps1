#requires -Version 5.1
<#
 check_prs.ps1 (Agent 1) - kiem tra PR truoc khi merge.
 Chay o D:\SrpingBoot-GYM\gms-docs; file da theo doi khong duoc co thay doi chua commit.

 Tang 1 - kiem tra tinh, nhanh, khong can Maven:
   .\check_prs.ps1
   .\check_prs.ps1 -Branches feature/agent2-refund-h3,fix/agent2-consume-session-atomic

 Tang 2 - mo phong merge tuan tu vao worktree TAM (khong dong vao master) roi build:
   .\check_prs.ps1 -Simulate -Order chore/agent2-seed-packages,fix/agent2-consume-session-atomic -Verify

 Tang 3 - them: chay ung dung that tren DB TRONG (gms_db_verify) de bat loi migration/HQL:
   .\check_prs.ps1 -Simulate -Order <cac nhanh> -Verify -Migrate

 Can: Docker dang chay container gms-sqlserver va gms-redis (docker compose up -d sqlserver redis).
 Script chua duoc Agent 1 chay thu (sandbox khong co PowerShell); neu loi cu phap, dan loi lai.
#>
param(
  [string[]]$Branches,
  [switch]$Simulate,
  [string[]]$Order,
  [switch]$Verify,
  [switch]$Migrate,
  [switch]$Keep,
  [string]$JavaHome  = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot',
  [string]$MavenHome = 'C:\tools\apache-maven-3.9.9',
  [string]$SaPassword = 'GmsStrongP@ss123'
)

$ErrorActionPreference = 'Continue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

function Write-Tag([string]$tag, [string]$msg) {
  $color = 'Gray'
  if ($tag -eq 'LOI')  { $color = 'Red' }
  if ($tag -eq 'CANH BAO') { $color = 'Yellow' }
  if ($tag -eq 'OK')   { $color = 'Green' }
  Write-Host ("  [{0}] {1}" -f $tag, $msg) -ForegroundColor $color
}

# ------------------------------------------------------------------ TANG 1
function Test-Branch([string]$name) {
  $ref = "origin/$name"
  $err = 0
  $warn = 0

  Write-Host ""
  Write-Host ("=== {0}" -f $name) -ForegroundColor Cyan
  $commits = (git rev-list --count "origin/master..$ref")
  $last = (git log -1 --format='%an, %ad' --date=short $ref)
  Write-Host ("  {0} commit moi, commit cuoi: {1}" -f $commits, $last)

  # 1. Xung dot voi master hien tai
  $mt = git merge-tree --write-tree origin/master $ref 2>&1
  $mtRc = $LASTEXITCODE
  $conflicts = @($mt | Where-Object { "$_" -match '^CONFLICT' })
  if ($mtRc -ne 0) {
    $err++
    Write-Tag 'LOI' 'xung dot voi master:'
    $conflicts | ForEach-Object { Write-Host ("       " + $_) -ForegroundColor Red }
  } else {
    Write-Tag 'OK' 'khong xung dot voi master hien tai'
  }

  # 2. Danh sach file doi
  $rows = @()
  foreach ($line in @(git diff --name-status "origin/master...$ref")) {
    $p = "$line" -split "`t"
    if ($p.Count -ge 2) {
      $rows += [pscustomobject]@{ Status = $p[0].Substring(0,1); Path = $p[$p.Count - 1] }
    }
  }
  Write-Host ("  {0} file doi" -f $rows.Count)

  # 3. Migration: khong duoc sua/xoa file da co; version khong trung master
  $masterMig = @(git ls-tree --name-only origin/master src/main/resources/db/migration/ | ForEach-Object { Split-Path $_ -Leaf })
  foreach ($r in ($rows | Where-Object { $_.Path -match 'db/migration/V' })) {
    if ($r.Status -ne 'A') {
      $err++
      Write-Tag 'LOI' ("SUA/XOA migration da co tren master: " + $r.Path)
    } else {
      $leaf = Split-Path $r.Path -Leaf
      $ver = ''
      if ($leaf -match '^V([0-9]+(?:_[0-9]+)*)__') { $ver = $Matches[1] }
      $clash = @($masterMig | Where-Object { $_ -match ("^V" + [regex]::Escape($ver) + "__") })
      if ($ver -ne '' -and $clash.Count -gt 0) {
        $err++
        Write-Tag 'LOI' ("version migration V$ver da ton tai tren master: " + ($clash -join ', '))
      } else {
        Write-Tag 'CANH BAO' ("migration moi: $leaf -> bat buoc co bang chung chay tren DB trong (dung -Simulate -Migrate)")
        $warn++
      }
    }
  }

  # 4. File SHARED / cau hinh (chi Agent 1 duoc sua)
  $shared = '(SecurityConfig|exception/ErrorCode\.java|GlobalExceptionHandler|exception/ApiResponse\.java|JwtTokenProvider|^pom\.xml$|^Jenkinsfile$|^docker-compose\.yml$|application(-[a-z]+)?\.yml$|^frontend/src/(lib|components)/)'
  foreach ($r in ($rows | Where-Object { $_.Path -match $shared -and $_.Path -notmatch 'example' })) {
    $warn++
    Write-Tag 'CANH BAO' ("SHARED/cau hinh, can Agent 1 duyet: " + $r.Path)
  }

  # 5. Frontend: chi duoc sua khu vuc cua minh
  $fe = @($rows | Where-Object { $_.Path -like 'frontend/*' })
  if ($fe.Count -gt 0) {
    $okFe = '^frontend/src/(app/\(app\)/|features/|config/navigation\.ts$)'
    foreach ($r in ($fe | Where-Object { $_.Path -notmatch $okFe -and $_.Path -notmatch '^frontend/src/(lib|components)/' })) {
      $warn++
      $why = 'ngoai khu vuc cho phep'
      if ($r.Path -match 'package(-lock)?\.json$') { $why = 'doi dependency, can ly do' }
      Write-Tag 'CANH BAO' ("frontend: " + $r.Path + " ($why)")
    }
    Write-Tag 'CANH BAO' 'co frontend: sau khi merge chay trong frontend: npm ci; npm run lint; npm run typecheck; npm test; npm run build'
    $warn++
  }

  # 6. Controller: so @Operation phai >= so mapping; co doi API thi phai doi API_DESIGN.md
  $ctrl = @($rows | Where-Object { $_.Status -ne 'D' -and $_.Path -match 'src/main/java/.*/controller/.*\.java$' })
  foreach ($r in $ctrl) {
    $src = (git show ("{0}:{1}" -f $ref, $r.Path)) -join "`n"
    $maps = [regex]::Matches($src, '(?m)^\s*@(Get|Post|Put|Patch|Delete)Mapping').Count
    $ops  = [regex]::Matches($src, '@Operation\b').Count
    if ($ops -lt $maps) {
      $warn++
      Write-Tag 'CANH BAO' ("{0}: {1} mapping nhung chi {2} @Operation (Swagger)" -f (Split-Path $r.Path -Leaf), $maps, $ops)
    }
  }
  if ($ctrl.Count -gt 0 -and -not ($rows | Where-Object { $_.Path -eq 'API_DESIGN.md' })) {
    $warn++
    Write-Tag 'CANH BAO' 'co sua controller nhung API_DESIGN.md khong doi'
  }

  # 7. Test: code moi trong service/controller phai co test di kem
  $newMain = @($rows | Where-Object { $_.Status -eq 'A' -and $_.Path -match 'src/main/java/.*/(service/impl|controller)/' })
  $anyTest = @($rows | Where-Object { $_.Path -like 'src/test/*' })
  if ($newMain.Count -gt 0 -and $anyTest.Count -eq 0) {
    $err++
    Write-Tag 'LOI' 'them service/controller moi nhung khong co file test nao'
  }

  # 8. Quet dong them vao (added lines) trong java/ts/sql/yml
  $diff = @(git diff -U0 "origin/master...$ref" -- 'src' 'frontend/src' 'scripts' 'Jenkinsfile' 2>$null)
  $added = @($diff | Where-Object { "$_" -match '^\+' -and "$_" -notmatch '^\+\+\+' })

  $clock = @($added | Where-Object { "$_" -match '(LocalDate|LocalDateTime|Instant|ZonedDateTime)\.now\(\)' })
  if ($clock.Count -gt 0) {
    $warn++
    Write-Tag 'CANH BAO' ("{0} cho dung now() tran (quy uoc: dung bean Clock). Vi du: {1}" -f $clock.Count, ("$($clock[0])").Trim())
  }
  $dbg = @($added | Where-Object { "$_" -match 'System\.out\.print|printStackTrace\(' })
  if ($dbg.Count -gt 0) {
    $warn++
    Write-Tag 'CANH BAO' ("{0} dong in debug (System.out/printStackTrace)" -f $dbg.Count)
  }
  $sec = @($added | Where-Object { "$_" -match '(?i)(password|secret|api[_-]?key|token)\s*[:=]\s*["''][^"'']{8,}["'']' -and "$_" -notmatch '(?i)Gym@12345|GmsStrongP@ss123|\$\{' })
  if ($sec.Count -gt 0) {
    $warn++
    Write-Tag 'CANH BAO' ("{0} dong co ve la bi mat ghi cung. Vi du: {1}" -f $sec.Count, ("$($sec[0])").Trim())
  }
  $bad1 = [string][char]0xFFFD
  $bad2 = ([string][char]0x00E1) + ([string][char]0x00BB)
  $moji = @($added | Where-Object { ("$_").Contains($bad1) -or ("$_").Contains($bad2) })
  if ($moji.Count -gt 0) {
    $err++
    Write-Tag 'LOI' ("{0} dong co dau hieu loi ma hoa chu Viet. Vi du: {1}" -f $moji.Count, ("$($moji[0])").Trim())
  }

  if ($err -eq 0 -and $warn -eq 0) { Write-Tag 'OK' 'khong co van de tinh nao' }
  return [pscustomobject]@{ Branch = $name; Commits = $commits; Conflict = ($mtRc -ne 0); Errors = $err; Warnings = $warn }
}

function Get-MigrationVersions([string]$name) {
  $out = @()
  foreach ($line in @(git diff --name-status "origin/master...origin/$name" -- 'src/main/resources/db/migration/')) {
    $p = "$line" -split "`t"
    if ($p.Count -ge 2 -and $p[0].Substring(0,1) -eq 'A') {
      $leaf = Split-Path $p[$p.Count - 1] -Leaf
      if ($leaf -match '^V([0-9]+(?:_[0-9]+)*)__') { $out += [pscustomobject]@{ Branch = $name; Version = $Matches[1]; File = $leaf } }
    }
  }
  return $out
}

# ------------------------------------------------------------------ TANG 2 + 3
function Invoke-Simulate {
  $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
  $wt = Join-Path $env:TEMP ("gms-sim-" + $stamp)
  Write-Host ""
  Write-Host ("=== MO PHONG MERGE trong worktree tam: " + $wt) -ForegroundColor Cyan
  git worktree add --detach $wt origin/master | Out-Null
  if (-not (Test-Path $wt)) { Write-Host 'Khong tao duoc worktree tam' -ForegroundColor Red; return }

  Push-Location $wt
  $failed = $false
  foreach ($b in $Order) {
    git merge --no-ff -m ("sim merge " + $b) ("origin/" + $b) 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
      Write-Host ("  XUNG DOT khi merge " + $b + ", file xung dot:") -ForegroundColor Red
      git diff --name-only --diff-filter=U | ForEach-Object { Write-Host ("     " + $_) -ForegroundColor Red }
      git merge --abort
      $failed = $true
      break
    }
    Write-Host ("  da merge " + $b) -ForegroundColor Green
  }

  $verifyOk = $null
  if (-not $failed -and $Verify) {
    $env:JAVA_HOME = $JavaHome
    $env:PATH = "$JavaHome\bin;$MavenHome\bin;" + $env:PATH
    Write-Host ""
    Write-Host '=== mvn -B clean verify (co the mat vai phut) ===' -ForegroundColor Cyan
    & "$MavenHome\bin\mvn.cmd" -B clean verify *>&1 | Tee-Object -FilePath (Join-Path $wt 'verify.log') | Out-Null
    $mvnRc = $LASTEXITCODE
    Select-String -Path (Join-Path $wt 'verify.log') -Pattern 'Tests run:.*Fail|BUILD (SUCCESS|FAILURE)|\[ERROR\]' |
      Select-Object -Last 25 | ForEach-Object { Write-Host ("  " + $_.Line) }
    $verifyOk = ($mvnRc -eq 0)
    if ($verifyOk) { Write-Host '  mvn verify: XANH' -ForegroundColor Green } else { Write-Host ("  mvn verify: DO (log day du: " + (Join-Path $wt 'verify.log') + ")") -ForegroundColor Red }

    if (Test-Path (Join-Path $wt 'frontend\package.json')) {
      if (Get-Command npm -ErrorAction SilentlyContinue) {
        Write-Host ""
        Write-Host '=== frontend: npm ci, lint, typecheck, test, build ===' -ForegroundColor Cyan
        Push-Location (Join-Path $wt 'frontend')
        foreach ($step in @('ci', 'run lint', 'run typecheck', 'test', 'run build')) {
          cmd /c "npm $step > NUL 2>&1"
          if ($LASTEXITCODE -eq 0) { Write-Host ("  npm " + $step + ": XANH") -ForegroundColor Green }
          else { Write-Host ("  npm " + $step + ": DO (chay lai tay trong " + (Get-Location) + " de xem loi)") -ForegroundColor Red; break }
        }
        Pop-Location
      }
    }
  }

  if (-not $failed -and $Migrate) {
    if ($verifyOk -ne $true) { Write-Host '  bo qua -Migrate vi mvn verify chua xanh' -ForegroundColor Yellow }
    else { Invoke-MigrateCheck $wt }
  }

  Pop-Location
  if (-not $Keep) {
    git worktree remove --force $wt 2>&1 | Out-Null
    Write-Host ("  da xoa worktree tam (dung -Keep de giu lai)")
  } else {
    Write-Host ("  giu lai worktree: " + $wt)
  }
}

function Invoke-MigrateCheck([string]$wt) {
  $db = 'gms_db_verify'
  $port = 8090
  Write-Host ""
  Write-Host ("=== chay ung dung that tren DB trong " + $db + " (cong " + $port + ") ===") -ForegroundColor Cyan

  $sql = "IF DB_ID('$db') IS NOT NULL BEGIN ALTER DATABASE [$db] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$db]; END; CREATE DATABASE [$db];"
  docker exec gms-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $SaPassword -C -Q $sql
  if ($LASTEXITCODE -ne 0) { Write-Host '  khong tao duoc DB (container gms-sqlserver dang chay chua?)' -ForegroundColor Red; return }

  $jar = Get-ChildItem (Join-Path $wt 'target') -Filter 'gym-management-system-*.jar' |
         Where-Object { $_.Name -notmatch 'original|sources|javadoc' } | Select-Object -First 1
  if (-not $jar) { Write-Host '  khong thay file jar trong target' -ForegroundColor Red; return }

  $env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=$db;encrypt=true;trustServerCertificate=true"
  $env:DB_USERNAME = 'sa'
  $env:DB_PASSWORD = $SaPassword
  $env:REDIS_HOST = 'localhost'
  $env:SERVER_PORT = "$port"
  $env:GMS_JWT_SECRET = 'verify-only-secret-0123456789-abcdefghijklmnop'
  $out = Join-Path $wt 'app.out.log'
  $errf = Join-Path $wt 'app.err.log'
  $proc = Start-Process -FilePath (Join-Path $JavaHome 'bin\java.exe') -ArgumentList @('-jar', ('"' + $jar.FullName + '"')) `
          -RedirectStandardOutput $out -RedirectStandardError $errf -PassThru -WindowStyle Hidden

  $up = $false
  for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 3
    if ($proc.HasExited) { break }
    try {
      $h = Invoke-WebRequest -Uri ("http://localhost:$port/actuator/health") -UseBasicParsing -TimeoutSec 3
      if ($h.StatusCode -eq 200) { $up = $true; break }
    } catch { }
  }

  if ($up) {
    Write-Host '  UNG DUNG KHOI DONG DUOC (HQL va Flyway deu qua)' -ForegroundColor Green
    foreach ($u in @('/v3/api-docs', '/api/v1/packages', '/api/v1/packages/admin')) {
      $code = 'khong ket noi'
      try { $code = (Invoke-WebRequest -Uri ("http://localhost:$port$u") -UseBasicParsing -TimeoutSec 5).StatusCode }
      catch { if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode } }
      Write-Host ("  GET {0} -> {1}" -f $u, $code)
    }
    Write-Host '  (ky vong: api-docs 200, packages 200, packages/admin 401 hoac 403)'
    Write-Host '  flyway_schema_history:'
    docker exec gms-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $SaPassword -C -d $db -W -s ' | ' `
      -Q 'SET NOCOUNT ON; SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank'
  } else {
    Write-Host '  UNG DUNG KHONG KHOI DONG. 40 dong cuoi cua log loi:' -ForegroundColor Red
    if (Test-Path $errf) { Get-Content $errf -Tail 40 }
    if (Test-Path $out)  { Get-Content $out  -Tail 40 | Select-String 'ERROR|Caused by|Flyway|Migration|SQL' }
  }

  if (-not $proc.HasExited) { Stop-Process -Id $proc.Id -Force }
}

# ------------------------------------------------------------------ MAIN
# Chi tinh file DA THEO DOI bi sua (file chua theo doi, vi du chinh script nay, khong anh huong merge-tree/worktree tam)
$dirty = @(git status --short --untracked-files=no)
if ($dirty.Count -gt 0) {
  Write-Host 'Cay lam viec co thay doi chua commit o file da theo doi. Dung lai. Cac file:' -ForegroundColor Red
  $dirty | ForEach-Object { Write-Host ("   " + $_) -ForegroundColor Red }
  Write-Host 'Xu ly: git stash (hoac commit) roi chay lai.'
  return
}
git fetch origin --prune | Out-Null

if ($Simulate) {
  if (-not $Order -or $Order.Count -eq 0) { Write-Host 'Thieu -Order <nhanh1,nhanh2,...>' -ForegroundColor Red; return }
  Invoke-Simulate
  return
}

if (-not $Branches -or $Branches.Count -eq 0) {
  $Branches = @(git branch -r --no-merged origin/master | ForEach-Object { $_.Trim() } |
              Where-Object { $_ -and $_ -notmatch 'HEAD' } | ForEach-Object { $_ -replace '^origin/', '' })
}

$results = @()
$migs = @()
foreach ($b in $Branches) {
  $results += Test-Branch $b
  $migs += Get-MigrationVersions $b
}

Write-Host ""
Write-Host '=== TONG KET ===' -ForegroundColor Cyan
$results | Format-Table Branch, Commits, Conflict, Errors, Warnings -AutoSize

$dups = $migs | Group-Object Version | Where-Object { $_.Group.Branch | Select-Object -Unique | Measure-Object | Where-Object { $_.Count -gt 1 } }
foreach ($d in $dups) {
  Write-Host ("TRUNG VERSION migration V" + $d.Name + " giua cac nhanh: " + (($d.Group | ForEach-Object { $_.Branch + '/' + $_.File }) -join ' ; ')) -ForegroundColor Red
}
Write-Host 'Nhanh xep chong (A nam trong B) chi can merge B. Kiem tra thu tu: git merge-base --is-ancestor <A> <B>'
Write-Host 'Buoc tiep: .\check_prs.ps1 -Simulate -Order <thu tu> -Verify [-Migrate]'
