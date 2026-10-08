import { expect, test } from '@playwright/test'
import { createExpense, formatInr, getCategories, registerUser } from './helpers'

test('tcD03_changeDateRangeUpdatesCharts', async ({ page, request }) => {
  const email = `e2e-dashboard-${Date.now()}@example.com`
  const password = 'password123'
  const { token } = await registerUser(request, email, password)
  const categories = await getCategories(request, token)
  const food = categories.find((category) => category.name === 'Food')
  if (!food) {
    throw new Error('Food category not found')
  }

  await createExpense(request, token, {
    categoryId: food.id,
    amount: 50,
    date: '2026-06-10',
    description: 'Lunch',
  })
  await createExpense(request, token, {
    categoryId: food.id,
    amount: 30,
    date: '2026-06-15',
    description: 'Dinner',
  })

  await page.goto('/login')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /sign in/i }).click()

  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  await page.locator('#dashboard-from').fill('2026-06-01')
  await page.locator('#dashboard-to').fill('2026-06-30')
  await expect(page.getByTestId('dashboard-total-spent')).toHaveText(formatInr(80))
  await expect(page.getByTestId('dashboard-bar-chart')).toBeVisible()
  await expect(page.getByTestId('dashboard-pie-chart')).toBeVisible()

  await page.locator('#dashboard-from').fill('2026-01-01')
  await page.locator('#dashboard-to').fill('2026-01-31')

  await expect(page.getByTestId('dashboard-empty-state')).toBeVisible()
  await expect(page.getByTestId('dashboard-bar-chart')).not.toBeVisible()
  await expect(page.getByTestId('dashboard-total-spent')).toHaveText(formatInr(0))

  await page.locator('#dashboard-from').fill('2026-06-01')
  await page.locator('#dashboard-to').fill('2026-06-30')

  await expect(page.getByTestId('dashboard-bar-chart')).toBeVisible()
  await expect(page.getByTestId('dashboard-pie-chart')).toBeVisible()
  await expect(page.getByTestId('dashboard-total-spent')).toHaveText(formatInr(80))
})
