---
name: build-spendsense-sprint-day
description: Builds SpendSense MVP features for a specific 17-day sprint day from the sprint calendar. Resolves day or date to focus, deliverables, and tests; reads the MVP plan for architecture/API/data model; audits spendsense/backend and spendsense/frontend; implements only that day's scope. Use when the user mentions a sprint day (1–17), date (23 Jun–9 Jul 2026), or asks to build today's SpendSense task.
---

# Build SpendSense Sprint Day

**Plan source:** [.cursor/plans/expense_tracker_mvp_d9134402.plan.md](../../plans/expense_tracker_mvp_d9134402.plan.md)  
**Codebase:** `spendsense/backend`, `spendsense/frontend`  
**Sprint:** 23 Jun – 9 Jul 2026 (17 days)

## Resolve the target day

Accept any of: `day 4`, `Day 4`, `26 Jun`, `June 26`, `2026-06-26`, `Fri 26 Jun`.

| Day | Date | Aliases |
|-----|------|---------|
| 1 | Tue, 23 Jun | bootstrap, infra |
| 2 | Wed, 24 Jun | auth |
| 3 | Thu, 25 Jun | categories |
| 4 | Fri, 26 Jun | expense api |
| 5 | Sat, 27 Jun | expense ui |
| 6 | Sun, 28 Jun | dashboard v1 |
| 7 | Mon, 29 Jun | dashboard v2 |
| 8 | Tue, 30 Jun | gemini, ai setup |
| 9 | Wed, 1 Jul | ai categorize ui |
| 10 | Thu, 2 Jul | ai insights |
| 11 | Fri, 3 Jul | csv export |
| 12 | Sat, 4 Jul | csv import |
| 13 | Sun, 5 Jul | ux, security |
| 14 | Mon, 6 Jul | e2e, test docs |
| 15 | Tue, 7 Jul | performance, ci |
| 16 | Wed, 8 Jul | deploy |
| 17 | Thu, 9 Jul | launch, mvp |

Look up the row below. **Build only that row** — do not implement later days.

---

## 17-Day Sprint Calendar

### Week 1 — Backend core + frontend shell + tests (23–29 Jun)

| Date | Day | Focus | Deliverable | Tests to create |
|------|-----|-------|-------------|-----------------|
| **Tue, 23 Jun** | 1 | Monorepo + test setup | Both apps run locally; `application-test.yml`; Vitest + JUnit configured; CI workflow skeleton | TC infra: sample passing backend + frontend smoke test |
| **Wed, 24 Jun** | 2 | Auth | JWT register/login; Spring Security; login/register UI | TC-A01–A04 integration tests; TC-A06 login component test |
| **Thu, 25 Jun** | 3 | Categories | Seed defaults; category CRUD UI | TC-C01–C03 integration; CategoryService unit tests |
| **Fri, 26 Jun** | 4 | Expense API | JPA entities; REST endpoints; validation | TC-E01–E04 integration; ExpenseService unit tests; TC-E03 isolation test |
| **Sat, 27 Jun** | 5 | Expense UI | Expense form, list, filters, edit/delete | TC-E05 E2E; TC-E06 ExpenseForm component test |
| **Sun, 28 Jun** | 6 | Dashboard v1 | Summary API; pie chart + total cards | TC-D01–D02 integration; DashboardService unit tests |
| **Mon, 29 Jun** | 7 | Dashboard v2 | Bar chart; date refetch; empty/loading states | TC-D03 E2E; TC-D04 empty-state component test |

**Week 1 milestone (29 Jun):** Core MVP working; auth, expense, category, and dashboard P0 integration tests green.

### Week 2 — AI, CSV, polish + tests (30 Jun – 6 Jul)

| Date | Day | Focus | Deliverable | Tests to create |
|------|-----|-------|-------------|-----------------|
| **Tue, 30 Jun** | 8 | Gemini setup | `AiService`; `MockAiService` for test profile | TC-AI01 unit; mock bean wired in `application-test.yml` |
| **Wed, 1 Jul** | 9 | AI categorize UI | Suggest category on blur; pre-fill dropdown | TC-AI02 integration test |
| **Thu, 2 Jul** | 10 | AI insights | Insights endpoint + cache; insight cards | TC-AI03 integration (cache hit); TC-AI04 E2E |
| **Fri, 3 Jul** | 11 | CSV export | Backend writer + download button | TC-CSV01 integration |
| **Sat, 4 Jul** | 12 | CSV import | Upload UI; validation; error report | TC-CSV02–04 unit + integration; sample CSV fixtures |
| **Sun, 5 Jul** | 13 | UX + security | Toasts, mobile layout, CORS, userId audit | TC-S01 security integration; TC-M01 manual mobile checklist |
| **Mon, 6 Jul** | 14 | E2E + test docs | Playwright smoke suite; complete `docs/test-cases.md`; fix failures | All P0 cases documented; E2E: register → expense → dashboard → insights |

**Week 2 milestone (6 Jul):** All P0 automated tests pass; test-case doc complete; app demo-ready locally.

### Week 3 — CI, deploy + launch (7–9 Jul)

| Date | Day | Focus | Deliverable | Tests to create |
|------|-----|-------|-------------|-----------------|
| **Tue, 7 Jul** | 15 | Performance + CI | SQLite indexes; pagination; CI runs full test suite on push | TC-P01 manual perf check; CI green gate enforced |
| **Wed, 8 Jul** | 16 | Deploy | Railway + Vercel; env vars; post-deploy smoke | Run P0 E2E against staging URL; TC-S02 secrets check |
| **Thu, 9 Jul** | 17 | **MVP Launch** | Production smoke test; README; SpendSense demo script | Final manual regression of P0 checklist |

---

## Build workflow

When the user names a day or date, follow these steps in order:

### 1. Lock scope

From the table row, extract **Focus**, **Deliverable**, and **Tests to create**. Treat these as the only in-scope work.

### 2. Read the plan

Open `expense_tracker_mvp_d9134402.plan.md` and read sections relevant to the day's focus:

| Plan section | Use for |
|--------------|---------|
| Tech Stack | Libraries, versions, test tools |
| Data Model | Entities, fields, indexes, Flyway migrations |
| Key REST API Endpoints | Paths, methods, query params, auth |
| Testing Strategy | Unit vs integration vs E2E; `test` profile; MockAiService rule |
| Test Case Catalog | TC-ID steps and expected results for named tests |
| Frontend Stack Highlights | shadcn/ui, TanStack Query, Recharts, routing |
| AI Implementation | Days 8–10 only |
| CSV Import/Export | Days 11–12 only |
| Deployment Notes | Days 16–17 only |

### 3. Audit existing code

Before writing code, inspect what already exists. Do not duplicate or break prior days.

**Backend** (`spendsense/backend/src/main/java/com/spendsense/`):

```
controller/   # REST — Auth, Category, Expense, Health (+ Dashboard, AI, CSV later)
service/      # Business logic — Auth, Category, Expense (+ Dashboard, Ai, Csv later)
repository/   # JPA queries — user-scoped
model/        # JPA entities
dto/          # Request/response records with jakarta.validation
security/     # JWT filter, SecurityUtils
config/       # Security, Web/CORS, JWT properties
```

**Backend tests** (`spendsense/backend/src/test/java/com/spendsense/`):

```
controller/*Test.java   # MockMvc integration — name methods tcXxx
service/*Test.java      # Mockito unit tests
AuthTestSupport.java    # registerAndGetToken helper
AbstractIntegrationTest.java
```

**Frontend** (`spendsense/frontend/src/`):

```
pages/          # LoginPage, RegisterPage, CategoriesPage, DashboardPage (+ Expenses later)
api/            # Axios clients — auth.ts, categories.ts (+ expenses, dashboard, ai later)
types/          # TypeScript interfaces matching DTOs
context/        # AuthContext — JWT storage
components/     # AppLayout, ProtectedRoute
App.tsx         # React Router routes
*.test.tsx      # Vitest + RTL component tests
```

**Schema:** `backend/src/main/resources/db/migration/V*.sql` — add new migration only when schema changes; never edit applied migrations.

**Test catalog:** `spendsense/docs/test-cases.md` — append a section for the current day when tests are added.

### 4. Implement deliverables

Match established patterns from the most recent completed day:

| Layer | Pattern to follow |
|-------|-------------------|
| Controller | Inject service + `SecurityUtils`; `@Valid` on body; scope by `getCurrentUserId()` |
| Service | `@Transactional`; `ResponseStatusException` for 404/409; user ownership via `findByIdAndUserId` |
| DTO | Java records; `jakarta.validation` annotations |
| Repository | User-scoped query method names |
| Frontend API | Axios + Bearer token from `AuthContext` |
| Frontend page | TanStack Query for server state; match existing page layout in `AppLayout` |

**Rules:**

- All non-auth endpoints require `Authorization: Bearer <jwt>`; data scoped by `userId`
- Never call live Gemini in tests — use `MockAiService` in `test` profile
- App DB: `backend/data/expenses.db`; test DB: `backend/data/test-expenses.db`
- Do not implement features from future sprint days

### 5. Write tests (same day)

Implement every test named in the sprint row:

- **Integration:** `@SpringBootTest` + `@AutoConfigureMockMvc` + `AuthTestSupport`; method names like `tcE01_…`
- **Unit:** `@ExtendWith(MockitoExtension.class)`; mock repositories
- **Component:** Vitest + RTL in `frontend/src/**/*.test.tsx`
- **E2E:** Playwright (Days 5, 7, 10, 14+)

Cross-check expected behavior against the plan's **Test Case Catalog** (P0 table).

### 6. Verify and document

```bash
cd spendsense/backend && ./mvnw test
cd spendsense/frontend && npm test
```

Windows: `.\mvnw.cmd test`

Update `spendsense/docs/test-cases.md` with a `## <Feature> (Day N — <date>)` section. Update `spendsense/README.md` sprint status if the day completes a major feature.

---

## Done criteria (every day)

- [ ] Deliverable column fully implemented for that day only
- [ ] All tests from the sprint row pass
- [ ] Prior days' tests still pass
- [ ] `docs/test-cases.md` updated for new automated cases
- [ ] No scope creep into later days

## Additional resources

- Per-day file hints and plan section line references: [reference.md](reference.md)
- Day 4 deep dive (optional): [../build-day-4-expense-api/SKILL.md](../build-day-4-expense-api/SKILL.md)
