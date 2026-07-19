param(
  [switch] $Strict
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$checks = New-Object System.Collections.Generic.List[object]

function Add-Check {
  param(
    [string] $Name,
    [bool] $Passed,
    [string] $Detail
  )
  $checks.Add([pscustomobject]@{
    Name = $Name
    Passed = $Passed
    Detail = $Detail
  })
}

function Test-FileContains {
  param(
    [string] $Path,
    [string] $Pattern
  )
  if (-not (Test-Path -LiteralPath $Path)) {
    return $false
  }
  $content = Get-Content -LiteralPath $Path -Raw
  return $content.Contains($Pattern)
}

$composePath = Join-Path $repoRoot 'docker-compose.yml'
$envExamplePath = Join-Path $repoRoot '.env.example'
$ciPath = Join-Path $repoRoot '.github/workflows/ci.yml'
$frontendPackagePath = Join-Path $repoRoot 'frontend/package.json'
$backendPomPath = Join-Path $repoRoot 'backend/pom.xml'

Add-Check 'Docker Compose 文件存在' (Test-Path -LiteralPath $composePath) $composePath
Add-Check '.env.example 文件存在' (Test-Path -LiteralPath $envExamplePath) $envExamplePath
Add-Check 'CI 工作流存在' (Test-Path -LiteralPath $ciPath) $ciPath
Add-Check '前端构建脚本存在' (Test-FileContains $frontendPackagePath '"build"') $frontendPackagePath
Add-Check '前端 smoke 脚本存在' (Test-FileContains $frontendPackagePath '"smoke"') $frontendPackagePath
Add-Check '后端 JDK 21 配置存在' (Test-FileContains $backendPomPath '<java.version>21</java.version>') $backendPomPath
Add-Check 'CI 支持手动触发' (Test-FileContains $ciPath 'workflow_dispatch') $ciPath
Add-Check 'CI 执行后端测试' (Test-FileContains $ciPath 'mvn -q test') $ciPath
Add-Check 'CI 执行前端 smoke' (Test-FileContains $ciPath 'npm run smoke') $ciPath

$envContent = if (Test-Path -LiteralPath $envExamplePath) {
  Get-Content -LiteralPath $envExamplePath -Raw
} else {
  ''
}
Add-Check '环境模板包含 JWT 配置' ($envContent.Contains('JWT') -or $envContent.Contains('FIXLEDGER_JWT')) $envExamplePath
Add-Check '环境模板包含数据库配置' ($envContent.Contains('MYSQL') -or $envContent.Contains('DB_')) $envExamplePath

$failed = $checks | Where-Object { -not $_.Passed }
foreach ($check in $checks) {
  $status = if ($check.Passed) { 'PASS' } else { 'FAIL' }
  Write-Host "[$status] $($check.Name) - $($check.Detail)"
}

if ($failed.Count -gt 0) {
  Write-Host ''
  Write-Host "Production readiness check failed: $($failed.Count) item(s)."
  if ($Strict) {
    exit 1
  }
}

if ($failed.Count -eq 0) {
  Write-Host ''
  Write-Host 'Production readiness check passed.'
}
