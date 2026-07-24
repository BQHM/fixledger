param(
  [switch]$Strict,
  [string]$ProductionEnvFile = '.env.production.example',
  [switch]$ValidateSecrets
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$checks = New-Object System.Collections.Generic.List[object]

function Add-Check {
  param([string]$Name, [bool]$Passed, [string]$Detail)
  $checks.Add([pscustomobject]@{ Name = $Name; Passed = $Passed; Detail = $Detail })
}

function Resolve-RepoPath {
  param([string]$Path)
  if ([System.IO.Path]::IsPathRooted($Path)) {
    return [System.IO.Path]::GetFullPath($Path)
  }
  return [System.IO.Path]::GetFullPath((Join-Path $repoRoot $Path))
}

function Read-EnvironmentFile {
  param([string]$Path)
  $values = @{}
  foreach ($line in Get-Content -LiteralPath $Path) {
    $trimmed = $line.Trim()
    if (-not $trimmed -or $trimmed.StartsWith('#')) { continue }
    $separator = $trimmed.IndexOf('=')
    if ($separator -gt 0) {
      $values[$trimmed.Substring(0, $separator).Trim()] = `
        $trimmed.Substring($separator + 1).Trim().Trim('"').Trim("'")
    }
  }
  return $values
}

function Test-StrongSecret {
  param([string]$Value, [int]$MinimumLength)
  if (-not $Value -or $Value.Length -lt $MinimumLength) { return $false }
  return $Value -notmatch '(?i)replace-with|change-me|example|fixledger123|root_password'
}

$requiredFiles = @(
  'docker-compose.yml',
  'docker-compose.prod.yml',
  '.env.example',
  '.env.production.example',
  '.github/workflows/ci.yml',
  'backend/pom.xml',
  'backend/Dockerfile',
  'backend/src/main/resources/application-prod.yml',
  'backend/src/main/resources/db/migration/V1__baseline_schema.sql',
  'backend/src/main/resources/db/migration/V2__complete_notification_and_dashboard_indexes.sql',
  'deploy/nginx/fixledger.prod.conf',
  'deploy/nginx/security-headers.conf',
  'scripts/backup-production.ps1',
  'scripts/restore-production.ps1',
  'scripts/deploy-production.ps1',
  'scripts/rollback-production.ps1',
  'scripts/check-production-health.ps1'
)
foreach ($relativePath in $requiredFiles) {
  $resolvedPath = Join-Path $repoRoot $relativePath
  Add-Check "Required file: $relativePath" `
    (Test-Path -LiteralPath $resolvedPath -PathType Leaf) $resolvedPath
}

$envPath = Resolve-RepoPath $ProductionEnvFile
Add-Check 'Production environment file exists' `
  (Test-Path -LiteralPath $envPath -PathType Leaf) $envPath

$environment = @{}
if (Test-Path -LiteralPath $envPath -PathType Leaf) {
  $environment = Read-EnvironmentFile $envPath
}
$requiredVariables = @(
  'APP_DOMAIN', 'BACKEND_IMAGE', 'FRONTEND_IMAGE', 'MYSQL_IMAGE', 'REDIS_IMAGE',
  'RUSTFS_IMAGE', 'GATEWAY_IMAGE', 'BACKUP_TOOLS_IMAGE', 'MYSQL_USERNAME',
  'MYSQL_PASSWORD', 'MYSQL_ROOT_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET',
  'FILE_S3_ACCESS_KEY', 'FILE_S3_SECRET_KEY', 'TLS_CERTIFICATE_DIR'
)
foreach ($name in $requiredVariables) {
  Add-Check "Production variable: $name" $environment.ContainsKey($name) $envPath
}

$compose = $null
if (Test-Path -LiteralPath $envPath -PathType Leaf) {
  try {
    $composeJson = & docker compose --env-file $envPath `
      -f (Join-Path $repoRoot 'docker-compose.prod.yml') `
      --profile tools config --format json 2>$null
    if ($LASTEXITCODE -ne 0) { throw 'docker compose config failed' }
    $compose = $composeJson | ConvertFrom-Json
    Add-Check 'Production Compose renders' $true 'docker compose config --format json'
  } catch {
    Add-Check 'Production Compose renders' $false $_.Exception.Message
  }
}

if ($compose) {
  $publishedServices = @()
  foreach ($serviceProperty in $compose.services.PSObject.Properties) {
    if ($serviceProperty.Value.ports.Count -gt 0) {
      $publishedServices += $serviceProperty.Name
    }
  }
  Add-Check 'Only gateway publishes host ports' `
    ($publishedServices.Count -eq 1 -and $publishedServices[0] -eq 'gateway') `
    ($publishedServices -join ', ')

  $gatewayTargets = @($compose.services.gateway.ports | ForEach-Object { $_.target })
  Add-Check 'Gateway publishes only 80 and 443' `
    ($gatewayTargets.Count -eq 2 -and 80 -in $gatewayTargets -and 443 -in $gatewayTargets) `
    ($gatewayTargets -join ', ')
  Add-Check 'Backend uses prod profile' `
    ($compose.services.backend.environment.SPRING_PROFILES_ACTIVE -eq 'prod') `
    $compose.services.backend.environment.SPRING_PROFILES_ACTIVE
  Add-Check 'Data network is internal' ($compose.networks.data.internal -eq $true) 'data'

  $images = @($compose.services.PSObject.Properties.Value.image)
  $floatingImages = @($images | Where-Object { $_ -match '(?i):latest$' })
  Add-Check 'Production images do not use latest' ($floatingImages.Count -eq 0) `
    ($floatingImages -join ', ')
}

$prodConfig = Get-Content -LiteralPath `
  (Join-Path $repoRoot 'backend/src/main/resources/application-prod.yml') -Raw
$backendDockerfile = Get-Content -LiteralPath `
  (Join-Path $repoRoot 'backend/Dockerfile') -Raw
Add-Check 'Backend image defaults to prod profile' `
  ($backendDockerfile -match 'SPRING_PROFILES_ACTIVE=prod') 'backend/Dockerfile'
Add-Check 'Production SQL initialization is disabled' `
  ($prodConfig -match '(?ms)sql:\s+init:\s+mode:\s+never') 'application-prod.yml'
Add-Check 'Production Flyway is enabled' `
  ($prodConfig -match '(?ms)flyway:\s+enabled:\s+true') 'application-prod.yml'
Add-Check 'Production Swagger is disabled' `
  ($prodConfig -match '(?ms)swagger-ui:\s+enabled:\s+false') 'application-prod.yml'
Add-Check 'Production graceful shutdown is enabled' `
  ($prodConfig -match '(?ms)shutdown:\s+graceful') 'application-prod.yml'

$migrationSql = Get-Content -LiteralPath @(
  (Join-Path $repoRoot 'backend/src/main/resources/db/migration/V1__baseline_schema.sql'),
  (Join-Path $repoRoot `
    'backend/src/main/resources/db/migration/V2__complete_notification_and_dashboard_indexes.sql')
) -Raw
Add-Check 'Flyway conditional columns use MySQL-compatible DDL' `
  (-not ($migrationSql -match '(?i)ADD\s+COLUMN\s+IF\s+NOT\s+EXISTS')) `
  'db/migration'

$nginxConfig = Get-Content -LiteralPath `
  (Join-Path $repoRoot 'deploy/nginx/fixledger.prod.conf') -Raw
$securityHeaders = Get-Content -LiteralPath `
  (Join-Path $repoRoot 'deploy/nginx/security-headers.conf') -Raw
Add-Check 'Gateway redirects HTTP to HTTPS' `
  ($nginxConfig.Contains('return 301 https://$host$request_uri')) 'fixledger.prod.conf'
Add-Check 'Gateway enables HSTS' `
  ($securityHeaders.Contains('Strict-Transport-Security')) 'security-headers.conf'
Add-Check 'Gateway blocks Actuator' `
  ($nginxConfig -match 'location \^~ /actuator/') 'fixledger.prod.conf'

if ($ValidateSecrets) {
  Add-Check 'JWT secret is strong' `
    (Test-StrongSecret $environment['JWT_SECRET'] 32) 'JWT_SECRET'
  foreach ($name in @('MYSQL_PASSWORD', 'MYSQL_ROOT_PASSWORD', 'REDIS_PASSWORD')) {
    Add-Check "$name is strong" `
      (Test-StrongSecret $environment[$name] 16) $name
  }
  Add-Check 'Object storage secret is strong' `
    (Test-StrongSecret $environment['FILE_S3_SECRET_KEY'] 16) 'FILE_S3_SECRET_KEY'
  Add-Check 'Object storage access key is not an example value' `
    (Test-StrongSecret $environment['FILE_S3_ACCESS_KEY'] 8) 'FILE_S3_ACCESS_KEY'
  $independentSecrets = @(
    $environment['MYSQL_PASSWORD'],
    $environment['MYSQL_ROOT_PASSWORD'],
    $environment['REDIS_PASSWORD'],
    $environment['FILE_S3_SECRET_KEY']
  )
  Add-Check 'Infrastructure secrets are not reused' `
    (@($independentSecrets | Select-Object -Unique).Count -eq $independentSecrets.Count) `
    'MYSQL/Redis/object storage'
  Add-Check 'Production domain is not an example domain' `
    ($environment['APP_DOMAIN'] -notmatch '(?i)example\.(com|org|net)$') 'APP_DOMAIN'
  foreach ($imageName in @('BACKEND_IMAGE', 'FRONTEND_IMAGE', 'RUSTFS_IMAGE')) {
    $image = $environment[$imageName]
    Add-Check "$imageName is deployable" `
      ($image -and $image -notmatch '(?i)example|latest|version_or_digest') $imageName
  }
  if ($environment['TLS_CERTIFICATE_DIR']) {
    $certificateDirectory = Resolve-RepoPath $environment['TLS_CERTIFICATE_DIR']
    Add-Check 'TLS full chain exists' `
      (Test-Path -LiteralPath (Join-Path $certificateDirectory 'fullchain.pem')) `
      $certificateDirectory
    Add-Check 'TLS private key exists' `
      (Test-Path -LiteralPath (Join-Path $certificateDirectory 'privkey.pem')) `
      $certificateDirectory
  }
}

$failed = @($checks | Where-Object { -not $_.Passed })
foreach ($check in $checks) {
  $status = if ($check.Passed) { 'PASS' } else { 'FAIL' }
  Write-Host "[$status] $($check.Name) - $($check.Detail)"
}

if ($failed.Count -gt 0) {
  Write-Host "`nProduction readiness check failed: $($failed.Count) item(s)."
  if ($Strict) { exit 1 }
} else {
  Write-Host "`nProduction readiness check passed."
}
