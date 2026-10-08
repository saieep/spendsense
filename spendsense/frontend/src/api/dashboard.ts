import { apiClient } from './auth'
import type { DashboardSummary, DashboardSummaryParams } from '../types/dashboard'

export async function fetchDashboardSummary(
  params: DashboardSummaryParams = {},
): Promise<DashboardSummary> {
  const { data } = await apiClient.get<DashboardSummary>('/api/dashboard/summary', { params })
  return data
}
