import { expect, type Page } from '@playwright/test'
import type { APIRequestContext } from '@playwright/test'

const API_BASE = process.env.VITE_API_URL ?? 'http://localhost:8080'

interface AuthResponse {
  token: string
  email: string
  userId: number
}

interface Category {
  id: number
  name: string
}

interface ExpenseInput {
  categoryId: number
  amount: number
  date: string
  description: string
}

export function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10)
}

export function formatInr(amount: number): string {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount)
}

export async function registerViaUi(
  page: Page,
  email: string,
  password = 'password123',
): Promise<void> {
  await page.goto('/register')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /create account/i }).click()
  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
}

export async function loginViaUi(
  page: Page,
  email: string,
  password = 'password123',
): Promise<void> {
  await page.goto('/login')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: /sign in/i }).click()
  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
}

export async function addExpenseViaUi(
  page: Page,
  options: {
    amount: string
    description: string
    date?: string
    category?: string
  },
): Promise<void> {
  await page.getByRole('link', { name: 'Expenses' }).click()
  await expect(page.getByRole('heading', { name: 'Expenses', exact: true })).toBeVisible()
  await page.getByLabel('Amount').fill(options.amount)
  await page.getByLabel('Date').fill(options.date ?? todayIsoDate())
  await page.getByLabel('Description').fill(options.description)
  await page.getByLabel('Category').selectOption(options.category ?? 'Food')
  await page.getByRole('button', { name: /add expense/i }).click()
}

export async function registerUser(
  request: APIRequestContext,
  email: string,
  password = 'password123',
): Promise<AuthResponse> {
  const response = await request.post(`${API_BASE}/api/auth/register`, {
    data: { email, password },
  })
  if (!response.ok()) {
    throw new Error(`Register failed: ${response.status()} ${await response.text()}`)
  }
  return response.json()
}

export async function getCategories(request: APIRequestContext, token: string): Promise<Category[]> {
  const response = await request.get(`${API_BASE}/api/categories`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!response.ok()) {
    throw new Error(`Get categories failed: ${response.status()}`)
  }
  return response.json()
}

export async function createExpense(
  request: APIRequestContext,
  token: string,
  expense: ExpenseInput,
): Promise<void> {
  const response = await request.post(`${API_BASE}/api/expenses`, {
    headers: { Authorization: `Bearer ${token}` },
    data: {
      amount: expense.amount,
      expenseDate: expense.date,
      description: expense.description,
      categoryId: expense.categoryId,
    },
  })
  if (!response.ok()) {
    throw new Error(`Create expense failed: ${response.status()} ${await response.text()}`)
  }
}
