import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ToastProvider } from '../context/ToastContext'
import { ExpenseForm } from './ExpenseForm'

const fetchCategoriesMock = vi.fn()
const categorizeMock = vi.fn()

vi.mock('../api/categories', () => ({
  fetchCategories: () => fetchCategoriesMock(),
}))

vi.mock('../api/ai', () => ({
  categorizeDescription: (...args: unknown[]) => categorizeMock(...args),
}))

function renderExpenseForm(onSubmit = vi.fn().mockResolvedValue(undefined)) {
  return render(
    <ToastProvider>
      <ExpenseForm onSubmit={onSubmit} />
    </ToastProvider>,
  )
}

describe('ExpenseForm', () => {
  it('tcE06_showsValidationErrorWhenCategoryNotSelected', async () => {
    fetchCategoriesMock.mockResolvedValue([
      { id: 1, name: 'Food', color: '#22c55e', isDefault: true },
    ])
    categorizeMock.mockResolvedValue({ category: 'Food', confidence: 0.9 })
    const user = userEvent.setup()
    renderExpenseForm()

    await waitFor(() => {
      expect(screen.getByLabelText(/category/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/amount/i), '12.50')
    await user.type(screen.getByLabelText(/description/i), 'ab')
    fireEvent.submit(screen.getByRole('button', { name: /add expense/i }).closest('form')!)

    expect(await screen.findByRole('alert')).toHaveTextContent('Select a category')
  })

  it('tcE06_showsValidationErrorForNonPositiveAmount', async () => {
    fetchCategoriesMock.mockResolvedValue([
      { id: 1, name: 'Food', color: '#22c55e', isDefault: true },
    ])
    categorizeMock.mockResolvedValue({ category: 'Food', confidence: 0.9 })
    const user = userEvent.setup()
    renderExpenseForm()

    await waitFor(() => {
      expect(screen.getByLabelText(/category/i)).toBeInTheDocument()
    })

    await user.clear(screen.getByLabelText(/amount/i))
    await user.type(screen.getByLabelText(/amount/i), '0')
    await user.type(screen.getByLabelText(/description/i), 'ab')
    await user.selectOptions(screen.getByLabelText(/category/i), '1')
    fireEvent.submit(screen.getByRole('button', { name: /add expense/i }).closest('form')!)

    expect(await screen.findByRole('alert')).toHaveTextContent('Enter an amount greater than 0')
  })
})
