-- V1__create_leads_table.sql
-- Creates the leads table representing potential client inquiries (CRM pipeline entry point).

CREATE TABLE leads (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    phone           VARCHAR(50),
    company         VARCHAR(255),
    source          VARCHAR(255),
    notes           TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'INBOUND',
    follow_up_date  TIMESTAMP,
    converted_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL,

    CONSTRAINT pk_leads PRIMARY KEY (id),
    CONSTRAINT chk_leads_status CHECK (status IN ('INBOUND', 'OUTBOUND', 'FOLLOW_UP', 'CONVERTED', 'LOST'))
);
