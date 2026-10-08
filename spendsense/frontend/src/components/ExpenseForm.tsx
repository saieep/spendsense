import { useEffect, useRef, useState } from 'react'

import { categorizeDescription } from '../api/ai'

import { fetchCategories } from '../api/categories'

import { useToast } from '../context/ToastContext'

import { CURRENCY_SYMBOL } from '../lib/currency'

import { theme } from '../lib/theme'

import type { Category } from '../types/category'

import type { ExpensePayload } from '../types/expense'



const SUGGEST_DEBOUNCE_MS = 300



function todayIsoDate(): string {

  return new Date().toISOString().slice(0, 10)

}



function findCategoryIdByName(categories: Category[], name: string): number | null {

  const match = categories.find((category) => category.name.toLowerCase() === name.toLowerCase())

  return match?.id ?? null

}



export interface ExpenseFormProps {

  onSubmit: (payload: ExpensePayload) => Promise<void>

}



export function ExpenseForm({ onSubmit }: ExpenseFormProps) {

  const { showError } = useToast()

  const [categories, setCategories] = useState<Category[]>([])

  const [amount, setAmount] = useState('')

  const [expenseDate, setExpenseDate] = useState(todayIsoDate())

  const [description, setDescription] = useState('')

  const [categoryId, setCategoryId] = useState<number | ''>('')

  const [note, setNote] = useState('')

  const [error, setError] = useState('')

  const [submitting, setSubmitting] = useState(false)

  const [suggesting, setSuggesting] = useState(false)

  const [aiSuggestion, setAiSuggestion] = useState<{ category: string; confidence: number } | null>(

    null,

  )

  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const latestDescriptionRef = useRef('')

  const categoriesRef = useRef<Category[]>([])



  useEffect(() => {

    categoriesRef.current = categories

  }, [categories])



  useEffect(() => {

    void fetchCategories()

      .then((loaded) => {

        setCategories(loaded)

      })

      .catch(() => {

        const message = 'Could not load categories'

        setError(message)

        showError(message)

      })

  }, [])



  useEffect(() => {

    return () => {

      if (debounceRef.current) {

        clearTimeout(debounceRef.current)

      }

    }

  }, [])



  function scheduleCategorySuggestion(value: string) {

    latestDescriptionRef.current = value.trim()



    if (debounceRef.current) {

      clearTimeout(debounceRef.current)

    }



    if (latestDescriptionRef.current.length < 3) {

      setAiSuggestion(null)

      return

    }



    debounceRef.current = setTimeout(() => {

      void requestCategorySuggestion(latestDescriptionRef.current)

    }, SUGGEST_DEBOUNCE_MS)

  }



  async function requestCategorySuggestion(value: string) {

    if (value.length < 3) {

      return

    }



    setSuggesting(true)

    setError('')



    try {

      const result = await categorizeDescription({ description: value })

      const matchedId = findCategoryIdByName(categoriesRef.current, result.category)



      setAiSuggestion(result)

      if (matchedId !== null) {

        setCategoryId(matchedId)

      }

    } catch {

      const message = 'Could not suggest a category. Pick one manually.'

      setError(message)

      showError(message)

    } finally {

      setSuggesting(false)

    }

  }



  function handleDescriptionBlur() {

    scheduleCategorySuggestion(description)

  }



  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {

    event.preventDefault()

    setError('')



    if (categoryId === '') {

      setError('Select a category')

      return

    }



    const parsedAmount = Number.parseFloat(amount)

    if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {

      setError('Enter an amount greater than 0')

      return

    }



    setSubmitting(true)

    try {

      await onSubmit({

        amount: parsedAmount,

        expenseDate,

        description: description.trim(),

        categoryId,

        note: note.trim() || null,

      })

      setAmount('')

      setDescription('')

      setCategoryId('')

      setNote('')

      setAiSuggestion(null)

      setExpenseDate(todayIsoDate())

    } catch {

      const message = 'Could not save expense'

      setError(message)

      showError(message)

    } finally {

      setSubmitting(false)

    }

  }



  return (

    <form className="space-y-4" onSubmit={handleSubmit}>

      <div className="grid gap-4 sm:grid-cols-2">

        <label className="block space-y-1">

          <span className={theme.label}>Amount</span>

          <div className="relative">

            <span className={`pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 ${theme.muted}`}>

              {CURRENCY_SYMBOL}

            </span>

            <input

              type="number"

              min="0.01"

              step="0.01"

              value={amount}

              onChange={(event) => setAmount(event.target.value)}

              required

              aria-label="Amount"

              className={`w-full py-2 pl-8 pr-3 ${theme.input}`}

            />

          </div>

        </label>

        <label className="block space-y-1">

          <span className={theme.label}>Date</span>

          <input

            type="date"

            value={expenseDate}

            onChange={(event) => setExpenseDate(event.target.value)}

            required

            className={`w-full ${theme.input}`}

          />

        </label>

      </div>



      <label className="block space-y-1">

        <span className={theme.label}>Description</span>

        <input

          value={description}

          onChange={(event) => {

            setDescription(event.target.value)

            setAiSuggestion(null)

          }}

          onBlur={handleDescriptionBlur}

          placeholder="e.g. Uber ride to airport"

          required

          maxLength={200}

          className={`w-full ${theme.input}`}

        />

        {suggesting && <p className="text-xs text-gray-500">Suggesting category…</p>}

        {aiSuggestion && !suggesting && (

          <p className={`text-xs ${theme.aiText}`}>

            AI suggested: {aiSuggestion.category} ({Math.round(aiSuggestion.confidence * 100)}%)

          </p>

        )}

      </label>



      <label className="block space-y-1">

        <span className={theme.label}>Category</span>

        <select

          value={categoryId}

          onChange={(event) => {

            setCategoryId(event.target.value === '' ? '' : Number(event.target.value))

            setAiSuggestion(null)

          }}

          required

          className={`w-full ${theme.input}`}

        >

          <option value="">Select category</option>

          {categories.map((category) => (

            <option key={category.id} value={category.id}>

              {category.name}

            </option>

          ))}

        </select>

      </label>



      <label className="block space-y-1">

        <span className={theme.label}>Note (optional)</span>

        <textarea

          value={note}

          onChange={(event) => setNote(event.target.value)}

          rows={2}

          maxLength={500}

          className={`w-full ${theme.input}`}

        />

      </label>



      {error && (

        <p className="text-sm text-red-400" role="alert">

          {error}

        </p>

      )}



      <button type="submit" disabled={submitting} className={theme.btnPrimary}>

        {submitting ? 'Saving…' : 'Add expense'}

      </button>

    </form>

  )

}


