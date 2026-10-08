import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('App routing smoke test', () => {
  it('redirects unauthenticated users to login', async () => {
    render(<App />)

    expect(await screen.findByRole('heading', { name: /sign in/i })).toBeInTheDocument()
  })
})
