param(
  [Parameter(Mandatory)][string]$BackupName,
  [string]$EnvFile = '.env.production',
  [string]$BackupDirectory = 'backups',
  [switch]$ConfirmRestore
)

. (Join-Path $PSScriptRoot 'production-common.ps1')

if (-not $ConfirmRestore) {
  throw 'Restore is destructive. Re-run with -ConfirmRestore after verifying the backup.'
}
if ($BackupName -notmatch '^[A-Za-z0-9._-]+$') {
  throw 'BackupName contains unsupported characters'
}

$environment = Get-ProductionEnvironment $EnvFile
Assert-ProductionIdentifier 'MYSQL_DATABASE' $environment['MYSQL_DATABASE']
Assert-ProductionIdentifier 'MYSQL_USERNAME' $environment['MYSQL_USERNAME']

$resolvedBackupDirectory = Resolve-ProductionPath $BackupDirectory
$batchDirectory = Join-Path $resolvedBackupDirectory $BackupName
$mysqlBackup = Join-Path $batchDirectory 'mysql.sql'
$rustfsBackup = Join-Path $batchDirectory 'rustfs-data.tar.gz'
$checksumFile = Join-Path $batchDirectory 'SHA256SUMS'
foreach ($requiredFile in @($mysqlBackup, $rustfsBackup, $checksumFile)) {
  if (-not (Test-Path -LiteralPath $requiredFile -PathType Leaf)) {
    throw "Backup file not found: $requiredFile"
  }
}

Push-Location $batchDirectory
try {
  $expectedHashes = Get-Content -LiteralPath $checksumFile
  foreach ($entry in $expectedHashes) {
    $parts = $entry -split '\s+', 2
    $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $parts[1]).Hash
    if ($actual -ne $parts[0]) {
      throw "Backup checksum mismatch: $($parts[1])"
    }
  }
} finally {
  Pop-Location
}

$previousBackupDirectory = $env:BACKUP_DIR
$env:BACKUP_DIR = $resolvedBackupDirectory
$servicesStopped = $false

try {
  Write-Host "[restore] Stopping application services for batch $BackupName"
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('stop', 'gateway')
  $servicesStopped = $true
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('stop', 'backend')
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('stop', 'frontend')
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('stop', 'rustfs')

  $resetDatabaseCommand = @'
set -eu
MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --user=root <<SQL
DROP DATABASE IF EXISTS \`$MYSQL_DATABASE\`;
CREATE DATABASE \`$MYSQL_DATABASE\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
GRANT ALL PRIVILEGES ON \`$MYSQL_DATABASE\`.* TO '$MYSQL_USER'@'%';
FLUSH PRIVILEGES;
SQL
'@
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @(
    'exec', '-T', 'mysql', 'sh', '-c', $resetDatabaseCommand
  )

  $importDatabaseCommand = @"
set -eu
MYSQL_PWD="`$MYSQL_ROOT_PASSWORD" mysql --user=root \
  "`$MYSQL_DATABASE" < '/backups/$BackupName/mysql.sql'
"@
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @(
    'exec', '-T', 'mysql', 'sh', '-c', $importDatabaseCommand
  )

  $restoreRustfsCommand = @"
set -eu
find /rustfs-data -mindepth 1 -maxdepth 1 -exec rm -rf {} +
tar -xzf '/backups/$BackupName/rustfs-data.tar.gz' -C /rustfs-data
"@
  Invoke-ProductionCompose -EnvFile $EnvFile -ToolsProfile -Arguments @(
    'run', '--rm', 'backup-tools', 'sh', '-c', $restoreRustfsCommand
  )

  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @('up', '-d', 'redis')
  $clearRedisCommand = 'redis-cli -a "$REDIS_PASSWORD" ' +
    '-n "$REDIS_DATABASE" FLUSHDB >/dev/null'
  Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @(
    'exec', '-T', 'redis', 'sh', '-c', $clearRedisCommand
  )

  Write-Host "[restore] Restore completed: $batchDirectory"
} finally {
  if ($servicesStopped) {
    Invoke-ProductionCompose -EnvFile $EnvFile -Arguments @(
      'up', '-d', 'rustfs', 'backend', 'frontend', 'gateway'
    )
  }
  $env:BACKUP_DIR = $previousBackupDirectory
}
