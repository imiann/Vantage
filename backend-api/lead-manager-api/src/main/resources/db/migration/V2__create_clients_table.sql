-- V2__create_clients_table.sql
-- Creates the clients table. A client may be created manually or via lead conversion.
-- Hard deletion is blocked at the service layer if the client has any associated projects.

CREATE TABLE clients (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    full_name           VARCHAR(255)    NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    phone               VARCHAR(50),
    company             VARCHAR(255)    NOT NULL,
    company_number      VARCHAR(100),
    address             TEXT,
    logo_url            VARCHAR(2000),
    primary_contact     VARCHAR(255),
    notes               TEXT,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    converted_from_id   UUID,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,

    CONSTRAINT pk_clients PRIMARY KEY (id),
    CONSTRAINT fk_clients_lead FOREIGN KEY (converted_from_id) REFERENCES leads (id) ON DELETE SET NULL,
    CONSTRAINT chk_clients_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);
