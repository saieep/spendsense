import { useEffect, useMemo, useRef, useState } from 'react'

import { createExpense, downloadExpensesCsv, fetchExpenses, importExpensesCsv } from '../api/expenses'

import { CsvImportSummary } from '../components/CsvImportSummary'

import { ExpenseForm } from '../components/ExpenseForm'

import { useToast } from '../context/ToastContext'

import { formatCurrency } from '../lib/currency'

import { theme } from '../lib/theme'

import type { CsvImportResult, Expense } from '../types/expense'



function currentMonthRange() {

  const now = new Date()

  return {

    from: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10),

    to: new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().slice(0, 10),

  }

}



export function ExpensesPage() {

  const { showSuccess, showError } = useToast()

  const defaultRange = useMemo(() => currentMonthRange(), [])

  const [exportFrom, setExportFrom] = useState(defaultRange.from)

  const [exportTo, setExportTo] = useState(defaultRange.to)

  const [exporting, setExporting] = useState(false)

  const [exportError, setExportError] = useState('')

  const [importing, setImporting] = useState(false)

  const [importError, setImportError] = useState('')

  const [importResult, setImportResult] = useState<CsvImportResult | null>(null)

  const fileInputRef = useRef<HTMLInputElement>(null)

  const [expenses, setExpenses] = useState<Expense[]>([])

  const [loading, setLoading] = useState(true)

  const [error, setError] = useState('')



  async function loadExpenses() {

    setLoading(true)

    setError('')

    try {

      setExpenses(await fetchExpenses())

    } catch {

      const message = 'Could not load expenses'

      setError(message)

      showError(message)

    } finally {

      setLoading(false)

    }

  }



  useEffect(() => {

    void loadExpenses()

  }, [])



  async function handleExportCsv() {

    setExporting(true)

    setExportError('')

    try {

      await downloadExpensesCsv(exportFrom, exportTo)

      showSuccess('CSV downloaded')

    } catch {

      const message = 'Could not export expenses'

      setExportError(message)

      showError(message)

    } finally {

      setExporting(false)

    }

  }



  async function handleImportCsv(event: React.ChangeEvent<HTMLInputElement>) {

    const file = event.target.files?.[0]

    event.target.value = ''

    if (!file) {

      return

    }



    setImporting(true)

    setImportError('')

    setImportResult(null)

    try {

      const result = await importExpensesCsv(file)

      setImportResult(result)

      await loadExpenses()

      if (result.failed === 0) {

        showSuccess(`Imported ${result.imported} expense${result.imported === 1 ? '' : 's'}`)

      } else {

        showSuccess(`Imported ${result.imported}; ${result.failed} row${result.failed === 1 ? '' : 's'} failed`)

      }

    } catch {

      const message = 'Could not import CSV file'

      setImportError(message)

      showError(message)

    } finally {

      setImporting(false)

    }

  }



  return (

    <div className="space-y-8">

      <section>

        <h1 className={theme.heading}>Expenses</h1>

        <p className={`mt-2 ${theme.muted}`}>

          Add an expense — tab out of the description field to get an AI category suggestion.

        </p>

      </section>



      <section className={`min-w-0 p-4 sm:p-6 ${theme.card}`}>

        <h2 className={theme.subheading}>New expense</h2>

        <div className="mt-4">

          <ExpenseForm

            onSubmit={async (payload) => {

              await createExpense(payload)

              await loadExpenses()

              showSuccess('Expense added')

            }}

          />

        </div>

      </section>



      <section className={`min-w-0 p-4 sm:p-6 ${theme.card}`}>

        <h2 className={theme.subheading}>Import CSV</h2>

        <p className={`mt-2 text-sm ${theme.muted}`}>

          Upload a CSV with columns: date, amount, description, category, note.

        </p>

        <div className="mt-4 flex flex-wrap items-center gap-3">

          <input

            ref={fileInputRef}

            type="file"

            accept=".csv,text/csv"

            className="hidden"

            onChange={(event) => void handleImportCsv(event)}

          />

          <button

            type="button"

            onClick={() => fileInputRef.current?.click()}

            disabled={importing}

            className={theme.btnSecondary}

          >

            {importing ? 'Importing…' : 'Upload CSV'}

          </button>

        </div>

        {importError && (

          <p className="mt-3 text-sm text-red-400" role="alert">

            {importError}

          </p>

        )}

        {importResult && (

          <CsvImportSummary result={importResult} onClose={() => setImportResult(null)} />

        )}

      </section>



      <section className={`min-w-0 p-4 sm:p-6 ${theme.card}`}>

        <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">

          <h2 className={theme.subheading}>Recent expenses</h2>

          <div className="flex flex-wrap items-end gap-3">

            <label className={`grid gap-1 ${theme.label}`}>

              Export from

              <input

                id="export-from"

                type="date"

                value={exportFrom}

                onChange={(event) => setExportFrom(event.target.value)}

                className={theme.input}

              />

            </label>

            <label className={`grid gap-1 ${theme.label}`}>

              Export to

              <input

                id="export-to"

                type="date"

                value={exportTo}

                onChange={(event) => setExportTo(event.target.value)}

                className={theme.input}

              />

            </label>

            <button

              type="button"

              onClick={() => void handleExportCsv()}

              disabled={exporting}

              className={theme.btnSecondary}

            >

              {exporting ? 'Exporting…' : 'Download CSV'}

            </button>

          </div>

        </div>

        {exportError && (

          <p className="mt-3 text-sm text-red-400" role="alert">

            {exportError}

          </p>

        )}

        {loading ? (

          <p className={`mt-4 ${theme.muted}`}>Loading…</p>

        ) : error ? (

          <p className="mt-4 text-sm text-red-400" role="alert">

            {error}

          </p>

        ) : expenses.length === 0 ? (

          <p className={`mt-4 ${theme.muted}`}>No expenses yet.</p>

        ) : (

          <ul className={`mt-4 divide-y ${theme.divide}`}>

            {expenses.map((expense) => (

              <li

                key={expense.id}

                className="flex flex-col gap-1 py-3 first:pt-0 last:pb-0 sm:flex-row sm:items-center sm:justify-between sm:gap-4"

              >

                <div className="min-w-0">

                  <p className="truncate font-medium text-gray-50">{expense.description}</p>

                  <p className="text-xs text-gray-500">

                    {expense.expenseDate} · {expense.categoryName}

                  </p>

                </div>

                <p className={`font-medium ${theme.amountText}`}>

                  {formatCurrency(expense.amount)}

                </p>

              </li>

            ))}

          </ul>

        )}

      </section>

    </div>

  )

}


