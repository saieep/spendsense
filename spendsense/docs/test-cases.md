# SpendSense Test Cases

## P0 launch checklist

All cases below must pass before MVP launch (Day 17). Automated coverage is noted per row.

| ID | Feature | Type | Automated test |
|----|---------|------|----------------|
| TC-A01 | Auth register | Integration | `AuthControllerTest.tcA01` |
| TC-A02 | Auth login | Integration | `AuthControllerTest.tcA02` |
| TC-A03 | Auth wrong password | Integration | `AuthControllerTest.tcA03` |
| TC-A04 | Auth no token | Integration | `AuthControllerTest.tcA04` |
| TC-A05 | Auth register UI | E2E | `e2e/auth.spec.ts` |
| TC-C01 | Default categories | Integration | `CategoryControllerTest.tcC01` |
| TC-C02 | Create category | Integration | `CategoryControllerTest.tcC02` |
| TC-C03 | Delete category | Integration | `CategoryControllerTest.tcC03` |
| TC-E01 | Create expense | Integration | `ExpenseControllerTest.tcE01` |
| TC-E02 | Invalid amount | Integration | `ExpenseControllerTest.tcE02` |
| TC-E03 | Expense isolation | Integration | `ExpenseControllerTest.tcE03` |
| TC-E04 | Date filter | Integration | `ExpenseControllerTest.tcE04` |
| TC-E05 | Add expense UI | E2E | `e2e/expenses.spec.ts` |
| TC-D01 | Dashboard totals | Integration | `DashboardControllerTest.tcD01` |
| TC-D02 | Dashboard byDay | Integration | `DashboardControllerTest.tcD02` |
| TC-D03 | Dashboard date range UI | E2E | `e2e/dashboard.spec.ts` |
| TC-AI01 | AI categorize unit | Unit | `AiServiceTest.tcAI01` |
| TC-AI02 | AI categorize API | Integration | `AiControllerTest.tcAI02` |
| TC-AI03 | AI insights cache | Integration | `AiControllerTest.tcAI03` |
| TC-AI04 | AI insights UI | E2E | `e2e/insights.spec.ts` |
| TC-CSV01 | CSV export | Integration | `ExpenseControllerTest.tcCSV01` |
| TC-CSV02 | CSV import valid | Integration | `ExpenseControllerTest.tcCSV02` |
| TC-CSV03 | CSV import partial | Integration | `ExpenseControllerTest.tcCSV03` |
| TC-CSV04 | CSV parser unit | Unit | `CsvImportParserTest.tcCSV04` |
| TC-S01 | userId scoping | Integration | `SecurityControllerTest.tcS01` |
| TC-S02 | JWT secret not in bundle | Manual | Day 16 deploy checklist |
| — | P0 smoke flow | E2E | `e2e/smoke.spec.ts` (register → expense → dashboard → insights) |

## Infrastructure (Day 1)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-INFRA-01 | Bootstrap | Integration | Start Spring Boot test context | Application context loads without error | Yes — `SpendSenseApplicationTests` |
| TC-INFRA-02 | Health API | Integration | `GET /api/health` | `200` with `{ "status": "UP", "app": "SpendSense" }` | Yes — `HealthControllerTest` |
| TC-INFRA-03 | Frontend smoke | Component | Render root `App` component | Unauthenticated user sees login page | Yes — `App.test.tsx` |

## Auth (Day 2 — 24 Jun)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-A01 | Auth | Integration | Register with valid email/password | `201` + JWT in response body | Yes — `AuthControllerTest.tcA01` |
| TC-A02 | Auth | Integration | Login with valid credentials | `200` + JWT | Yes — `AuthControllerTest.tcA02` |
| TC-A03 | Auth | Integration | Login with wrong password | `401` + error message | Yes — `AuthControllerTest.tcA03` |
| TC-A04 | Auth | Integration | `GET /api/expenses` without token | `401` | Yes — `AuthControllerTest.tcA04` |
| TC-A05 | Auth | E2E | Register via UI with valid email/password | Redirect to dashboard | Yes — `e2e/auth.spec.ts` |
| TC-A06 | Auth | Component | Submit login with invalid credentials | Error message shown on form | Yes — `LoginPage.test.tsx` |

## Categories (Day 3 — 25 Jun)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-C01 | Categories | Integration | Register new user, `GET /api/categories` | 10 default categories returned | Yes — `CategoryControllerTest.tcC01` |
| TC-C02 | Categories | Integration | `POST /api/categories` with valid data | `201` + custom category in response | Yes — `CategoryControllerTest.tcC02` |
| TC-C03 | Categories | Integration | `DELETE /api/categories/{id}` with no linked expenses | `204` No Content | Yes — `CategoryControllerTest.tcC03` |
| TC-C04 | Categories | Unit | `CategoryService` seed/delete/create | Defaults seeded once; delete blocked when expenses exist | Yes — `CategoryServiceTest` |

## Expenses (Day 4 — 26 Jun)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-E01 | Expenses | Integration | POST valid expense | `201` + expense body | Yes — `ExpenseControllerTest.tcE01` |
| TC-E02 | Expenses | Integration | POST amount ≤ 0 | `400` | Yes — `ExpenseControllerTest.tcE02` |
| TC-E03 | Expenses | Integration | User B GET User A expense | `404` | Yes — `ExpenseControllerTest.tcE03` |
| TC-E04 | Expenses | Integration | GET with from/to filters | Correct subset | Yes — `ExpenseControllerTest.tcE04` |
| TC-E05 | Expenses | E2E | Add expense from Expenses page form | Expense appears in recent list with amount | Yes — `e2e/expenses.spec.ts` |
| TC-E06 | Expenses | Component | Submit without category or with amount ≤ 0 | Validation error shown on form | Yes — `ExpenseForm.test.tsx` |
| — | Expenses | Unit | `ExpenseService` ownership + CRUD | Isolation enforced | Yes — `ExpenseServiceTest` (incl. tcE03 isolation) |

## Dashboard (Day 6 — 28 Jun)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-D01 | Dashboard | Integration | Seed expenses, `GET /api/dashboard/summary` | Correct `totalSpent` and `byCategory` | Yes — `DashboardControllerTest.tcD01` |
| TC-D02 | Dashboard | Integration | Seed expenses on multiple dates | `byDay` aggregation matches seeded data | Yes — `DashboardControllerTest.tcD02` |
| — | Dashboard | Unit | `DashboardService` aggregation | Totals, byCategory, byDay computed correctly | Yes — `DashboardServiceTest` |

## Dashboard v2 (Day 7 — 29 Jun)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-D03 | Dashboard | E2E | Login, seed expenses, change date range on dashboard | Bar/pie charts and totals update; empty state when no expenses in range | Yes — `e2e/dashboard.spec.ts` |
| TC-D04 | Dashboard | Component | Render dashboard with zero expenses in period | Empty state message shown; charts hidden | Yes — `DashboardEmptyState.test.tsx`, `DashboardPage.test.tsx` |

## AI setup (Day 8 — 30 Jun)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-AI01 | AI categorize | Unit | `MockAiService.categorize("Uber ride", categories)` | Returns `Transport` with confidence ≥ 0.9 | Yes — `AiServiceTest.tcAI01_uberRideReturnsTransport` |
| — | AI profile | Integration | Start Spring Boot with `test` profile | `AiService` bean is `MockAiService` (no live Gemini) | Yes — `AiServiceProfileTest` |

**Test profile:** `src/test/resources/application.yml` sets `spring.profiles.active: test` so `MockAiService` is wired instead of `GeminiAiService`. `MockAiService` also activates for the default profile (no `dev`/`prod`) so local `spring-boot:run` and Playwright E2E work without `GEMINI_API_KEY`.

**Dev/prod:** Set `GEMINI_API_KEY` to use `GeminiAiService` (Gemini REST API, model `gemini-2.5-flash`).

## AI categorize UI (Day 9 — 1 Jul)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-AI02 | AI categorize | Integration | `POST /api/ai/categorize` with `{ "description": "Uber ride to airport" }` | `200` + JSON `{ "category": "Transport", "confidence": <number> }` | Yes — `AiControllerTest.tcAI02_categorizeReturnsValidCategory` |

**UI:** On the Expenses page, blur the description field after typing (e.g. "Uber ride") — the category dropdown pre-fills with the AI suggestion and a hint shows the suggested category and confidence. User can override before saving.

## AI insights (Day 10 — 2 Jul)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-AI03 | AI insights | Integration | `POST /api/ai/insights` with `{ "from", "to" }`; repeat same request | First response has `summary`, `highlights`, `suggestions`, `cached: false`; second has `cached: true` | Yes — `AiControllerTest.tcAI03_insightsReturnsSummaryAndCachesOnRepeat` |
| TC-AI04 | AI insights | E2E | Login, seed expenses, set date range, click "Generate insights" | Insight cards render with summary and suggestions | Yes — `e2e/insights.spec.ts` |

**Cache:** Results stored in `insight_cache` keyed by `userId + period`; valid 24 hours.

## CSV export (Day 11 — 3 Jul)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-CSV01 | CSV export | Integration | Seed expenses, `GET /api/expenses/export?from=&to=` | `200` + `text/csv` with header `date,amount,description,category,note` and correct row count for range | Yes — `ExpenseControllerTest.tcCSV01_exportReturnsCorrectHeadersAndRowCount` |

**UI:** On the Expenses page, pick an export date range and click **Download CSV**.

## CSV import (Day 12 — 4 Jul)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-CSV02 | CSV import | Integration | Upload `valid-expenses.csv` | All rows imported; `failed: 0` | Yes — `ExpenseControllerTest.tcCSV02_validCsvImportsAllRows` |
| TC-CSV03 | CSV import | Integration | Upload `invalid-expenses.csv` | Partial import + error report; unknown category maps to Other | Yes — `ExpenseControllerTest.tcCSV03_invalidRowsReturnPartialImportAndErrors` |
| TC-CSV04 | CSV import | Unit | Parser rows with negative amount and bad date | Validation errors returned per row | Yes — `CsvImportParserTest.tcCSV04_rejectsNegativeAmountAndBadDate` |

**Fixtures:** `backend/src/test/resources/test-data/valid-expenses.csv`, `invalid-expenses.csv`

**UI:** Expenses page → **Upload CSV** → summary shows imported/failed counts and per-line errors.

## UX + security (Day 13 — 5 Jul)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| TC-S01 | Security | Integration | Register User A with expenses/categories; authenticate as User B | User B cannot read/update/delete User A expenses or categories; lists, dashboard, export, and insights are scoped to User B only | Yes — `SecurityControllerTest.tcS01_allDataQueriesScopedByAuthenticatedUserId` |
| — | CORS | Integration | `OPTIONS /api/health` with `Origin: http://localhost:5173` | `Access-Control-Allow-Origin` echoes configured origin; methods include GET | Yes — `SecurityControllerTest.corsPreflightAllowsConfiguredOrigin` |
| TC-M01 | Mobile | Manual | Resize browser to 375px width; visit dashboard, expenses, categories | No horizontal scroll; nav, forms, and charts usable on small screens | No — manual checklist below |

**CORS:** Dev allows `http://localhost:5173` (`application.yml`). Prod uses `SPENDSENSE_CORS_ALLOWED_ORIGINS` (comma-separated) in `application-prod.yml`.

**Toasts:** Success/error toasts appear for expense create, category CRUD, CSV import/export, and AI insights.

### TC-M01 manual mobile checklist (375px)

- [ ] Login/register forms fit without horizontal scroll
- [ ] App nav links visible (scroll or wrap); logout reachable
- [ ] Dashboard date pickers and summary cards stack cleanly
- [ ] Bar and pie charts render within viewport (no overflow)
- [ ] Expense form fields stack; category select full width
- [ ] Expense list and category list readable; actions wrap if needed
- [ ] CSV upload/export controls usable on narrow screen
- [ ] Toast notifications visible and not clipped

## E2E + test docs (Day 14 — 6 Jul)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-------------|
| — | P0 smoke | E2E | Register → add expense → dashboard totals → generate insights | Full critical path completes without error | Yes — `e2e/smoke.spec.ts` |

**Playwright suite** (`frontend/e2e/`):

| Spec | Covers |
|------|--------|
| `smoke.spec.ts` | Register → expense → dashboard → insights (P0 critical path) |
| `auth.spec.ts` | TC-A05 register redirect |
| `expenses.spec.ts` | TC-E05 add expense from UI |
| `dashboard.spec.ts` | TC-D03 date range chart updates |
| `insights.spec.ts` | TC-AI04 insight cards |

**Helpers:** `e2e/helpers.ts` — API seeding (`registerUser`, `createExpense`) and UI flows (`registerViaUi`, `loginViaUi`, `addExpenseViaUi`).

**Manual (pre-launch):** TC-M01 mobile checklist (Day 13), TC-S02 JWT secret not in frontend bundle (Day 16), TC-P01 performance (Day 15).

## Database separation

| Database | Path | Used by |
|----------|------|---------|
| App DB | `backend/data/expenses.db` | `spring-boot:run` (main `application.yml`) |
| Test DB | `backend/data/test-expenses.db` | `mvnw test` (`src/test/resources/application.yml`) |

## Run tests

```bash
# Backend (uses test-expenses.db)
cd spendsense/backend && ./mvnw test

# Frontend
cd spendsense/frontend && npm test

# E2E (requires backend + frontend — Playwright starts both)
cd spendsense/frontend && npm run test:e2e
```
