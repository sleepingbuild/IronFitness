# perf-api.ps1 - Do thoi gian phan hoi API HomeFit (phuc vu chuong Kiem thu hieu nang).
# Chay:  powershell -ExecutionPolicy Bypass -File scripts\perf-api.ps1 -BaseUrl http://localhost:8090 -Runs 30 -Concurrency 10
# Backend phai dang chay. Script tao 1 nguoi dung perf-...@example.test; don bang SQL ghi o cuoi.
# Ket qua chi co gia tri tham khao tren may phat trien.

param(
    [string]$BaseUrl = "http://localhost:8090",
    [int]$Runs = 20,
    [int]$Concurrency = 10,
    [string]$Csv = ""
)

$ErrorActionPreference = "Stop"
$api = "$BaseUrl/api/v1"
$email = "perf-" + [guid]::NewGuid().ToString("N").Substring(0, 12) + "@example.test"
$password = "Perf12345"
$json = "application/json"

function Measure-Request {
    param([string]$Method, [string]$Url, [string]$Body, [string]$Token)
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    if ($Body) {
        $null = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Url -Headers $headers -ContentType $json -Body $Body
    } else {
        $null = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Url -Headers $headers
    }
    $watch.Stop()
    return $watch.Elapsed.TotalMilliseconds
}

function Get-Stats {
    param([string]$Name, [double[]]$Samples)
    $sorted = $Samples | Sort-Object
    $p95Index = [math]::Min($sorted.Count - 1, [math]::Ceiling($sorted.Count * 0.95) - 1)
    [pscustomobject]@{
        Kich_ban = $Name
        So_lan   = $sorted.Count
        TB_ms    = [math]::Round(($sorted | Measure-Object -Average).Average, 1)
        Min_ms   = [math]::Round($sorted[0], 1)
        P95_ms   = [math]::Round($sorted[$p95Index], 1)
        Max_ms   = [math]::Round($sorted[$sorted.Count - 1], 1)
    }
}

Write-Host "Dang ky nguoi dung do hieu nang: $email"
$registerBody = @{ email = $email; password = $password; displayName = "Perf" } | ConvertTo-Json -Compress
$auth = Invoke-RestMethod -Method Post -Uri "$api/auth/register" -ContentType $json -Body $registerBody
$token = $auth.accessToken
Write-Host "Da dang ky. Bat dau do ($Runs lan moi kich ban)..."

# Khoi dong nong
$null = Measure-Request "GET" "$api/categories" $null $token

$results = @()

$samples = 1..$Runs | ForEach-Object {
    $loginBody = @{ email = $email; password = $password } | ConvertTo-Json -Compress
    Measure-Request "POST" "$api/auth/login" $loginBody $null
}
$results += Get-Stats "P03a Dang nhap (POST /auth/login)" $samples

$samples = 1..$Runs | ForEach-Object { Measure-Request "GET" "$api/categories" $null $token }
$results += Get-Stats "P03b Danh muc (GET /categories)" $samples

$samples = 1..$Runs | ForEach-Object { Measure-Request "GET" "$api/exercises" $null $token }
$results += Get-Stats "P03c Bai tap (GET /exercises)" $samples

$samples = 1..$Runs | ForEach-Object {
    $body = @{
        clientUuid = [guid]::NewGuid().ToString(); categoryId = $null
        startedAt = "2026-10-31T08:00:00Z"; completedAt = "2026-10-31T08:20:00Z"
        totalSeconds = 1200; exercisesDone = 8
    } | ConvertTo-Json -Compress
    Measure-Request "POST" "$api/workout-sessions" $body $token
}
$results += Get-Stats "P03d Ghi 1 buoi tap (POST /workout-sessions)" $samples

# P04: dong bo 1 lo 100 buoi tap
$sessions = 1..100 | ForEach-Object {
    @{
        clientUuid = [guid]::NewGuid().ToString(); categoryId = $null
        startedAt = "2026-10-31T09:00:00Z"; completedAt = "2026-10-31T09:10:00Z"
        totalSeconds = 600; exercisesDone = 5
    }
}
$batchBody = @{ sessions = $sessions } | ConvertTo-Json -Compress -Depth 5
$batchMs = Measure-Request "POST" "$api/workout-sessions/batch" $batchBody $token
$results += Get-Stats "P04 Dong bo lo 100 buoi tap (batch)" @($batchMs)

# P05: tai song song - $Concurrency cong viec, moi cong viec $Runs/$Concurrency yeu cau doc
$perJob = [math]::Max(1, [int][math]::Ceiling($Runs / $Concurrency))
$watch = [System.Diagnostics.Stopwatch]::StartNew()
$jobs = 1..$Concurrency | ForEach-Object {
    Start-Job -ArgumentList $api, $token, $perJob -ScriptBlock {
        param($api, $token, $perJob)
        $ok = 0
        1..$perJob | ForEach-Object {
            try {
                $null = Invoke-WebRequest -UseBasicParsing -Uri "$api/exercises" -Headers @{ Authorization = "Bearer $token" }
                $ok++
            } catch { }
        }
        $ok
    }
}
$done = $jobs | Wait-Job | Receive-Job
$jobs | Remove-Job
$watch.Stop()
$total = $Concurrency * $perJob
$success = ($done | Measure-Object -Sum).Sum
$seconds = [math]::Round($watch.Elapsed.TotalSeconds, 2)

Write-Host ""
Write-Host "===== KET QUA ====="
$results | Format-Table -AutoSize
Write-Host ("P05 Tai song song: {0} yeu cau tu {1} luong, thanh cong {2}/{0}, tong {3} giay, khoang {4} yeu cau/giay" -f `
    $total, $Concurrency, $success, $seconds, [math]::Round($success / [math]::Max($seconds, 0.001), 1))

if ($Csv) {
    $results | Export-Csv -Path $Csv -NoTypeInformation -Encoding UTF8
    Write-Host "Da ghi CSV: $Csv"
}

Write-Host ""
Write-Host "Don du lieu do hieu nang (chay trong MySQL):"
Write-Host "  DELETE FROM users WHERE email LIKE 'perf-%@example.test';"
