-- V4__create_external_links_table.sql
-- Creates the external_links table. Links are optionally attached to a project.
-- When a project is deleted, its associated links are cascade-deleted.
-- Async validation updates status and last_checked via the LinkConsumerWorker.

CREATE TABLE external_links (
    id           UUID            NOT NULL DEFAULT gen_random_uuid(),
    project_id   UUID,
    url          VARCHAR(2000)   NOT NULL,
    name         VARCHAR(255),
    status       VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    last_checked TIMESTAMP,
    created_at   TIMESTAMP       NOT NULL,
    updated_at   TIMESTAMP       NOT NULL,

    CONSTRAINT pk_external_links PRIMARY KEY (id),
    CONSTRAINT fk_external_links_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT chk_external_links_status CHECK (status IN ('PENDING', 'VALIDATED', 'BROKEN'))
);
