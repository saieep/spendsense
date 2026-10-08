import { useEffect, useMemo, useState } from 'react'

import axios from 'axios'

import { useQuery } from '@tanstack/react-query'

import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

import { fetchInsights } from '../api/ai'

import { fetchDashboardSummary } from '../api/dashboard'

import { DashboardEmptyState } from '../components/DashboardEmptyState'

import { InsightsCards } from '../components/InsightsCards'

import { useToast } from '../context/ToastContext'

import { formatCurrency } from '../lib/currency'

import { theme } from '../lib/theme'

import type { InsightsResult } from '../types/ai'

import type { DashboardSummary } from '../types/dashboard'



function currentMonthRange() {

  const now = new Date()

  return {

    from: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10),

    to: new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().slice(0, 10),

  }

}



function DashboardSummaryCards({ summary }: { summary: DashboardSummary }) {

  const avg = summary.expenseCount > 0 ? summary.totalSpent / summary.expenseCount : 0

  return (

    <section className="grid gap-4 sm:grid-cols-3">

      <article className={theme.cardKpi}>

        <p className={theme.label}>Total spent</p>

        <p className="mt-2 text-3xl font-semibold text-slate-100" data-testid="dashboard-total-spent">

          {formatCurrency(summary.totalSpent)}

        </p>

        {summary.expenseCount > 0 && (

          <p className={`mt-2 text-sm ${theme.successText}`}>

            {summary.expenseCount} expense{summary.expenseCount === 1 ? '' : 's'} in period

          </p>

        )}

      </article>

      <article className={theme.cardKpi}>

        <p className={theme.label}>Expenses</p>

        <p className="mt-2 text-3xl font-semibold text-slate-100">{summary.expenseCount}</p>

      </article>

      <article className={theme.cardKpi}>

        <p className={theme.label}>Average per expense</p>

        <p className="mt-2 text-3xl font-semibold text-slate-100">{formatCurrency(avg)}</p>

      </article>

    </section>

  )

}



export function DashboardPage() {

  const { showSuccess, showError } = useToast()

  const defaultRange = useMemo(() => currentMonthRange(), [])

  const [from, setFrom] = useState(defaultRange.from)

  const [to, setTo] = useState(defaultRange.to)

  const [insights, setInsights] = useState<InsightsResult | null>(null)

  const [insightsLoading, setInsightsLoading] = useState(false)

  const [insightsError, setInsightsError] = useState('')

  const { data: summary, isLoading, isFetching, error } = useQuery({

    queryKey: ['dashboard', from, to],

    queryFn: () => fetchDashboardSummary({ from, to }),

  })



  useEffect(() => {

    setInsights(null)

    setInsightsError('')

  }, [from, to])



  async function handleGenerateInsights() {

    setInsightsLoading(true)

    setInsightsError('')

    try {

      setInsights(await fetchInsights({ from, to }))

      showSuccess('Insights generated')

    } catch (error) {

      const serverMessage = axios.isAxiosError(error)
        ? (error.response?.data as { message?: string } | undefined)?.message
        : undefined
      const message = serverMessage ?? 'Could not generate insights. Try again later.'

      setInsightsError(message)

      showError(message)

    } finally {

      setInsightsLoading(false)

    }

  }



  return (

    <div className="min-w-0 space-y-8">

      <section className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">

        <div>

          <h1 className={theme.heading}>Dashboard</h1>

          <p className={`mt-2 ${theme.muted}`}>Your spending at a glance</p>

        </div>

        <div className="flex flex-wrap items-end gap-3">

          <label className={`grid gap-1 ${theme.label}`}>

            From

            <input

              id="dashboard-from"

              type="date"

              value={from}

              onChange={(e) => setFrom(e.target.value)}

              className={theme.input}

            />

          </label>

          <label className={`grid gap-1 ${theme.label}`}>

            To

            <input

              id="dashboard-to"

              type="date"

              value={to}

              onChange={(e) => setTo(e.target.value)}

              className={theme.input}

            />

          </label>

        </div>

      </section>

      {error && <p className="text-sm text-red-400" role="alert">Could not load dashboard summary</p>}

      {isLoading ? (

        <p className={theme.muted} data-testid="dashboard-loading">Loading dashboard...</p>

      ) : summary ? (

        <>

          {isFetching && !isLoading && (

            <p className="text-sm text-gray-500" data-testid="dashboard-refetching">Updating charts...</p>

          )}

          <DashboardSummaryCards summary={summary} />

          {summary.expenseCount === 0 ? (

            <DashboardEmptyState />

          ) : (

            <>

              <section className={`min-w-0 p-4 sm:p-6 ${theme.card}`}>

                <h2 className={theme.subheading}>Spending over time</h2>

                <div className="mt-6 h-64 min-w-0 sm:h-80" data-testid="dashboard-bar-chart">

                  <ResponsiveContainer width="100%" height="100%">

                    <BarChart data={summary.byDay}>

                      <CartesianGrid strokeDasharray="3 3" stroke={theme.chartGrid} />

                      <XAxis dataKey="date" stroke={theme.chartAxis} tick={{ fontSize: 12 }} />

                      <YAxis stroke={theme.chartAxis} tick={{ fontSize: 12 }} />

                      <Tooltip

                        formatter={(v) => formatCurrency(Number(v ?? 0))}

                        contentStyle={theme.tooltip}

                      />

                      <Bar dataKey="amount" fill={theme.chartBar} radius={[4, 4, 0, 0]} />

                    </BarChart>

                  </ResponsiveContainer>

                </div>

              </section>

              <section className={`min-w-0 p-4 sm:p-6 ${theme.card}`}>

                <h2 className={theme.subheading}>Spending by category</h2>

                <div className="mt-6 h-64 min-w-0 sm:h-80" data-testid="dashboard-pie-chart">

                  <ResponsiveContainer width="100%" height="100%">

                    <PieChart>

                      <Pie

                        data={summary.byCategory}

                        dataKey="amount"

                        nameKey="categoryName"

                        cx="50%"

                        cy="50%"

                        innerRadius={60}

                        outerRadius={110}

                        paddingAngle={2}

                      >

                        {summary.byCategory.map((entry) => (

                          <Cell key={entry.categoryId} fill={entry.color} />

                        ))}

                      </Pie>

                      <Tooltip

                        formatter={(v) => formatCurrency(Number(v ?? 0))}

                        contentStyle={theme.tooltip}

                      />

                      <Legend />

                    </PieChart>

                  </ResponsiveContainer>

                </div>

              </section>

            </>

          )}

          <section className={`min-w-0 p-4 sm:p-6 ${theme.card} ${theme.aiPanel}`}>

            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

              <div>

                <h2 className={theme.subheading}>AI insights</h2>

                <p className={`mt-1 text-sm ${theme.muted}`}>

                  Get spending tips for the selected date range.

                </p>

              </div>

              <button

                type="button"

                onClick={() => void handleGenerateInsights()}

                disabled={insightsLoading || summary.expenseCount === 0}

                className={theme.btnPrimary}

              >

                {insightsLoading ? 'Generating…' : 'Generate insights'}

              </button>

            </div>

            {insightsError && (

              <p className="mt-4 text-sm text-red-400" role="alert">

                {insightsError}

              </p>

            )}

            {insights && (

              <div className="mt-6">

                <InsightsCards insights={insights} />

              </div>

            )}

          </section>

        </>

      ) : null}

    </div>

  )

}


