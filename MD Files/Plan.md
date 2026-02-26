# Implementation Plan — Vantage Phased Roadmap

Strict Rule: Complete one step fully, including tests and verification, before moving to the next.

---

## Phase 0: Infrastructure & Link Validation (Introductory — Complete)

> These steps established the foundational infrastructure and the async link-validation pipeline.

### Step 0.1 — Environment & Infrastructure Init ✅

- **Goal:** Initialize Spring Boot with Postgres and Redis.
- **Tasks:**
  - Spring Boot project (Java 21, Web, Data JPA, Data Redis, PostgreSQL, Flyway, Micrometer).
  - `docker-compose.yml` with PostgreSQL and Redis.
  - Health Check endpoint showing Redis & DB status.
- **Status:** Complete.

### Step 0.2 — Link Schema & Migrations ✅

- **Goal:** Define `ExternalLink` persistence and Redis messaging.
- **Tasks:**
  - `ExternalLink` entity (id, url, status: PENDING | VALIDATED | BROKEN).
  - `LinkValidationTask` DTO for Redis queue.
  - Flyway migration `V1__create_external_links.sql`.
- **Status:** Complete.

### Step 0.3 — Link Service Layer & Workers ✅

- **Goal:** Async link-validation flow.
- **Tasks:**
  - `LinkProducerService` → push events to Redis.
  - `LinkConsumerWorker` → consume from Redis, HEAD-request, update DB.
- **Status:** Complete. Unit tests passing.

### Step 0.4 — Link API & Async Response ✅

- **Goal:** Non-blocking link creation endpoint.
- **Tasks:**
  - `POST /api/links` returns `202 Accepted` after queuing.
  - Integration test confirming 202 + background processing.
- **Status:** Complete.

### Step 0.5 — DevOps & Observability ✅

- **Goal:** Dockerized services + Micrometer metrics.
- **Tasks:**
  - Separate Docker images for API and Worker.
  - Health check reporting queue depth.
  - `link.validation.latency` metric.
- **Status:** Complete.

---

## Phase 1: Lead CRM — Backend

### Step 1.1 — Lead Entity & Migration

- **Goal:** Create the `Lead` data model.
- **Tasks:**
  - `Lead` entity fields:
    - `id` — UUID, primary key, auto-generated.
    - `name` — String, **required**.
    - `email` — String, **required**.
    - `phone` — String, nullable.
    - `company` — String, nullable.
    - `source` — String, nullable (e.g. "Instagram", "Referral").
    - `notes` — TEXT, nullable.
    - `status` — Enum (`INBOUND` _(default)_, `OUTBOUND`, `FOLLOW_UP`, `CONVERTED`, `LOST`). `CONVERTED` and `LOST` are terminal.
    - `followUpDate` — LocalDateTime, nullable. Set when scheduling a follow-up.
    - `convertedAt` — LocalDateTime, nullable. Auto-stamped by service when status → `CONVERTED`.
    - `createdAt` / `updatedAt` — LocalDateTime, auto-managed by JPA auditing (`@CreatedDate` / `@LastModifiedDate`).
  - Flyway migration `V2__create_leads.sql`.
  - `LeadRepository` extending `JpaRepository` with query methods (`countByStatus`, `findByStatus`, `findByFollowUpDateBefore`, etc.).
- **Verify:** Schema created, app starts cleanly.

### Step 1.2 — Lead Service & Validation

- **Goal:** Business logic for lead CRUD and status transitions.
- **Tasks:**
  - `LeadService`: create, update, delete, get, list, change status.
  - Validate status transitions (e.g., LOST → CONVERTED not allowed).
  - Unit tests with Mockito for every service method.
- **Verify:** All unit tests pass.

### Step 1.3 — Lead REST API

- **Goal:** Full CRUD + status-change endpoints.
- **Tasks:**
  - `LeadController`:
    - `POST /api/leads` — create lead.
    - `GET /api/leads` — list all (with optional status filter).
    - `GET /api/leads/{id}` — get single lead.
    - `PUT /api/leads/{id}` — update lead.
    - `DELETE /api/leads/{id}` — delete lead.
    - `PATCH /api/leads/{id}/status` — change status.
  - SpringDoc `@Operation` annotations.
  - Controller integration test (`@WebMvcTest`).
- **Verify:** Swagger UI shows all endpoints. Integration test passes.

---

## Phase 2: Client & Project Management — Backend

### Step 2.1 — Client Entity & Migration

- **Goal:** Create the `Client` data model.
- **Tasks:**
  - `Client` entity fields:
    - `id` — UUID, primary key, auto-generated.
    - `fullName` — String, **required**. Full legal name for invoices.
    - `email` — String, **required**.
    - `phone` — String, nullable.
    - `company` — String, **required**. Trading or company name.
    - `companyNumber` — String, nullable. Business registration number for invoicing.
    - `address` — TEXT, nullable. Billing address for invoicing.
    - `logoUrl` — String, nullable. URL to client logo image.
    - `primaryContact` — String, nullable. Name/role of main point of contact.
    - `notes` — TEXT, nullable.
    - `convertedFromId` — UUID (FK → `leads.id`), nullable. Auto-set on lead conversion; `null` for manually created clients.
    - `createdAt` / `updatedAt` — Auto-managed by JPA auditing.
  - Flyway migration `V3__create_clients.sql`.
  - `ClientRepository` with query methods.
  - Cascade: deleting a `Client` cascade-deletes all their `Project`s.
- **Verify:** Schema created, app starts cleanly.

### Step 2.2 — Client Service & Lead Conversion

- **Goal:** Client CRUD + converting a Lead into a Client.
- **Tasks:**
  - `ClientService`: create, update, delete, get, list.
  - `convertLeadToClient(leadId)`: creates a skeleton `Client` from the lead's name/email/phone/company, sets lead status to `CONVERTED`.
  - Unit tests for all methods including conversion.
- **Verify:** All unit tests pass.

### Step 2.3 — Client REST API

- **Goal:** Full CRUD + conversion endpoint.
- **Tasks:**
  - `ClientController`:
    - `POST /api/clients` — create client.
    - `GET /api/clients` — list all.
    - `GET /api/clients/{id}` — get single client.
    - `PUT /api/clients/{id}` — update client.
    - `DELETE /api/clients/{id}` — delete client.
    - `POST /api/leads/{id}/convert` — convert lead → client.
  - Controller integration test.
- **Verify:** Swagger UI shows all endpoints. Tests pass.

### Step 2.4 — Project Entity & Migration

- **Goal:** Create the `Project` data model linked to Client.
- **Tasks:**
  - `Project` entity fields:
    - `id` — UUID, primary key, auto-generated.
    - `clientId` — UUID (FK → `clients.id`), **required**. Cannot be null or changed after creation.
    - `title` — String, **required**.
    - `description` — TEXT, nullable.
    - `status` — Enum (`UPCOMING` _(default)_, `ACTIVE`, `IN_REVIEW`, `DELIVERED`, `ARCHIVED`).
    - `price` — BigDecimal, nullable. Agreed project price; used by financial dashboard later.
    - `currency` — String, nullable, **defaults to `'CAD'`**. ISO 4217 code (e.g. `CAD`, `USD`, `EUR`). Stored alongside `price`.
    - `notes` — TEXT, nullable.
    - `createdAt` / `updatedAt` — Auto-managed by JPA auditing.
  - Flyway migration `V4__create_projects.sql` with FK to `clients` and default `currency = 'CAD'`.
  - Update `ExternalLink` to optionally reference `projectId` (nullable FK to `projects`).
  - `ProjectRepository`.
  - Cascade: deleting a `Project` cascade-deletes all its `ExternalLink`s.
- **Verify:** Schema created with FK constraints. App starts cleanly.

### Step 2.5 — Project Service

- **Goal:** Project CRUD with client association.
- **Tasks:**
  - `ProjectService`: create (requires clientId), update, delete, get, list, list by client.
  - Price is stored but not aggregated yet.
  - Unit tests.
- **Verify:** All unit tests pass.

### Step 2.6 — Project REST API

- **Goal:** Full CRUD for projects.
- **Tasks:**
  - `ProjectController`:
    - `POST /api/projects` — create project (clientId in body).
    - `GET /api/projects` — list all (optional clientId filter).
    - `GET /api/projects/{id}` — get single project.
    - `PUT /api/projects/{id}` — update project.
    - `DELETE /api/projects/{id}` — delete project.
  - Controller integration test.
- **Verify:** Swagger UI shows all endpoints. Tests pass.

---

## Phase 3: Frontend — Lead CRM

### Step 3.1 — Frontend Scaffolding

- **Goal:** React + TypeScript + Vite foundation.
- **Tasks:**
  - Initialize Vite project with React + TypeScript.
  - Install Tailwind CSS, Axios, React Router.
  - Create layout shell: sidebar navigation, header, main content area.
  - API client utility (`src/api/client.ts`) with Axios base config.
- **Verify:** `npm run dev` serves the app. Layout renders.

### Step 3.2 — Lead List View

- **Goal:** Display all leads in a table/list with status badges.
- **Tasks:**
  - `LeadList` component: fetch from `GET /api/leads`, display in a styled table.
  - Status badges with colour coding per lifecycle stage.
  - Status filter dropdown.
- **Verify:** Leads load from backend and display correctly.

### Step 3.3 — Lead Create / Edit Forms

- **Goal:** Forms to add and edit leads.
- **Tasks:**
  - `LeadForm` component: name, email, phone, company, source, notes, status.
  - Create mode (`POST`) and edit mode (`PUT`).
  - Inline validation (required fields, email format).
- **Verify:** Can create and edit a lead via the UI.

### Step 3.4 — Lead Detail & Conversion

- **Goal:** Lead detail view with conversion action.
- **Tasks:**
  - `LeadDetail` component: show full lead info.
  - "Convert to Client" button → calls `POST /api/leads/{id}/convert`.
  - Confirmation modal before conversion.
  - Status-change buttons/dropdown for lifecycle transitions.
- **Verify:** Can view a lead, change its status, and convert it.

---

## Phase 4: Frontend — Client & Project Views

### Step 4.1 — Client Card Grid

- **Goal:** Display clients as cards.
- **Tasks:**
  - `ClientList` component: card grid layout.
  - Each card shows: logo (or placeholder), company name, primary contact name, email, current project count.
  - Click card → client detail page.
- **Verify:** Clients render as cards. Link to detail works.

### Step 4.2 — Client Detail & Edit

- **Goal:** Full client detail view with editing.
- **Tasks:**
  - `ClientDetail` component: all fields (fullName, email, phone, company, companyNumber, address, notes, logo).
  - Edit mode inline or via modal.
  - Section showing linked projects.
- **Verify:** Can view and edit client details.

### Step 4.3 — Project List & Create

- **Goal:** Project management UI.
- **Tasks:**
  - `ProjectList` component: table or card view showing title, client, status, price.
  - `ProjectForm` component: create/edit with client selector, status, price, notes.
  - Status badges with colour coding.
- **Verify:** Can create, view, and edit projects.

### Step 4.4 — Project Detail & Linked Resources

- **Goal:** Project detail page with external links.
- **Tasks:**
  - `ProjectDetail` component: full project info, price, status.
  - Linked external links section showing validation status.
  - Ability to add new external links to a project.
- **Verify:** Project detail renders with link statuses.

---

## Phase 5: Polish & Integration

### Step 5.1 — Navigation & Routing

- **Goal:** Complete navigation between all views.
- **Tasks:**
  - React Router routes for: `/leads`, `/leads/:id`, `/clients`, `/clients/:id`, `/projects`, `/projects/:id`.
  - Active-state sidebar highlighting.
  - Breadcrumb navigation.
- **Verify:** All routes work. Navigation feels cohesive.

### Step 5.2 — Global Error Handling & Loading States

- **Goal:** Consistent UX for errors and loading.
- **Tasks:**
  - Backend: `@ControllerAdvice` for global exception → HTTP response mapping.
  - Frontend: error boundary, toast notifications, skeleton loaders.
- **Verify:** API errors surface gracefully in the UI.

### Step 5.3 — End-to-End Smoke Test

- **Goal:** Validate the full workflow.
- **Tasks:**
  - Scripted flow: create lead → convert to client → create project → add link → verify link validated.
  - Docker-compose full stack test.
- **Verify:** Entire flow works end-to-end.

---

## Future (Post-V1)

- **Financial Dashboard:** Aggregate project revenue, tax set-aside calculator, donut/line charts, per-client toggle.
- **Filters & Search:** Advanced filtering across leads, clients, projects.
- **Invoicing:** Generate invoices from client + project data.
- **Social / Integrations:** External service connections.

---

## Timeline & Quality Gate

- **Estimated Duration:** 8–12 weeks for a solo developer.
- **Standard:** Every step requires JUnit 5 tests (backend) or React Testing Library tests (frontend). Coverage target > 80%.
