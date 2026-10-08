import { apiClient } from './auth'
import type { CsvImportResult, Expense, ExpensePayload } from '../types/expense'

export async function fetchExpenses(): Promise<Expense[]> {
  const { data } = await apiClient.get<Expense[]>('/api/expenses')
  return data
}

export async function createExpense(payload: ExpensePayload): Promise<Expense> {
  const { data } = await apiClient.post<Expense>('/api/expenses', payload)
  return data
}

export async function downloadExpensesCsv(from: string, to: string): Promise<void> {
  const response = await apiClient.get('/api/expenses/export', {
    params: { from, to },
    responseType: 'blob',
  })

  const blob = new Blob([response.data], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `expenses-${from}-to-${to}.csv`
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

export async function importExpensesCsv(file: File): Promise<CsvImportResult> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await apiClient.post<CsvImportResult>('/api/expenses/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}
