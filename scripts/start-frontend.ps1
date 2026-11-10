$ErrorActionPreference = 'Stop'
& python (Join-Path $PSScriptRoot 'prepare-runtime.py')
if ($LASTEXITCODE -ne 0) { throw 'Could not prepare frontend source.' }
$olsRuntime = (Get-Item (Join-Path $env:TEMP 'onlinelearn-build\frontend')).FullName
$olsVite = Join-Path $olsRuntime 'node_modules\.bin\vite.cmd'

$olsProcess = Get-CimInstance Win32_Process -Filter "name='node.exe'" |
  Where-Object { $_.CommandLine -match 'onlinelearn-build\\frontend\\node_modules\\.*vite' -and $_.CommandLine -match '--port 5173' } |
  Select-Object -First 1
$olsHealthy = $false
try {
  $olsResponse = Invoke-WebRequest 'http://127.0.0.1:5173/@vite/client' -UseBasicParsing -TimeoutSec 3
  $olsHealthy = $olsResponse.Headers['Content-Type'] -like 'text/javascript*'
} catch { }
if ($olsProcess) {
  if ($olsHealthy) {
    Write-Host 'OnlineLearn frontend is already running at http://127.0.0.1:5173/'
    return
  }
  Write-Host 'Restarting the existing OnlineLearn Vite process with the full Windows path.'
  Stop-Process -Id $olsProcess.ProcessId
  Start-Sleep -Milliseconds 500
} elseif ($olsHealthy) {
  throw 'Port 5173 belongs to another application. Close that application before starting OnlineLearn.'
}
Push-Location $olsRuntime
try {
  if (-not (Test-Path $olsVite)) {
    & npm ci --include=dev --prefer-offline --no-audit --no-fund --fetch-retries=1 --fetch-timeout=15000
    if ($LASTEXITCODE -ne 0) { throw 'Could not install frontend dependencies.' }
  }
  & $olsVite --host 127.0.0.1 --port 5173 --strictPort
  if ($LASTEXITCODE -ne 0) { throw 'Vite failed to start.' }
} finally { Pop-Location }
