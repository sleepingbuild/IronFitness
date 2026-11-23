#requires -Version 5.1
<#
 apply_docker.ps1 (Agent 1) - them bo dong goi Docker vao repo GMS.
 Chay o GOC repo (noi co pom.xml), tren nhanh moi tao tu origin/master, cay lam viec sach:
   git fetch origin --prune
   git switch -c feature/agent1-docker-demo --no-track origin/master
   & D:\AgentChange\Agent1\apply_docker.ps1
 Script KHONG ghi de file da co: neu file dich ton tai thi dung lai.
 Them 7 file moi va sua 1 dong trong frontend/next.config.mjs (output: "standalone").
#>
$ErrorActionPreference = 'Stop'

if (-not (Test-Path 'pom.xml') -or -not (Test-Path 'frontend\package.json')) {
  throw 'Chay o goc repo (thay pom.xml va frontend\package.json).'
}
$dirty = @(git status --short --untracked-files=no)
if ($dirty.Count -gt 0) { throw ('Cay lam viec co thay doi chua commit: ' + ($dirty -join '; ')) }
$branch = (git branch --show-current)
if ($branch -eq 'master') { throw 'Dang o nhanh master. Tao nhanh moi truoc (xem huong dan dau file).' }

$root = (Get-Location).Path
$files = [ordered]@{}
$bomFiles = @{}

$files['Dockerfile'] = @'
# Dockerfile backend GMS (Agent 1). Build 2 giai doan: Maven + JDK 17, chay bang JRE 17, khong chay bang root.
# Test da chay o Jenkins (mvn verify) nen build image bo qua test de nhanh hon.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests package \
 && cp target/gym-management-system-*.jar /app.jar

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tzdata \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --create-home --uid 10001 gms \
 && mkdir -p /app/uploads \
 && chown -R gms:gms /app
WORKDIR /app
COPY --from=build --chown=gms:gms /app.jar /app/app.jar
USER gms
ENV TZ=Asia/Ho_Chi_Minh \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
'@

$files['.dockerignore'] = @'
.git
.github
.idea
.vscode
target
frontend
docs
scripts
uploads
*.md
**/*.log
# Khong dua file cau hinh bi mat cua may ca nhan vao image
src/main/resources/application-local.yml
'@

$files['frontend/Dockerfile'] = @'
# Dockerfile frontend GMS (Agent 1). Next.js 14 che do standalone.
# NEXT_PUBLIC_API_BASE_URL duoc nhung vao ma JS luc BUILD (trinh duyet goi thang backend), nen phai truyen bang build arg.
FROM node:20-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

FROM node:20-alpine AS build
WORKDIR /app
ARG NEXT_PUBLIC_API_BASE_URL=http://localhost:8082
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL \
    NEXT_TELEMETRY_DISABLED=1
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:20-alpine AS run
WORKDIR /app
ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    PORT=3000 \
    HOSTNAME=0.0.0.0
RUN addgroup -S gms && adduser -S gms -G gms
COPY --from=build --chown=gms:gms /app/public ./public
COPY --from=build --chown=gms:gms /app/.next/standalone ./
COPY --from=build --chown=gms:gms /app/.next/static ./.next/static
USER gms
EXPOSE 3000
CMD ["node", "server.js"]
'@

$files['frontend/.dockerignore'] = @'
node_modules
.next
.env.local
.env*.local
coverage
*.log
'@

$files['docker-compose.demo.yml'] = @'
# docker-compose.demo.yml (Agent 1). Chay TOAN BO he thong de test va demo: SQL Server + Redis + Backend + Frontend + du lieu mau.
#   docker compose -f docker-compose.demo.yml up -d --build
# Doc docs/DOCKER_DEMO.md. File nay tach rieng khoi docker-compose.yml (chi co SQL Server + Redis cho dev) va dung
# cong khac (web 3000, API 8082, SQL Server 14330) de khong va cham voi Jenkins (8081) hay container dev dang chay.
name: gms-demo

x-sql-image: &sql-image mcr.microsoft.com/mssql/server:2022-latest

services:
  sqlserver:
    image: *sql-image
    restart: unless-stopped
    environment:
      ACCEPT_EULA: "Y"
      MSSQL_PID: "Developer"
      MSSQL_SA_PASSWORD: "${GMS_DB_PASSWORD:-GmsStrongP@ss123}"
    ports:
      - "${GMS_SQL_PORT:-14330}:1433"
    volumes:
      - sqlserver-data:/var/opt/mssql
    healthcheck:
      test: ["CMD-SHELL", "/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P \"$$MSSQL_SA_PASSWORD\" -C -b -Q 'SELECT 1' || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 20
      start_period: 20s

  # Chay 1 lan: tao database gms_demo. Cac bang do Flyway cua backend tao.
  db-init:
    image: *sql-image
    restart: "no"
    depends_on:
      sqlserver:
        condition: service_healthy
    environment:
      MSSQL_SA_PASSWORD: "${GMS_DB_PASSWORD:-GmsStrongP@ss123}"
    entrypoint: ["/bin/bash", "-c"]
    command:
      - >
        /opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$$MSSQL_SA_PASSWORD" -C -b -Q
        "IF DB_ID('gms_demo') IS NULL CREATE DATABASE gms_demo;"

  redis:
    image: redis:7-alpine
    restart: unless-stopped

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    restart: unless-stopped
    depends_on:
      db-init:
        condition: service_completed_successfully
      redis:
        condition: service_started
    environment:
      DB_URL: "jdbc:sqlserver://sqlserver:1433;databaseName=gms_demo;encrypt=true;trustServerCertificate=true"
      DB_USERNAME: "sa"
      DB_PASSWORD: "${GMS_DB_PASSWORD:-GmsStrongP@ss123}"
      REDIS_HOST: "redis"
      # Chi dung cho demo. Dat GMS_JWT_SECRET (>= 32 ky tu) neu muon khac.
      GMS_JWT_SECRET: "${GMS_JWT_SECRET:-demo-only-secret-change-me-0123456789abcdef}"
    ports:
      - "${GMS_API_PORT:-8082}:8080"
    volumes:
      - uploads:/app/uploads
    healthcheck:
      test: ["CMD-SHELL", "curl -fsS http://localhost:8080/actuator/health | grep -q UP || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 30
      start_period: 60s

  # Chay 1 lan sau khi backend len (Flyway da tao bang): nap du lieu mau scripts/dev-seed/*.sql theo thu tu ten.
  # Chay lai khi can: docker compose -f docker-compose.demo.yml run --rm seed
  seed:
    image: *sql-image
    restart: "no"
    depends_on:
      backend:
        condition: service_healthy
    environment:
      MSSQL_SA_PASSWORD: "${GMS_DB_PASSWORD:-GmsStrongP@ss123}"
    volumes:
      - ./scripts/dev-seed:/seed:ro
    entrypoint: ["/bin/bash", "-c"]
    command:
      - |
        set -e
        for f in $$(ls /seed/*.sql | sort); do
          echo "== nap $$f"
          /opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "$$MSSQL_SA_PASSWORD" -C -d gms_demo -b -r1 -f 65001 -i "$$f" > /dev/null
        done
        echo "SEED XONG"

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        NEXT_PUBLIC_API_BASE_URL: "http://localhost:${GMS_API_PORT:-8082}"
    restart: unless-stopped
    depends_on:
      backend:
        condition: service_healthy
    ports:
      - "${GMS_WEB_PORT:-3000}:3000"
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://127.0.0.1:3000/ > /dev/null || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 15s

volumes:
  sqlserver-data:
  uploads:
'@

$files['scripts/demo-smoke.ps1'] = @'
<#
 demo-smoke.ps1 (Agent 1) - kiem thu khoi (smoke test) he thong GMS theo tung vai tro.
 Dang nhap 6 tai khoan mau (scripts/dev-seed/00_users.sql), goi cac endpoint GET chinh va kiem tra ma tra ve:
 200 voi cho duoc phep, 401 khi chua dang nhap, 403 khi sai vai tro.
 Chay:   .\scripts\demo-smoke.ps1                       (mac dinh API http://localhost:8082 cua docker-compose.demo.yml)
         .\scripts\demo-smoke.ps1 -Api http://localhost:8081   (ban Jenkins, seed vao gms_db_ci)
 Day la kiem tra "co song va dung quyen", khong thay the test nghiep vu. Dong FAIL: doc ma Thuc_te de biet 401/403/404/500.
#>
param(
  [string]$Api = 'http://localhost:8082',
  [string]$Password = 'Gym@12345'
)

$today = (Get-Date).ToString('yyyy-MM-dd')
$script:fail = 0
$script:results = @()

function Get-Code([string]$Path, [string]$Token) {
  $h = @{}
  if ($Token) { $h['Authorization'] = "Bearer $Token" }
  try {
    $r = Invoke-WebRequest -Uri ($Api + $Path) -Method Get -Headers $h -UseBasicParsing -TimeoutSec 30
    return [int]$r.StatusCode
  } catch {
    if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
    return 0
  }
}

function Get-Token([string]$Email) {
  try {
    $body = (@{ email = $Email; password = $Password } | ConvertTo-Json)
    $r = Invoke-RestMethod -Method Post -Uri "$Api/api/v1/auth/login" -ContentType 'application/json' -Body $body -TimeoutSec 30
    return $r.data.accessToken
  } catch { return $null }
}

function Add-Result([string]$Who, [string]$Path, $Expect, $Actual, [bool]$Ok) {
  if (-not $Ok) { $script:fail++ }
  $label = 'FAIL'
  if ($Ok) { $label = 'PASS' }
  $script:results += [pscustomobject]@{ Ket_qua = $label; Vai_tro = $Who; Kiem_tra = $Path; Ky_vong = $Expect; Thuc_te = $Actual }
}

function Check([string]$Who, [string]$Token, [string]$Path, [int]$Expect) {
  $code = Get-Code $Path $Token
  Add-Result $Who $Path $Expect $code ($code -eq $Expect)
}

function Check-Count([string]$Who, [string]$Token, [string]$Path, [string]$Prop, [int]$Min) {
  $n = -1
  try {
    $r = Invoke-RestMethod -Uri ($Api + $Path) -Headers @{ Authorization = "Bearer $Token" } -TimeoutSec 30
    $d = $r.data
    if ($Prop) { $d = $r.data.$Prop }
    $n = @($d).Count
  } catch { }
  Add-Result $Who ($Path + '  (so dong tra ve)') ('>= ' + $Min) $n ($n -ge $Min)
}

Write-Host ("Kiem thu khoi he thong tai " + $Api) -ForegroundColor Cyan

# --- Chua dang nhap
Check 'khach' $null '/actuator/health' 200
Check 'khach' $null '/v3/api-docs' 200
Check 'khach' $null '/api/v1/packages' 200
Check 'khach' $null '/api/v1/packages/admin' 401
Check 'khach' $null '/api/v1/users' 401

$accounts = [ordered]@{
  'admin'     = 'admin@gym.test'
  'reception' = 'reception@gym.test'
  'sales'     = 'sales@gym.test'
  'trainer'   = 'trainer1@gym.test'
  'member'    = 'member.active@gym.test'
  'member0'   = 'member.nopackage@gym.test'
}
$tok = @{}
foreach ($k in $accounts.Keys) {
  $t = Get-Token $accounts[$k]
  $tok[$k] = $t
  Add-Result $k ('dang nhap ' + $accounts[$k]) 'co token' $(if ($t) { 'co token' } else { 'khong co' }) ([bool]$t)
}

# --- Quan tri vien
if ($tok['admin']) {
  $t = $tok['admin']; $w = 'admin'
  Check $w $t '/api/v1/packages/admin' 200
  Check $w $t '/api/v1/reports/revenue' 200
  Check $w $t '/api/v1/reports/top-packages' 200
  Check $w $t '/api/v1/reports/checkin-traffic' 200
  Check $w $t '/api/v1/reports/renewal-rate' 200
  Check $w $t '/api/v1/reports/commissions-summary' 200
  Check $w $t '/api/v1/commissions' 200
  Check $w $t "/api/v1/staff-attendance?date=$today" 200
  Check $w $t "/api/v1/bookings?date=$today" 200
  Check $w $t '/api/v1/payments' 200
  Check $w $t '/api/v1/pos/orders' 200
  Check $w $t '/api/v1/leads' 200
  Check $w $t '/api/v1/branches' 200
  Check-Count $w $t '/api/v1/trainers' '' 2
  Check-Count $w $t '/api/v1/users?role=MEMBER&q=gym.test' 'content' 3
}

# --- Le tan
if ($tok['reception']) {
  $t = $tok['reception']; $w = 'reception'
  Check-Count $w $t '/api/v1/users?role=MEMBER&q=active' 'content' 1
  Check $w $t '/api/v1/payments' 200
  Check $w $t '/api/v1/pos/orders' 200
  Check $w $t '/api/v1/products/admin' 200
  Check $w $t '/api/v1/staff-attendance/me' 200
  Check $w $t '/api/v1/reports/revenue' 403
  Check $w $t '/api/v1/commissions' 403
}

# --- Sales
if ($tok['sales']) {
  $t = $tok['sales']; $w = 'sales'
  Check $w $t '/api/v1/leads' 200
  Check $w $t '/api/v1/leads/expiring-soon' 200
  Check $w $t '/api/v1/staff-attendance/me' 200
  Check $w $t '/api/v1/pos/orders' 403
  Check $w $t '/api/v1/users' 403
}

# --- Huan luyen vien
if ($tok['trainer']) {
  $t = $tok['trainer']; $w = 'trainer'
  Check $w $t '/api/v1/bookings/me' 200
  Check $w $t '/api/v1/commissions/me' 200
  Check $w $t '/api/v1/trainer-schedules' 200
  Check $w $t '/api/v1/profile/me' 200
  Check $w $t '/api/v1/leads' 403
  Check $w $t '/api/v1/commissions' 403
}

# --- Hoi vien co goi
if ($tok['member']) {
  $t = $tok['member']; $w = 'member'
  Check $w $t '/api/v1/member-packages/me' 200
  Check $w $t '/api/v1/bookings/me' 200
  Check $w $t '/api/v1/check-ins/me' 200
  Check $w $t '/api/v1/payments/me' 200
  Check $w $t '/api/v1/trainer-schedules/trainers' 200
  Check $w $t '/api/v1/packages' 200
  Check $w $t '/api/v1/users' 403
  Check $w $t '/api/v1/reports/revenue' 403
}

# --- Hoi vien chua co goi
if ($tok['member0']) {
  $t = $tok['member0']; $w = 'member0'
  Check $w $t '/api/v1/profile/me' 200
  Check $w $t '/api/v1/payments/me' 200
  Check $w $t '/api/v1/leads' 403
}

Write-Host ""
$script:results | Format-Table -AutoSize
$total = $script:results.Count
if ($script:fail -eq 0) {
  Write-Host ("TAT CA $total kiem tra deu dat.") -ForegroundColor Green
} else {
  Write-Host ("$($script:fail)/$total kiem tra KHONG dat. Dan bang tren (cac dong FAIL) cho Agent 1.") -ForegroundColor Red
  exit 1
}
'@

$bomFiles['scripts/demo-smoke.ps1'] = $true

$files['docs/DOCKER_DEMO.md'] = @'
# Chạy toàn bộ hệ thống bằng Docker (test và demo)

Một lệnh dựng đủ: SQL Server, Redis, Backend Spring Boot, Frontend Next.js, và dữ liệu mẫu cho mọi vai trò.
Dùng file riêng `docker-compose.demo.yml`; `docker-compose.yml` cũ (chỉ SQL Server + Redis cho dev) giữ nguyên.

## Yêu cầu
- Docker Desktop (Compose v2), còn trống khoảng **5 GB RAM** cho Docker (SQL Server cần tối thiểu 2 GB) và 6 GB ổ đĩa.
- Cổng trống: **3000** (web), **8082** (API), **14330** (SQL Server). Không trùng Jenkins (8081), SQL Server dev (1433).
- Máy có mạng ở lần build đầu (tải image, Maven, npm). Lần đầu mất khoảng 5 đến 10 phút.

## Chạy
```powershell
docker compose -f docker-compose.demo.yml up -d --build
docker compose -f docker-compose.demo.yml ps
```
Thứ tự tự động: SQL Server khỏe, tạo database `gms_demo`, backend lên (Flyway tạo bảng V1 đến V9), nạp dữ liệu mẫu, frontend lên.
Khi `backend` và `frontend` đều `healthy` và `seed` thoát với mã 0 là xong.

| Địa chỉ | Dùng để |
|---|---|
| http://localhost:3000 | Giao diện web |
| http://localhost:8082/swagger-ui.html | Tài liệu và thử API |
| http://localhost:8082/actuator/health | Kiểm tra sống |
| `localhost,14330` (user `sa`) | Mở SSMS xem database `gms_demo` |

Tài khoản: xem `docs/TEST_ACCOUNTS.md` (mật khẩu chung `Gym@12345`, ví dụ `admin@gym.test`, `reception@gym.test`).

## Kiểm thử khói theo vai trò
```powershell
.\scripts\demo-smoke.ps1
```
Đăng nhập 6 tài khoản, kiểm tra quyền truy cập từng endpoint chính (200, 401, 403).

## Lệnh thường dùng
```powershell
docker compose -f docker-compose.demo.yml logs -f backend          # xem log backend
docker compose -f docker-compose.demo.yml run --rm seed            # nạp lại dữ liệu mẫu (chạy lại không lỗi)
docker compose -f docker-compose.demo.yml up -d --build backend    # build lại sau khi sửa code backend
docker compose -f docker-compose.demo.yml down                     # dừng, giữ dữ liệu
docker compose -f docker-compose.demo.yml down -v                  # dừng và XÓA dữ liệu (làm lại từ đầu)
```
Đổi cổng hoặc mật khẩu bằng biến môi trường trước lệnh `up`: `GMS_WEB_PORT`, `GMS_API_PORT`, `GMS_SQL_PORT`, `GMS_DB_PASSWORD`, `GMS_JWT_SECRET`.
Nếu đổi `GMS_API_PORT` phải build lại frontend (`up -d --build frontend`), vì địa chỉ API được nhúng vào mã web lúc build.

## Lưu ý khi demo
- **Camera (điểm danh khuôn mặt)** chỉ chạy trên `http://localhost` hoặc HTTPS. Mở web từ máy khác trong mạng bằng IP sẽ bị trình duyệt chặn camera.
- Hồ sơ khuôn mặt không có trong dữ liệu mẫu: phải đăng ký thật bằng camera ở màn hình đăng ký khuôn mặt.
- Dữ liệu mẫu có ngày giờ tương đối theo giờ Việt Nam; nạp lại bằng lệnh `seed` nếu để lâu ngày.
- Bản demo dùng JWT secret mặc định chỉ để thử, không dùng cho môi trường thật.

## Xử lý sự cố
- `backend` không lên: `docker compose -f docker-compose.demo.yml logs backend` và tìm dòng `APPLICATION FAILED` hoặc `Caused by`.
- Báo trùng cổng: đặt `GMS_WEB_PORT`, `GMS_API_PORT` hoặc `GMS_SQL_PORT` khác.
- `sqlserver` khởi động lại liên tục: Docker Desktop chưa được cấp đủ RAM (Settings, Resources).
'@


# Kiem tra truoc: khong ghi de
foreach ($rel in $files.Keys) {
  if (Test-Path (Join-Path $root $rel)) { throw ("File da ton tai, dung lai: " + $rel) }
}

foreach ($rel in $files.Keys) {
  $path = Join-Path $root $rel
  $dir = Split-Path $path -Parent
  if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
  $text = ($files[$rel] -replace "`r`n", "`n") + "`n"
  $enc = New-Object System.Text.UTF8Encoding($bomFiles.ContainsKey($rel))
  [IO.File]::WriteAllText($path, $text, $enc)
  Write-Host ("  + " + $rel) -ForegroundColor Green
}

# Sua frontend/next.config.mjs: them output standalone (bat buoc cho Dockerfile frontend)
$cfg = Join-Path $root 'frontend\next.config.mjs'
$utf8 = New-Object System.Text.UTF8Encoding($false)
$s = [IO.File]::ReadAllText($cfg, $utf8)
if ($s.Contains('output:')) {
  Write-Host '  = next.config.mjs da co output, bo qua' -ForegroundColor Yellow
} else {
  $anchor = 'reactStrictMode: true,'
  if (-not $s.Contains($anchor)) { throw 'Khong thay dong neo reactStrictMode trong frontend/next.config.mjs' }
  $nl = "`n"
  if ($s.Contains("`r`n")) { $nl = "`r`n" }
  $s = $s.Replace($anchor, $anchor + $nl + '  output: "standalone",')
  [IO.File]::WriteAllText($cfg, $s, $utf8)
  Write-Host '  ~ frontend/next.config.mjs (output: standalone)' -ForegroundColor Green
}

Write-Host ''
Write-Host 'Xong. Kiem tra: git diff frontend/next.config.mjs (dung 1 dong them), git status --short (7 file moi + 1 file sua).'
