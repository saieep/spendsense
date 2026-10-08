# SpendSense

**SpendSense – Personal Expense Tracker with AI Insights**

Monorepo for the 17-day MVP sprint.

## Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.3, Java 17, SQLite, Flyway |
| Frontend | React 19, Vite, TypeScript, Tailwind CSS |
| AI (planned) | Google Gemini via Spring AI |
| Tests | JUnit 5 + MockMvc, Vitest + React Testing Library |

## Project structure

```
spendsense/
  backend/     Spring Boot REST API (port 8080)
  frontend/    React SPA (port 5173)
  docs/        Test case catalog
```

## Prerequisites

- Java 17+
- Node.js 20+

Maven is bundled via `mvnw` in the backend — no global Maven install required.

## Local development

### Backend

```bash
cd spendsense/backend
./mvnw spring-boot:run
```

Windows:

```powershell
cd spendsense\backend
.\mvnw.cmd spring-boot:run
```

Health check: [http://localhost:8080/api/health](http://localhost:8080/api/health)

### Frontend

```bash
cd spendsense/frontend
npm install
npm run dev
```

App: [http://localhost:5173](http://localhost:5173)

## Run tests

### Backend

```bash
cd spendsense/backend
./mvnw test
```

### Frontend

```bash
cd spendsense/frontend
npm test
```

## Environment variables (later days)

| Variable | Purpose |
|----------|---------|
| `GEMINI_API_KEY` | Google Gemini API key — **dev only**; put in `spendsense/.env` (gitignored). Use `backend/run-dev.ps1` (activates `dev` profile + rate limits). Tests never call Gemini. |
| `JWT_SECRET` | JWT signing secret |
| `SPENDSENSE_CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins for prod CORS (e.g. `https://spendsense.vercel.app`) |
| `VITE_API_URL` | Backend URL for frontend (default `http://localhost:8080`) |

### Gemini dev safeguards (free tier)

| Setting | Dev (`dev` profile) | Purpose |
|---------|---------------------|---------|
| `spendsense.gemini.enabled` | `true` | Only calls Gemini when profile is `dev` or `prod` |
| `spendsense.gemini.dev-mode` | `true` | Enables in-app rate limiting |
| `daily-request-limit` | `100` | Stays under ~1500/day free tier |
| `min-request-interval-ms` | `4000` | ~15 requests/min max |
| Model | `gemini-2.5-flash` | Free-tier model from MVP plan |

**Local run:** `cd spendsense/backend && .\run-dev.ps1` — loads `.env`, sets `dev` profile.

**Tests:** `test` profile uses `MockAiService` — zero Gemini API calls.

**Production:** set `SPRING_PROFILES_ACTIVE=prod` and `GEMINI_API_KEY` in Railway/Render env vars (not in git).

## Sprint status

- **Day 1 (23 Jun):** Monorepo bootstrap, test infrastructure, smoke tests
- **Day 2 (24 Jun):** JWT auth — register/login API, Spring Security, login/register UI, separate test DB
- **Day 3 (25 Jun):** Categories — 10 defaults on register, CRUD API + UI, unit/integration tests
- **Day 4 (26 Jun):** Expense API — JPA entity, REST CRUD, validation, user-scoped queries
- **Day 6 (28 Jun):** Dashboard v1 — summary API, pie chart + total cards on dashboard
- **Day 7 (29 Jun):** Dashboard v2 — bar chart, date range refetch, empty/loading states, E2E + component tests
- **Day 8 (30 Jun):** Gemini setup — `AiService`, `MockAiService` (test profile), `GeminiAiService` (dev/prod via `GEMINI_API_KEY`)
- **Day 9 (1 Jul):** AI categorize UI — `POST /api/ai/categorize`, ExpenseForm blur-to-suggest, category dropdown pre-fill
- **Day 10 (2 Jul):** AI insights — `POST /api/ai/insights` with 24h cache, insight cards on dashboard
- **Day 11 (3 Jul):** CSV export — `GET /api/expenses/export`, Apache Commons CSV writer, download button on Expenses page
- **Day 12 (4 Jul):** CSV import — `POST /api/expenses/import`, validation + error report, upload UI on Expenses page
- **Day 13 (5 Jul):** UX + security — toast notifications, mobile-responsive layout, CORS prod config, userId isolation audit (TC-S01)
- **Day 14 (6 Jul):** E2E smoke suite — Playwright P0 flow (register → expense → dashboard → insights); complete P0 test-case catalog
- **Day 5, 15+:** Full expense UI filters/edit, CI E2E gate

## Category API (Day 3)

| Method | Endpoint | Auth |
|--------|----------|------|
| GET | `/api/categories` | Bearer JWT |
| POST | `/api/categories` | Bearer JWT |
| PUT | `/api/categories/{id}` | Bearer JWT |
| DELETE | `/api/categories/{id}` | Bearer JWT |

Default categories seeded on register: Food, Transport, Housing, Utilities, Entertainment, Health, Shopping, Travel, Education, Other.

## Expense API (Day 4)

| Method | Endpoint | Auth |
|--------|----------|------|
| GET | `/api/expenses` | Bearer JWT — optional `from`, `to`, `categoryId` query params |
| GET | `/api/expenses/{id}` | Bearer JWT |
| POST | `/api/expenses` | Bearer JWT |
| PUT | `/api/expenses/{id}` | Bearer JWT |
| DELETE | `/api/expenses/{id}` | Bearer JWT |

## Dashboard API (Day 6)

| Method | Endpoint | Auth |
|--------|----------|------|
| GET | `/api/dashboard/summary` | Bearer JWT — optional `from`, `to` query params |

Returns `totalSpent`, `expenseCount`, `byCategory` (for pie chart), and `byDay` (for bar chart on Day 7).

## Auth API (Day 2)

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |

## Databases

| File | Purpose |
|------|---------|
| `backend/data/expenses.db` | Local development app data |
| `backend/data/test-expenses.db` | Automated tests only (separate file) |
