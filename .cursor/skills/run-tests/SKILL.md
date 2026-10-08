---
name: run-tests
description: Runs SpendSense backend and frontend test suites and reports results. Use when the user says "run tests", "run test cases", "run all tests", "check tests", or wants backend Maven and frontend Vitest output for spendsense.
---

# Run Tests

Runs **both** SpendSense test suites and reports pass/fail output. Do not run only one unless the user asks for backend or frontend tests alone.

**Plan reference:** [.cursor/plans/expense_tracker_mvp_d9134402.plan.md](../../plans/expense_tracker_mvp_d9134402.plan.md) — `## Testing in CI`

**CI commands (from plan):**

1. Backend: `./mvnw test` (or `./gradlew test`) on push/PR
2. Frontend: `npm run test` (Vitest)
3. E2E (optional on PR): `npx playwright test` against local stack
4. **Deploy gate:** Day 16 deploy only if all P0 tests green

E2E (Playwright) is **out of scope** for the default "run tests" prompt — run only when the user explicitly asks for E2E.

## Workflow

1. **Run backend tests** — capture full Maven/Surefire output.
2. **Run frontend tests** — capture full Vitest output.
3. **Summarize for the user:**
   - Backend: total run / failures / errors; list failed test class + method names
   - Frontend: passed / failed file count; list failed test names
   - Overall: **PASS** or **FAIL**
4. **On failure** — quote the relevant error lines; do not claim success.

Run backend then frontend sequentially so output stays readable.

## Commands

### Windows (default for this workspace)

**Backend:**

```powershell
cd spendsense\backend
.\mvnw.cmd test
```

**Frontend:**

```powershell
cd spendsense\frontend
npm run test
```

### macOS / Linux

```bash
cd spendsense/backend && ./mvnw test
```

```bash
cd spendsense/frontend && npm run test
```

`npm test` is equivalent (`vitest run` per `package.json`).

## Prerequisites

- Java 17+ for backend
- Node.js 20+ for frontend
- First-time frontend: `npm install` in `spendsense/frontend` if `node_modules` is missing

## Test environments

| Layer | Tool | Location |
|-------|------|----------|
| Backend unit + integration | JUnit 5, MockMvc, Mockito | `spendsense/backend/src/test/java/` |
| Frontend component | Vitest + React Testing Library | `spendsense/frontend/src/**/*.test.tsx` |

Backend tests use `spendsense/backend/data/test-expenses.db` (see `src/test/resources/application.yml`) — separate from the dev app database.

Test case catalog: `spendsense/docs/test-cases.md`

## Output template

Report results using this structure:

```markdown
## Test Results

### Backend (`mvnw test`)
- Status: PASS | FAIL
- Tests run: N | Failures: N | Errors: N | Skipped: N
- Failed (if any): `ClassName.methodName` — short reason

### Frontend (`npm run test`)
- Status: PASS | FAIL
- Files: N passed | N failed
- Failed (if any): `file.test.tsx` > test name — short reason

### Overall: PASS | FAIL
```

## Troubleshooting

| Issue | Action |
|-------|--------|
| Backend compile errors | Fix source before re-running; read Surefire compile output |
| Frontend `vitest` not found | Run `npm install` in `spendsense/frontend` |
| Flaky auth/expense tests | Ensure test DB is not locked by a running backend on same file |
| User wants E2E | Run `npx playwright test` from frontend with local stack up (see `start-server` skill) |

## Out of scope (unless requested)

- `npx playwright test` — E2E smoke suite
- `npm run test:watch` — watch mode
- Single-test filters (`-Dtest=…`, `vitest run path`)
