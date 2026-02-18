# Implementation Plan — Step by step

Strict Rule: Complete one step fully, including tests and verification, before moving to the next.

## Step 1 — Environment & Project Init

- Goal: Initialize Spring Boot backend and verify a minimal endpoint.
- Tasks:
  - Create a Spring Boot project with Java 21, Spring Web, Spring Data JPA, PostgreSQL driver, Flyway, SpringDoc OpenAPI.
  - Add a `GET /api/health` or `GET /` Hello World endpoint.
  - Add `application.yml` with datasource placeholders (use env vars for password).
  - Add a simple unit test for the controller.
- Verify:
  - `./mvnw spring-boot:run` starts and `/actuator/health` or `GET /api/health` returns 200.

Quick commands (macOS/zsh):

```bash
# start local DB (optional)
docker-compose up -d db

# run backend (Maven)
./mvnw spring-boot:run

# run tests
./mvnw test
```

## Step 2 — Database & Entities

- Goal: Model `Lead`, `Client`, `Project` with JPA and create migrations.
- Tasks:
  - Create JPA entities with fields described in `Architecture.md`.
  - Add Flyway migration SQL files to create tables and indexes.
  - Add basic `Repository` interfaces (extend `JpaRepository`).
- Verify:
  - Migrations run on app start and tables exist in DB.
  - Repository unit tests using an embedded DB or testcontainers.

## Step 3 — Backend Logic (Service Layer)

- Goal: Implement `LeadService`, `FinanceService`, `ClientService`.
- Tasks:
  - `LeadService`: create, update, find, convertToClient(id).
  - `FinanceService`: totalRevenue(period), taxReserve(percent)
  - Unit tests: Mockito mocks for repositories and edge cases (zero leads, no revenue).
- Verify: all unit tests pass locally.

## Step 4 — API Layer & Swagger

- Goal: Expose CRUD via REST controllers and generate Swagger UI.
- Tasks:
  - `LeadController`, `ClientController`, `ProjectController`, `FinanceController`.
  - Add DTOs and request validation with `@Valid`.
  - Configure SpringDoc for `/swagger-ui/index.html`.
- Verify: Manual testing via Swagger UI and integration tests for endpoints.

## Step 5 — Frontend Scaffolding

- Goal: Initialize Vite + React + TypeScript + Tailwind CSS.
- Tasks:
  - Create components: `LeadList`, `QuickAddModal`, `Dashboard`.
  - Add Axios service to call backend APIs.
- Verify: `npm run dev` serves the app and basic layout loads.

## Step 6 — Lead Tracking Dashboard

- Goal: Implement lead list with overdue highlighting and Quick Add.
- Tasks:
  - Overdue logic: `(lead.followUpDate.isBefore(today) && lead.status != CONVERTED)` sets red text.
  - Add Quick Add form and optimistic UI updates.
- Verify: Manual QA and unit tests for date logic.

## Step 7 — Analytics Integration

- Goal: Add Recharts pie chart for lead statuses and a conversion percent UI.
- Tasks:
  - Create `/api/leads/stats` endpoint returning counts by status.
  - Frontend queries endpoint and renders Pie Chart + percentage.
- Verify: Chart displays correct numbers from seeded test data.

## Step 8 — DevOps & CI/CD

- Goal: Dockerize the backend and add CI pipeline.
- Tasks:
  - `Dockerfile` for Spring Boot app.
  - `docker-compose.yml` to run app + db locally.
  - `.github/workflows/main.yml` to run tests on push.
- Verify: GitHub Action passes on PRs and push.

## Step 9 — AWS Deployment

- Goal: Document steps to provision RDS and deploy the Docker image to EC2.
- Tasks:
  - Build and push Docker image to ECR (or Docker Hub).
  - Provision RDS (Postgres) with proper security groups.
  - Launch EC2 instance and run container with env vars.
- Verify: Production app responds to HTTP requests; health checks pass.

## Testing & Quality Gate

- Unit test coverage target: keep coverage for service classes > 80%.
- Run `./mvnw test` in CI and fail builds on test failures.

## Checklist & Timeline

- Phase 1 (Week 1-2): Steps 1–3 complete with tests.
- Phase 2 (Week 3-4): Steps 4–7 complete, frontend connected to backend.
- Phase 3 (Week 5-6): Steps 8–9 complete, deployed to AWS for production.

---
This `Plan.md` should be treated as the single source of task sequencing; each completed step must be accompanied by passing unit tests and an updated entry in `ME_PROGRESS.md`.
