# Implementation Plan — Vantage Phased Roadmap

Strict Rule: Complete one step fully, including tests and verification, before moving to the next.

## Phase 1: Infrastructure & Core Logic

### Step 1 — Environment & Infrastructure Init

- **Goal:** Initialize Spring Boot with Postgres and Redis.
- **Tasks:**
  - Create Spring Boot project (Java 21, Web, Data JPA, Data Redis, PostgreSQL, Flyway, Micrometer).
  - Create `docker-compose.yml` with PostgreSQL and Redis.
  - Implement basic Health Check endpoint showing Redis & DB status.
- **Verify:**
  - `docker-compose up -d` starts successfully.
  - Logs show successful connection to both SQL database and Redis.

### Step 2 — Database & Message Schema

- **Goal:** Define persistence and messaging structures.
- **Tasks:**
  - Add `ExternalLink` entity (id, url, status ENUM: PENDING, VALIDATED, BROKEN).
  - Create `Message` DTO (Data Transfer Object) for Redis queue.
  - Add Flyway migrations for the new entities.
- **Verify:**
  - Database schema contains `external_links` table.

### Step 3 — Service Layer & Background Workers

- **Goal:** Implement the asynchronous validation flow.
- **Tasks:**
  - Implement `LinkProducerService` to push events to Redis.
  - Implement `LinkConsumerWorker` to listen to Redis.
  - Logic: Consumer performs a HEAD request to validate the link and updates the DB.
- **Verify:**
  - Unit tests for Producer/Consumer using `MockRedis` or Testcontainers.

## Phase 2: Performance & Scalability

### Step 4 — API Layer & Asynchrony

- **Goal:** Implement non-blocking I/O for project creation.
- **Tasks:**
  - Implement `POST /api/projects` endpoint.
  - Ensure it returns `202 Accepted` immediately after queuing the link.
- **Verify:**
  - Integration test confirming immediate 202 response while worker processes link in background.

### Step 5 — Frontend Foundation

- **Goal:** React + TypeScript scaffolding. (Placeholder from original plan)
- **Tasks:** Use Vite, Tailwind, and Axios to build the initial layout.

### Step 6 — Dashboards & Links

- **Goal:** Display link statuses in the UI.

### Step 7 — Advanced Features

- **Goal:** Conversion logic and revenue tracking. (Integration from original plan)

### Step 8 — DevOps & Observability

- **Goal:** Production-grade setup and monitoring.
- **Tasks:**
  - Dockerize API Service and Worker Service separately.
  - Add Health Check reporting queue depth.
  - Use Micrometer to track the time from "Pending" to "Validated".
- **Verify:**
  - Separate Docker images built.
  - Micrometer metrics visible at `/actuator/metrics`.

## Timeline & Quality Gate

- **Estimated Duration:** 4 - 6 weeks for a solo developer.
- **Standard:** Every step requires JUnit 5 tests. Coverage target > 80%.
