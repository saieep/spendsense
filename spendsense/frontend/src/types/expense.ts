export interface Expense {
  id: number
  amount: number
  expenseDate: string
  description: string
  note: string | null
  categoryId: number
  categoryName: string
}

export interface ExpensePayload {
  amount: number
  expenseDate: string
  description: string
  categoryId: number
  note?: string | null
}

export interface CsvImportError {
  line: number
  message: string
}

export interface CsvImportResult {
  imported: number
  failed: number
  errors: CsvImportError[]
}
