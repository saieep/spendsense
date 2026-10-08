const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export const apiConfig = {
  baseURL: API_BASE_URL,
}

export const authStorageKey = 'spendsense_token'
