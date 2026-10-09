---
name: onlinelearn-requirements
description: The functional requirements for the OnlineLearn system, organized by actor/role, consolidated from the original project assignment document plus the implementation decisions made for this specific build. Read this before implementing or reviewing any feature to confirm scope and behavior.
---

# OnlineLearn — Functional Requirements (for AI Agents)

> Source: `OnlineLearn_Requirements.pdf` (FPT University, Online Learning System — Project Assignment Requirement, May 2021), reorganized by actor. Section 12 adds the implementation decisions made specifically for this build (tech stack, security level, deliberate deviations) — when section 12 conflicts with an earlier section, **section 12 wins** for this project.

---

## 1. System overview

OnlineLearn supports an organization/expert to build and sell online courses, and supports users to search, register for, and access online courses for learning. Six actor types exist:

| Actor | Description |
|---|---|
| Guest | Unregistered users |
| Customer | Registered users who are actual or potential customers |
| Marketing | Marketing members of the organization |
| Sale | Sale members of the organization |
| Expert | Accesses and prepares course/test content as assigned by Admin |
| Admin | Organization leader/manager, acts as system administrator |

The system has 8 functional groups: Common, Public, Customer, Marketing, Course Content, Testing Content, Sales, Admin.

---

## 2. COMMON Feature (all actor types)

| Function | Description |
|---|---|
| User Login | Authenticate the user to grant access to authorized features |
| User Register | Register a new user; verified via the registered email |
| Reset Password | Used when the user forgot their password |
| User Authorization | Authorize the user into system functions based on their role |
| User Profile | View & edit/update the user's profile |
| Change Password | Change the user's login password |

---

## 3. PUBLIC Feature (Guest)

| Function | Description |
|---|---|
| Home Page | The starting page of the system |
| Blogs List | List of shared posts |
| Blog Details | Detail of a shared post |
| Courses List | List of active courses |
| Course Details | Details of the selected course |
| Course Register | Lets the user register to access a course with a specific price package |

---

## 4. CUSTOMER Feature

### 4.1 My Registrations
View the customer's own submitted course registrations.

### 4.2 My Courses
View the customer's access-allowed courses, displayed as a card grid (thumbnail, title, instructor, `% Complete` progress, "START COURSE" button).

### 4.3 Lesson View
Lets the customer access course lessons for studying. Two layout variants: with lessons list (sidebar) or without lessons list.
- If the user picks an HTML-format lesson, HTML content is shown instead of a video.
- If a quiz lesson is chosen, the **Quiz Lesson** screen is shown instead.

### 4.4 Quiz Lesson
Lets the user take the topic quiz or view quiz results. Shown as one of 2 SubForms:
- **SubForm 1** (not taken yet): quiz name, exam name, number of questions, description, **Start Test** button → redirects to Quiz Handle.
- **SubForm 2** (already taken): score, correct/total, failed/passed, results by group, results by domain, average time per question, total time, unanswered count, **Review Test** button → redirects to Quiz Review, **Redo Test** button → redirects to Quiz Handle.

### 4.5 Quiz Handle
Lets the user browse and answer quiz questions.
- Header: question position (`X / Total`), countdown timer.
- Question body: question ID, content, 4 answer options (single-select radio).
- Footer buttons: **Peek at Answer** (shows a popup with the correct answer + explanation/domain/source/page), **Mark for Review**.
- Navigation buttons depend on position:
  - First question: **Next** only
  - Middle question: **Previous** & **Next**
  - Last question: **Previous** & **Score Exam**
- **Review Progress** button (bottom-left) opens a popup with 4 filters — Unanswered, Marked, Answered, All Questions — and a numbered question grid; clicking a number jumps back to that question in Quiz Handle. The popup also has a **Score Exam Now** button.
- The timer counts down. **Refreshing the page during the quiz has no impact** (state must be persisted).
- Scoring confirmation popup — 3 variants depending on how many questions are answered:
  1. **0 answered**: "Exit Exam?" — options: Back / **Exit Exam** (returns to Quiz Lesson SubForm 1).
  2. **Some unanswered**: "Score Exam?" shows "X of Y Questions Answered" warning — options: Back / **Score Exam** (completes exam, returns to Quiz Lesson SubForm 2, answers can no longer be changed after this point).
  3. **All answered**: "Score Exam?" — options: Back / **Score Exam** (same completion behavior).

### 4.6 Quiz Review
Lets the user review the details of the quiz they just took.
- Header: question position (no timer here).
- Shows the user's selected answer (marked), the correct answer (marked), for the current question.
- Footer buttons: **Review Results**, **Explanation** (only enabled when the question has a non-empty explanation), **Next**.
- **Review Results** button opens a popup with 4 filters — Marked, Answered, Incorrect, All Questions — and a numbered question grid; clicking a number jumps back to that question in Quiz Review.
- **Explanation** button opens a popup with: the correct answer, explanation text, domain, source, page reference.

---

## 5. MARKETING Feature

| Function | Description |
|---|---|
| Dashboard | **Shared by Admin, Sale, and Marketing** — shows relevant management statistics with links to related screens |
| Posts List | List, filter, search, show, hide, view, add new, or edit existing post(s) |
| Post Details | View & edit the details of a specific post |
| Sliders List | List, filter, search, show, hide, view, add new, or edit existing slider(s) |
| Slider Details | View & edit the details of a specific slider |

---

## 6. COURSE CONTENT Feature (Admin + Expert, shared with different permissions)

Allows Admin to add/edit subject contents and assign an Expert to a subject for content preparation.

### 6.1 Subjects List
Shows the existing course list. **Admin sees all courses; Expert sees only their assigned courses.**

### 6.2 New Subject
**Admin only.** Lets Admin add a new course and assign the Expert (owner) for further content preparation.

### 6.3 Subject Details
Lets Admin and the assigned Expert access and prepare subject information: general info, dimension, price package. **Only Admin can change status to Published/Unpublished and add/edit price packages.**

Tabbed layout, 3 tabs: **Overview**, **Dimension**, **Price Package**.
- Overview: Subject Name, Category (combo), Featured Subject (checkbox), Status (combo), Description, plus a thumbnail/image area. **Admin's Overview tab also displays the owner (Expert) info, and Admin can change the owner from there.**
- Dimension tab: table of (#, Type, Dimension name, Action: Edit/Delete), with Add New.
- Price Package tab: table of (#, Package name, Duration, List Price, Sale Price, Status, Action: Edit/Activate/Deactivate), with Add New.

### 6.4 Subject Dimension
Lets Admin/Expert input or edit subject dimension info: **type, name, description**. Type is a fixed setting value — see section 11 (`Subject Dimension`: Domain, Group).

### 6.5 Price Package
Lets Admin input or edit price package info: **name, access duration (in months), status (read-only here), list price, sale price, description**. **The Expert can only view this information** (no add/edit).

### 6.6 Subject Lessons
Shows the list of lessons for a selected subject/package. Admin/Expert can activate/deactivate a lesson, or open Lesson Details to add/edit. Columns: ID, Lesson, Order, Type, Status, Action. Has filters (lesson group, status) and a name search box, plus **Add Lesson**.

### 6.7 Lesson Details
Lets Admin/Expert input/edit lesson info: Name, Type, Topic (parent grouping), Order, Video link, HTML Content (rich text editor).

**Conditional UI by lesson Type** (mandatory rule):
- `Subject Topic` → hide Video link & HTML Content
- `Lesson` → show Video link & HTML Content
- `Quiz` → hide Video link, show a Quiz dropdown list instead

---

## 7. TESTING CONTENT Feature (Admin + Expert, shared)

Allows Admin and Expert to prepare testing-related content (question bank & quizzes list).

### 7.1 Questions List
Shows the existing questions list for a specific subject/course, paginated. Admin/Expert can filter by subject, lesson, dimension(s), level, status, and search by content. Includes an **Import** option (redirects to Questions Import popup).

### 7.2 Questions Details
Lets Admin/Expert input or edit a question: subject, dimension(s), lesson, status, content, media (image/audio/video, with preview option), answer options (with the correct key marked, ability to add/edit/delete options), and explanation.

### 7.3 Questions Import
Popup screen that lets the user choose a file and import questions from it into the question bank. Also provides an option to download a sample import template.

### 7.4 Quizzes List
Shows existing quizzes, paginated. Filterable by subject, quiz type, and searchable by name. Columns: id, name, subject, level, # question, duration, pass rate, quiz type. From here, the user can open Quiz Details to add/edit, and can delete a quiz.

> **Original rule: a quiz can only be edited while it has not yet had any test taken.** (This project's demo build intentionally overrides this — see section 12.)

### 7.5 Quiz Details
Lets the user input or edit quiz information. Two tabs:
- **Overview**: Name, Subject, Exam Level, Duration (minutes), Pass Rate (%), Quiz Type, Description.
- **Setting**: Total number of questions, question selection mode (**by topic / by group / by domain** — radio), and a dynamic list of "choose quantity of questions per selected group" rows (each row: a group/topic/domain dropdown + question count, with Add/Remove row).

> This is a **criteria-based random question selection** model (pick N questions from each chosen group/topic/domain), not a manual question-by-question checklist. See section 12 for this project's chosen approach.

---

## 8. SALES Feature

### 8.1 Registrations List
Shows the list of users' registrations.
- Columns: id, email, registration time, subject, package, total cost, status, valid from, valid to, **last updated by**.
- Filters: subject (searchable), registration time (from/to), status; search by email.
- Sortable by: id, email, registration time, subject, package, total cost, status, valid from, valid to.
- From here the user can add new or edit an existing registration via Registration Details.

### 8.2 Registration Details
Shows detailed registration info: subject, package (with list price/sale price), full name, gender, email, mobile, registration time, sale (handler), status, valid from, valid to, notes. The user can add new or change the registration's status with notes. If the registration was created by the current user, they can also edit the other registration fields.

> **Core business rule**: when changing a registration's status to **Paid**, if no user exists with the registered email, the system creates one and sends back the login information (link, email, password) to that user, along with other notification notes.

---

## 9. ADMIN Feature

| Function | Description |
|---|---|
| Users List | List, filter, search, show, hide, view, add new, or edit existing user(s) |
| User Details | View & edit the details of a specific user |
| Settings List | List, filter, search, show, hide, view, add new, or edit existing system setting(s) |
| Setting Details | View & edit the details of a specific system setting |

Admin also has full access to Course Content and Testing Content (section 6–7, unrestricted by owner) and to Marketing's Dashboard.

---

## 10. Role → Screen matrix (summary)

| Screen | Guest | Customer | Marketing | Sale | Expert | Admin |
|---|---|---|---|---|---|---|
| Home / Blogs / Courses browsing | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Course Register | ✓ | ✓ | – | – | – | – |
| Login / Register | ✓ | – | – | – | – | – |
| Profile / Change Password | – | ✓ | ✓ | ✓ | ✓ | ✓ |
| My Registrations / My Courses / Lesson View / Quiz Lesson / Quiz Handle / Quiz Review | – | ✓ | – | – | – | – |
| Dashboard | – | – | ✓ | ✓ | – | ✓ |
| Posts / Sliders | – | – | ✓ | – | – | ✓* |
| Subjects / Dimension / Lessons / Lesson Details | – | – | – | – | ✓ (assigned only) | ✓ (all) |
| New Subject / Publish-Unpublish / Price Package CRUD | – | – | – | – | – (view-only on Price Package) | ✓ |
| Questions / Quizzes | – | – | – | – | ✓ (assigned only) | ✓ (all) |
| Registrations List / Details | – | – | – | ✓ | – | ✓* |
| Users / Settings | – | – | – | – | – | ✓ |

`*` = the original PDF does not fully specify whether Admin has full CRUD here or only view/oversight — confirm with supervisor if exact behavior matters.

---

## 11. System settings (configurable, not hardcoded)

The following must be managed dynamically through **Settings List / Setting Details** (Admin feature), not hardcoded as Java enums:

| # | Setting type | Known fixed values (from the PDF) |
|---|---|---|
| 1 | User Roles | — |
| 2 | System Menu | — |
| 3 | Post Categories | — |
| 4 | Subject Categories | — |
| 5 | Test Types | **Simulation, Lesson-Quiz** |
| 6 | Question Levels | **Hard, Medium, Easy** |
| 7 | Lesson Types | **Subject Topic, Lesson, Quiz** |
| 8 | Subject Dimension | **Domain, Group** |

## 12. Other requirements (from the PDF)

- **User input data formats** (type, length, validation pattern, etc.) are not fully specified in the PDF — they must be proposed by the project team and **agreed with the supervisor before implementation**. Once agreed, they must be applied consistently in input validation, UI display, and the database schema.

---

## 13. Implementation decisions for THIS specific build (overrides where noted)

This project is being implemented as a demo, with an AI-agent-driven workflow. The following decisions were made during planning and take precedence over the original PDF where they conflict:

| Area | Original PDF | This build's decision |
|---|---|---|
| Database | Not specified (originally built with SQL Server in early planning) | **MySQL 8.x** |
| Package structure | Not specified | **Layer-first, then Role** hybrid (`controller/service/dto` at top level, role sub-packages inside each) — see `SKILL.md` section 2 |
| Security strictness | Standard (email verification, expiring reset links, authorization enforced in UI + backend) | **Simplified for demo**: no email verification, no expiring reset links, CSRF disabled, minimal ownership checks except where explicitly listed as a business rule — see `SKILL.md` section 5 |
| Quiz editability | "Quiz can be edited only when there is not any test taken yet" | **Overridden**: Expert/Admin have full CRUD on Quiz at any time, including quizzes that already have attempts — see the Quiz CRUD prompt |
| Quiz Details — question selection | Criteria-based random selection (by topic/group/domain, with quantity per group) | **Confirmed: implemented exactly as the PDF describes.** Quiz stores selection *criteria* (not a fixed question list); the actual question set is randomly drawn and persisted per `QuizAttempt` when the customer starts the test — see the updated Quiz CRUD prompt (`Prompt_Quiz_CRUD_Expert_v2.md`) for the entity/service design. |
| Seed/demo data | Not specified | Seeded on first run: sample Subjects/Lessons/Questions/Quizzes, 1 Admin account (`admin@onlinelearn.com`), 1 Customer account (`customer@onlinelearn.com`), optionally 1 Expert account (`expert@onlinelearn.com`) |
| Registration entity | Includes `last updated by` field per PDF | **Must be added** to the `Registration` entity if not already present — this was missed in earlier entity lists and should be corrected |

> Any AI agent implementing a feature should check this section first: if the task touches Quiz editing rules or Quiz question-selection, or the exact Course Content/Marketing admin permissions marked `*` in section 10, confirm the current expectation with the user before assuming the original PDF behavior applies as-is.

---

### How to use this file
Read this file together with `SKILL.md` (coding/architecture/UI conventions) before implementing any feature. This file defines **what** to build; `SKILL.md` defines **how** to build it (package structure, naming, Thymeleaf conventions, UI design system). Where this file's section 13 does not mention a topic, the earlier sections (2–12, sourced from the original requirement PDF) are the authoritative spec.