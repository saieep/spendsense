import { Link, useNavigate } from 'react-router-dom'
import { AuthLayout } from '../components/AuthLayout'
import { useAuth } from '../context/AuthContext'
import { theme } from '../lib/theme'

export function RegisterPage() {
  const navigate = useNavigate()
  const { register } = useAuth()

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const email = String(form.get('email') ?? '')
    const password = String(form.get('password') ?? '')

    try {
      await register({ email, password })
      navigate('/dashboard')
    } catch {
      const error = document.getElementById('register-error')
      if (error) {
        error.textContent = 'Could not create account. Email may already be in use.'
      }
    }
  }

  return (
    <AuthLayout
      title="Create account"
      subtitle="Start tracking your expenses in under two minutes"
      footer={
        <>
          Already have an account?{' '}
          <Link to="/login" className={theme.link}>
            Sign in
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
        <p id="register-error" className="text-sm text-red-400" role="alert" />
        <button type="submit" className={`w-full ${theme.btnPrimary}`}>
          Create account
        </button>
      </form>
    </AuthLayout>
  )
}
