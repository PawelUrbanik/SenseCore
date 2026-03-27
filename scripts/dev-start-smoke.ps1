param(
    [switch]$NoFrontend,
    [int]$TimeoutSec = 180,
    [int]$IntervalSec = 3
)

$ErrorActionPreference = 'Stop'

function Get-ComposeExecutable {
    if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
        return @{
            FilePath = 'docker-compose'
            BaseArgs = @('-f', 'docker-compose.yaml')
            StopHint = 'docker-compose -f infra/docker-compose.yaml down'
        }
    }

    if (Get-Command docker -ErrorAction SilentlyContinue) {
        return @{
            FilePath = 'docker'
            BaseArgs = @('compose', '-f', 'docker-compose.yaml')
            StopHint = 'docker compose -f infra/docker-compose.yaml down'
        }
    }

    throw 'Neither docker-compose nor docker compose is available in PATH.'
}

function Wait-ForHttpStatus {
    param(
        [string]$Name,
        [string]$Url,
        [int[]]$ExpectedStatusCodes,
        [int]$Timeout,
        [int]$Interval
    )

    $deadline = (Get-Date).AddSeconds($Timeout)
    do {
        try {
            $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec 10
            if ($ExpectedStatusCodes -contains [int]$response.StatusCode) {
                Write-Host "[ok] $Name ready ($($response.StatusCode))"
                return $response
            }
        } catch {
            $statusCode = $null
            if ($_.Exception.Response) {
                $statusCode = [int]$_.Exception.Response.StatusCode.value__
                if ($ExpectedStatusCodes -contains $statusCode) {
                    Write-Host "[ok] $Name ready ($statusCode)"
                    return $null
                }
            }
        }

        Start-Sleep -Seconds $Interval
    } while ((Get-Date) -lt $deadline)

    throw "Timeout waiting for $Name at $Url"
}

function Invoke-SmokeRequest {
    param(
        [string]$Name,
        [string]$Url,
        [int[]]$ExpectedStatusCodes
    )

    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec 15
        $status = [int]$response.StatusCode
    } catch {
        $status = $null
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode.value__
        }
    }

    if ($null -eq $status -or -not ($ExpectedStatusCodes -contains $status)) {
        throw "Smoke check failed for $Name. URL=$Url, Status=$status"
    }

    Write-Host "[smoke] $Name -> $status"
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$infraPath = Join-Path $repoRoot 'infra'
$frontendPath = Join-Path $repoRoot 'frontend/sensecore-web'
$stateDir = Join-Path $repoRoot '.codex-tmp'
$pidFile = Join-Path $stateDir 'frontend-dev.pid'
$compose = Get-ComposeExecutable

if (-not $NoFrontend -and -not (Get-Command npm -ErrorAction SilentlyContinue)) {
    throw 'npm is required but was not found in PATH.'
}

Write-Host '[step] Starting infrastructure and backend services via docker compose...'
Push-Location $infraPath
try {
    & $compose.FilePath @($compose.BaseArgs + @('up', '-d'))
    if ($LASTEXITCODE -ne 0) {
        throw 'docker compose up failed. Check Docker daemon access and compose logs.'
    }
} finally {
    Pop-Location
}

Wait-ForHttpStatus -Name 'nginx health' -Url 'http://localhost:8080/health' -ExpectedStatusCodes @(200) -Timeout $TimeoutSec -Interval $IntervalSec | Out-Null
Wait-ForHttpStatus -Name 'query API devices' -Url 'http://localhost:8080/api/devices' -ExpectedStatusCodes @(200) -Timeout $TimeoutSec -Interval $IntervalSec | Out-Null

$frontendStartedByScript = $false
if (-not $NoFrontend) {
    $frontendAlive = $false
    try {
        $frontendCheck = Invoke-WebRequest -Uri 'http://localhost:4200' -Method GET -TimeoutSec 3
        $frontendAlive = $frontendCheck.StatusCode -eq 200
    } catch {
        $frontendAlive = $false
    }

    if ($frontendAlive) {
        Write-Host '[step] Frontend already running on http://localhost:4200, skipping start.'
    } else {
        if (-not (Test-Path (Join-Path $frontendPath 'node_modules'))) {
            Write-Host '[step] Installing frontend dependencies (npm ci)...'
            Push-Location $frontendPath
            try {
                npm ci
            } finally {
                Pop-Location
            }
        }

        Write-Host '[step] Starting frontend dev server (npm start)...'
        $frontendProcess = Start-Process -FilePath 'npm.cmd' -ArgumentList 'start' -WorkingDirectory $frontendPath -PassThru
        $frontendStartedByScript = $true

        New-Item -ItemType Directory -Path $stateDir -Force | Out-Null
        Set-Content -Path $pidFile -Value $frontendProcess.Id

        Wait-ForHttpStatus -Name 'frontend app' -Url 'http://localhost:4200' -ExpectedStatusCodes @(200) -Timeout $TimeoutSec -Interval $IntervalSec | Out-Null
    }
}

Write-Host '[step] Running smoke tests...'
Invoke-SmokeRequest -Name 'Nginx health endpoint' -Url 'http://localhost:8080/health' -ExpectedStatusCodes @(200)
Invoke-SmokeRequest -Name 'Devices endpoint' -Url 'http://localhost:8080/api/devices' -ExpectedStatusCodes @(200)
Invoke-SmokeRequest -Name 'Latest telemetry endpoint' -Url 'http://localhost:8080/api/telemetry/latest?deviceId=smoke-device&sensorType=temperature' -ExpectedStatusCodes @(200, 404)
Invoke-SmokeRequest -Name 'Telemetry history endpoint' -Url 'http://localhost:8080/api/telemetry/history?deviceId=smoke-device&sensorType=temperature&from=2026-01-01T00:00:00Z&to=2026-12-31T23:59:59Z&limit=5' -ExpectedStatusCodes @(200)

Write-Host ''
Write-Host 'All smoke checks passed.'
Write-Host 'Backend stack is running via docker compose.'
if ($NoFrontend) {
    Write-Host 'Frontend was skipped (NoFrontend mode).'
} elseif ($frontendStartedByScript) {
    Write-Host "Frontend started by script (PID $(Get-Content $pidFile))."
}
Write-Host ''
Write-Host 'To stop backend stack:'
Write-Host "  $($compose.StopHint)"
if (-not $NoFrontend -and (Test-Path $pidFile)) {
    Write-Host 'To stop frontend started by this script:'
    Write-Host "  Stop-Process -Id $(Get-Content $pidFile)"
}
