import { test } from '@playwright/test'
import { registerViaUi } from './helpers'

test('tcA05_registerRedirectsToDashboard', async ({ page }) => {
  const email = `e2e-register-${Date.now()}@example.com`
  await registerViaUi(page, email)
})
