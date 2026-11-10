/* =====================================================================
   OnlineLearn LMS - Physical database (Microsoft SQL Server)
   Matches "3. Database Design" (3.1 - 3.20) of the OnlineLearnSystem SDD.
   20 tables. Run in SSMS / Azure Data Studio (sqlcmd: the GO separators are required).
   ===================================================================== */

IF DB_ID(N'OnlineLearnDB') IS NULL
    CREATE DATABASE OnlineLearnDB;
GO
USE OnlineLearnDB;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;   -- required for the filtered UNIQUE indexes below (sqlcmd defaults it OFF)
GO

/* ---------- drop in reverse dependency order (safe re-run) ---------- */
DROP TABLE IF EXISTS dbo.payment_transactions;
DROP TABLE IF EXISTS dbo.sliders;
DROP TABLE IF EXISTS dbo.posts;
DROP TABLE IF EXISTS dbo.quiz_answers;
DROP TABLE IF EXISTS dbo.quiz_attempts;
DROP TABLE IF EXISTS dbo.quiz_questions;
DROP TABLE IF EXISTS dbo.question_dimensions;
DROP TABLE IF EXISTS dbo.question_options;
DROP TABLE IF EXISTS dbo.questions;
DROP TABLE IF EXISTS dbo.lesson_progress;
DROP TABLE IF EXISTS dbo.course_access;
DROP TABLE IF EXISTS dbo.registrations;
DROP TABLE IF EXISTS dbo.lessons;
DROP TABLE IF EXISTS dbo.quizzes;
DROP TABLE IF EXISTS dbo.price_packages;
DROP TABLE IF EXISTS dbo.subject_dimensions;
DROP TABLE IF EXISTS dbo.subjects;
DROP TABLE IF EXISTS dbo.settings;
DROP TABLE IF EXISTS dbo.auth_tokens;
DROP TABLE IF EXISTS dbo.users;
GO

/* 3.1 Users */
CREATE TABLE dbo.users (
    user_id            BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_users PRIMARY KEY,
    full_name          NVARCHAR(150)  NOT NULL,
    gender             NVARCHAR(20)   NULL,
    email              NVARCHAR(254)  NOT NULL CONSTRAINT UQ_users_email UNIQUE,
    mobile             VARCHAR(20)    NULL,
    password_hash      NVARCHAR(255)  NULL,          -- never store plaintext password
    role_code          VARCHAR(30)    NOT NULL,      -- 6 account roles (Guest has no user row)
    status             VARCHAR(20)    NOT NULL CONSTRAINT DF_users_status DEFAULT 'Active',
    email_verified_at  DATETIME2      NULL,
    avatar_url         NVARCHAR(500)  NULL,
    address            NVARCHAR(255)  NULL,
    created_at         DATETIME2      NOT NULL CONSTRAINT DF_users_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_users_status CHECK (status IN ('Active', 'Inactive'))
);
GO

/* 3.2 Auth Tokens */
CREATE TABLE dbo.auth_tokens (
    token_id     BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_auth_tokens PRIMARY KEY,
    user_id      BIGINT        NOT NULL CONSTRAINT FK_auth_tokens_user REFERENCES dbo.users(user_id) ON DELETE CASCADE,
    token_hash   VARCHAR(128)  NOT NULL CONSTRAINT UQ_auth_tokens_hash UNIQUE,
    purpose      VARCHAR(25)   NOT NULL,
    expires_at   DATETIME2     NOT NULL,
    consumed_at  DATETIME2     NULL,
    CONSTRAINT CK_auth_tokens_purpose CHECK (purpose IN ('VERIFY_EMAIL', 'RESET_PASSWORD'))
);
GO

/* 3.3 Settings */
CREATE TABLE dbo.settings (
    setting_id   BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_settings PRIMARY KEY,
    type         VARCHAR(50)    NOT NULL,            -- Role, category, level, type, ...
    value        NVARCHAR(200)  NOT NULL,
    sort_order   INT            NULL,
    description  NVARCHAR(500)  NULL,
    status       VARCHAR(20)    NOT NULL CONSTRAINT DF_settings_status DEFAULT 'Active',
    CONSTRAINT UQ_settings_type_value UNIQUE (type, value),
    CONSTRAINT CK_settings_status CHECK (status IN ('Active', 'Inactive'))
);
GO

/* 3.4 Subjects */
CREATE TABLE dbo.subjects (
    subject_id           BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_subjects PRIMARY KEY,
    name                 NVARCHAR(200)  NOT NULL,
    owner_user_id        BIGINT         NOT NULL CONSTRAINT FK_subjects_owner REFERENCES dbo.users(user_id),
    category_setting_id  BIGINT         NOT NULL CONSTRAINT FK_subjects_category REFERENCES dbo.settings(setting_id),
    tagline              NVARCHAR(255)  NULL,
    thumbnail_url        NVARCHAR(500)  NULL,
    description          NVARCHAR(MAX)  NULL,
    featured             BIT            NOT NULL CONSTRAINT DF_subjects_featured DEFAULT 0,
    publish_status       VARCHAR(20)    NOT NULL CONSTRAINT DF_subjects_publish DEFAULT 'Unpublished',
    updated_at           DATETIME2      NOT NULL CONSTRAINT DF_subjects_updated DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_subjects_publish CHECK (publish_status IN ('Published', 'Unpublished'))
);
GO

/* 3.5 Subject Dimensions */
CREATE TABLE dbo.subject_dimensions (
    dimension_id     BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_subject_dimensions PRIMARY KEY,
    subject_id       BIGINT         NOT NULL CONSTRAINT FK_dimensions_subject REFERENCES dbo.subjects(subject_id),
    type_setting_id  BIGINT         NOT NULL CONSTRAINT FK_dimensions_type REFERENCES dbo.settings(setting_id),
    name             NVARCHAR(150)  NOT NULL,
    description      NVARCHAR(500)  NULL
);
GO

/* 3.6 Price Packages */
CREATE TABLE dbo.price_packages (
    package_id       BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_price_packages PRIMARY KEY,
    subject_id       BIGINT          NOT NULL CONSTRAINT FK_packages_subject REFERENCES dbo.subjects(subject_id),
    name             NVARCHAR(150)   NOT NULL,
    duration_months  INT             NOT NULL,
    list_price       DECIMAL(18,2)   NOT NULL,
    sale_price       DECIMAL(18,2)   NOT NULL,
    description      NVARCHAR(500)   NULL,
    status           VARCHAR(20)     NOT NULL CONSTRAINT DF_packages_status DEFAULT 'Active',
    CONSTRAINT CK_packages_duration CHECK (duration_months > 0),
    CONSTRAINT CK_packages_price CHECK (list_price >= 0 AND sale_price >= 0 AND sale_price <= list_price),
    CONSTRAINT CK_packages_status CHECK (status IN ('Active', 'Inactive'))
);
GO

/* 3.14 Quizzes (created before lessons because lessons.quiz_id references it) */
CREATE TABLE dbo.quizzes (
    quiz_id               BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_quizzes PRIMARY KEY,
    subject_id            BIGINT          NOT NULL CONSTRAINT FK_quizzes_subject REFERENCES dbo.subjects(subject_id),
    name                  NVARCHAR(200)   NOT NULL,
    quiz_type_setting_id  BIGINT          NOT NULL CONSTRAINT FK_quizzes_type REFERENCES dbo.settings(setting_id),
    level_setting_id      BIGINT          NULL     CONSTRAINT FK_quizzes_level REFERENCES dbo.settings(setting_id),
    duration_minutes      INT             NOT NULL,
    pass_rate             DECIMAL(5,2)    NOT NULL,
    description           NVARCHAR(MAX)   NULL,
    CONSTRAINT CK_quizzes_duration CHECK (duration_minutes > 0),
    CONSTRAINT CK_quizzes_pass_rate CHECK (pass_rate BETWEEN 0 AND 100)
);
GO

/* 3.7 Lessons */
CREATE TABLE dbo.lessons (
    lesson_id        BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_lessons PRIMARY KEY,
    subject_id       BIGINT          NOT NULL CONSTRAINT FK_lessons_subject REFERENCES dbo.subjects(subject_id),
    package_id       BIGINT          NULL     CONSTRAINT FK_lessons_package REFERENCES dbo.price_packages(package_id),
    parent_topic_id  BIGINT          NULL     CONSTRAINT FK_lessons_topic REFERENCES dbo.lessons(lesson_id),
    quiz_id          BIGINT          NULL     CONSTRAINT FK_lessons_quiz REFERENCES dbo.quizzes(quiz_id),
    name             NVARCHAR(200)   NOT NULL,
    lesson_type      VARCHAR(30)     NOT NULL,
    sort_order       INT             NOT NULL,
    video_url        NVARCHAR(500)   NULL,
    html_content     NVARCHAR(MAX)   NULL,
    status           VARCHAR(20)     NOT NULL CONSTRAINT DF_lessons_status DEFAULT 'Active',
    CONSTRAINT CK_lessons_type CHECK (lesson_type IN ('Subject Topic', 'Lesson', 'Quiz')),
    CONSTRAINT CK_lessons_quiz CHECK (lesson_type = 'Quiz' OR quiz_id IS NULL),
    CONSTRAINT CK_lessons_status CHECK (status IN ('Active', 'Inactive'))
);
GO

/* 3.8 Registrations */
CREATE TABLE dbo.registrations (
    registration_id   BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_registrations PRIMARY KEY,
    customer_user_id  BIGINT          NULL     CONSTRAINT FK_registrations_customer REFERENCES dbo.users(user_id),
    package_id        BIGINT          NOT NULL CONSTRAINT FK_registrations_package REFERENCES dbo.price_packages(package_id),
    full_name         NVARCHAR(150)   NOT NULL,
    email             NVARCHAR(254)   NOT NULL,
    mobile            VARCHAR(20)     NOT NULL,
    gender            NVARCHAR(20)    NULL,
    total_cost        DECIMAL(18,2)   NOT NULL,
    status            VARCHAR(20)     NOT NULL CONSTRAINT DF_registrations_status DEFAULT 'Submitted',
    registered_at     DATETIME2       NOT NULL CONSTRAINT DF_registrations_at DEFAULT SYSUTCDATETIME(),
    valid_from        DATETIME2       NULL,
    valid_to          DATETIME2       NULL,
    updated_by        BIGINT          NULL     CONSTRAINT FK_registrations_updated_by REFERENCES dbo.users(user_id),
    notes             NVARCHAR(MAX)   NULL,
    updated_at        DATETIME2       NULL,
    CONSTRAINT CK_registrations_status CHECK (status IN ('Submitted', 'Paid', 'Cancelled')),
    CONSTRAINT CK_registrations_cost CHECK (total_cost >= 0),
    CONSTRAINT CK_registrations_validity CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to > valid_from)
);
GO

/* 3.9 Course Access */
CREATE TABLE dbo.course_access (
    access_id        BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_course_access PRIMARY KEY,
    user_id          BIGINT      NOT NULL CONSTRAINT FK_access_user REFERENCES dbo.users(user_id),
    subject_id       BIGINT      NOT NULL CONSTRAINT FK_access_subject REFERENCES dbo.subjects(subject_id),
    registration_id  BIGINT      NOT NULL CONSTRAINT FK_access_registration REFERENCES dbo.registrations(registration_id),
    valid_from       DATETIME2   NOT NULL,
    valid_to         DATETIME2   NOT NULL,
    CONSTRAINT UQ_access_registration UNIQUE (registration_id),   -- one access per registration
    CONSTRAINT CK_access_validity CHECK (valid_to > valid_from)
);
GO

/* 3.10 Lesson Progress */
CREATE TABLE dbo.lesson_progress (
    progress_id   BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_lesson_progress PRIMARY KEY,
    user_id       BIGINT      NOT NULL CONSTRAINT FK_progress_user REFERENCES dbo.users(user_id),
    lesson_id     BIGINT      NOT NULL CONSTRAINT FK_progress_lesson REFERENCES dbo.lessons(lesson_id),
    is_completed  BIT         NOT NULL CONSTRAINT DF_progress_completed DEFAULT 0,
    updated_at    DATETIME2   NOT NULL CONSTRAINT DF_progress_updated DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_progress_user_lesson UNIQUE (user_id, lesson_id)
);
GO

/* 3.11 Questions */
CREATE TABLE dbo.questions (
    question_id       BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_questions PRIMARY KEY,
    subject_id        BIGINT          NOT NULL CONSTRAINT FK_questions_subject REFERENCES dbo.subjects(subject_id),
    lesson_id         BIGINT          NULL     CONSTRAINT FK_questions_lesson REFERENCES dbo.lessons(lesson_id),
    level_setting_id  BIGINT          NOT NULL CONSTRAINT FK_questions_level REFERENCES dbo.settings(setting_id),
    content           NVARCHAR(MAX)   NOT NULL,
    media_url         NVARCHAR(500)   NULL,
    media_type        VARCHAR(20)     NULL,
    explanation       NVARCHAR(MAX)   NULL,
    status            VARCHAR(20)     NOT NULL CONSTRAINT DF_questions_status DEFAULT 'Active',
    CONSTRAINT CK_questions_status CHECK (status IN ('Active', 'Inactive'))
);
GO

/* 3.12 Question Options */
CREATE TABLE dbo.question_options (
    option_id    BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_question_options PRIMARY KEY,
    question_id  BIGINT          NOT NULL CONSTRAINT FK_options_question REFERENCES dbo.questions(question_id) ON DELETE CASCADE,
    content      NVARCHAR(MAX)   NOT NULL,
    is_correct   BIT             NOT NULL CONSTRAINT DF_options_correct DEFAULT 0,
    sort_order   INT             NOT NULL
);
GO
-- exactly one correct option per question is enforced by the application (QuestionService);
-- this index guarantees there is never more than one.
CREATE UNIQUE INDEX UX_options_one_correct ON dbo.question_options(question_id) WHERE is_correct = 1;
GO

/* 3.13 Question Dimensions */
CREATE TABLE dbo.question_dimensions (
    question_id   BIGINT NOT NULL CONSTRAINT FK_qdim_question REFERENCES dbo.questions(question_id) ON DELETE CASCADE,
    dimension_id  BIGINT NOT NULL CONSTRAINT FK_qdim_dimension REFERENCES dbo.subject_dimensions(dimension_id),
    CONSTRAINT PK_question_dimensions PRIMARY KEY (question_id, dimension_id)
);
GO

/* 3.15 Quiz Questions */
CREATE TABLE dbo.quiz_questions (
    quiz_id      BIGINT NOT NULL CONSTRAINT FK_qq_quiz REFERENCES dbo.quizzes(quiz_id) ON DELETE CASCADE,
    question_id  BIGINT NOT NULL CONSTRAINT FK_qq_question REFERENCES dbo.questions(question_id),
    sort_order   INT    NULL,
    CONSTRAINT PK_quiz_questions PRIMARY KEY (quiz_id, question_id)
);
GO

/* 3.16 Quiz Attempts */
CREATE TABLE dbo.quiz_attempts (
    attempt_id    BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_quiz_attempts PRIMARY KEY,
    quiz_id       BIGINT         NOT NULL CONSTRAINT FK_attempts_quiz REFERENCES dbo.quizzes(quiz_id),
    user_id       BIGINT         NOT NULL CONSTRAINT FK_attempts_user REFERENCES dbo.users(user_id),
    started_at    DATETIME2      NOT NULL CONSTRAINT DF_attempts_started DEFAULT SYSUTCDATETIME(),
    expires_at    DATETIME2      NOT NULL,
    submitted_at  DATETIME2      NULL,
    status        VARCHAR(20)    NOT NULL CONSTRAINT DF_attempts_status DEFAULT 'In Progress',
    score         DECIMAL(5,2)   NULL,
    CONSTRAINT CK_attempts_status CHECK (status IN ('In Progress', 'Submitted')),
    CONSTRAINT CK_attempts_score CHECK (score IS NULL OR score BETWEEN 0 AND 100)
);
GO
CREATE INDEX IX_attempts_user_quiz ON dbo.quiz_attempts(user_id, quiz_id, started_at DESC);  -- "latest attempt" lookup (2.6.7)
GO

/* 3.17 Quiz Answers */
CREATE TABLE dbo.quiz_answers (
    answer_id          BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_quiz_answers PRIMARY KEY,
    attempt_id         BIGINT      NOT NULL CONSTRAINT FK_answers_attempt REFERENCES dbo.quiz_attempts(attempt_id) ON DELETE CASCADE,
    question_id        BIGINT      NOT NULL CONSTRAINT FK_answers_question REFERENCES dbo.questions(question_id),
    option_id          BIGINT      NULL     CONSTRAINT FK_answers_option REFERENCES dbo.question_options(option_id),
    marked_for_review  BIT         NOT NULL CONSTRAINT DF_answers_marked DEFAULT 0,
    updated_at         DATETIME2   NOT NULL CONSTRAINT DF_answers_updated DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_answers_attempt_question UNIQUE (attempt_id, question_id)
);
GO

/* 3.18 Posts */
CREATE TABLE dbo.posts (
    post_id              BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_posts PRIMARY KEY,
    author_user_id       BIGINT          NOT NULL CONSTRAINT FK_posts_author REFERENCES dbo.users(user_id),
    category_setting_id  BIGINT          NOT NULL CONSTRAINT FK_posts_category REFERENCES dbo.settings(setting_id),
    title                NVARCHAR(250)   NOT NULL,
    thumbnail_url        NVARCHAR(500)   NULL,
    brief_info           NVARCHAR(500)   NULL,
    body                 NVARCHAR(MAX)   NOT NULL,
    featured             BIT             NOT NULL CONSTRAINT DF_posts_featured DEFAULT 0,
    status               VARCHAR(20)     NOT NULL CONSTRAINT DF_posts_status DEFAULT 'Hidden',
    updated_at           DATETIME2       NOT NULL CONSTRAINT DF_posts_updated DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_posts_status CHECK (status IN ('Visible', 'Hidden'))
);
GO

/* 3.19 Sliders */
CREATE TABLE dbo.sliders (
    slider_id  BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_sliders PRIMARY KEY,
    title      NVARCHAR(250)  NOT NULL,
    image_url  NVARCHAR(500)  NOT NULL,
    backlink   NVARCHAR(500)  NULL,
    status     VARCHAR(20)    NOT NULL CONSTRAINT DF_sliders_status DEFAULT 'Hidden',
    notes      NVARCHAR(500)  NULL,
    CONSTRAINT CK_sliders_status CHECK (status IN ('Visible', 'Hidden'))
);
GO

/* 3.20 Payment Transactions */
CREATE TABLE dbo.payment_transactions (
    payment_id        BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_payment_transactions PRIMARY KEY,
    registration_id   BIGINT          NOT NULL CONSTRAINT FK_payments_registration REFERENCES dbo.registrations(registration_id),
    order_code        VARCHAR(50)     NOT NULL CONSTRAINT UQ_payments_order_code UNIQUE,
    provider          VARCHAR(30)     NOT NULL,
    provider_txn_ref  VARCHAR(100)    NULL,
    amount            DECIMAL(18,2)   NOT NULL,        -- must equal registrations.total_cost (checked by the service)
    currency          CHAR(3)         NOT NULL CONSTRAINT DF_payments_currency DEFAULT 'VND',
    status            VARCHAR(20)     NOT NULL CONSTRAINT DF_payments_status DEFAULT 'Pending',
    checkout_url      NVARCHAR(500)   NULL,
    paid_at           DATETIME2       NULL,
    raw_response      NVARCHAR(MAX)   NULL,             -- verified callback / query data, kept for audit
    created_at        DATETIME2       NOT NULL CONSTRAINT DF_payments_created DEFAULT SYSUTCDATETIME(),
    updated_at        DATETIME2       NOT NULL CONSTRAINT DF_payments_updated DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_payments_status CHECK (status IN ('Pending', 'Success', 'Failed', 'Cancelled', 'Expired')),
    CONSTRAINT CK_payments_amount CHECK (amount > 0)
);
GO
-- provider_txn_ref is UNIQUE but nullable (Pending rows have no gateway reference yet)
CREATE UNIQUE INDEX UX_payments_provider_txn_ref ON dbo.payment_transactions(provider_txn_ref) WHERE provider_txn_ref IS NOT NULL;
GO

/* ---------- supporting indexes on foreign keys used by the list screens ---------- */
CREATE INDEX IX_subjects_owner        ON dbo.subjects(owner_user_id);
CREATE INDEX IX_lessons_subject       ON dbo.lessons(subject_id, sort_order);
CREATE INDEX IX_registrations_customer ON dbo.registrations(customer_user_id);
CREATE INDEX IX_registrations_package ON dbo.registrations(package_id);
CREATE INDEX IX_access_user_subject   ON dbo.course_access(user_id, subject_id, valid_to);
CREATE INDEX IX_questions_subject     ON dbo.questions(subject_id);
CREATE INDEX IX_quizzes_subject       ON dbo.quizzes(subject_id);
CREATE INDEX IX_posts_category        ON dbo.posts(category_setting_id, status);
CREATE INDEX IX_payments_registration ON dbo.payment_transactions(registration_id);
GO

PRINT 'OnlineLearnDB created: 20 tables.';
