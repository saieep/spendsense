import { useEffect, useState } from 'react'

import {

  createCategory,

  deleteCategory,

  fetchCategories,

  updateCategory,

} from '../api/categories'

import { useToast } from '../context/ToastContext'

import { theme } from '../lib/theme'

import type { Category } from '../types/category'



const presetColors = ['#6366f1', '#3b82f6', '#ec4899', '#f59e0b', '#8b5cf6', '#64748b']



export function CategoriesPage() {

  const { showSuccess, showError } = useToast()

  const [categories, setCategories] = useState<Category[]>([])

  const [loading, setLoading] = useState(true)

  const [error, setError] = useState('')

  const [name, setName] = useState('')

  const [color, setColor] = useState(presetColors[0])

  const [editingId, setEditingId] = useState<number | null>(null)



  async function loadCategories() {

    setLoading(true)

    setError('')

    try {

      setCategories(await fetchCategories())

    } catch {

      const message = 'Could not load categories'

      setError(message)

      showError(message)

    } finally {

      setLoading(false)

    }

  }



  useEffect(() => {

    void loadCategories()

  }, [])



  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {

    event.preventDefault()

    setError('')



    try {

      if (editingId) {

        await updateCategory(editingId, { name, color })

        showSuccess('Category updated')

      } else {

        await createCategory({ name, color })

        showSuccess('Category created')

      }

      setName('')

      setColor(presetColors[0])

      setEditingId(null)

      await loadCategories()

    } catch {

      const message = editingId ? 'Could not update category' : 'Could not create category'

      setError(message)

      showError(message)

    }

  }



  function startEdit(category: Category) {

    setEditingId(category.id)

    setName(category.name)

    setColor(category.color)

  }



  async function handleDelete(category: Category) {

    if (!window.confirm(`Delete category "${category.name}"?`)) {

      return

    }



    setError('')

    try {

      await deleteCategory(category.id)

      showSuccess('Category deleted')

      await loadCategories()

    } catch {

      const message = 'Could not delete category. It may be linked to expenses.'

      setError(message)

      showError(message)

    }

  }



  return (

    <div className="space-y-8">

      <section>

        <h1 className={theme.heading}>Categories</h1>

        <p className={`mt-2 ${theme.muted}`}>

          Manage default and custom categories for your expenses.

        </p>

      </section>



      <section className={`min-w-0 p-4 sm:p-6 ${theme.card}`}>

        <h2 className={theme.subheading}>{editingId ? 'Edit category' : 'Add category'}</h2>

        <form className="mt-4 grid gap-4 sm:grid-cols-[1fr_auto_auto]" onSubmit={handleSubmit}>

          <input

            value={name}

            onChange={(event) => setName(event.target.value)}

            placeholder="Category name"

            required

            maxLength={50}

            className={theme.input}

          />

          <div className="flex items-center gap-2">

            <input

              type="color"

              value={color}

              onChange={(event) => setColor(event.target.value)}

              className="h-10 w-14 cursor-pointer rounded border border-gray-600 bg-gray-900"

              aria-label="Category color"

            />

            <span className={`text-sm ${theme.muted}`}>{color}</span>

          </div>

          <div className="flex gap-2">

            {editingId && (

              <button

                type="button"

                onClick={() => {

                  setEditingId(null)

                  setName('')

                  setColor(presetColors[0])

                }}

                className={theme.btnSecondary}

              >

                Cancel

              </button>

            )}

            <button type="submit" className={theme.btnPrimary}>

              {editingId ? 'Save' : 'Add'}

            </button>

          </div>

        </form>

        {error && (

          <p className="mt-3 text-sm text-red-400" role="alert">

            {error}

          </p>

        )}

      </section>



      <section className={`min-w-0 p-4 sm:p-6 ${theme.card}`}>

        <h2 className={theme.subheading}>Your categories</h2>

        {loading ? (

          <p className={`mt-4 ${theme.muted}`}>Loading...</p>

        ) : (

          <ul className={`mt-4 divide-y ${theme.divide}`}>

            {categories.map((category) => (

              <li

                key={category.id}

                className="flex flex-col gap-3 py-3 first:pt-0 last:pb-0 sm:flex-row sm:items-center sm:justify-between sm:gap-4"

              >

                <div className="flex min-w-0 items-center gap-3">

                  <span

                    className="h-3 w-3 rounded-full"

                    style={{ backgroundColor: category.color }}

                  />

                  <div>

                    <p className="font-medium text-gray-50">{category.name}</p>

                    <p className="text-xs text-gray-500">

                      {category.isDefault ? 'Default' : 'Custom'}

                    </p>

                  </div>

                </div>

                <div className="flex flex-wrap gap-2">

                  <button type="button" onClick={() => startEdit(category)} className={theme.btnGhost}>

                    Edit

                  </button>

                  <button

                    type="button"

                    onClick={() => handleDelete(category)}

                    className={theme.btnDestructive}

                  >

                    Delete

                  </button>

                </div>

              </li>

            ))}

          </ul>

        )}

      </section>

    </div>

  )

}


