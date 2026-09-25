import { forwardRef } from 'react'

const Select = forwardRef(function Select(
  { label, error, options = [], placeholder, className = '', containerClassName = '', ...rest },
  ref
) {
  return (
    <div className={containerClassName}>
      {label && <label className="mb-1.5 block text-sm font-medium text-gray-700">{label}</label>}
      <select
        ref={ref}
        className={`w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
          error ? 'border-red-400' : 'border-gray-300'
        } ${className}`}
        {...rest}
      >
        {placeholder && (
          <option value="" disabled hidden>
            {placeholder}
          </option>
        )}
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  )
})

export default Select
