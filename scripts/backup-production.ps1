param(
  [string]$EnvFile = '.env.production',
  [string]$BackupDirectory = 'backups',
  [string]$BackupName = (Get-Date -Format 'yyyyMMdd-HHmmss')
)

. (Join-Path $PSScriptRoot 'production-common.ps1')

if ($BackupName -notmatch '^[A-Za-z0-9._-]+$') {
  throw 'BackupName contains unsupported characters'
}

$resolvedBackupDirectory = Resolve-ProductionPath $BackupDirectory
$batchDirectory = Join-Path $resolvedBackupDirectory $BackupName
if (Test-Path -LiteralPath $batchDirectory) {
  throw "Backup batch already exists: $batchDirectory"
}
New-Item -ItemType Directory -Force -Path $batchDirectory | Out-Null

$previousBackupDirectory = $env:BACKUP_DIR
$env:BACKUP_DIR = $resolvedBackupDirectory
$servicesStopped = $false

try {
  Write-Host "[backup] Entering maintenance window for batch $BackupName"
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('stop', 'gateway')
  $servicesStopped = $true
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('stop', 'backend')
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('stop', 'rustfs')

  $mysqlCommand = @"
set -eu
mkdir -p '/backups/$BackupName'
MYSQL_PWD="`$MYSQL_PASSWORD" mysqldump \
  --user="`$MYSQL_USER" \
  --single-transaction \
  --routines \
  --triggers \
  --set-gtid-purged=OFF \
  "`$MYSQL_DATABASE" > '/backups/$BackupName/mysql.sql'
"@
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @(
    'exec', '-T', 'mysql', 'sh', '-c', $mysqlCommand
  )

  $rustfsCommand = "tar -czf '/backups/$BackupName/rustfs-data.tar.gz' " +
    "-C /rustfs-data ."
  Invoke-ProductionCompose -EnvFile $EnvFile -ToolsProfile -Arguments @(
    'run', '--rm', 'backup-tools', 'sh', '-c', $rustfsCommand
  )

  $images = & docker compose --env-file (Resolve-ProductionPath $EnvFile) `
    -f $script:ProductionComposeFile config --images
  if ($LASTEXITCODE -ne 0) {
    throw 'Unable to capture production image list'
  }
  $manifest = [ordered]@{
    backupName = $BackupName
    createdAt = (Get-Date).ToUniversalTime().ToString('o')
    gitRevision = (& git -C $script:ProductionRepoRoot rev-parse HEAD)
    images = @($images)
  }
  $manifest | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath `
    (Join-Path $batchDirectory 'manifest.json') -Encoding UTF8

  $hashes = Get-FileHash -Algorithm SHA256 -LiteralPath @(
    (Join-Path $batchDirectory 'mysql.sql'),
    (Join-Path $batchDirectory 'rustfs-data.tar.gz')
  )
  $hashes | ForEach-Object {
    "$($_.Hash)  $([System.IO.Path]::GetFileName($_.Path))"
  } | Set-Content -LiteralPath (Join-Path $batchDirectory 'SHA256SUMS') -Encoding ASCII

  Write-Host "[backup] Backup completed: $batchDirectory"
} finally {
  if ($servicesStopped) {
    Write-Host '[backup] Leaving maintenance window'
    Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @(
      'up', '-d', 'rustfs', 'backend', 'frontend', 'gateway'
    )
  }
  $env:BACKUP_DIR = $previousBackupDirectory
}
