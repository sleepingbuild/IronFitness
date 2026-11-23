#requires -Version 5.1
<#
 apply_missing_param_fix.ps1 (Agent 1)
 Sua GlobalExceptionHandler: thieu tham so bat buoc tren query string tra 400 VALIDATION_ERROR (hien dang roi xuong 500).
 Them test GlobalExceptionHandlerTest. Sua scripts\demo-smoke.ps1 neu co (them ?date= va 1 kiem tra thieu tham so -> 400).
 Chay o goc repo, tren nhanh moi tao tu origin/master:
   git fetch origin --prune
   git switch -c fix/agent1-missing-param-400 --no-track origin/master
   & D:\AgentChange\Agent1\apply_missing_param_fix.ps1
#>
$ErrorActionPreference = 'Stop'
if (-not (Test-Path 'pom.xml')) { throw 'Chay o goc repo (khong thay pom.xml).' }
$dirty = @(git status --short --untracked-files=no)
if ($dirty.Count -gt 0) { throw ('Cay lam viec co thay doi chua commit: ' + ($dirty -join '; ')) }
if ((git branch --show-current) -eq 'master') { throw 'Dang o nhanh master. Tao nhanh moi truoc.' }

$utf8 = New-Object System.Text.UTF8Encoding($false)
$root = (Get-Location).Path

# ---- 1. GlobalExceptionHandler
$p = Join-Path $root 'src\main\java\com\gym\management\exception\GlobalExceptionHandler.java'
$s = [IO.File]::ReadAllText($p, $utf8)
if ($s.Contains('MissingServletRequestParameterException')) { throw 'Da co MissingServletRequestParameterException, dung lai.' }
$impAnchor = 'import org.springframework.web.bind.MethodArgumentNotValidException;'
$methAnchor = '    @ExceptionHandler(Exception.class)'
if (-not $s.Contains($impAnchor)) { throw 'Khong thay dong neo import' }
if (-not $s.Contains($methAnchor)) { throw 'Khong thay dong neo @ExceptionHandler(Exception.class)' }
$nl = "`n"; if ($s.Contains("`r`n")) { $nl = "`r`n" }

$handler = @'
    /** Thiếu tham số bắt buộc trên query string (vd: thiếu ?date=). Trả 400 thay vì để rơi xuống 500. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("Thiếu tham số bắt buộc '{}'", ex.getParameterName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Thiếu tham số bắt buộc '" + ex.getParameterName() + "'", ErrorCode.VALIDATION_ERROR));
    }

'@
$handler = ($handler -replace "`r`n", "`n").Replace("`n", $nl)

$s = $s.Replace($impAnchor, $impAnchor + $nl + 'import org.springframework.web.bind.MissingServletRequestParameterException;')
$s = $s.Replace($methAnchor, $handler + $methAnchor)
[IO.File]::WriteAllText($p, $s, $utf8)
Write-Host '  ~ GlobalExceptionHandler.java' -ForegroundColor Green

# ---- 2. Test
$tdir = Join-Path $root 'src\test\java\com\gym\management\exception'
$tfile = Join-Path $tdir 'GlobalExceptionHandlerTest.java'
if (Test-Path $tfile) { throw 'GlobalExceptionHandlerTest.java da ton tai, dung lai.' }
if (-not (Test-Path $tdir)) { New-Item -ItemType Directory -Path $tdir | Out-Null }
$test = @'
package com.gym.management.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingRequestParameter_returns400WithValidationErrorCode() {
        ResponseEntity<ApiResponse<Void>> res =
                handler.handleMissingParameter(new MissingServletRequestParameterException("date", "LocalDate"));

        assertEquals(400, res.getStatusCode().value());
        assertFalse(res.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", res.getBody().getErrorCode());
        assertTrue(res.getBody().getMessage().contains("date"));
    }

    @Test
    void unknownException_stillReturns500() {
        ResponseEntity<ApiResponse<Void>> res = handler.handleGenericException(new RuntimeException("boom"));

        assertEquals(500, res.getStatusCode().value());
        assertEquals("INTERNAL_SERVER_ERROR", res.getBody().getErrorCode());
    }
}
'@
[IO.File]::WriteAllText($tfile, (($test -replace "`r`n", "`n").Replace("`n", $nl) + $nl), $utf8)
Write-Host '  + GlobalExceptionHandlerTest.java' -ForegroundColor Green

# ---- 3. Script smoke (khong bat buoc, file co the chua co)
$smoke = Join-Path $root 'scripts\demo-smoke.ps1'
if (Test-Path $smoke) {
  $m = [IO.File]::ReadAllText($smoke)
  $old = 'Check $w $t ''/api/v1/trainer-schedules/trainers'' 200'
  if ($m.Contains($old)) {
    $snl = "`n"; if ($m.Contains("`r`n")) { $snl = "`r`n" }
    $new = 'Check $w $t "/api/v1/trainer-schedules/trainers?date=$today" 200' + $snl + '  Check $w $t ''/api/v1/trainer-schedules/trainers'' 400'
    $enc = New-Object System.Text.UTF8Encoding($true)
    [IO.File]::WriteAllText($smoke, $m.Replace($old, $new), $enc)
    Write-Host '  ~ scripts\demo-smoke.ps1' -ForegroundColor Green
  } else {
    Write-Host '  = demo-smoke.ps1: khong thay dong can sua (da sua roi?), bo qua' -ForegroundColor Yellow
  }
}

Write-Host ''
Write-Host 'Xong. Kiem tra: git diff (handler: 1 import + 1 method), git status --short (1 file M, 1 file moi).'
