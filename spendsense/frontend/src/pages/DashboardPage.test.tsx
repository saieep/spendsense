import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { fetchDashboardSummary } from '../api/dashboard'
import { ToastProvider } from '../context/ToastContext'
import { DashboardPage } from './DashboardPage'

vi.mock('../api/dashboard', () => ({
  fetchDashboardSummary: vi.fn(),
}))

function renderDashboardPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <MemoryRouter>
          <DashboardPage />
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>,
  )
}

describe('DashboardPage', () => {
  it('tcD04_showsLoadingStateWhileFetching', () => {
    vi.mocked(fetchDashboardSummary).mockReturnValue(new Promise(() => {}))
    renderDashboardPage()

    expect(screen.getByTestId('dashboard-loading')).toBeInTheDocument()
  })

  it('tcD04_showsEmptyStateWhenNoExpensesInPeriod', async () => {
    vi.mocked(fetchDashboardSummary).mockResolvedValue({
      totalSpent: 0,
      expenseCount: 0,
      byCategory: [],
      byDay: [],
    })
    renderDashboardPage()

    expect(await screen.findByTestId('dashboard-empty-state')).toBeInTheDocument()
    expect(screen.queryByTestId('dashboard-bar-chart')).not.toBeInTheDocument()
    expect(screen.queryByTestId('dashboard-pie-chart')).not.toBeInTheDocument()
  })
})
