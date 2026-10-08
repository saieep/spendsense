---
name: build-day-4-expense-api
description: Implements SpendSense Day 4 (26 Jun) Expense API — JPA entities, REST CRUD, validation, and tests TC-E01–E04. Use when building Day 4, expense API, ExpenseService, ExpenseController, or expense integration/unit tests in spendsense/backend.
---

# Build Day 4 — Expense API (26 Jun)

**Focus:** Expense API  
**Deliverables:** JPA entities; REST endpoints; validation  
**Tests:** TC-E01–E04 integration; ExpenseService unit tests; TC-E03 isolation test

## Prerequisites

Confirm Days 1–3 are green before starting:

```bash
cd spendsense/backend && ./mvnw test
```

Reuse existing patterns from `CategoryController`, `CategoryService`, `CategoryControllerTest`, and `CategoryServiceTest`. Do **not** implement CSV export/import or dashboard — those are later days.

## Implementation checklist

Copy and track progress:

```
Day 4 Progress:
- [ ] Complete Expense JPA entity (match V4 migration)
- [ ] ExpenseRequest / ExpenseResponse DTOs with validation
- [ ] ExpenseRepository user-scoped queries
- [ ] ExpenseService (CRUD + ownership checks)
- [ ] ExpenseController REST endpoints
- [ ] ExpenseControllerTest (TC-E01–E04)
- [ ] ExpenseServiceTest (unit + TC-E03 isolation)
- [ ] Update docs/test-cases.md Expenses section
- [ ] ./mvnw test passes
```

## 1. JPA entity

Migration already exists: `backend/src/main/resources/db/migration/V4__expenses.sql`.

Complete `Expense.java` to match the table:

| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK | `@GeneratedValue(IDENTITY)` |
| user_id | INTEGER NOT NULL | Set from JWT, never from request body |
| category_id | INTEGER NOT NULL | FK to user's category |
| amount | REAL NOT NULL | `BigDecimal` |
| expense_date | TEXT NOT NULL | ISO `YYYY-MM-DD` string |
| description | TEXT NOT NULL | |
| note | TEXT | Optional |
| ai_suggested_category | TEXT | Optional; leave null for Day 4 |

Follow `Category.java` conventions: protected no-arg ctor, explicit ctor for creation, getters/setters as needed.

## 2. DTOs and validation

Create `ExpenseRequest` and `ExpenseResponse` in `com.spendsense.dto`.

**ExpenseRequest** (create + update):

```java
@NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal amount
@NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be YYYY-MM-DD") String expenseDate
@NotBlank @Size(max = 200) String description
@NotNull Long categoryId
@Size(max = 500) String note  // optional
```

**ExpenseResponse**: id, amount, expenseDate, description, note, categoryId, categoryName (join or lookup).

Use `@Valid` on controller `@RequestBody` parameters (same as `CategoryRequest`).

## 3. ExpenseRepository

Extend `JpaRepository<Expense, Long>` with user-scoped methods:

```java
Optional<Expense> findByIdAndUserId(Long id, Long userId);

List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
    Long userId, String from, String to);

// Optional filter: add categoryId param when present in query
boolean existsByCategoryId(Long categoryId);  // already exists
```

Add query methods needed for date-range filtering (TC-E04). Keep queries scoped by `userId`.

## 4. ExpenseService

Mirror `CategoryService` structure:

| Method | Behavior |
|--------|----------|
| `list(userId, from, to, categoryId)` | Return expenses for user; apply date + category filters |
| `getById(userId, id)` | Return expense or throw `404` if missing or wrong user |
| `create(userId, request)` | Validate category belongs to user; persist |
| `update(userId, id, request)` | Ownership check + category ownership |
| `delete(userId, id)` | Ownership check; delete |

**Ownership rule (TC-E03):** Never expose another user's expense. Use `findByIdAndUserId` — return `ResponseStatusException(NOT_FOUND, "Expense not found")` for wrong/missing id (same pattern as `CategoryService.getOwnedCategory`).

**Category validation:** Before create/update, verify `categoryRepository.findByIdAndUserId(categoryId, userId)` exists; else `400` or `404`.

## 5. REST endpoints

Replace the stub in `ExpenseController`. Inject `ExpenseService` + `SecurityUtils` (same as `CategoryController`).

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| GET | `/api/expenses` | 200 | Query params: `from`, `to`, `categoryId` (all optional) |
| GET | `/api/expenses/{id}` | 200 | Single expense; used by TC-E03 |
| POST | `/api/expenses` | 201 | `@Valid ExpenseRequest` |
| PUT | `/api/expenses/{id}` | 200 | `@Valid ExpenseRequest` |
| DELETE | `/api/expenses/{id}` | 204 | |

All endpoints require Bearer JWT. Scope every operation with `securityUtils.getCurrentUserId()`.

**Out of scope for Day 4:** `/api/expenses/export`, `/api/expenses/import`, dashboard, AI fields.

## 6. Tests

### Integration — `ExpenseControllerTest`

Extend `AbstractIntegrationTest`, use `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Import(AuthTestSupport.class)`.

Name test methods `tcE01_…`, `tcE02_…`, etc. (match `CategoryControllerTest` style).

| ID | Test method | Steps | Expected |
|----|-------------|-------|----------|
| TC-E01 | `tcE01_createExpenseWithValidData` | Register user → get Food category id → POST valid expense | `201`, body has amount, description, categoryId |
| TC-E02 | `tcE02_createExpenseWithZeroAmount` | POST with `amount: 0` or negative | `400` |
| TC-E03 | `tcE03_userCannotReadOtherUsersExpense` | User A creates expense → User B GET `/api/expenses/{id}` | `404` (or `403`) |
| TC-E04 | `tcE04_filterByDateRange` | Seed 3 expenses on different dates → GET with `from` & `to` | Only expenses in range returned |

**Helper pattern:** Use `authTestSupport.registerAndGetToken(prefix)` for JWT. Fetch a category id via `GET /api/categories` before creating expenses.

### Unit — `ExpenseServiceTest`

Use `@ExtendWith(MockitoExtension.class)` with mocked `ExpenseRepository` and `CategoryRepository` (mirror `CategoryServiceTest`).

Cover at minimum:

- `create` persists when category belongs to user
- `create` throws when amount/category invalid (if validated in service)
- `delete` removes owned expense
- `getById` returns expense for owner

### TC-E03 isolation test

Add a **dedicated** test in `ExpenseServiceTest` named `tcE03_getById_throwsWhenExpenseBelongsToAnotherUser`:

- Mock `findByIdAndUserId(expenseId, userB)` → `Optional.empty()` (or expense with different userId)
- Assert `getById(userB, expenseId)` throws `ResponseStatusException` with `NOT_FOUND`

This isolates ownership logic without MockMvc. The integration test in `ExpenseControllerTest` still implements the full HTTP flow for TC-E03.

## 7. Update test documentation

Add an **Expenses (Day 4 — 26 Jun)** section to `spendsense/docs/test-cases.md`:

```markdown
## Expenses (Day 4 — 26 Jun)

| ID | Feature | Type | Steps | Expected Result | Automated |
|----|---------|------|-------|-----------------|-----------|
| TC-E01 | Expenses | Integration | POST valid expense | 201 + expense body | Yes — ExpenseControllerTest.tcE01 |
| TC-E02 | Expenses | Integration | POST amount ≤ 0 | 400 | Yes — ExpenseControllerTest.tcE02 |
| TC-E03 | Expenses | Integration | User B GET User A expense | 404/403 | Yes — ExpenseControllerTest.tcE03 |
| TC-E04 | Expenses | Integration | GET with from/to filters | Correct subset | Yes — ExpenseControllerTest.tcE04 |
| — | Expenses | Unit | ExpenseService ownership + CRUD | Isolation enforced | Yes — ExpenseServiceTest (incl. tcE03 isolation) |
```

## 8. Verify

```bash
cd spendsense/backend
./mvnw test
```

Windows: `.\mvnw.cmd test`

All tests must pass, including prior Day 1–3 tests. Do not break `CategoryControllerTest` or auth tests.

## Done criteria

- Expense CRUD works via REST with JWT auth
- Validation rejects amount ≤ 0 (TC-E02)
- User-scoped queries — no cross-user data leakage (TC-E03)
- Date-range filter returns correct subset (TC-E04)
- `ExpenseServiceTest` and `ExpenseControllerTest` green
- `docs/test-cases.md` updated with Day 4 expense cases

## Reference

For full sprint context and data model, see [.cursor/plans/expense_tracker_mvp_d9134402.plan.md](../../plans/expense_tracker_mvp_d9134402.plan.md).
