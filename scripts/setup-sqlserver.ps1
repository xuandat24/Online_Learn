$ErrorActionPreference = 'Stop'
# Run elevated; only changes the explicitly selected SQLEXPRESS instance.
$instanceId = (Get-ItemProperty 'HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\Instance Names\SQL').SQLEXPRESS
if (-not $instanceId) { throw 'SQLEXPRESS is not installed.' }
$tcpPath = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\$instanceId\MSSQLServer\SuperSocketNetLib\Tcp"
$backup = Join-Path $env:TEMP 'onlinelearn-build\sqlserver-tcp-before.json'
[System.IO.Directory]::CreateDirectory((Split-Path $backup)) | Out-Null
@{ instance=$instanceId; tcp=(Get-ItemProperty $tcpPath); ipAll=(Get-ItemProperty "$tcpPath\IPAll") } | ConvertTo-Json -Depth 4 | Set-Content $backup
Set-ItemProperty -Path $tcpPath -Name Enabled -Value 1
Set-ItemProperty -Path $tcpPath -Name ListenOnAllIPs -Value 0
Get-ChildItem $tcpPath | Where-Object { $_.PSChildName -ne 'IPAll' } | ForEach-Object {
    $address = (Get-ItemProperty $_.PSPath).IpAddress
    if ($address -in @('127.0.0.1', '::1')) {
        Set-ItemProperty -Path $_.PSPath -Name Enabled -Value 1
        Set-ItemProperty -Path $_.PSPath -Name TcpDynamicPorts -Value ''
        Set-ItemProperty -Path $_.PSPath -Name TcpPort -Value '14333'
    } else {
        Set-ItemProperty -Path $_.PSPath -Name Enabled -Value 0
    }
}
Restart-Service -Name 'MSSQL$SQLEXPRESS' -Force
& sqlcmd -S 'tcp:127.0.0.1,14333' -E -C -b -Q "SELECT local_net_address,local_tcp_port FROM sys.dm_exec_connections WHERE session_id=@@SPID;"
if ($LASTEXITCODE -ne 0) { throw 'SQL Server connection failed.' }
Write-Host 'SQL Server accepts loopback connections only on port 14333.'
