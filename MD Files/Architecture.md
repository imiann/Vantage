## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4, Spring Data JPA, Lombok (optional), Spring Security (optional for v1 local auth), SpringDoc OpenAPI (Swagger UI).
- **Database:** PostgreSQL 14+ (local dev via Docker), Flyway or Liquibase for migrations.
- **Frontend:** React (Vite) + TypeScript, Tailwind CSS, Recharts for charts, Axios for HTTP.
- **DevOps:** Docker, docker-compose, GitHub Actions (CI), AWS (RDS, EC2) for production.

## Folder Structure (recommended)

Backend (Maven/Gradle project):

- `src/main/java/com/yourorg/freelanceapp` — application packages
  - `config` — Spring configuration, Swagger, CORS, security
  - `controller` — REST controllers
  - `service` — business logic and transactional services
  - `repository` — Spring Data JPA repositories
  - `model` or `entity` — JPA entities
  - `dto` — request/response DTOs and mappers
  - `exception` — centralized exception handling

- `src/main/resources` — `application.yml`, `db/migration` (Flyway)

Frontend:

- `src/` — React app
  - `components/` — reusable UI components
  - `pages/` — dashboard, leads, clients, projects
  - `services/` — Axios wrappers for API calls
  - `types/` — TypeScript interfaces

## Data Schema

- Entities: `Lead`, `Client`, `Project`.
- Relationships:
  - One `Client` -> Many `Project` (One-to-Many).
  - A `Lead` is independent until conversion. On conversion: create `Client` and initial `Project` and mark `Lead.status = CONVERTED`.

Suggested entity fields (minimal):

- `Lead`:
  - `id: UUID`, `name: String`, `email: String?`, `source: String`, `status: ENUM`, `followUpDate: LocalDateTime?`, `notes: Text`, `createdAt`, `updatedAt`
- `Client`:
  - `id: UUID`, `name: String`, `contactInfo`, `createdAt`, `updatedAt`
- `Project`:
  - `id: UUID`, `clientId: UUID`, `title`, `status: ENUM`, `revenue: BigDecimal`, `versionLinks: JSON/array`, `notes`, `createdAt`, `updatedAt`

Indexes:

- Index `leads(follow_up_date)` and `leads(status)` for fast dashboard queries.
- Index `projects(status)` where revenue and completed queries will filter.

Migrations:

- Keep schema migrations in `src/main/resources/db/migration` (Flyway) and version each change.

## Backend Components

- **Entities & DTOs:** Use JPA entities for persistence and lightweight DTOs for API boundaries.
- **Repositories:** Extend `JpaRepository<T, ID>` and add custom queries as needed (`countByStatus`, `findByFollowUpDateBeforeAndStatusNot` etc.).
- **Services:** Implement business logic (LeadService, ClientService, ProjectService, FinanceService). Unit test each service method with JUnit 5 + Mockito.
- **Controllers:** REST controllers annotated with `@RestController` and `@RequestMapping("/api/..." )`. Use `@CrossOrigin` in dev or configure CORS globally.

Finance Calculations:

- Conversion rate: `conversionRate = convertedLeads / totalLeads` (handle zero division).
- Tax reserve: `taxReserve = totalEarned.multiply(config.taxReservePercent)`; make percent configurable via `application.yml`.

## API & Documentation

- Use SpringDoc OpenAPI to generate documentation. Configure UI at `/swagger-ui/index.html`.

## Local Dev: docker-compose

Example `docker-compose.yml` (skeleton):

```yaml
version: '3.8'
services:
  db:
    image: postgres:14
    environment:
      POSTGRES_DB: freelance_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - '5432:5432'
    volumes:
      - db-data:/var/lib/postgresql/data
volumes:
  db-data:
```

Start dev DB with `docker-compose up -d db`.

## CI/CD

- Provide a `GitHub Actions` workflow to run `mvn -DskipTests=false test` (or Gradle equivalent), and run frontend lint/build steps if present.

## Production Deployment (notes)

- **DB:** Use AWS RDS (PostgreSQL) with automated backups and Multi-AZ for resilience.
- **App:** Package as Docker image and run on EC2 (or ECS) with environment variables for DB and secrets.
- **Secrets:** Use AWS Secrets Manager or environment variables injected by deployment tooling.

## Observability

- Add Spring Actuator for `/actuator/health`, and optionally metrics export to CloudWatch/Prometheus.

## Notes for Agents

- Prefer simple, testable service methods. Each service method must have a JUnit 5 test using Mockito for dependencies.
- Use `UUID` primary keys for portability and ease of creating records in tests.
