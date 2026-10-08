import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { loginRequest, registerRequest } from '../api/auth'
import { authStorageKey } from '../lib/config'
import type { AuthUser, LoginPayload, RegisterPayload } from '../types/auth'

interface AuthContextValue {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  login: (payload: LoginPayload) => Promise<void>
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function readStoredAuth(): { token: string | null; user: AuthUser | null } {
  const token = localStorage.getItem(authStorageKey)
  const email = localStorage.getItem('spendsense_email')
  const userId = localStorage.getItem('spendsense_user_id')

  if (!token || !email || !userId) {
    return { token: null, user: null }
  }

  return {
    token,
    user: { email, userId: Number(userId) },
  }
}

function persistAuth(token: string, email: string, userId: number) {
  localStorage.setItem(authStorageKey, token)
  localStorage.setItem('spendsense_email', email)
  localStorage.setItem('spendsense_user_id', String(userId))
}

function clearAuth() {
  localStorage.removeItem(authStorageKey)
  localStorage.removeItem('spendsense_email')
  localStorage.removeItem('spendsense_user_id')
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const stored = readStoredAuth()
  const [token, setToken] = useState<string | null>(stored.token)
  const [user, setUser] = useState<AuthUser | null>(stored.user)

  const login = useCallback(async (payload: LoginPayload) => {
    const response = await loginRequest(payload)
    persistAuth(response.token, response.email, response.userId)
    setToken(response.token)
    setUser({ email: response.email, userId: response.userId })
  }, [])

  const register = useCallback(async (payload: RegisterPayload) => {
    const response = await registerRequest(payload)
    persistAuth(response.token, response.email, response.userId)
    setToken(response.token)
    setUser({ email: response.email, userId: response.userId })
  }, [])

  const logout = useCallback(() => {
    clearAuth()
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [user, token, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
