param(
  [Parameter(Mandatory)][string]$PreviousEnvFile,
  [string]$PublicUrl,
  [string]$RestoreBackupName,
  [string]$BackupDirectory = 'backups',
  [switch]$ConfirmDataRestore
)

. (Join-Path $PSScriptRoot 'production-common.ps1')

& (Join-Path $PSScriptRoot 'check-production-readiness.ps1') `
  -Strict -ProductionEnvFile $PreviousEnvFile -ValidateSecrets
if ($LASTEXITCODE -ne 0) {
  throw 'Previous release environment failed readiness checks'
}

Write-Host '[rollback] Restoring previous application image versions'
Invoke-ProductionCompose -EnvFile $PreviousEnvFile -Arguments @(
  'pull', 'backend', 'frontend', 'gateway'
)
Invoke-ProductionCompose -EnvFile $PreviousEnvFile -Arguments @(
  'up', '-d', '--no-deps', 'backend', 'frontend', 'gateway'
)

if ($RestoreBackupName) {
  if (-not $ConfirmDataRestore) {
    throw 'Data restore requested without -ConfirmDataRestore'
  }
  & (Join-Path $PSScriptRoot 'restore-production.ps1') `
    -EnvFile $PreviousEnvFile `
    -BackupDirectory $BackupDirectory `
    -BackupName $RestoreBackupName `
    -ConfirmRestore
}

& (Join-Path $PSScriptRoot 'check-production-health.ps1') `
  -EnvFile $PreviousEnvFile -PublicUrl $PublicUrl
Write-Host '[rollback] Production rollback completed.'
