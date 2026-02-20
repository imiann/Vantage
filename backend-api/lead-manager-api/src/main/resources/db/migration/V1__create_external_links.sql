-- Migration: Create external_links table
-- This schema matches the ExternalLink JPA Entity exactly.

CREATE TABLE external_links (
                                id BIGSERIAL PRIMARY KEY,
                                url TEXT NOT NULL,
                                status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Optimization: Add an index on status.
-- In a distributed system, workers will frequently query for 'PENDING' links.
-- An index prevents full table scans and keeps the system fast as it scales.
CREATE INDEX idx_links_status ON external_links(status);