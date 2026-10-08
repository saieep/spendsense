import { apiClient } from './auth'
import type { Category, CategoryPayload } from '../types/category'

export async function fetchCategories(): Promise<Category[]> {
  const { data } = await apiClient.get<Category[]>('/api/categories')
  return data
}

export async function createCategory(payload: CategoryPayload): Promise<Category> {
  const { data } = await apiClient.post<Category>('/api/categories', payload)
  return data
}

export async function updateCategory(id: number, payload: CategoryPayload): Promise<Category> {
  const { data } = await apiClient.put<Category>(`/api/categories/${id}`, payload)
  return data
}

export async function deleteCategory(id: number): Promise<void> {
  await apiClient.delete(`/api/categories/${id}`)
}
