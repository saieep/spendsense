import { expect, test } from '@playwright/test'
import { addExpenseViaUi, formatInr, loginViaUi, registerUser, todayIsoDate } from './helpers'

test('tcE05_addExpenseFromUiAppearsInList', async ({ page, request }) => {
  const email = `e2e-expense-${Date.now()}@example.com`
  const password = 'password123'
  await registerUser(request, email, password)
  await loginViaUi(page, email, password)

  const description = 'E2E UI lunch'
  await addExpenseViaUi(page, {
    amount: '24.50',
    description,
    date: todayIsoDate(),
    category: 'Food',
  })

  await expect(page.getByText(description)).toBeVisible()
  await expect(page.getByText(formatInr(24.5))).toBeVisible()
})
