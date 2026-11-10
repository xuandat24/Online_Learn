# Windows Integrated Authentication

The Windows Maven profile automatically copies Microsoft's `mssql-jdbc_auth:12.10.2.x64` DLL to `backend/target/native` during `generate-resources`. It matches `mssql-jdbc:12.10.2.jre11` and a 64-bit JVM. The profile sets `-Djava.library.path=target/native` for `spring-boot:run`.

Use this JDBC configuration for the existing database:

```dotenv
DB_URL=jdbc:sqlserver://localhost:14333;databaseName=OnlineLearnDB;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=NativeAuthentication;ipAddressPreference=IPv6First
OLS_WINDOWS_TCP_PIPES=true
```

Windows Authentication uses the account running Java. The launcher clears SQL username/password in the launch process. With `OLS_WINDOWS_TCP_PIPES=true`, it sets the JDK socket option for this launch so the Windows JVM uses its TCP pipe fallback.

Run from the repository root:

```powershell
.\scripts\start-backend.ps1
```

For direct Maven use, first export the environment variables from `.env.local`, then run `mvn spring-boot:run` from `backend`. On this protected-folder Windows machine, the launcher uses `%TEMP%\onlinelearn-build\backend` as the build checkout.

On 2026-11-10, Windows Authentication through `127.0.0.1:14333` failed with SQL error 18452 and SSPI code `0x8009030c`. The existing `::1:14333` listener accepted the same Windows account. The JDBC URL prefers IPv6 so it reaches that listener.

- Java 17 x64 loaded the Microsoft-signed DLL.
- SQL Server reported live application sessions under the current Windows account using TCP and NTLM at `::1`.
- Flyway validated V1 and V2, JPA initialized, and Tomcat served `/actuator/health` on port 8080 with status `UP`.
