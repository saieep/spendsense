export interface Category {
  id: number
  name: string
  color: string
  isDefault: boolean
}

export interface CategoryPayload {
  name: string
  color: string
}
