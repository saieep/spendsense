# SpendSense × Cursor — Speaker Notes & Minute-by-Minute Script

**Audience:** Engineering team  
**Duration:** 10–12 minutes  
**Format:** 2 slides + live Cursor file tour (no skill execution) + thin app demo  
**Deck:** `SpendSense-Cursor-Demo.pptx` (speaker notes embedded in each slide)

---

## Pre-flight checklist (5 min before)

- [ ] PowerPoint open: `spendsense/docs/presentation/SpendSense-Cursor-Demo.pptx`
- [ ] Cursor open on repo root: `C:\Cursor Sprint-enablement`
- [ ] Tabs ready in Cursor (open in this order for the tour):
  1. `.cursor/plans/expense_tracker_mvp_d9134402.plan.md`
  2. `.cursor/skills/build-spendsense-sprint-day/SKILL.md`
  3. `spendsense/docs/test-cases.md`
  4. (Optional peek) `spendsense/backend/src/test/java/.../ExpenseControllerTest.java` — method `tcE03`
  5. (Optional peek) `spendsense/backend/src/main/java/.../MockAiService.java` or test profile config
- [ ] Browser: app running at `http://localhost:5173`, logged in, 3–4 seeded expenses
- [ ] Second monitor or window: slides on one screen, Cursor + browser on the other

---

## Minute-by-minute script

### 0:00 – 1:00 | Slide 1 — Opening

**On screen:** Slide 1

**Say:**

> "I'm [name]. This is SpendSense — a personal expense tracker we built in a structured 17-day sprint: JWT auth, categories, expense CRUD, dashboard charts, Gemini AI for categorize and insights, CSV import/export, and automated tests end to end."

> "Stack: Spring Boot 3, SQLite, React 19, Vite. Tests: JUnit and MockMvc on the backend, Vitest and RTL on the frontend, Playwright for E2E."

> "For this room, the product is proof that we shipped. The interesting part is **how we used Cursor** — plan-first, day-scoped skills, tests as done criteria — without the agent rewriting half the repo every session."

**Do:** Advance mentally to Cursor; don't demo the app yet.

---

### 1:00 – 2:30 | Cursor — MVP plan

**On screen:** `.cursor/plans/expense_tracker_mvp_d9134402.plan.md` (scroll to Tech Stack + Repository Layout)

**Say:**

> "Before writing code, we put the MVP in a plan file under `.cursor/plans`. This isn't README fluff — it's what the agent reads every time."

> "It has the stack rationale, repo layout, data model, REST endpoints, testing strategy, and a 17-day calendar. Post-MVP stuff — Plaid, budgets, OCR — is explicitly deferred so the agent doesn't 'helpfully' add features."

**Do:** Scroll briefly to **Testing Strategy** or **Test Case Catalog** in the plan.

**Say:**

> "Notice tests aren't an afterthought. The plan names TC-IDs and expected behavior before implementation. That became our contract with the agent: implement the feature **and** the named tests the same day."

**Engineering hook:** "Same idea as an RFC or design doc — but machine-readable for the agent."

---

### 2:30 – 4:00 | Cursor — Sprint day skill

**On screen:** `.cursor/skills/build-spendsense-sprint-day/SKILL.md` (sprint table + Build workflow section)

**Say:**

> "Skills are reusable instructions. This one resolves 'Day 4' or '26 Jun' to exactly one row: focus, deliverable, tests."

> "The critical line is **build only that row**. Without it, every prompt becomes 'while you're here, add AI insights.' Scope creep is the main failure mode with coding agents."

**Do:** Point at the **Build workflow** steps: Lock scope → Read plan → Audit existing code → Implement → Write tests → Verify.

**Say:**

> "Audit before implement is non-negotiable. The skill tells the agent to inspect controllers, services, and frontend patterns from prior days so Day 4 expenses match Day 2 auth and Day 3 categories — same DTO style, same `SecurityUtils` scoping, same test helpers."

**Do:** Optionally flash `run-tests` / `start-server` skill paths in the file tree — don't open unless you have time.

**Say:**

> "Smaller skills wrap repetitive commands — run backend and frontend tests, start both dev servers. We didn't run skills live in this talk, but that's what we'd invoke in Agent chat for day-to-day work."

---

### 4:00 – 5:00 | Cursor — Test catalog + one test file

**On screen:** `spendsense/docs/test-cases.md` (P0 launch checklist table)

**Say:**

> "`test-cases.md` is the human-facing catalog. Every P0 row links to a real test class and method — not aspirational coverage."

**Do:** Highlight `TC-E03` (expense isolation) and `TC-AI03` (insights cache).

**Say:**

> "TC-E03: user B must get 404 on user A's expense. TC-AI03: second insights call must hit cache, not Gemini. When we said 'done,' we meant these IDs were green."

**Optional (15 sec):** Open `ExpenseControllerTest.java`, jump to `tcE03` — show the test name matches the catalog.

---

### 5:00 – 9:00 | Live app demo (thin thread)

**On screen:** Browser — `http://localhost:5173`

**Narration style:** One sentence of product, one sentence of Cursor/process per step.

| Time | Action | Say |
|------|--------|-----|
| 5:00–5:45 | Login (or already logged in) | "Day 2 scope — JWT register/login, Spring Security, separate test DB. Agent followed existing security patterns from the plan." |
| 5:45–6:45 | Add expense — type merchant, blur field | "Day 9 — AI categorize on blur. In **dev**, this calls Gemini; in **test** profile, `MockAiService` returns deterministic categories. That's how we kept CI fast and free of API keys." |
| 6:45–7:45 | Dashboard — pie/bar, change date range | "Days 6–7 — summary API plus Recharts. TanStack Query refetch on filter change. E2E lives in `e2e/dashboard.spec.ts` if you want to see the Playwright side." |
| 7:45–8:30 | AI insights cards | "Day 10 — insights endpoint with 24-hour cache. We learned quickly that uncached Gemini calls burn quota and slow the dashboard." |
| 8:30–9:00 | CSV export button (optional: skip import) | "Days 11–12 — backend CSV writer/parser, integration tests with fixtures. UI is thin; correctness is in the API layer." |

**If something breaks:** "We have Playwright smoke — register, expense, dashboard, insights — that's our safety net." Don't debug live.

---

### 9:00 – 10:30 | Challenges & best practices (talk track, no slide)

**On screen:** Cursor or browser — your choice; many presenters stay on dashboard.

**Say — pick all three:**

**1. AI in a testable pipeline**

> "Gemini is non-deterministic and rate-limited. We use `MockAiService` in the test profile, real `GeminiAiService` only in dev/prod, rate limits in `run-dev.ps1`. Tests never call the network."

**2. Multi-tenant isolation**

> "Every repository method is user-scoped — `findByIdAndUserId`. Security test TC-S01 audits that new endpoints don't leak data. The agent needed explicit reminders; the test catalog enforced it."

**3. Scope control**

> "The sprint skill and MVP plan defer post-MVP features. Prompting 'build Day 4' beats 'build an expense tracker' — specificity beats enthusiasm."

**Best practices summary (30 sec):**

> "What worked for us: (1) plan as source of truth, (2) skills for bounded scope and repeatable ops, (3) TC-IDs wired to real tests, (4) audit-before-implement, (5) never trust 'it looks fine' without `mvnw test` and `npm test`."

---

### 10:30 – 12:00 | Slide 2 — Close + Q&A

**On screen:** Slide 2

**Say:**

> "Three artifacts: MVP plan, day-scoped skills, test catalog with real test mapping."

> "Cursor accelerated typing and boilerplate. **Discipline** — scope, mocks, isolation, green tests — is what made it shippable in 17 days."

> "SpendSense is one app; the workflow transfers. Happy to dig into skill authoring, prompt structure, or how we'd adapt this for [your team's stack]."

**Q&A prompts if room is quiet:**

- "How do you prevent the agent from editing Flyway migrations that already ran?"
- "When do you write a new skill vs. a one-off prompt?"
- "How do you review agent output — PR size, test-first, etc.?"

---

## Slide speaker notes (quick reference)

Full notes are in the PPT **Notes** pane (View → Notes). Abbreviated below.

### Slide 1 notes

- Frame: product = proof, Cursor workflow = story.
- Mention stack once; don't enumerate all 17 days.
- Transition to repo tour within 60 seconds.

### Slide 2 notes

- Recap plan → skills → test catalog.
- Hard rules: MockAiService, test DB, userId scoping.
- Close with "execution accelerator, not replacement for design/review."
- Don't re-tour files; invite Q&A.

---

## Files to show in Cursor (cheat sheet)

| Order | Path | What to point at |
|-------|------|------------------|
| 1 | `.cursor/plans/expense_tracker_mvp_d9134402.plan.md` | Tech stack, test strategy, deferred features |
| 2 | `.cursor/skills/build-spendsense-sprint-day/SKILL.md` | Sprint table, "build only that row", workflow steps |
| 3 | `spendsense/docs/test-cases.md` | P0 table, TC-E03, TC-AI03 |
| 4 | `.cursor/skills/run-tests/SKILL.md` | (Optional) one-glance repeatable test command |
| 5 | `ExpenseControllerTest.java` | (Optional) `tcE03` method name |

---

## Timing buffer

| Segment | Target | Flex |
|---------|--------|------|
| Opening + Slide 1 | 1:00 | Can cut to 0:45 |
| Cursor tour | 4:00 | Can extend to 5:00 if fewer demo steps |
| App demo | 4:00 | Cut CSV + insights first |
| Challenges | 1:30 | Can merge into demo narration |
| Slide 2 + close | 1:30 | Hold 30s for Q&A |

**If running long:** Skip CSV export; mention insights cache in talk only.  
**If running short:** Open `MockAiService` or `application-test.yml` for 30 seconds.
