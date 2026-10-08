import { expect, test } from '@playwright/test'
import { createExpense, getCategories, registerUser } from './helpers'

test('tcAI04_generateInsightsRendersCards', async ({ page, request }) => {
  const email = `e2e-insights-${Date.now()}@example.com`
  const password = 'password123'
  const { token } = await registerUser(request, email, password)
  const categories = await getCategories(request, token)
  const food = categories.find((category) => category.name === 'Food')
  if (!food) {
    throw new Error('Food category not found')
  }

  await createExpense(request, token, {
    categoryId: food.id,
    amount: 45,
    date: '2026-06-12',
    description: 'Groceries',
  })
  await createExpense(request, token, {
    categoryId: food.id,
    amount: 25,
    date: '2026-06-18',
    description: 'Restaurant',
  })

  await page.goto('/login')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /sign in/i }).click()

  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  await page.locator('#dashboard-from').fill('2026-06-01')
  await page.locator('#dashboard-to').fill('2026-06-30')

  await page.getByRole('button', { name: /generate insights/i }).click()

  await expect(page.getByTestId('insights-cards')).toBeVisible()
  await expect(page.getByTestId('insights-summary')).not.toBeEmpty()
  await expect(page.getByTestId('insights-suggestions')).toBeVisible()
})
