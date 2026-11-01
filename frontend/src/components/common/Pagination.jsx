import { ChevronLeft, ChevronRight } from 'lucide-react'

function Pagination({ page, totalPages, totalElements, onPageChange }) {
  if (totalPages <= 0) return null

  const canPrev = page > 0
  const canNext = page < totalPages - 1

  return (
    <div className="flex items-center justify-between border-t border-gray-100 px-4 py-3">
      <p className="text-sm text-gray-500">
        Trang <span className="font-medium text-gray-700">{page + 1}</span> / {totalPages}
        {typeof totalElements === 'number' && <> · {totalElements} bản ghi</>}
      </p>
      <div className="flex items-center gap-2">
        <button
          type="button"
          disabled={!canPrev}
          onClick={() => onPageChange(page - 1)}
          className="flex items-center gap-1 rounded-lg border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <ChevronLeft size={16} />
          Trước
        </button>
        <button
          type="button"
          disabled={!canNext}
          onClick={() => onPageChange(page + 1)}
          className="flex items-center gap-1 rounded-lg border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Sau
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  )
}

export default Pagination
