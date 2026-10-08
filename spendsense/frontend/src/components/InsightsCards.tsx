import type { InsightsResult } from '../types/ai'
import { formatInsightCurrency } from '../lib/currency'
import { theme } from '../lib/theme'

export function InsightsCards({ insights }: { insights: InsightsResult }) {
  return (
    <section className="space-y-4" data-testid="insights-cards">
      <article className={theme.aiSummaryCard} data-testid="insights-summary">
        <h2 className={`text-sm font-medium uppercase tracking-wide ${theme.aiText}`}>Summary</h2>
        <p className="mt-3 text-slate-100">{formatInsightCurrency(insights.summary)}</p>
        {insights.cached && (
          <p className="mt-2 text-xs text-slate-500">Loaded from cache (valid 24h)</p>
        )}
      </article>

      {insights.highlights.length > 0 && (
        <article className={`${theme.card} p-6`}>
          <h2 className="text-sm font-medium uppercase tracking-wide text-slate-400">Highlights</h2>
          <ul className="mt-3 list-disc space-y-2 pl-5 text-slate-200" data-testid="insights-highlights">
            {insights.highlights.map((highlight) => (
              <li key={highlight}>{formatInsightCurrency(highlight)}</li>
            ))}
          </ul>
        </article>
      )}

      {insights.suggestions.length > 0 && (
        <article className={`${theme.card} p-6`}>
          <h2 className="text-sm font-medium uppercase tracking-wide text-slate-400">Suggestions</h2>
          <ul className="mt-3 list-disc space-y-2 pl-5 text-slate-200" data-testid="insights-suggestions">
            {insights.suggestions.map((suggestion) => (
              <li key={suggestion}>{formatInsightCurrency(suggestion)}</li>
            ))}
          </ul>
        </article>
      )}
    </section>
  )
}
