import { Link, useNavigate } from 'react-router-dom'
import { AuthLayout } from '../components/AuthLayout'
import { useAuth } from '../context/AuthContext'
import { theme } from '../lib/theme'

export function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const email = String(form.get('email') ?? '')
    const password = String(form.get('password') ?? '')

    try {
      await login({ email, password })
      navigate('/dashboard')
    } catch {
      const error = document.getElementById('login-error')
      if (error) {
        error.textContent = 'Invalid email or password'
      }
    }
  }

  return (
    <AuthLayout
      title="Sign in"
      subtitle="Welcome back — enter your credentials to continue"
      footer={
        <>
          No account?{' '}
          <Link to="/register" className={theme.link}>
            Create one
          </Link>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit}>
        <div>
          <label htmlFor="email" className={`mb-1 block ${theme.label}`}>
            Email
          </label>
          <input id="email" name="email" type="email" required className={`w-full ${theme.input}`} />
        </div>
        <div>
          <label htmlFor="password" className={`mb-1 block ${theme.label}`}>
            Password
          </label>
          <input
            id="password"
            name="password"
            type="password"
            required
            minLength={8}
            className={`w-full ${theme.input}`}
          />
        </div>
        <p id="login-error" className="text-sm text-red-400" role="alert" />
        <button type="submit" className={`w-full ${theme.btnPrimary}`}>
          Sign in
        </button>
      </form>
    </AuthLayout>
  )
}
