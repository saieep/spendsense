export interface CategorySpend {
  categoryId: number
  categoryName: string
  color: string
  amount: number
}

export interface DaySpend {
  date: string
  amount: number
}

export interface DashboardSummary {
  totalSpent: number
  expenseCount: number
  byCategory: CategorySpend[]
  byDay: DaySpend[]
}

export interface DashboardSummaryParams {
  from?: string
  to?: string
}
