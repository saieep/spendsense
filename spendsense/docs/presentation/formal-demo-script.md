# SpendSense × Cursor — Formal Demo Script (10–12 minutes)

**Audience:** Engineering team  
**Presenter:** [Your name]  
**Duration:** 10–12 minutes  
**Format:** 2 slides · Cursor file tour (no skill execution) · live application demo  
**Deck:** `spendsense/docs/presentation/SpendSense-Cursor-Demo.pptx`

---

## Pre-flight checklist

- [ ] Slide deck open in Presenter View (Alt+F5)
- [ ] Cursor open at repo root with these tabs ready:
  1. `.cursor/plans/expense_tracker_mvp_d9134402.plan.md`
  2. `.cursor/skills/build-spendsense-sprint-day/SKILL.md`
  3. `spendsense/docs/test-cases.md`
  4. `spendsense/backend/src/test/java/com/spendsense/controller/ExpenseControllerTest.java` (optional)
- [ ] Browser: http://localhost:5173 — logged in, 3–4 sample expenses seeded
- [ ] Dual screen: slides on display; Cursor and browser on presenter screen

---

## Full script

### [0:00 – 1:00] Opening — Slide 1

**[ON SCREEN: Slide 1 — "SpendSense + Cursor"]**

**SAY:**

"Good [morning/afternoon/evening], everyone. Thank you for joining this session.

My name is [Your Name], and today I will walk you through **SpendSense** — a personal expense tracker with AI-powered insights that we delivered as part of a structured seventeen-day MVP sprint.

SpendSense provides JWT-based authentication, category and expense management, an interactive dashboard with charts, Gemini-powered expense categorisation and spending insights, and CSV import and export. The technology stack comprises Spring Boot 3 with SQLite on the backend, React 19 with Vite on the frontend, and a comprehensive test suite spanning JUnit and MockMvc, Vitest with React Testing Library, and Playwright end-to-end tests.

The purpose of this presentation is not solely to demonstrate the application. Our primary objective is to illustrate **how we used Cursor** to accelerate delivery while maintaining engineering discipline — through written plans, scoped agent skills, and test-driven completion criteria.

I will begin with our Cursor workflow in the repository, follow with a concise live demonstration of the application, and conclude with the challenges we encountered and the practices that addressed them.

Let me switch to the codebase."

**[ACTION: Advance to Cursor. Do not open the application yet.]**

---

### [1:00 – 2:30] Cursor — MVP plan

**[ON SCREEN: Cursor — `.cursor/plans/expense_tracker_mvp_d9134402.plan.md`]**

**SAY:**

"I am now showing our MVP plan file, located at `.cursor/plans/expense_tracker_mvp_d9134402.plan.md`. This document served as the single source of truth throughout the sprint.

Before any feature implementation, we committed the architecture, technology choices, repository layout, data model, REST API contracts, testing strategy, and sprint calendar to this plan. The agent referenced this file at the start of every working session, which eliminated the need to re-explain the system context repeatedly."

**[ACTION: Scroll to the **Tech Stack** section.]**

**SAY:**

"As you can see here, the plan documents not only what we selected — Spring Boot, SQLite, React, Gemini — but also why each choice was made. Post-MVP capabilities such as bank synchronisation, receipt OCR, and budget alerts are explicitly listed as deferred, which prevented scope expansion during agent-assisted development."

**[ACTION: Scroll to **Testing Strategy** or **Test Case Catalog**.]**

**SAY:**

"Critically, the plan defines our testing approach and assigns Test Case identifiers — TC-IDs — before implementation begins. Each identifier maps to a specific behaviour and expected outcome. Our agreement with the agent was unambiguous: a feature is complete only when both the implementation and the named tests pass. This is analogous to an RFC or design document, but structured for machine consumption by the coding agent."

---

### [2:30 – 4:00] Cursor — Sprint day skill

**[ON SCREEN: Cursor — `.cursor/skills/build-spendsense-sprint-day/SKILL.md`]**

**SAY:**

"I am now opening our primary Agent Skill: `build-spendsense-sprint-day`. Agent Skills are reusable instruction sets that guide Cursor's behaviour for recurring workflows.

This skill resolves a sprint day — for example, 'Day 4' or '26 June' — to exactly one row in our seventeen-day calendar: a defined focus area, deliverable, and set of tests to create."

**[ACTION: Point to the sprint calendar table — e.g. Day 4: Expense API.]**

**SAY:**

"The most important constraint in this skill is the instruction to **build only that row**. Without this boundary, agent prompts naturally expand in scope — for instance, requesting expense API work while simultaneously introducing AI features intended for Day 8. Scope control was the principal risk we identified when adopting coding agents, and this skill directly mitigates it."

**[ACTION: Scroll to the **Build workflow** section.]**

**SAY:**

"The skill prescribes a six-step workflow: lock scope, read the plan, audit existing code, implement deliverables, write tests, and verify. I wish to emphasise the third step — audit before implement. The agent is instructed to inspect controllers, services, repositories, and frontend patterns from prior sprint days so that new code conforms to established conventions: consistent DTO structure, `SecurityUtils` for user scoping, and shared test helpers such as `AuthTestSupport`."

**[ACTION: Briefly show the `.cursor/skills/` folder in the file tree — point at `run-tests` and `start-server` without opening.]**

**SAY:**

"We also authored smaller skills for operational tasks — running the full test suite and starting development servers. I will not execute these skills during this demonstration; however, in daily practice, they are invoked from Agent chat to ensure consistent, repeatable commands across the team."

---

### [4:00 – 5:00] Cursor — Test catalog

**[ON SCREEN: Cursor — `spendsense/docs/test-cases.md`]**

**SAY:**

"I am now showing `spendsense/docs/test-cases.md`, our human-readable test case catalog. This document serves as the launch checklist for the MVP.

Each row in the P0 table references a concrete automated test — a specific class and method — rather than aspirational coverage."

**[ACTION: Highlight rows TC-E03 and TC-AI03 in the P0 launch checklist.]**

**SAY:**

"For example, TC-E03 verifies expense isolation: User B must receive a 404 response when attempting to access User A's expense. TC-AI03 verifies that a second call to the insights endpoint returns a cached result rather than invoking the Gemini API again. When we declared a sprint day complete, these identifiers had to be green in our test suite."

**[ACTION — optional, 15 seconds: Switch to `ExpenseControllerTest.java` and locate method `tcE03`.]**

**SAY:**

"As you can see, the test method name corresponds directly to the catalog entry. This traceability between documentation and code was essential for maintaining confidence in agent-generated output."

---

### [5:00 – 9:00] Live application demonstration

**[ON SCREEN: Browser — http://localhost:5173]**

**SAY:**

"I will now demonstrate the application itself. I will keep this portion concise and relate each capability to the sprint day and Cursor workflow that produced it."

---

#### [5:00 – 5:45] Authentication

**[ACTION: Show the login screen, or confirm you are already authenticated and on the dashboard.]**

**SAY:**

"This is the SpendSense login interface, implemented on Sprint Day 2. The scope included JWT-based registration and login, Spring Security configuration on the backend, and the corresponding React authentication pages.

The agent implemented this feature by auditing the security patterns defined in our MVP plan and establishing a separate test database — `test-expenses.db` — so that development data would not interfere with automated tests."

---

#### [5:45 – 6:45] Expense entry with AI categorisation

**[ACTION: Navigate to the Expenses page. Open the add-expense form. Type a merchant name — for example, 'Starbucks' — and tab out of the field to trigger categorisation.]**

**SAY:**

"I am adding a new expense. When I enter a merchant name and move focus away from the field, the application requests an AI-suggested category. This capability was delivered on Sprint Day 9.

In the development profile, this invokes the Gemini API. In our test profile, however, a `MockAiService` returns deterministic responses. This design decision was deliberate: our continuous integration pipeline never calls external AI services, which keeps tests fast, cost-free, and reproducible."

**[ACTION: Select the suggested category, enter an amount, and save the expense.]**

**SAY:**

"The expense is persisted via the REST API established on Day 4, with validation enforced at both the DTO and service layers."

---

#### [6:45 – 7:45] Dashboard

**[ACTION: Navigate to the Dashboard. Show the pie chart and bar chart. Change the date range filter.]**

**SAY:**

"I am now on the Dashboard, delivered across Sprint Days 6 and 7. The summary API aggregates total spend, expense count, and breakdowns by category and by day. The frontend renders these aggregates using Recharts, with TanStack Query managing cache invalidation when the date filter changes.

End-to-end coverage for this flow exists in `e2e/dashboard.spec.ts`. The agent was guided to implement loading and empty states, not merely the happy path."

---

#### [7:45 – 8:30] AI insights

**[ACTION: Scroll to the AI insights section on the Dashboard.]**

**SAY:**

"These insight cards are generated by our Gemini integration, delivered on Sprint Day 10. The backend caches insights for twenty-four hours per user. We introduced caching after observing that uncached API calls consumed quota rapidly and degraded dashboard load time. This is an example of a challenge identified during development and resolved through an explicit architectural decision documented in the plan."

---

#### [8:30 – 9:00] CSV export

**[ACTION: Navigate to Expenses. Click the CSV export button. You may show the downloaded file briefly or simply confirm the download.]**

**SAY:**

"Finally, CSV export — Sprint Day 11 — with import following on Day 12. The correctness of this feature resides in the backend CSV writer and parser, validated by integration tests with fixture files. The user interface is intentionally thin; the agent was directed to prioritise API-layer correctness, which aligns with our test catalog."

**[ACTION: Return to the Dashboard or a neutral screen.]**

---

### [9:00 – 10:30] Challenges overcome and best practices

**[ON SCREEN: Dashboard or Cursor — speaker's preference]**

**SAY:**

"Before I conclude, I would like to summarise three challenges we encountered and the practices that resolved them.

**First — integrating AI within a testable pipeline.** Large language model APIs are non-deterministic and rate-limited. Our solution was to implement `MockAiService` for the test profile, restrict live Gemini calls to development and production profiles, and apply rate limiting in our development startup script. No automated test in our suite makes a network call to Gemini.

**Second — multi-tenant data isolation.** Every repository query is scoped by user identifier — `findByIdAndUserId` — and security test TC-S01 audits that new endpoints do not expose cross-user data. The agent required explicit instruction on this pattern; the test catalog enforced compliance.

**Third — scope control across a seventeen-day sprint.** The combination of a deferred-features list in the MVP plan and day-scoped Agent Skills prevented feature creep. A prompt such as 'Build Day 4 — Expense API' produces substantially better outcomes than a general request to 'build an expense tracker.'

In summary, the practices that proved most effective were: maintaining the plan as the authoritative reference; using skills to bound scope and standardise operations; wiring TC-IDs to real test implementations; auditing existing code before generating new code; and verifying completion through `mvnw test` and `npm test` rather than visual inspection alone.

Cursor functioned as an execution accelerator. Engineering discipline — bounded scope, deterministic test doubles, data isolation, and automated verification — is what rendered the output shippable."

---

### [10:30 – 12:00] Closing — Slide 2 and Q&A

**[ON SCREEN: Slide 2 — "Cursor workflow that scaled"]**

**SAY:**

"To conclude, three artefacts enabled this workflow at scale.

The **MVP plan** provided architectural and testing context for every agent session. **Agent Skills** translated sprint days into bounded, repeatable tasks. The **test case catalog** linked documentation to executable proof.

SpendSense demonstrates that a structured Cursor workflow can deliver a full-stack application with AI integration in seventeen days. The workflow itself — plan, skill, audit, implement, verify — is transferable to other services and codebases within our organisation.

Thank you for your attention. I am happy to take questions on skill authoring, prompt design, test strategy, or adaptation to your team's stack."

**[PAUSE for Q&A]**

**Suggested questions if the room is quiet:**

- "How do we prevent the agent from modifying Flyway migrations that have already been applied?"
- "When should we author a new skill versus issuing a one-off prompt?"
- "What is our recommended review process for agent-generated pull requests?"

---

## Slide reference

### Slide 1 — SpendSense — Personal Expense Tracker with AI Insights
- **Subtitle:** Seventeen-Day MVP Sprint | Engineering Demonstration | 10–12 Minutes
- Delivered capabilities · Stack · Session focus (Cursor workflow) · Agenda
- Footer: *SpendSense is the proof. The Cursor workflow is the story.*

### Slide 2 — Cursor Workflow That Scaled
- **Subtitle:** Plan → Skill → Audit → Implement → Verify
- Three artefacts · Challenges overcome · Best practices · Outcome
- Footer: *Cursor accelerates execution. Discipline keeps it shippable.*

---

## Timing summary

| Segment | Duration |
|---------|----------|
| Greeting + Slide 1 | 1:00 |
| MVP plan (`plan.md`) | 1:30 |
| Sprint skill (`SKILL.md`) | 1:30 |
| Test catalog (`test-cases.md`) | 1:00 |
| Live app demo | 4:00 |
| Challenges + best practices | 1:30 |
| Slide 2 + close + Q&A buffer | 1:30 |
| **Total** | **~12:00** |

**If running over:** Omit CSV export; mention insights caching verbally only.  
**If running under:** Show `ExpenseControllerTest.tcE03` or `application-test.yml` MockAiService configuration.
