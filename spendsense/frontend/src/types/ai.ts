export interface CategorizeResult {
  category: string
  confidence: number
}

export interface CategorizePayload {
  description: string
}

export interface InsightsResult {
  summary: string
  highlights: string[]
  suggestions: string[]
  cached: boolean
}

export interface InsightsPayload {
  from: string
  to: string
}
