CREATE TABLE app_user
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issuer       VARCHAR(2048) NOT NULL,
    subject      VARCHAR(255)  NOT NULL,
    email        VARCHAR(320)  NOT NULL,
    display_name VARCHAR(255)  NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_app_user_issuer_subject UNIQUE (issuer, subject)
);
