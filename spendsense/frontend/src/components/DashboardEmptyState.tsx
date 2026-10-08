import { theme } from '../lib/theme'

export function DashboardEmptyState() {
  return (
    <section
      className={`${theme.emptyState} p-10 text-center`}
      data-testid="dashboard-empty-state"
    >
      <p className="text-lg font-medium text-slate-100">No expenses in this period</p>
      <p className={`mt-2 text-sm ${theme.muted}`}>
        Try a wider date range or add expenses to see charts here.
      </p>
    </section>
  )
}
