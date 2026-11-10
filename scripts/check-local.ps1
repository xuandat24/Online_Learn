$ErrorActionPreference = 'Stop'
$olsProject = Split-Path $PSScriptRoot
$olsConfig = Join-Path $olsProject '.env.local'
if (-not (Test-Path $olsConfig)) { throw '.env.local is missing.' }
$olsValues = @{}
Get-Content $olsConfig | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $olsEntry = $_ -split '=', 2
    $olsValues[$olsEntry[0]] = $olsEntry[1]
}
foreach ($olsName in @('DB_USERNAME','DB_PASSWORD','JWT_SECRET','VNPAY_TMN_CODE','VNPAY_HASH_SECRET')) {
    $olsPresent = -not [string]::IsNullOrWhiteSpace($olsValues[$olsName])
    Write-Host "$olsName configured: $olsPresent"
}
$olsIntegrated = $olsValues['DB_URL'] -match '(?i)integratedSecurity\s*=\s*true'
if ($olsIntegrated) {
    Write-Host 'Checking Windows Integrated Authentication over IPv6 loopback, as preferred by the local JDBC URL.'
    & sqlcmd -S 'tcp:[::1],14333' -E -C -b -l 5 -d OnlineLearnDB -Q "SET NOCOUNT ON; SELECT DB_NAME() AS database_name, SUSER_SNAME() AS authenticated_login; SELECT COUNT(*) AS application_tables FROM sys.tables WHERE name <> 'flyway_schema_history';"
    if ($LASTEXITCODE -ne 0) { throw 'Windows TCP authentication failed. SQL username/password do not resolve an SSPI error.' }
} elseif ($olsValues['DB_USERNAME'] -and $olsValues['DB_PASSWORD']) {
    # SQL Server local connection. Never put the password into process arguments or output.
    $olsPreviousPassword = [Environment]::GetEnvironmentVariable('SQLCMDPASSWORD','Process')
    try {
        $env:SQLCMDPASSWORD = $olsValues['DB_PASSWORD']
        & sqlcmd -S 'tcp:127.0.0.1,14333' -U $olsValues['DB_USERNAME'] -C -b -l 5 -d OnlineLearnDB -Q "SET NOCOUNT ON; SELECT DB_NAME() AS database_name, SUSER_SNAME() AS authenticated_login; SELECT COUNT(*) AS application_tables FROM sys.tables WHERE name <> 'flyway_schema_history';"
        if ($LASTEXITCODE -ne 0) { throw 'SQL authentication failed. Check the existing login/password and database access.' }
    } finally {
        [Environment]::SetEnvironmentVariable('SQLCMDPASSWORD',$olsPreviousPassword,'Process')
    }
} else { Write-Host 'SQL authentication check pending: fill DB_USERNAME and DB_PASSWORD.' }
foreach ($olsUrl in @('http://127.0.0.1:8801/actuator/health','http://127.0.0.1:5173/api/public/courses')) {
    try {
        $olsResponse = Invoke-RestMethod -Uri $olsUrl -TimeoutSec 5
        if ($olsUrl.EndsWith('/health')) { Write-Host "Backend health: $($olsResponse.status)" }
        else { Write-Host "Frontend -> Backend catalog: $($olsResponse.totalElements) published courses" }
    } catch { Write-Host "$olsUrl : not ready ($($_.Exception.Message))" }
}
