-- V3__create_projects_table.sql
-- Creates the projects table. Each project belongs to a client.
-- Hard deletion is blocked at the service layer for projects beyond UPCOMING status.

CREATE TABLE projects (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    client_id   UUID            NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    description TEXT,
    status      VARCHAR(20)     NOT NULL DEFAULT 'UPCOMING',
    price       NUMERIC(12, 2),
    currency    VARCHAR(3)               DEFAULT 'CAD',
    notes       TEXT,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,

    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT fk_projects_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT chk_projects_status CHECK (status IN ('UPCOMING', 'ACTIVE', 'IN_REVIEW', 'DELIVERED', 'ARCHIVED'))
);
