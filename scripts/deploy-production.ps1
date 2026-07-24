param(
  [string]$EnvFile = '.env.production',
  [string]$BackupDirectory = 'backups',
  [switch]$SkipBackup,
  [string]$PublicUrl
)

. (Join-Path $PSScriptRoot 'production-common.ps1')

& (Join-Path $PSScriptRoot 'check-production-readiness.ps1') `
  -Strict -ProductionEnvFile $EnvFile -ValidateSecrets
if ($LASTEXITCODE -ne 0) {
  throw 'Production readiness check failed'
}

if (-not $SkipBackup) {
  & (Join-Path $PSScriptRoot 'backup-production.ps1') `
    -EnvFile $EnvFile -BackupDirectory $BackupDirectory
}

Write-Host '[deploy] Pulling versioned production images'
Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('pull')

Write-Host '[deploy] Applying production release'
Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @(
  'up', '-d', '--remove-orphans'
)

& (Join-Path $PSScriptRoot 'check-production-health.ps1') `
  -EnvFile $EnvFile -PublicUrl $PublicUrl
Write-Host '[deploy] Production deployment completed.'
