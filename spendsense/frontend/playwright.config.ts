import { defineConfig, devices } from '@playwright/test'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const frontendDir = dirname(fileURLToPath(import.meta.url))
const backendDir = join(frontendDir, '..', 'backend')
const backendStartCommand =
  process.platform === 'win32'
    ? 'cmd /c "set SPRING_PROFILES_ACTIVE=test&& mvnw.cmd spring-boot:run"'
    : 'SPRING_PROFILES_ACTIVE=test ./mvnw spring-boot:run'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: [
    {
      command: backendStartCommand,
      url: 'http://localhost:8080/api/health',
      cwd: backendDir,
      reuseExistingServer: !process.env.CI,
      timeout: 180_000,
    },
    {
      command: 'npm run dev',
      url: 'http://localhost:5173',
      cwd: frontendDir,
      reuseExistingServer: !process.env.CI,
      timeout: 60_000,
    },
  ],
})
