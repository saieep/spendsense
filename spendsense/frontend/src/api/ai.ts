import { apiClient } from './auth'
import type { CategorizePayload, CategorizeResult, InsightsPayload, InsightsResult } from '../types/ai'

export async function categorizeDescription(payload: CategorizePayload): Promise<CategorizeResult> {
  const { data } = await apiClient.post<CategorizeResult>('/api/ai/categorize', payload)
  return data
}

export async function fetchInsights(payload: InsightsPayload): Promise<InsightsResult> {
  const { data } = await apiClient.post<InsightsResult>('/api/ai/insights', payload)
  return data
}
