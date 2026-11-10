$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot
$launchMutex = [System.Threading.Mutex]::new($false, 'Local\OnlineLearn-Backend-8801')
try {
  try { $ownsLaunch = $launchMutex.WaitOne(0) }
  catch [System.Threading.AbandonedMutexException] { $ownsLaunch = $true }
  if (-not $ownsLaunch) {
    Write-Host 'OnlineLearn is already running or starting through the launcher on port 8801.'
    return
  }
$config = Join-Path $project '.env.local'
if (Test-Path $config) {
  Get-Content $config | Where-Object { $_ -match '^[A-Z_][A-Z0-9_]*=' } | ForEach-Object {
    $entry = $_ -split '=',2
    [Environment]::SetEnvironmentVariable($entry[0],$entry[1],'Process')
  }
}
$env:PORT = '8801'
$backend = Join-Path $project 'backend'
$listeners = @(netstat -ano -p tcp | ForEach-Object {
  if ($_ -match '^\s*TCP\s+\S+:(8080|8801)\s+\S+\s+LISTENING\s+(\d+)\s*$') {
    [pscustomobject]@{ Port = [int]$Matches[1]; OwnerId = [int]$Matches[2] }
  }
})
foreach ($port in @(8801, 8080)) {
  $owners = @($listeners | Where-Object Port -eq $port | Select-Object -ExpandProperty OwnerId -Unique)
  if ($owners.Count -eq 0) { continue }
  foreach ($ownerId in $owners) {
    $process = Get-CimInstance Win32_Process -Filter "ProcessId=$ownerId" -ErrorAction SilentlyContinue
    if ($process.CommandLine -match '(?:^|[\s"])vn\.onlinelearn\.OnlineLearnApplication(?:[\s"]|$)') {
      Write-Host "OnlineLearn is already listening on port $port (PID $ownerId)."
      return
    }
  }
  if ($port -eq 8801) { throw "Port 8801 is already in use (PID: $($owners -join ', '))." }
  Write-Host "Port 8080 is in use (PID: $($owners -join ', ')). Starting OnlineLearn on 8801."
}
if (-not $env:JWT_SECRET) {
  $olsBytes = New-Object byte[] 48
  [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($olsBytes)
  $env:JWT_SECRET = [Convert]::ToBase64String($olsBytes)
  Write-Host 'Development JWT key generated for this process. Set JWT_SECRET to keep sessions across restarts.'
}
Push-Location $backend
$olsPreviousJavaOptions = $env:JDK_JAVA_OPTIONS
try {
  if ($env:DB_URL -match '(?i)integratedSecurity\s*=\s*true') {
    # NativeAuthentication uses the current Windows account, not the saved SQL login.
    $env:DB_USERNAME = ''
    $env:DB_PASSWORD = ''
    Write-Host 'Using Windows Integrated Authentication; native DLL is provisioned by the Windows Maven profile.'
  }
  if ($env:OLS_WINDOWS_TCP_PIPES -eq 'true') {
    # Local Windows workaround: JDK falls back to TCP pipes if UNIX-socket temp paths are unavailable.
    $olsSocketDirectory = Join-Path $env:TEMP 'onlinelearn-build\no-unix-sockets'
    $olsSocketOption = '"-Djdk.net.unixdomain.tmpdir=' + $olsSocketDirectory + '"'
    $env:JDK_JAVA_OPTIONS = (@($olsPreviousJavaOptions, $olsSocketOption) -join ' ').Trim()
  }
  $buildDirectory = Join-Path $env:TEMP 'onlinelearn-build\swd-backend-target'
  & mvn "-Donlinelearn.build.directory=$buildDirectory" spring-boot:run
  if ($LASTEXITCODE -ne 0) { throw 'Backend failed.' }
} finally {
  $env:JDK_JAVA_OPTIONS = $olsPreviousJavaOptions
  Pop-Location
}
} finally {
  if ($ownsLaunch) { $launchMutex.ReleaseMutex() }
  $launchMutex.Dispose()
}
