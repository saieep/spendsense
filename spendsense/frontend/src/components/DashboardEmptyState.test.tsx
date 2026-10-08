import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { DashboardEmptyState } from './DashboardEmptyState'

describe('DashboardEmptyState', () => {
  it('tcD04_showsEmptyStateMessage', () => {
    render(<DashboardEmptyState />)

    expect(screen.getByTestId('dashboard-empty-state')).toBeInTheDocument()
    expect(screen.getByText('No expenses in this period')).toBeInTheDocument()
    expect(screen.getByText(/Try a wider date range or add expenses/i)).toBeInTheDocument()
  })
})
