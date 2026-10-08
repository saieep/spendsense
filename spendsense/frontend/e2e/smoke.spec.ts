import { expect, test } from '@playwright/test'
import { addExpenseViaUi, formatInr, registerViaUi, todayIsoDate } from './helpers'

test('p0Smoke_registerExpenseDashboardInsights', async ({ page }) => {
  const email = `e2e-smoke-${Date.now()}@example.com`
  const description = 'E2E smoke groceries'
  const amount = '32.75'
  const expenseDate = todayIsoDate()

  await registerViaUi(page, email)

  await addExpenseViaUi(page, {
    amount,
    description,
    date: expenseDate,
    category: 'Food',
  })
  await expect(page.getByText(description)).toBeVisible()
  await expect(page.getByText(formatInr(32.75))).toBeVisible()

  await page.getByRole('link', { name: 'Dashboard' }).click()
  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  await page.locator('#dashboard-from').fill(expenseDate)
  await page.locator('#dashboard-to').fill(expenseDate)
  await expect(page.getByTestId('dashboard-total-spent')).toHaveText(formatInr(32.75), { timeout: 15_000 })
  await expect(page.getByTestId('dashboard-bar-chart')).toBeVisible()

  await page.getByRole('button', { name: /generate insights/i }).click()
  await expect(page.getByTestId('insights-cards')).toBeVisible({ timeout: 15_000 })
  await expect(page.getByTestId('insights-summary')).not.toBeEmpty()
  await expect(page.getByTestId('insights-suggestions')).toBeVisible()
})
