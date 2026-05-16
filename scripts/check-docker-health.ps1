param(
  [switch]$DryRun,
  [string]$FrontendUrl = "http://localhost:5173",
  [string]$BackendHealthUrl = "http://localhost:8080/actuator/health"
)

$ErrorActionPreference = "Stop"

$composeServices = @(
  "mysql",
  "redis",
  "rustfs",
  "backend",
  "frontend"
)

function Write-Step {
  param([string]$Message)
  Write-Host "[check] $Message"
}

function Test-CommandAvailable {
  param([string]$CommandName)
  if (-not (Get-Command $CommandName -ErrorAction SilentlyContinue)) {
    throw "Command not found: $CommandName"
  }
}

function Invoke-HttpCheck {
  param(
    [string]$Name,
    [string]$Url,
    [string]$ExpectedText
  )

  Write-Step "Checking $Name at $Url"
  $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 10
  if ($response.StatusCode -lt 200 -or $response.StatusCode -ge 400) {
    throw "$Name returned HTTP $($response.StatusCode)"
  }
  if ($ExpectedText -and $response.Content -notlike "*$ExpectedText*") {
    throw "$Name response did not contain expected text: $ExpectedText"
  }
}

Write-Step "Validating Docker Compose config"
Test-CommandAvailable "docker"
docker compose config --quiet

if ($DryRun) {
  Write-Step "Dry run complete. Skipped container and HTTP checks."
  Write-Step "Would check services: $($composeServices -join ', ')"
  Write-Step "Would check frontend: $FrontendUrl"
  Write-Step "Would check backend: $BackendHealthUrl"
  exit 0
}

Write-Step "Checking Docker Compose service containers"
foreach ($service in $composeServices) {
  $containerId = docker compose ps -q $service
  if (-not $containerId) {
    throw "Service is not running: $service"
  }
  $state = docker inspect -f "{{.State.Status}}" $containerId
  if ($state -ne "running") {
    throw "Service $service is $state"
  }
  Write-Step "$service is running"
}

Invoke-HttpCheck -Name "frontend" -Url $FrontendUrl -ExpectedText "id=\"app\""
Invoke-HttpCheck -Name "backend health" -Url $BackendHealthUrl -ExpectedText "UP"

Write-Step "Docker health check passed."
