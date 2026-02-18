# ME Progress (Personal Tracker)

Use this file as the single place to see current progress, mark tasks done, and run quick verification commands.

## How to use

- Edit this file to update statuses. Prefer short notes and dates.
- After finishing a step in `Plan.md`, set the corresponding row to `Done` and add a one-line note about verification (tests passed / swagger visible).

## Status Table

- **Column meanings:** `Step` matches `Plan.md`; `Status` = Not started / In progress / Done; `Checked` = quick verification summary.

| Step                                  |      Status | Checked |
| ------------------------------------- | ----------: | ------- |
| Step 1: Environment & Project Init    | Not started |         |
| Step 2: Database & Entities           | Not started |         |
| Step 3: Backend Logic (Service Layer) | Not started |         |
| Step 4: API Layer & Swagger           | Not started |         |
| Step 5: Frontend Scaffolding          | Not started |         |
| Step 6: Lead Tracking Dashboard       | Not started |         |
| Step 7: Analytics Integration         | Not started |         |
| Step 8: DevOps & CI/CD                | Not started |         |
| Step 9: AWS Deployment                | Not started |         |

## Quick Commands

Copyable commands for common checks:

```bash
# Start DB
docker-compose up -d db

# Run backend tests
./mvnw test

# Run frontend dev server (from frontend dir)
npm install
npm run dev

# Run full local stack (db + backend)
docker-compose up -d db
./mvnw spring-boot:run
```

## Commit & PR workflow (examples)

```bash
git checkout -b feat/lead-crud
git add .
git commit -m "feat: add Lead entity and repository"
git push --set-upstream origin feat/lead-crud
# Create PR and reference Plan.md step
```

## Notes

- After completing each step, update this file and the `manage_todo_list` in repo metadata if you use automation.
