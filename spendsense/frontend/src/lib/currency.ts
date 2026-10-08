export const CURRENCY_CODE = 'INR'
export const CURRENCY_SYMBOL = '₹'

const currencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: CURRENCY_CODE,
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

export function formatCurrency(amount: number): string {
  return currencyFormatter.format(amount)
}

/** Normalize AI-generated text that may use $ instead of ₹ */
export function formatInsightCurrency(text: string): string {
  return text.replace(/\bUSD\b/gi, 'INR').replace(/\$/g, '₹')
}
