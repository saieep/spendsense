export interface AuthResponse {
  token: string
  email: string
  userId: number
}

export interface AuthUser {
  email: string
  userId: number
}

export interface LoginPayload {
  email: string
  password: string
}

export interface RegisterPayload {
  email: string
  password: string
}
