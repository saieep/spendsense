import type { CsvImportResult } from '../types/expense'
import { theme } from '../lib/theme'

export function CsvImportSummary({
  result,
  onClose,
}: {
  result: CsvImportResult
  onClose: () => void
}) {
  return (
    <div
      className="mt-4 rounded-xl border border-gray-600 bg-gray-900 p-4"
      data-testid="csv-import-summary"
      role="status"
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <h3 className="font-medium text-gray-50">Import complete</h3>
          <p className={`mt-1 text-sm ${theme.muted}`}>
            {result.imported} imported · {result.failed} failed
          </p>
        </div>
        <button type="button" onClick={onClose} className={theme.btnGhost}>
          Close
        </button>
      </div>
      {result.errors.length > 0 && (
        <ul className="mt-3 space-y-2 text-sm text-red-300" data-testid="csv-import-errors">
          {result.errors.map((error) => (
            <li key={`${error.line}-${error.message}`}>
              Line {error.line}: {error.message}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
