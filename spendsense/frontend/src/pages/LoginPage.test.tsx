import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { LoginPage } from './LoginPage'

const loginMock = vi.fn()

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    login: loginMock,
    register: vi.fn(),
    logout: vi.fn(),
    user: null,
    token: null,
    isAuthenticated: false,
  }),
}))

describe('LoginPage', () => {
  it('tcA06_showsErrorOnFailedLogin', async () => {
    loginMock.mockRejectedValueOnce(new Error('Unauthorized'))
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText(/email/i), 'user@example.com')
    await user.type(screen.getByLabelText(/password/i), 'wrongpassword')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password')
  })
})
