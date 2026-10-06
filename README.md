# Morrow Online Learning

Online-learning platform for public course discovery, price-package registration, Sale payment confirmation, paid course access, and assigned Expert content management.

## Stack

- Frontend: Next.js App Router, React, TypeScript
- Backend: Spring Boot 3.5, Java 17, Spring Security, JWT
- Database: MySQL 8.4 with Spring Data JPA
- Deployment: Docker Compose

## Run with Docker

1. Copy `.env.example` to `.env` and replace every example password and the JWT secret with unique values. Keep `.env` private.
2. Start the stack from this directory:

   ```sh
   docker compose up --build -d
   ```

3. Open the frontend at `http://localhost:3000`. The API is available at `http://localhost:8080`; health is at `/actuator/health`.
4. The first startup creates sample courses. If `ADMIN_EMAIL` and `ADMIN_PASSWORD` are set, an admin account is created once.

Configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_SMTP_AUTH`, and `MAIL_SMTP_STARTTLS` for a real SMTP provider. Sale approval rolls back if a new customer's login email cannot be delivered.

To stop the stack, run `docker compose down`. Add `-v` only when you intentionally want to delete the MySQL data volume.

## Run locally

Start MySQL 8.4, then run the backend from `backend`:

```sh
mvn spring-boot:run
```

Run the frontend from `frontend`:

```sh
npm install
npm run dev
```

The Next.js server proxies `/api/*` to `API_INTERNAL_URL` (default `http://localhost:8080`).

## API

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/courses?search=&category=` | Guest | Search published courses and published price packages |
| `GET` | `/api/courses/{id}` | Guest | View course details |
| `POST` | `/api/auth/register` | Guest | Create a Customer account |
| `POST` | `/api/auth/login` | Guest | Receive a JWT |
| `POST` | `/api/registrations` | Guest, Customer | Submit a registration against a published price package |
| `GET` | `/api/registrations/me` | Customer | List own registration requests |
| `PUT`, `DELETE` | `/api/registrations/{id}` | Customer owner | Edit/cancel while status is `SUBMITTED` |
| `GET` | `/api/sales/registrations` | Admin, Sale | Review submitted requests |
| `PATCH` | `/api/sales/registrations/{id}/paid` | Admin, Sale | Confirm payment, create a Customer if needed, grant access, email login details |
| `GET` | `/api/enrollments/me` | Customer | List active course access |
| `GET` | `/api/admin/courses` | Admin, assigned Expert | List manageable courses |
| `POST`, `PUT` | `/api/admin/courses[/{id}]` | Admin, assigned Expert | Create/update courses and price packages |
| `DELETE` | `/api/admin/courses/{id}` | Admin | Delete a course |
| `GET`, `POST`, `PUT` | `/api/admin/subjects[/{id}]` | Admin | Manage Subjects |
| `PUT`, `DELETE` | `/api/admin/subjects/{id}/experts/{expertId}` | Admin | Assign/unassign an Expert |
| `GET`, `POST`, `PUT`, `DELETE` | `/api/admin/courses/{courseId}/lessons[/{lessonId}]` | Admin, assigned Expert | Manage lessons |
| `GET`, `POST`, `PUT`, `DELETE` | `/api/admin/courses/{courseId}/quizzes[/{quizId}]` | Admin, assigned Expert | Manage quizzes/questions; locked after first attempt |
| `GET` | `/api/learning/courses/{courseId}` | Customer with active access | Load course lessons and quizzes |
| `PUT` | `/api/learning/lessons/{lessonId}/progress` | Customer with active access | Save lesson completion |
| `GET` | `/api/learning/quizzes/{quizId}` | Customer with active access | Load quiz without answer keys |
| `POST` | `/api/learning/quizzes/{quizId}/attempts` | Customer with active access | Start or resume an attempt |
| `GET`, `PUT` | `/api/learning/attempts/{attemptId}[/answers/{questionId}]` | Attempt owner with active access | Read/resume and persist answers |
| `POST` | `/api/learning/attempts/{attemptId}/submit` | Attempt owner with active access | Submit and score an attempt |
| `GET` | `/api/admin/users` | Admin | List platform accounts |
| `PATCH` | `/api/admin/users/{id}/role` | Admin | Assign an internal role |

New accounts always receive the `CUSTOMER` role. `GUEST` is an anonymous visitor role; internal roles (`MARKETING`, `SALE`, `EXPERT`, `ADMIN`) must be provisioned by trusted operations. Admin assigns Experts to Subjects, and an Expert can manage only courses within assigned Subjects. Passwords are BCrypt-hashed and protected endpoints require a bearer token.

## Deployment notes

- Configure `JWT_SECRET`, database credentials, admin credentials, SMTP credentials, and `CORS_ALLOWED_ORIGINS` using the deployment platform's secret manager. Do not use `.env.example` values in production.
- MySQL is only bound to loopback in the local Compose setup. For a hosted deployment, use a private managed database and remove the local `db` service.
- Hibernate schema update is convenient for this MVP. Before production data migrations, replace `ddl-auto: update` with Flyway or Liquibase migrations and a validate-only schema mode.
- New registrations always start `SUBMITTED`. A Guest cannot submit with an email that already has an account; they must sign in. Duplicate `SUBMITTED` or `PAID` registrations for the same course/email are rejected. Only Sale/Admin can mark a request `PAID`; Customer edit/cancel is allowed only before that transition, and an edit cannot transfer the request to another email. A new email creates a Customer account with a random temporary password, which is emailed after payment confirmation. SMTP must be configured for this flow.
- Payment capture is manual; a payment gateway is not integrated. Both free and paid packages require Sale confirmation, so there is no enrollment-to-access shortcut.
- A `PAID` registration creates active course access. Package `accessDays: 0` means no expiry; positive values create an expiry. Every learning endpoint checks current access.
- Quiz answers are persisted after each selection, unfinished attempts resume from the server, and learner quiz responses omit correct-answer keys. Every question must be answered before submission. Quiz edit/delete is blocked after the first attempt.
- API DTOs apply server-side Bean Validation; browser forms also constrain required fields, lengths, ranges, email, phone, URL, currency, and quiz answers. Server validation remains authoritative.
- Lesson/video delivery and quiz/progress APIs are implemented; video hosting/transcoding, password reset, marketing posts/banners, and a dedicated Marketing dashboard remain future modules.
- Run backend integration tests with `mvn -f backend/pom.xml -DforkCount=0 clean test`. They use H2 and mock SMTP delivery.
- Demo course photography is loaded from Unsplash URLs; replace with owned/licensed assets or an image CDN for production.