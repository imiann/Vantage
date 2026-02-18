These rules should be included in prompts when asking automated agents (or humans) to write code for this project. They enforce language, testing, style, and safety constraints.

Primary Guidelines

- **Primary Language & Frameworks:** Java 21 with Spring Boot 3.4 for backend; React + TypeScript (Vite) for frontend.
- **Database:** PostgreSQL. Use Spring Data JPA naming conventions and relational schema with foreign keys.
- **Migrations:** Use Flyway or Liquibase and keep migrations in `src/main/resources/db/migration`.

Coding Style & Structure

- **Simplicity:** Prefer clear, direct implementations over clever abstractions.
- **Comments:** Add brief JavaDoc for public service methods; keep inline comments minimal and purposeful.
- **Naming:** Use descriptive names; entities in singular (`Lead`, `Client`, `Project`). Repositories named `LeadRepository` etc.

Testing Requirements

- **Unit Tests:** Every service method must have a JUnit 5 test. Use Mockito to mock repository dependencies.
- **Controller Tests:** Include at least one integration-style test per controller using `@WebMvcTest` or `@SpringBootTest` with `TestRestTemplate` for critical endpoints.
- **Frontend Tests:** Use React Testing Library for critical UI components (Quick Add and Lead List) when applicable.

Backend Implementation Rules

- **Entities:** Use JPA annotations, `UUID` primary keys, `@Enumerated` for enums, and `LocalDateTime` for timestamps.
- **Repositories:** Extend `JpaRepository`. Add well-named methods (`countByStatus`, `findByFollowUpDateBeforeAndStatusNot`, etc.).
- **Services:** Keep services focused; single responsibility per service. Validate input at controller/service boundary and throw specific exceptions for invalid data.
- **Exception Handling:** Use a global `@ControllerAdvice` to translate exceptions into proper HTTP responses.

API & Documentation

- **OpenAPI:** Use SpringDoc to generate OpenAPI doc. Ensure controller methods have `@Operation` summaries where non-obvious.

Frontend Rules

- **Components:** Use functional components with TypeScript; all props typed using interfaces.
- **State:** Use `useState` and `useEffect` for local state; avoid global state libraries until necessary.
- **Styling:** Tailwind CSS only. Keep styles in classes on elements rather than separate CSS files unless complex.

DevOps & CI

- **Docker:** Provide a `Dockerfile` for the backend and a `docker-compose.yml` to run app + postgres locally.
- **CI:** GitHub Actions should run unit tests for backend and basic build/lint for frontend on push.

Commit & PR Guidelines

- **Commit messages:** Use imperative, concise messages: `feat: add LeadService convertToClient`, `fix: handle null followUpDate`.
- **PRs:** Each PR should include a brief description, link to related issue or todo, and list of tests added/updated.

Agent Interaction Hints

- When asked to implement a feature, first output a short plan (2–5 bullets) of files to change, tests to add, and commands to run.
- Always include unit tests alongside code changes. Mark one todo in `ME_PROGRESS.md` when you finish a step.

Safety & Licensing

- Avoid suggesting or embedding copyrighted content without attribution. Use permissive example data.

This file is a machine- and human-readable contract for agents working on this repository.
