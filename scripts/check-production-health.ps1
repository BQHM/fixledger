param(
  [string]$EnvFile = '.env.production',
  [string]$PublicUrl
)

. (Join-Path $PSScriptRoot 'production-common.ps1')

$environment = Get-ProductionEnvironment $EnvFile
if (-not $PublicUrl) {
  $PublicUrl = "https://$($environment['APP_DOMAIN'])"
}
$PublicUrl = $PublicUrl.TrimEnd('/')

foreach ($service in @('mysql', 'redis', 'rustfs', 'backend', 'frontend', 'gateway')) {
  $containerId = & docker compose --env-file (Resolve-ProductionPath $EnvFile) `
    -f $script:ProductionComposeFile ps -q $service
  if ($LASTEXITCODE -ne 0 -or -not $containerId) {
    throw "Production service is not running: $service"
  }
  $state = & docker inspect -f '{{.State.Status}}' $containerId
  if ($state -ne 'running') {
    throw "Production service $service is $state"
  }
  Write-Host "[health] $service is running"
}

$backendHealth = & docker compose --env-file (Resolve-ProductionPath $EnvFile) `
  -f $script:ProductionComposeFile exec -T gateway `
  wget -q -O - http://backend:8080/actuator/health
if ($LASTEXITCODE -ne 0 -or $backendHealth -notlike '*"status":"UP"*') {
  throw 'Backend health endpoint is not UP inside the production network'
}

$response = Invoke-WebRequest -UseBasicParsing -TimeoutSec 15 -Uri "$PublicUrl/"
if ($response.StatusCode -ne 200 -or $response.Content -notlike '*id="app"*') {
  throw "Public frontend check failed: $PublicUrl/"
}

$swaggerStatus = $null
try {
  Invoke-WebRequest -UseBasicParsing -TimeoutSec 10 -Uri "$PublicUrl/swagger-ui.html"
  $swaggerStatus = 200
} catch {
  $swaggerStatus = [int]$_.Exception.Response.StatusCode
}
if ($swaggerStatus -ne 404) {
  throw "Swagger must return 404 on the public production gateway, got $swaggerStatus"
}

Write-Host '[health] Production health check passed.'
