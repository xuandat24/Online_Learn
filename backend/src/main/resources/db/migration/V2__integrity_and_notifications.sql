SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO
ALTER TABLE users ADD token_version INT NOT NULL CONSTRAINT DF_users_token_version DEFAULT 0;
ALTER TABLE users ADD CONSTRAINT CK_users_role CHECK (role_code IN ('Customer','Admin','Author','Sale','Marketing','Staff'));
ALTER TABLE registrations ADD public_token_hash VARCHAR(64) NULL;
ALTER TABLE registrations ADD duration_months INT NOT NULL CONSTRAINT DF_registration_duration DEFAULT 1;
ALTER TABLE registrations ADD created_by BIGINT NULL CONSTRAINT FK_registration_creator REFERENCES users(user_id);
GO
UPDATE r SET duration_months = p.duration_months FROM registrations r JOIN price_packages p ON r.package_id = p.package_id;
ALTER TABLE registrations ADD CONSTRAINT CK_registration_duration CHECK(duration_months > 0);
ALTER TABLE payment_transactions ADD expires_at DATETIME2 NULL;
ALTER TABLE payment_transactions ALTER COLUMN checkout_url NVARCHAR(2000) NULL;
CREATE UNIQUE INDEX UX_subject_name ON subjects(name);
CREATE UNIQUE INDEX UX_active_attempt ON quiz_attempts(user_id, quiz_id) WHERE status = 'In Progress';
CREATE UNIQUE INDEX UX_pending_payment ON payment_transactions(registration_id) WHERE status = 'Pending';
CREATE TABLE mail_outbox (
    mail_id BIGINT IDENTITY PRIMARY KEY,
    recipient NVARCHAR(254) NOT NULL,
    subject NVARCHAR(250) NOT NULL,
    body NVARCHAR(MAX) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    sent_at DATETIME2 NULL
);
CREATE INDEX IX_mail_pending ON mail_outbox(status, created_at);
