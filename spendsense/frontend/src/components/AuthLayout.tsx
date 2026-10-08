import type { ReactNode } from 'react'
import { theme } from '../lib/theme'

export function AuthLayout({
  title,
  subtitle,
  children,
  footer,
}: {
  title: string
  subtitle: string
  children: ReactNode
  footer: ReactNode
}) {
  return (
    <div className={`flex min-h-screen items-center justify-center px-6 py-10 ${theme.page}`}>
      <div className={`w-full max-w-md ${theme.cardAuth}`}>
        <p className={theme.logo}>SpendSense</p>
        <h1 className="mt-6 text-3xl font-bold text-white">{title}</h1>
        <p className={`mt-2 ${theme.muted}`}>{subtitle}</p>
        <div className="mt-8">{children}</div>
        <div className={`mt-6 text-center text-sm ${theme.muted}`}>{footer}</div>
      </div>
    </div>
  )
}
