# SpendSense Sprint Day — Reference

## Plan file quick index

File: `.cursor/plans/expense_tracker_mvp_d9134402.plan.md`

| Topic | Section heading |
|-------|-----------------|
| Stack | `## Tech Stack` |
| Folder layout | `## Repository Layout` |
| MVP scope | `## MVP Features (17-day scope)` |
| ER diagram | `## Data Model` |
| API list | `## Key REST API Endpoints` |
| Spring config | `## Spring Boot Configuration Highlights` |
| Test pyramid | `## Testing Strategy` |
| All TC-IDs | `## Test Case Catalog` |
| Sprint tables | `## 17-Day Sprint Calendar` |
| Charts/UI libs | `## Frontend Stack Highlights` |
| Gemini prompts | `## AI Implementation (Gemini via Spring AI)` |
| CSV format | `## CSV Import/Export` |
| Deploy | `## Deployment Notes` |

---

## REST API (full MVP — implement per day)

| Method | Endpoint | Day (typical) |
|--------|----------|---------------|
| POST | `/api/auth/register` | 2 |
| POST | `/api/auth/login` | 2 |
| GET/POST/PUT/DELETE | `/api/categories[/{id}]` | 3 |
| GET/POST/PUT/DELETE | `/api/expenses[/{id}]` | 4–5 |
| GET | `/api/dashboard/summary` | 6–7 |
| POST | `/api/ai/categorize` | 8–9 |
| POST | `/api/ai/insights` | 10 |
| GET | `/api/expenses/export` | 11 |
| POST | `/api/expenses/import` | 12 |

`GET /api/expenses` query params: `from`, `to`, `categoryId`, `page`.

---

## Per-day implementation hints

### Day 1 — Monorepo + test setup
- Backend: `SpendSenseApplication`, `HealthController`, Flyway baseline
- Tests: `SpendSenseApplicationTests`, `HealthControllerTest`, `App.test.tsx`
- CI: `.github/workflows/ci.yml` skeleton

### Day 2 — Auth
- Backend: `User` entity, `AuthController`, `AuthService`, `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`
- Frontend: `LoginPage`, `RegisterPage`, `AuthContext`, `ProtectedRoute`, `api/auth.ts`
- Tests: `AuthControllerTest` (tcA01–tcA04), `LoginPage.test.tsx` (tcA06)

### Day 3 — Categories
- Backend: `Category` entity, V3 migration, `CategoryService.seedDefaults` (10 defaults on register), CRUD
- Frontend: `CategoriesPage`, `api/categories.ts`, route `/categories`
- Tests: `CategoryControllerTest`, `CategoryServiceTest`

### Day 4 — Expense API
- Backend: complete `Expense` entity (V4 migration), `ExpenseRequest`/`ExpenseResponse`, `ExpenseService`, `ExpenseController`
- Validation: amount > 0, ISO date `YYYY-MM-DD`, category belongs to user
- Tests: `ExpenseControllerTest` (tcE01–tcE04), `ExpenseServiceTest` + tcE03 isolation
- No CSV, no dashboard, no expense UI yet

### Day 5 — Expense UI
- Frontend: `ExpensesPage`, `ExpenseForm`, `api/expenses.ts`, route `/expenses`
- Filters: date range, category; edit/delete actions
- Tests: Playwright TC-E05; `ExpenseForm` component test TC-E06

### Day 6 — Dashboard v1
- Backend: `DashboardController`, `DashboardService`, `GET /api/dashboard/summary`
- Response: totals, `byCategory`, `byDay`
- Frontend: pie chart + summary cards on `DashboardPage` (Recharts)
- Tests: `DashboardControllerTest` (tcD01–tcD02), `DashboardServiceTest`

### Day 7 — Dashboard v2
- Bar chart (spend over time); TanStack Query refetch on date change
- Empty and loading states
- Tests: Playwright TC-D03; empty-state component TC-D04

### Day 8 — Gemini setup
- `AiService` interface; `GeminiAiService` (prod/dev); `MockAiService` (`@Profile("test")`)
- Wire mock in `src/test/resources/application.yml`
- Tests: TC-AI01 unit on mock responses

### Day 9 — AI categorize UI
- `POST /api/ai/categorize`; frontend blur handler on expense description
- Tests: TC-AI02 integration

### Day 10 — AI insights
- `InsightCache` entity + migration; `POST /api/ai/insights`; 24h cache
- Frontend insight cards on dashboard
- Tests: TC-AI03 (cache hit), TC-AI04 E2E

### Day 11 — CSV export
- `GET /api/expenses/export?from=&to=` → `text/csv`
- Columns: `date,amount,description,category,note`
- Frontend download button
- Tests: TC-CSV01

### Day 12 — CSV import
- `POST /api/expenses/import` multipart; Apache Commons CSV parser
- Validate rows; return `{ imported, failed, errors }`
- Fixtures: `backend/src/test/resources/test-data/valid-expenses.csv`, `invalid-expenses.csv`
- Tests: TC-CSV02–TC-CSV04

### Day 13 — UX + security
- Toasts, responsive layout (375px), CORS review
- Audit all repositories for `userId` scoping (TC-S01)
- Manual: TC-M01 mobile checklist

### Day 14 — E2E + test docs
- Playwright: register → expense → dashboard → insights
- Complete all P0 rows in `docs/test-cases.md`
- Fix any failing P0 tests

### Day 15 — Performance + CI
- Confirm indexes on `(user_id, expense_date)` and `(user_id, category_id)`
- Pagination on expense list; CI runs full suite on push
- Manual: TC-P01 (100 expenses, dashboard < 2s)

### Day 16 — Deploy
- Railway/Render backend; Vercel frontend
- Env: `GEMINI_API_KEY`, `JWT_SECRET`, `VITE_API_URL`
- Post-deploy smoke; TC-S02 secrets not in frontend bundle

### Day 17 — MVP Launch
- Production smoke; README demo script; final P0 manual regression

---

## Data model (entities)

| Entity | Key fields |
|--------|------------|
| User | id, email, passwordHash |
| Category | id, userId, name, color, isDefault |
| Expense | id, userId, categoryId, amount, expenseDate, description, note, aiSuggestedCategory |
| InsightCache | id, userId, periodStart, periodEnd, contentJson, createdAt |

---

## Test naming conventions

| Type | Location | Naming |
|------|----------|--------|
| Integration | `controller/*Test.java` | `tcA01_register…`, `tcE03_userCannot…` |
| Unit | `service/*Test.java` | descriptive + `tcE03_…` for isolation cases |
| Component | `frontend/src/**/*.test.tsx` | describe TC-ID in comment or test name |
| E2E | `e2e/` or Playwright config | map to TC-E05, TC-D03, etc. |

Shared helper: `AuthTestSupport.registerAndGetToken(emailPrefix)` returns JWT for MockMvc calls.

---

## Commands

```bash
# Backend dev
cd spendsense/backend && ./mvnw spring-boot:run

# Backend tests
cd spendsense/backend && ./mvnw test

# Frontend dev
cd spendsense/frontend && npm run dev

# Frontend tests
cd spendsense/frontend && npm test

# E2E (when configured)
cd spendsense/frontend && npx playwright test
```
