import { Search, X } from 'lucide-react'

function SearchInput({ value, onChange, placeholder = 'Tìm kiếm...', className = '', id = 'pos-search-input' }) {
  return (
    <div className={`relative ${className}`}>
      <Search size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
      <input
        id={id}
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full rounded-xl border border-gray-200 bg-white py-2.5 pl-9 pr-8 text-sm text-gray-900 placeholder:text-gray-400 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-100 transition-colors"
      />
      {value && (
        <button
          type="button"
          onClick={() => onChange('')}
          className="absolute right-2.5 top-1/2 -translate-y-1/2 rounded p-0.5 text-gray-400 hover:text-gray-600 transition-colors"
          aria-label="Xóa tìm kiếm"
        >
          <X size={14} />
        </button>
      )}
    </div>
  )
}

export default SearchInput
