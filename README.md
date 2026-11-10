# OnlineLearn

React/Vite + Spring Boot MVC + Spring Security/JWT + Spring Data JPA + Microsoft SQL Server. Node is used only for frontend tooling. Guest is anonymous; the six account roles are Customer, Admin, Author, Sale, Marketing and Staff. Staff has no management permissions.

## Run on this Windows machine

Java 17+, Maven, Node 20.19+ and SQL Server are required. Existing code is in `backend/` and `frontend/`. Windows protected Documents folders prevented build tools from writing normally, so the launch scripts mirror source into `%TEMP%\onlinelearn-build` and run there. Repository source remains authoritative.

1. Fill `.env.local` (ignored by Git), using `.env.example` as the reference. Do not put secrets in committed files.
2. Use an existing SQL login with access to `OnlineLearnDB`. Set:

   ```dotenv
   DB_URL=jdbc:sqlserver://127.0.0.1:14333;databaseName=OnlineLearnDB;encrypt=true;trustServerCertificate=true
   DB_USERNAME=your_existing_sql_login
   DB_PASSWORD=your_sql_password
   JWT_SECRET=at_least_32_random_bytes_as_a_string
   FRONTEND_URL=http://localhost:5173
   ```

   The example above uses a SQL login. On this machine, Windows Integrated Authentication has been verified against the existing IPv6 loopback listener on port 14333. Use the [Windows Authentication configuration](docs/windows-authentication.md) for that mode. Use a trusted server certificate instead of `trustServerCertificate=true` for a deployed system.
3. Start two PowerShell terminals at the repository root:

   ```powershell
   .\scripts\start-backend.ps1
   .\scripts\start-frontend.ps1
   ```

   Backend: `http://127.0.0.1:8080/actuator/health`. Frontend: `http://localhost:5173` or `http://127.0.0.1:5173`.
4. If this Windows Java runtime reports `Unable to establish loopback connection` with `UnixDomainSockets.connect`, add `OLS_WINDOWS_TCP_PIPES=true` to `.env.local`. The launcher sets a JVM-only temporary socket-directory option to use the JDK TCP fallback; it does not change machine settings. A development JWT key is generated for the process if omitted; set a persistent strong `JWT_SECRET` to keep sessions valid across restarts.

For machines where the repository is writable, normal commands work too:

```powershell
cd backend
mvn test package
mvn spring-boot:run
# Other terminal, from frontend:
npm ci
npm run dev
npm run build
```

Export environment variables before direct Maven commands; Spring does not read `.env.local` automatically. The launcher does.

## SQL migrations

Flyway applies `backend/src/main/resources/db/migration/V1__sdd_schema.sql` and `V2__integrity_and_notifications.sql`, then Hibernate validates mappings. V1 preserves the supplied 20-table SDD design. V2 adds the mail outbox, purchase-duration snapshots, token invalidation and uniqueness checks. No application startup uses `ddl-auto=update` or drops tables.

**Do not execute `database/OnlineLearn_Database.reference.sql` against existing data**: it is the unchanged supplied reference and includes DROP TABLE statements. An existing manually initialized database needs a deliberate Flyway baseline after schema comparison; do not enable automatic baselining blindly.

Migration validation created isolated databases `OnlineLearn_Verification_20261110` (diagnostic partial run) and `OnlineLearn_Verification_20261110_v2` (successful clean V1/V2 run, 21 tables, 25 CHECK constraints). `OnlineLearnDB` was not populated or reset during this verification.

## First administrator and optional demo data

For a fresh empty users table, configure `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` before the first launch. The bootstrap account is an explicitly configured local administrator. Remove those variables after initialization. An Admin invites Authors and other users; invitations send a password-setup link.

For demonstration only, set `DEMO_SEED=true` and a strong `DEMO_PASSWORD`. This creates `[DEMO]` users at `{role}@onlinelearn.test`, reference settings, a published sample course/package, lesson, question, quiz and post. Seeding does not overwrite existing accounts or fabricate paid registrations/access. All six demo account roles use the configured demo password. Disable seeding for normal use.

## Gmail

Set `GMAIL_USERNAME`, `GMAIL_APP_PASSWORD` and `MAIL_MODE=smtp`. SMTP uses Gmail port 587 with STARTTLS. `FRONTEND_URL` must match the reachable frontend URL for email links. Verification and reset tokens are hashed in `auth_tokens`, expire and are single-use. Mail is queued transactionally and retried by the outbox worker. The worker removes delivered token links from stored message bodies.

For local email-flow testing without sending real messages, explicitly set `MAIL_MODE=file` and optionally `MAIL_DIRECTORY`. Messages are written as `.eml` with a **LOCAL TEST ONLY - NOT SENT** header; default path is `.runtime/mail` relative to the temporary build directory. This does not establish real Gmail delivery. With SMTP credentials missing, messages stay Pending and login remains blocked until email verification.

## VNPAY sandbox

Configure your own `VNPAY_TMN_CODE` and `VNPAY_HASH_SECRET`; obtain them from the [official sandbox registration](https://sandbox.vnpayment.vn/devreg/). Configure `VNPAY_RETURN_URL` to the frontend `/payment/return` route. Register an externally reachable HTTPS IPN endpoint pointing to `/api/payments/vnpay/ipn` on the Spring backend. Localhost cannot receive a provider's external callback.

Implementation follows VNPAY 2.1.0 [checkout/IPN](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html) and [querydr](https://sandbox.vnpayment.vn/apis/docs/truy-van-hoan-tien/querydr%26refund.html). Amounts are VND converted to VNPAY minor units. Backend verifies signatures, merchant, order, amount and transaction status. Only verified success changes a registration to Paid and grants one course access. Unknown results remain Pending. Browser return parameters never grant access. Admin/Sale can request a signed `querydr` recheck; no API accepts a manually supplied Paid status.

Missing credentials return HTTP 503, leaving the registration Submitted. No production credential or successful payment is invented. Real sandbox checkout, IPN delivery and querydr still need verification with the merchant's credentials.

## Checks and remaining verification

Verified on 2026-11-10: `mvn test package` passed **19 tests** with no failures/errors; `npm run build` passed; Playwright passed **4 browser tests** against the isolated real Spring server. The main application subsequently connected to **OnlineLearnDB on SQL Server**, Flyway applied V1/V2, Hibernate validated mappings, and `/actuator/health` returned UP. A Chrome smoke check verified actual React form login, a Guest Submitted registration persisted in SQL Server, and denial of unpaid learning access. VNPAY credentials are still absent; the actual sandbox transaction remains unverified.

- Maven unit/API integration tests cover signatures, failed/pending/cancelled/duplicate callbacks, snapshot duration, authorization, course ownership, protected lesson access, quiz answer storage/grading, quiz immutability and access expiry.
- Integration and browser fixtures use **H2 only in test source/classpath**. The production JAR has no H2 driver and uses SQL Server. Passing those tests does not prove a production SQL Server JDBC connection.
- Playwright tests call a real isolated Spring HTTP server and check guest registration, missing-payment-configuration state, Admin workspace, form-based Admin/Author course/lesson creation and mobile layout. `BrowserTestServer` is test-only and never included in the production JAR.
- The main SQL Server runtime has been verified with the SQL login supplied privately in `.env.local`. Gmail delivery and actual VNPAY sandbox transactions remain unverified without credentials. `MAIL_MODE=file` is enabled for local testing; SMTP health checking is optional (`SMTP_HEALTH_CHECK=true` enables it).
- Question import uses a downloadable JSON template (up to 100 questions per batch, all-or-nothing validation). CSV/Excel import is not implemented; the SDD leaves the import format open. Advanced dashboard analytics are outside the supplied MVP scope.

To prepare the test-only browser server:

```powershell
python scripts/prepare-runtime.py
cd "$env:TEMP\onlinelearn-build\backend"
mvn test-compile dependency:build-classpath '-Dmdep.outputFile=test-classpath.txt' '-DincludeScope=test'
$olsClasspath = 'target/test-classes;target/classes;' + (Get-Content -Raw test-classpath.txt)
java -cp $olsClasspath vn.onlinelearn.BrowserTestServer
```

It uses port 18080 and explicitly labelled `[DEMO]` fixtures; demo test password is specified only in the test server class. On this Windows host use the JVM fallback option described above if needed. In another terminal start Vite with `VITE_API_PROXY=http://127.0.0.1:18080`, then run `npm run test:e2e` from `frontend`. Install Playwright Chromium or set `PLAYWRIGHT_CHROMIUM_EXECUTABLE` to an installed Chrome executable. The test harness is not a replacement for the required SQL Server deployment.
