---
name: onlinelearn-project-conventions
description: Project-specific coding, architecture, Thymeleaf, and UI conventions for the OnlineLearn Spring Boot + Thymeleaf + MySQL project. Use this skill whenever generating, reviewing, or modifying backend code, Thymeleaf templates, or UI/CSS for this project, so output stays consistent with the established architecture and design system instead of inventing new patterns each time.
---

# OnlineLearn — Project Conventions & Rules for AI Agents

> Read this file CAREFULLY before generating or modifying any code in the OnlineLearn project. This is the single source of truth for architecture, coding conventions, and UI conventions. Do not silently switch to a different pattern even if it seems "better" from general knowledge — if you see a conflict or need an exception, note it in a code comment instead of quietly deviating.

---

## 1. Fixed tech stack (do not substitute)

- Java 21, Spring Boot 3.x stable GA release (no SNAPSHOT/milestone versions)
- Maven, packaging: Jar
- Thymeleaf (server-side rendering) + **Thymeleaf Layout Dialect** (`nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect`) — this is the most widely adopted community standard for managing shared layouts in Spring Boot + Thymeleaf projects. Do NOT manually repeat `th:insert`/`th:replace` on every page when the layout dialect can be used instead.
- Bootstrap 5 (CSS framework)
- Spring Security (simplified demo-level, see section 5)
- Spring Data JPA + Hibernate, MySQL 8.x
- Lombok for Entities/DTOs (`@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` as needed)
- Configuration in `application.yml`, split by profile (`dev`, `prod`)

## 2. Package structure — "Role-first" standard (finalized, do not change)

Among the two common approaches in the Spring Boot community — **package-by-layer** vs **package-by-feature** — this project uses **package-by-feature organized by actor/role**, because the system has 6 clearly separated actors and each module is handed off independently to a different dev/AI agent. Package-by-feature lets you add or remove an entire module without touching other modules, which matches the general community recommendation whenever a system has clear feature boundaries like this one.

```
com.onlinelearn
├── common
│   ├── config         (SecurityConfig, MailConfig, WebConfig, ThymeleafConfig)
│   ├── entity          (shared by every role: User, Subject, Lesson, Question, Quiz...)
│   ├── repository
│   ├── security
│   ├── exception       (GlobalExceptionHandler)
│   └── util
├── auth                (Login/Register/Reset password — not tied to a specific actor)
│   ├── controller / service / dto
├── publicweb            (Guest: Home, Blog, Course browsing, Course Register)
│   ├── controller / service / dto
├── customer
│   ├── controller / service / dto
├── learning              (Quiz Lesson, Quiz Handle, Quiz Review)
│   ├── controller / service / dto
├── expert
│   ├── controller / service / dto
├── admin
│   ├── controller / service / dto
├── sale
│   ├── controller / service / dto
└── marketing
    ├── controller / service / dto
```

**Mandatory rules**:
- `entity` and `repository` always live in `common` because multiple roles operate on the same tables (e.g. `Subject` is touched by both `expert` and `admin`).
- When two modules operate on the same Entity but have different visible fields/permissions (e.g. Expert cannot see the Publish button, cannot edit Price Package) → **create a separate Service per module** (`ExpertSubjectService`, `AdminSubjectService`). Do NOT share one Service with `if (role == ...)` branches inside — keep each module independent, even at the cost of some light code duplication.
- Never place a class in the default package (no `package` declaration).

## 3. Naming conventions

- Classes: `PascalCase`. Controllers end with `Controller` (`ExpertSubjectController`), Services end with `Service`/`ServiceImpl`, Repositories end with `Repository`, DTOs end with `*DTO` or `*FormDTO` for form input, `*ResponseDTO` if a complex output needs to be separated.
- Methods/fields: `camelCase`. DB boolean columns don't need an `is` prefix, but the Java field can still be `isFeatured`, `isCorrect` (Lombok generates the `isX()` getter automatically for `boolean`).
- Route URLs: `kebab-case`, plural for list endpoints, singular + `{id}` for detail endpoints — following the pattern already used throughout this project:
  ```
  /content/subjects          GET  (list)
  /content/subjects/new      GET/POST (create form)
  /content/subjects/{id}     GET  (detail)
  /content/subjects/{id}/edit GET/POST (edit form)
  ```
- Template files: named to match the route, consistent kebab-case/snake-case matching the route folder name (`templates/content/subjects/list.html`, `form.html`, `details.html`).

## 4. Thymeleaf — mandatory conventions from the Spring Boot + Thymeleaf community

1. **Layout via Thymeleaf Layout Dialect**: one root layout file `templates/layout/base.html` holding the shared chrome (head, navbar, role-based sidebar, footer). Child pages declare `layout:decorate="~{layout/base}"` and only write their own content inside `layout:fragment="content"`.
2. **Reusable fragments** go in `templates/fragments/` — e.g. `fragments/pagination.html`, `fragments/status-badge.html`, `fragments/form-errors.html`. Do not copy-paste the same HTML across multiple list/form pages.
3. **Form binding**: always use `th:object="${formDTO}"`, fields use `th:field="*{propertyName}"`, validate with `@Valid` + `BindingResult`, show errors right below the field using `th:if="${#fields.hasErrors('propertyName')}"` + `th:errors="*{propertyName}"`.
4. **No business logic inside templates** — Thymeleaf is only for display (`th:if`/`th:each`/`th:text`). All computation (progress %, currency formatting, complex conditions) must happen in the Controller/Service and be passed into the Model/DTO already computed.
5. **Static resources**: module-specific CSS/JS go in `static/css/{module}.css`, `static/js/{module}.js`. Avoid long inline `<style>`/`<script>` blocks in templates (small UI toggle snippets like the Lesson Details conditional fields are an acceptable exception).
6. Set `spring.thymeleaf.cache=false` in the `dev` profile so templates don't require an app restart to reflect changes.

## 5. Security — demo-level (finalized, re-read before writing security code)

- Authorize by route prefix using the lambda-style `SecurityFilterChain` (Spring Security 6.x) — do NOT use the deprecated `WebSecurityConfigurerAdapter`.
- Detailed ownership checks are not required almost everywhere, **except** these two business rules that must stay:
  - An Expert only sees Subjects/Questions/Quizzes where `owner = themselves` (this filter lives in the Service layer, it is a business rule, not a security rule).
  - A Customer only sees their own Registration/CourseAccess/QuizAttempt records.
- CSRF is disabled. No email verification, no expiring reset-password links required.
- Passwords are always BCrypt-hashed — this is the one security practice that stays strict no matter what.

## 6. Fixed business rules (must not be dropped even when other things are simplified)

- Registration → `PAID`: look up the User by email → if found, grant `CourseAccess`; if not found, create a User and send (or console-log) login info.
- Expert has **full CRUD on Quiz** — do not block editing/deleting a Quiz that already has a `QuizAttempt` (this was intentionally changed from the original restriction; see the Quiz CRUD prompt).
- Each `Question` must have at least 2 `AnswerOption`s, with at least one marked correct.
- `Lesson` UI changes based on `type` (`SUBJECT_TOPIC`/`LESSON`/`QUIZ`) — show/hide fields accordingly.
- Admin can only edit a User's `Role` and `Status`, not their personal information.
- Quiz Handle must persist state in the database (`QuizAttempt`, `QuizAnswer`) — never keep state only in JS/session, so a page refresh doesn't lose progress.

---

## 7. UI/UX Design System — standards for a course/LMS website

Based on common patterns from established online-learning platforms (Udemy, Coursera, Moodle) and modern UI design standards, applied consistently across all 40 screens.

### 7.1 Overall layout

- **Guest/Customer (Public)**: fixed top navbar (logo on the left, menu in the center: Home/Courses/Blog, right side: search box + Login/Register button or avatar if logged in) + a simple footer (static links, social, copyright). No sidebar on Public pages.
- **Customer (Learning area)**: compact top navbar (logo + avatar + "Back to courses" button only) + a 2-column layout on Lesson View (280px left sidebar with the course content tree, full-width right panel with the lesson content).
- **Admin/Expert/Sale/Marketing (Back-office)**: standard dashboard layout — fixed left sidebar (240px, collapsible) with the role's menu, a horizontal top bar with breadcrumb + avatar + logout, main content area full-width on the right with consistent padding (`24px`).

### 7.2 Colors & status badges — consistent system-wide

| Status | Badge color | Used for |
|---|---|---|
| Active / Published / Paid / Correct | Green (`success`, e.g. Bootstrap `#198754` / `bg-success`) | Subject published, User active, Registration paid, correct answer |
| Inactive / Unpublished / Draft | Gray (`secondary`, `#6c757d`) | Unpublished Subject, inactive Lesson |
| Submitted / Pending / In Progress | Amber (`warning`, `#ffc107`) | Submitted Registration, Quiz attempt in progress |
| Cancelled / Incorrect / Expired | Red (`danger`, `#dc3545`) | Cancelled Registration, wrong answer, expired CourseAccess |

Badges are always pill-shaped (`rounded-pill`), sentence case (not ALL CAPS).

### 7.3 Course card (Courses List, My Courses) — standard LMS pattern

```
┌─────────────────────┐
│   [Thumbnail 16:9]   │
├─────────────────────┤
│ Category tag (small)    │
│ Course Name (bold, max 2 lines, truncate ...) │
│ Instructor/Owner       │
│ ⭐ rating (if any) · # students (if any) │
│ Price: ~~List Price~~  Sale Price (bold, accent color) │
│ [Progress bar, My Courses only]            │
│ [Button: View Details / START COURSE]     │
└─────────────────────┘
```
- Use CSS Grid/Bootstrap `row-cols-1 row-cols-md-3 row-cols-lg-4 g-4` for responsiveness: 1 column on mobile, 3 on tablet, 4 on desktop.
- Thumbnail fixed at 16:9 (`aspect-ratio: 16/9`), `object-fit: cover` to avoid distortion.
- Hover state: a subtle box-shadow lift + pointer cursor; the layout must not shift on hover.

### 7.4 Lesson View / Quiz Handle — learning UI

- Course content sidebar: use an accordion (Bootstrap Collapse) per Section, listing Lessons/Quizzes inside with distinguishing icons (play icon for video, document icon for HTML, checklist icon for Quiz). The current lesson is highlighted with a light background; completed lessons show a green checkmark.
- Quiz Handle: the progress bar and timer are pinned at the top (sticky top) so they stay visible while scrolling. The question and answers are centered, max-width ~720px for readability (don't stretch across a wide screen). Navigation buttons (Previous/Next/Score Exam) are pinned to the bottom (sticky bottom) on mobile.
- Answer coloring: default light gray border; when selected → accent-colored border + light fill; after submission in Quiz Review: correct → green border, user's wrong selection → red border, the actual correct answer (if the user got it wrong) → green border with a check icon.

### 7.5 Data tables (List screens: Users List, Subjects List, Registrations List...)

- Use Bootstrap Table (`table table-hover align-middle`), with a sticky header when the table is tall.
- Every table includes: a filter/search bar above it (filter dropdowns left-aligned, search box right-aligned), pagination (`fragments/pagination.html`) below it, and a total-count indicator ("Showing 1–10 of 42 results").
- Row action buttons use icon-only buttons (Bootstrap Icons) with a `title`/`aria-label` describing the action (never a bare icon with no label, for accessibility). Group more than 3 actions into a "..." dropdown to keep the table from getting too wide.
- Empty state (no data): show a light icon/illustration + a suggested next action ("No courses yet. [+ Create new course]") instead of leaving the table blank.

### 7.6 Forms (Create/Edit screens)

- Labels sit above the input (not beside it, old-form style), inputs are full-width within a sensibly constrained max-width container (don't stretch an input across the full desktop width).
- Required fields are marked with a small red `*` after the label.
- Related fields are grouped into a `fieldset`/card with its own subheading (e.g. the Subject Details form already separates the Overview/Dimension/Price Package tabs this way).
- Save/Submit buttons are always on the right, Cancel/Back on the left of the same row — this placement stays consistent across every form in the system.

### 7.7 Typography & spacing

- Font: use Bootstrap's default sans-serif stack (or a single Google Font such as Inter/Roboto if customizing) — pick exactly one font family for the whole system, never mix multiple fonts.
- Heading scale follows Bootstrap defaults (`h1`–`h6`); don't set arbitrary `font-size` values per page.
- Spacing uses Bootstrap's spacing scale (`p-3`, `mb-4`, `gap-3`...) instead of hand-written pixel margins like `margin: 13px` — keep spacing consistent system-wide.

### 7.8 Responsive & accessibility

- Breakpoints follow Bootstrap 5 defaults (`sm` 576px, `md` 768px, `lg` 992px, `xl` 1200px) — don't define custom breakpoints.
- Every image has descriptive `alt` text; icon-only buttons have `aria-label`; text/background contrast meets at least WCAG AA (don't use light gray text on a white background for important content).
- Back-office sidebars collapse into a hamburger menu below the `md` breakpoint (768px).

---

## 8. Self-check checklist before marking a task "done"

- [ ] Code is placed in the correct role-first package from section 2, not leaking into `common` except for Entity/Repository.
- [ ] Controllers use DTOs, never bind Entities directly to forms.
- [ ] Templates extend `layout/base.html` via Layout Dialect, no copy-pasted HTML chrome.
- [ ] Routes follow the kebab-case + plural/singular pattern from section 3.
- [ ] Status badge colors match section 7.2 exactly — never invent a different color for the same status meaning.
- [ ] List screens include: filter/search, pagination, and an empty state.
- [ ] Business rules from section 6 are not missed.
- [ ] No security/validation added beyond what's defined in section 5, unless the user explicitly asked for it.

---

### How to use this file
Place this file at the repo root (`SKILL.md` or `AGENTS.md`, or `.claude/skills/onlinelearn/SKILL.md` depending on the AI tool in use — Claude Code, Antigravity, and Cursor all auto-read convention files at the repo root). Attach or point to this file at the start of EVERY new task prompt given to an AI agent, so the agent consistently follows these conventions instead of defaulting to general knowledge each time.
