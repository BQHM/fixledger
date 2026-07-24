$ErrorActionPreference = 'Stop'
$script:ProductionRepoRoot = Split-Path -Parent $PSScriptRoot
$script:ProductionComposeFile = Join-Path $script:ProductionRepoRoot 'docker-compose.prod.yml'

function Resolve-ProductionPath {
  param([Parameter(Mandatory)][string]$Path)

  if ([System.IO.Path]::IsPathRooted($Path)) {
    return [System.IO.Path]::GetFullPath($Path)
  }
  return [System.IO.Path]::GetFullPath((Join-Path $script:ProductionRepoRoot $Path))
}

function Get-ProductionEnvironment {
  param([Parameter(Mandatory)][string]$EnvFile)

  $resolvedPath = Resolve-ProductionPath $EnvFile
  if (-not (Test-Path -LiteralPath $resolvedPath -PathType Leaf)) {
    throw "Production environment file not found: $resolvedPath"
  }

  $values = @{}
  foreach ($line in Get-Content -LiteralPath $resolvedPath) {
    $trimmed = $line.Trim()
    if (-not $trimmed -or $trimmed.StartsWith('#')) {
      continue
    }
    $separator = $trimmed.IndexOf('=')
    if ($separator -le 0) {
      throw "Invalid environment line in ${resolvedPath}: $line"
    }
    $name = $trimmed.Substring(0, $separator).Trim()
    $value = $trimmed.Substring($separator + 1).Trim().Trim('"').Trim("'")
    $values[$name] = $value
  }
  return $values
}

function Invoke-ProductionCompose {
  param(
    [Parameter(Mandatory)][string]$EnvFile,
    [Parameter(Mandatory)][string[]]$Arguments,
    [switch]$ToolsProfile
  )

  $resolvedEnvFile = Resolve-ProductionPath $EnvFile
  $baseArguments = @('compose', '--env-file', $resolvedEnvFile, '-f',
    $script:ProductionComposeFile)
  if ($ToolsProfile) {
    $baseArguments += @('--profile', 'tools')
  }

  & docker @baseArguments @Arguments
  if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose command failed: $($Arguments -join ' ')"
  }
}

function Assert-ProductionIdentifier {
  param(
    [Parameter(Mandatory)][string]$Name,
    [Parameter(Mandatory)][string]$Value
  )

  if ($Value -notmatch '^[A-Za-z0-9_]+$') {
    throw "$Name must contain only letters, numbers, and underscores"
  }
}
