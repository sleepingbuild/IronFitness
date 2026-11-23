#requires -Version 5.1
<#
 apply_sonar.ps1 (Agent 1) - them SonarQube + do phu test JaCoCo.
 Chay o GOC repo, tren nhanh moi tao tu origin/master, cay lam viec sach:
   git fetch origin --prune
   git switch -c feature/agent1-sonarqube --no-track origin/master
   & D:\AgentChange\Agent1\apply_sonar.ps1
 Them 4 file moi (khong ghi de file da co) va sua pom.xml:
   <properties>  + jacoco.version va cac thuoc tinh sonar.*
   <build>       + jacoco-maven-plugin (prepare-agent luc test, bao cao XML o pha test)
#>
$ErrorActionPreference = 'Stop'

if (-not (Test-Path 'pom.xml') -or -not (Test-Path 'docker-compose.demo.yml')) {
  throw 'Chay o goc repo (can thay pom.xml va docker-compose.demo.yml).'
}
$dirty = @(git status --short --untracked-files=no)
if ($dirty.Count -gt 0) { throw ('Cay lam viec co thay doi chua commit: ' + ($dirty -join '; ')) }
if ((git branch --show-current) -eq 'master') { throw 'Dang o nhanh master. Tao nhanh moi truoc (xem huong dan dau file).' }

$root = (Get-Location).Path
$utf8 = New-Object System.Text.UTF8Encoding($false)
$files = [ordered]@{}
$bomFiles = @{}

$files['lombok.config'] = @'
# Dung cho JaCoCo va SonarQube: danh dau ma do Lombok sinh ra (getter, setter, builder...) la @Generated
# de khong tinh vao do phu test. Khong anh huong cach ung dung chay.
config.stopBubbling = true
lombok.addLombokGeneratedAnnotation = true
'@

$files['docker-compose.sonar.yml'] = @'
# docker-compose.sonar.yml (Agent 1). SonarQube Community + PostgreSQL de phan tich chat luong ma nguon.
# Chay rieng, doc lap voi ban demo (docker-compose.demo.yml):
#   docker compose -f docker-compose.sonar.yml up -d
# Can khoang 3 GB RAM cho Docker. Neu may yeu, tat ban demo truoc: docker compose -f docker-compose.demo.yml down
# Doc docs/SONARQUBE.md.
name: gms-sonar

services:
  sonar-db:
    image: postgres:16-alpine
    restart: unless-stopped
    environment:
      POSTGRES_USER: "sonar"
      POSTGRES_PASSWORD: "${GMS_SONAR_DB_PASSWORD:-sonar-demo-pass}"
      POSTGRES_DB: "sonar"
    volumes:
      - sonar-db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U sonar -d sonar"]
      interval: 10s
      timeout: 5s
      retries: 10

  sonarqube:
    image: sonarqube:10.6-community
    restart: unless-stopped
    depends_on:
      sonar-db:
        condition: service_healthy
    environment:
      SONAR_JDBC_URL: "jdbc:postgresql://sonar-db:5432/sonar"
      SONAR_JDBC_USERNAME: "sonar"
      SONAR_JDBC_PASSWORD: "${GMS_SONAR_DB_PASSWORD:-sonar-demo-pass}"
      # Docker Desktop (WSL2) thuong khong dat duoc vm.max_map_count=262144 ma Elasticsearch cua SonarQube yeu cau.
      # Tat mmap cho Elasticsearch de chay duoc (chi nen dung cho dev/demo, khong dung production).
      SONAR_SEARCH_JAVAADDITIONALOPTS: "-Dnode.store.allow_mmap=false"
    ports:
      - "${GMS_SONAR_PORT:-9000}:9000"
    volumes:
      - sonar-data:/opt/sonarqube/data
      - sonar-extensions:/opt/sonarqube/extensions
      - sonar-logs:/opt/sonarqube/logs

volumes:
  sonar-db-data:
  sonar-data:
  sonar-extensions:
  sonar-logs:
'@

$files['scripts/sonar-analyze.ps1'] = @'
<#
 sonar-analyze.ps1 (Agent 1) - chay test, do do phu (JaCoCo) va gui ket qua phan tich len SonarQube.
 Can: SonarQube dang chay (docker compose -f docker-compose.sonar.yml up -d) va mot token (xem docs/SONARQUBE.md).
 Chay o goc repo:
   $env:SONAR_TOKEN = 'squ_xxxxxxxx'
   .\scripts\sonar-analyze.ps1
 hoac:  .\scripts\sonar-analyze.ps1 -Token 'squ_xxxxxxxx' -HostUrl 'http://localhost:9000'
 Mat vai phut vi chay toan bo test truoc khi phan tich.
#>
param(
  [string]$Token = $env:SONAR_TOKEN,
  [string]$HostUrl = 'http://localhost:9000',
  [string]$JavaHome = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot',
  [string]$MavenHome = 'C:\tools\apache-maven-3.9.9'
)

if (-not $Token) {
  Write-Host 'Thieu token. Dat $env:SONAR_TOKEN hoac truyen -Token (xem docs/SONARQUBE.md).' -ForegroundColor Red
  exit 1
}
if (-not (Test-Path 'pom.xml')) {
  Write-Host 'Chay o goc repo (khong thay pom.xml).' -ForegroundColor Red
  exit 1
}

try {
  $st = Invoke-RestMethod -Uri ($HostUrl + '/api/system/status') -TimeoutSec 10
  if ($st.status -ne 'UP') { Write-Host ('SonarQube chua san sang, trang thai: ' + $st.status) -ForegroundColor Yellow; exit 1 }
} catch {
  Write-Host ('Khong ket noi duoc SonarQube tai ' + $HostUrl + '. Da chay docker-compose.sonar.yml chua? Doi 1-2 phut sau khi khoi dong.') -ForegroundColor Red
  exit 1
}

$env:JAVA_HOME = $JavaHome
$env:PATH = "$JavaHome\bin;$MavenHome\bin;" + $env:PATH

& "$MavenHome\bin\mvn.cmd" -B -ntp clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar `
  "-Dsonar.host.url=$HostUrl" "-Dsonar.token=$Token"
if ($LASTEXITCODE -ne 0) {
  Write-Host 'Phan tich that bai. Xem log Maven o tren.' -ForegroundColor Red
  exit 1
}
Write-Host ''
Write-Host ('Xong. Xem ket qua: ' + $HostUrl + '/dashboard?id=gym-management-system') -ForegroundColor Green
'@

$bomFiles['scripts/sonar-analyze.ps1'] = $true

$files['docs/SONARQUBE.md'] = @'
# Phân tích chất lượng mã bằng SonarQube

SonarQube quét mã nguồn backend để tìm lỗi (bug), lỗ hổng bảo mật, đoạn mã khó bảo trì (code smell), mã trùng lặp và đo **độ phủ test** (JaCoCo). Kết quả xem trên web, có "Quality Gate" đạt hoặc không đạt.

## Yêu cầu
- Docker Desktop còn trống khoảng **3 GB RAM**. Nếu đang chạy bản demo (`docker-compose.demo.yml`) mà máy yếu thì tắt nó trước: `docker compose -f docker-compose.demo.yml down`.
- Cổng **9000** trống (đổi bằng biến `GMS_SONAR_PORT`).

## Bước 1: chạy SonarQube
```powershell
docker compose -f docker-compose.sonar.yml up -d
docker compose -f docker-compose.sonar.yml logs -f sonarqube
```
Đợi tới khi log có dòng `SonarQube is operational` (khoảng 1 đến 2 phút, lần đầu lâu hơn), bấm Ctrl+C để thoát phần xem log (SonarQube vẫn chạy). Kiểm tra: mở http://localhost:9000.

## Bước 2: đăng nhập và tạo token (làm một lần)
1. Vào http://localhost:9000, đăng nhập `admin` / `admin`, hệ thống buộc đổi mật khẩu.
2. Góc phải chọn biểu tượng tài khoản, vào **My Account**, mục **Security**.
3. Ở "Generate Tokens": đặt tên `gms`, loại **User Token**, hạn tuỳ chọn, bấm **Generate**. **Sao chép token ngay** (chỉ hiện một lần, dạng `squ_...`).

## Bước 3: phân tích
```powershell
$env:SONAR_TOKEN = 'squ_dan_token_vao_day'
.\scripts\sonar-analyze.ps1
```
Script chạy toàn bộ test, đo độ phủ, rồi gửi kết quả lên SonarQube (mất vài phút). Xong thì mở http://localhost:9000/dashboard?id=gym-management-system.

## Cách đọc kết quả
| Mục | Ý nghĩa |
|---|---|
| Quality Gate | Đạt hoặc không đạt theo ngưỡng mặc định ("Sonar way") trên **mã mới** |
| Bugs | Lỗi có khả năng gây sai kết quả |
| Vulnerabilities, Security Hotspots | Lỗ hổng bảo mật và chỗ cần con người xem xét |
| Code Smells | Mã khó đọc, khó bảo trì |
| Coverage | Độ phủ test do JaCoCo đo (đã loại DTO, entity, config và lớp khởi động) |
| Duplications | Tỉ lệ mã trùng lặp |

## Cách đo độ phủ
`pom.xml` có `jacoco-maven-plugin`: tác tử JaCoCo gắn vào lúc chạy test, báo cáo XML ở `target/site/jacoco/jacoco.xml` mà SonarQube đọc. `lombok.config` đánh dấu mã Lombok sinh ra (getter, setter, builder) là `@Generated` để không bị tính vào độ phủ. Các thư mục `dto`, `entity`, `config` và lớp `GymManagementApplication` được loại khỏi **độ phủ** (`sonar.coverage.exclusions` trong `pom.xml`) vì chỉ chứa dữ liệu hoặc cấu hình; chúng **vẫn được quét** lỗi và lỗ hổng.

## Xử lý sự cố
- Không vào được http://localhost:9000: xem log `docker compose -f docker-compose.sonar.yml logs sonarqube`; lỗi liên quan `max virtual memory` hoặc `vm.max_map_count` nghĩa là cấu hình bỏ qua mmap chưa có hiệu lực, báo lại để đổi cách.
- Script báo "Khong ket noi duoc SonarQube": chờ thêm, hoặc kiểm tra `http://localhost:9000/api/system/status` có `"status":"UP"`.
- Lỗi xác thực (`Not authorized`): token sai hoặc hết hạn, tạo lại ở Bước 2.
- Dừng: `docker compose -f docker-compose.sonar.yml down` (giữ dữ liệu), thêm `-v` để xoá hết.
'@


foreach ($rel in $files.Keys) {
  if (Test-Path (Join-Path $root $rel)) { throw ("File da ton tai, dung lai: " + $rel) }
}

# ---- Sua pom.xml truoc (neu neo sai thi dung truoc khi ghi file nao)
$pom = Join-Path $root 'pom.xml'
$p = [IO.File]::ReadAllText($pom, $utf8)
if ($p.Contains('jacoco')) { throw 'pom.xml da co jacoco, dung lai.' }
$nl = "`n"; if ($p.Contains("`r`n")) { $nl = "`r`n" }

$propAnchor = '    <jjwt.version>0.12.5</jjwt.version>' + $nl + '  </properties>'
if (-not $p.Contains($propAnchor)) { throw 'Khong thay neo <jjwt.version> / </properties> trong pom.xml' }
$propAdd = '    <jjwt.version>0.12.5</jjwt.version>' + $nl +
  '    <jacoco.version>0.8.12</jacoco.version>' + $nl +
  '    <!-- SonarQube: xem docs/SONARQUBE.md. DTO, entity, config chi chua du lieu/cau hinh nen khong tinh vao do phu test -->' + $nl +
  '    <sonar.projectKey>gym-management-system</sonar.projectKey>' + $nl +
  '    <sonar.projectName>GMS Backend</sonar.projectName>' + $nl +
  '    <sonar.coverage.exclusions>**/dto/**,**/entity/**,**/config/**,**/GymManagementApplication.java</sonar.coverage.exclusions>' + $nl +
  '  </properties>'
$p = $p.Replace($propAnchor, $propAdd)

$plugAnchor = '    </plugins>' + $nl + '  </build>'
if (-not $p.Contains($plugAnchor)) { throw 'Khong thay neo </plugins> / </build> trong pom.xml' }
$plugAdd = '      <plugin>' + $nl +
  '        <groupId>org.jacoco</groupId>' + $nl +
  '        <artifactId>jacoco-maven-plugin</artifactId>' + $nl +
  '        <version>${jacoco.version}</version>' + $nl +
  '        <executions>' + $nl +
  '          <execution>' + $nl +
  '            <id>prepare-agent</id>' + $nl +
  '            <goals>' + $nl +
  '              <goal>prepare-agent</goal>' + $nl +
  '            </goals>' + $nl +
  '          </execution>' + $nl +
  '          <execution>' + $nl +
  '            <id>report</id>' + $nl +
  '            <phase>test</phase>' + $nl +
  '            <goals>' + $nl +
  '              <goal>report</goal>' + $nl +
  '            </goals>' + $nl +
  '          </execution>' + $nl +
  '        </executions>' + $nl +
  '      </plugin>' + $nl +
  '    </plugins>' + $nl + '  </build>'
$p = $p.Replace($plugAnchor, $plugAdd)
[IO.File]::WriteAllText($pom, $p, $utf8)
Write-Host '  ~ pom.xml' -ForegroundColor Green

# ---- File moi (LF, UTF-8; .ps1 co BOM)
foreach ($rel in $files.Keys) {
  $path = Join-Path $root $rel
  $dir = Split-Path $path -Parent
  if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
  $text = ($files[$rel] -replace "`r`n", "`n") + "`n"
  $enc = New-Object System.Text.UTF8Encoding($bomFiles.ContainsKey($rel))
  [IO.File]::WriteAllText($path, $text, $enc)
  Write-Host ("  + " + $rel) -ForegroundColor Green
}

Write-Host ''
Write-Host 'Xong. Kiem tra: git diff (pom.xml: them 6 dong o properties va 20 dong plugin), git status --short (1 file M, 4 file moi).'
