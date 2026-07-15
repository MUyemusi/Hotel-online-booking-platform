$ErrorActionPreference = "Stop"

$backendDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$logDir = Join-Path $backendDir "target"
$stdoutLog = Join-Path $logDir "backend-verify.out.log"
$stderrLog = Join-Path $logDir "backend-verify.err.log"

if (!(Test-Path -LiteralPath $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

$securePassword = Read-Host "Enter MySQL root password for backend verification" -AsSecureString
$passwordPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringUni($passwordPtr)

$env:DB_HOST = "127.0.0.1"
$env:DB_PORT = "3306"
$env:DB_NAME = "zhisu_hotel"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = $plainPassword

Write-Host "Starting Spring Boot on http://127.0.0.1:8080 ..."
$process = Start-Process -FilePath "mvn" `
    -ArgumentList @("spring-boot:run", "-Dspring-boot.run.profiles=prod") `
    -WorkingDirectory $backendDir `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -PassThru `
    -WindowStyle Hidden

try {
    $deadline = (Get-Date).AddSeconds(90)
    $healthy = $false
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Seconds 3
        if ($process.HasExited) {
            throw "Spring Boot exited early with code $($process.ExitCode). See $stdoutLog and $stderrLog"
        }

        try {
            $response = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/health" -TimeoutSec 3
            Write-Host "Health check OK:"
            $response | ConvertTo-Json -Compress
            $healthy = $true
            break
        } catch {
            Write-Host "Waiting for backend..."
        }
    }

    if (!$healthy) {
        throw "Backend did not become healthy within 90 seconds. See $stdoutLog and $stderrLog"
    }
} finally {
    Write-Host "Stopping Spring Boot verification process..."
    $toStop = New-Object System.Collections.Generic.List[int]
    $queue = New-Object System.Collections.Generic.Queue[int]
    $queue.Enqueue($process.Id)

    while ($queue.Count -gt 0) {
        $parentId = $queue.Dequeue()
        $children = Get-CimInstance Win32_Process -Filter "ParentProcessId = $parentId" -ErrorAction SilentlyContinue
        foreach ($child in $children) {
            $childId = [int]$child.ProcessId
            $toStop.Add($childId)
            $queue.Enqueue($childId)
        }
    }

    $toStopArray = $toStop.ToArray()
    [array]::Reverse($toStopArray)
    foreach ($processId in $toStopArray) {
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
    Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    Remove-Item Env:\DB_PASSWORD -ErrorAction SilentlyContinue
    if ($passwordPtr -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPtr)
    }
}

Write-Host "Backend verification completed. Logs:"
Write-Host $stdoutLog
Write-Host $stderrLog
