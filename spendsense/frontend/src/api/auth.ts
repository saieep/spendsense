import axios from 'axios'
import { apiConfig, authStorageKey } from '../lib/config'
import type { AuthResponse, LoginPayload, RegisterPayload } from '../types/auth'

export const apiClient = axios.create({
  baseURL: apiConfig.baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(authStorageKey)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export async function loginRequest(payload: LoginPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/login', payload)
  return data
}

export async function registerRequest(payload: RegisterPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/register', payload)
  return data
}
